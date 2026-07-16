package com.ansonboby.almanac.data.export

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKeys
import com.ansonboby.almanac.MainActivity
import com.ansonboby.almanac.R
import com.ansonboby.almanac.data.local.Entry
import com.ansonboby.almanac.data.repository.EntryRepository
import com.ansonboby.almanac.data.repository.HabitRepository
import com.ansonboby.almanac.data.util.LocalDateUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Local-first ledger export (PRD 4: backup/export). Everything stays on the
 * device — backups are encrypted archives written to a user-chosen URI via the
 * Storage Access Framework, and the PDF "Field Report" is generated locally.
 *
 * GOTCHA (Room co-location): the Room database file must not be swapped out
 * while the DB connection is open. [restoreFromUri] therefore copies the files
 * and then restarts the process so Room reopens the restored database cleanly.
 */
@Singleton
class LedgerExport @Inject constructor(
    @ApplicationContext private val context: Context,
    private val entryRepository: EntryRepository,
    private val habitRepository: HabitRepository,
) {
    private val photosDir = File(context.filesDir, "photos").apply { mkdirs() }
    private val dbFile = context.getDatabasePath("almanac.db")
    private val tempDir = context.cacheDir

    /** Zip the DB + photos into [out], then encrypt it to [uri] (SAF). */
    suspend fun backupToUri(uri: Uri) = withContext(Dispatchers.IO) {
        val plainZip = File(tempDir, "almanac_backup.zip")
        val encFile = File(tempDir, "almanac_backup.aes")
        try {
            zipSources(plainZip)
            encryptFile(plainZip, encFile)
            context.contentResolver.openOutputStream(uri)?.use { out ->
                encFile.inputStream().use { it.copyTo(out) }
            }
        } finally {
            plainZip.delete()
            encFile.delete()
        }
    }

    /**
     * Decrypt + unzip a backup into the live ledger, then restart the app so
     * Room reloads the restored database. Returns false if decryption fails
     * (e.g. the file isn't an Almanac backup).
     */
    suspend fun restoreFromUri(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        val encFile = File(tempDir, "almanac_restore.aes")
        val plainZip = File(tempDir, "almanac_restore.zip")
        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                encFile.outputStream().use { input.copyTo(it) }
            }
            decryptFile(encFile, plainZip)
        } catch (_: Exception) {
            encFile.delete(); plainZip.delete()
            return@withContext false
        }
        val ok = runCatching { unzipToLedger(plainZip) }.isSuccess
        encFile.delete(); plainZip.delete()
        if (ok) restartApp()
        ok
    }

    /** Render a simple text "Field Report" PDF to [uri] (SAF). */
    suspend fun exportPdfToUri(uri: Uri) = withContext(Dispatchers.IO) {
        val entries = entryRepository.allEntries().first()
        val pdf = buildFieldReportPdf(entries)
        try {
            context.contentResolver.openOutputStream(uri)?.use { out ->
                pdf.inputStream().use { it.copyTo(out) }
            }
        } finally {
            pdf.delete()
        }
    }

    /** Wipe all local data (entries, habit marks, habits). */
    suspend fun purge() {
        entryRepository.deleteAll()
        habitRepository.deleteAll()
    }

    // --- internals ---

    private fun zipSources(out: File) {
        ZipOutputStream(BufferedOutputStream(FileOutputStream(out))).use { zos ->
            if (dbFile.exists()) addEntry(zos, dbFile, "almanac.db")
            photosDir.listFiles()?.forEach { f ->
                if (f.isFile) addEntry(zos, f, "photos/${f.name}")
            }
        }
    }

    private fun addEntry(zos: ZipOutputStream, file: File, name: String) {
        zos.putNextEntry(ZipEntry(name))
        FileInputStream(file).use { it.copyTo(zos) }
        zos.closeEntry()
    }

    private fun unzipToLedger(zip: File) {
        ZipInputStream(FileInputStream(zip)).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                when {
                    entry.name == "almanac.db" ->
                        FileOutputStream(dbFile).use { zis.copyTo(it) }
                    entry.name.startsWith("photos/") -> {
                        // Guard against Zip Slip: only accept a bare file name
                        // (no path separators) and verify it resolves inside photosDir.
                        val name = entry.name.removePrefix("photos/")
                            .substringAfterLast('/')
                            .substringAfterLast('\\')
                        if (name.isNotBlank() && !name.contains("..")) {
                            val out = File(photosDir, name).canonicalFile
                            if (out.canonicalPath == File(photosDir, name).canonicalPath) {
                                FileOutputStream(out).use { zis.copyTo(it) }
                            }
                        }
                    }
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }
    }

    private fun encryptFile(plain: File, enc: File) {
        val keyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        val ef = EncryptedFile.Builder(
            enc,
            context,
            "ledger_backup",
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB,
        ).setKeysetAlias(keyAlias).build()
        ef.openFileOutput().use { out -> plain.inputStream().use { it.copyTo(out) } }
    }

    private fun decryptFile(enc: File, plain: File) {
        val keyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        val ef = EncryptedFile.Builder(
            enc,
            context,
            "ledger_backup",
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB,
        ).setKeysetAlias(keyAlias).build()
        ef.openFileInput().use { input -> plain.outputStream().use { input.copyTo(it) } }
    }

    private fun restartApp() {
        val intent = Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        context.startActivity(intent)
        Runtime.getRuntime().exit(0)
    }

    private fun buildFieldReportPdf(entries: List<Entry>): File {
        val file = File(tempDir, "field_report.pdf")
        val document = PdfDocument()
        val pageWidth = 595 // A4 pt
        val pageHeight = 842
        val margin = 48f
        val contentWidth = pageWidth - margin * 2
        val lineHeight = 16f
        val blockGap = 10f
        val bodyPaint = Paint().apply {
            color = Color.BLACK
            textSize = 11f
        }
        val metaPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 10f
        }
        val titlePaint = Paint().apply {
            color = Color.BLACK
            textSize = 20f
        }
        val rulePaint = Paint().apply {
            color = Color.GRAY
            strokeWidth = 1f
        }
        val dateFmt = SimpleDateFormat("d MMM yyyy, h:mm a", Locale.getDefault())
        val dayFmt = DateTimeFormatter.ofPattern("EEEE, d MMM yyyy")

        var page = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create())
        var canvas = page.canvas
        var y = margin

        fun newPage() {
            document.finishPage(page)
            page = document.startPage(
                PdfDocument.PageInfo.Builder(pageWidth, pageHeight, document.pages.size + 1).create(),
            )
            canvas = page.canvas
            y = margin
        }

        fun drawWrapped(text: String, paint: Paint, indent: Float = 0f): Float {
            var yy = 0f
            val words = text.split(" ")
            val sb = StringBuilder()
            for (word in words) {
                val trial = if (sb.isEmpty()) word else "$sb $word"
                if (paint.measureText(trial) > contentWidth - indent && sb.isNotEmpty()) {
                    canvas.drawText(sb.toString(), margin + indent, y + yy, paint)
                    yy += lineHeight
                    sb.clear()
                    sb.append(word)
                } else {
                    sb.clear()
                    sb.append(trial)
                }
            }
            if (sb.isNotEmpty()) {
                canvas.drawText(sb.toString(), margin + indent, y + yy, paint)
                yy += lineHeight
            }
            return yy
        }

        canvas.drawText(context.getString(R.string.export_pdf_title), margin, y, titlePaint)
        y += lineHeight * 1.6f
        canvas.drawText(
            context.getString(R.string.export_pdf_generated, dateFmt.format(java.util.Date())),
            margin,
            y,
            metaPaint,
        )
        y += lineHeight * 1.2f
        canvas.drawLine(margin, y, pageWidth - margin, y, rulePaint)
        y += lineHeight * 1.6f

        if (entries.isEmpty()) {
            canvas.drawText(context.getString(R.string.export_pdf_empty), margin, y, bodyPaint)
        }

        entries.sortedByDescending { it.createdAt }.forEach { entry ->
            val label = LocalDateUtil.toLocalDate(entry.epochDayLocal).format(dayFmt)
            val mood = entry.moodScore?.let { " · mood $it" } ?: ""
            val meta = "${entry.type.name.lowercase()}$mood"
            val body = entry.textContent?.takeIf { it.isNotBlank() } ?: "(no text)"

            // Estimate block height; start a new page if it won't fit.
            val estLines = 2 + (body.length / 70f).toInt().coerceAtLeast(1)
            if (y + estLines * lineHeight + blockGap > pageHeight - margin) newPage()

            val metaH = drawWrapped(label, metaPaint)
            y += metaH
            val tagH = drawWrapped(meta, bodyPaint)
            y += tagH
            val bodyH = drawWrapped(body, bodyPaint, indent = 8f)
            y += bodyH + blockGap
            canvas.drawLine(margin, y - blockGap / 2, pageWidth - margin, y - blockGap / 2, rulePaint)
            y += blockGap / 2
        }

        document.finishPage(page)
        FileOutputStream(file).use { document.writeTo(it) }
        document.close()
        return file
    }
}

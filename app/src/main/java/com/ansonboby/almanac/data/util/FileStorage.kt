package com.ansonboby.almanac.data.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

/**
 * App-private photo storage (PRD 7 / AGENTS Gotchas: scoped storage, never
 * MediaStore shared storage — these are private journal photos). Files live
 * under `filesDir/photos` and are shared with the camera via a FileProvider URI.
 */
class FileStorage private constructor(private val context: Context) {

    private val dir = File(context.filesDir, "photos").apply { mkdirs() }

    fun newCaptureFile(): File =
        File(dir, "photo_${System.currentTimeMillis()}.jpg")

    fun uriForFile(file: File): Uri =
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

    /** Copy an external (gallery) URI into app-private storage; returns the new URI. */
    fun importUri(uri: Uri): Uri? = runCatching {
        val out = newCaptureFile()
        context.contentResolver.openInputStream(uri)?.use { input ->
            out.outputStream().use { input.copyTo(it) }
        }
        Uri.fromFile(out)
    }.getOrNull()

    fun delete(path: String) {
        runCatching { File(path).takeIf { it.exists() }?.delete() }
    }

    companion object {
        fun get(context: Context): FileStorage = FileStorage(context)
    }
}

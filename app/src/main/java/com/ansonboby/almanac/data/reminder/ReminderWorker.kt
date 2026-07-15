package com.ansonboby.almanac.data.reminder

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ansonboby.almanac.MainActivity
import com.ansonboby.almanac.R
import javax.inject.Inject

/**
 * Daily local nudge to "ink the day" (PRD 4: reminders). Posts a silent,
 * local-only notification — no server, no network (PRD 7). Respects the
 * Android 13+ [Manifest.permission.POST_NOTIFICATIONS] runtime gate.
 */
class ReminderWorker(
    private val context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        postReminder()
        return Result.success()
    }

    private fun postReminder() {
        if (android.os.Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pi = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_ledger)
            .setContentTitle(context.getString(R.string.reminder_title))
            .setContentText(context.getString(R.string.reminder_body))
            .setContentIntent(pi)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        NotificationManagerCompat.from(context).notify(NOTIFY_ID, notification)
    }

    companion object {
        const val CHANNEL_ID = "almanac_reminders"
        const val NOTIFY_ID = 4242
    }
}

/** Creates the reminder notification channel (call once at app start). */
fun ensureReminderChannel(context: Context) {
    val manager = context.getSystemService(NotificationManager::class.java)
    val channel = NotificationChannel(
        ReminderWorker.CHANNEL_ID,
        context.getString(R.string.reminder_channel_name),
        NotificationManager.IMPORTANCE_DEFAULT,
    ).apply { description = context.getString(R.string.reminder_channel_desc) }
    manager.createNotificationChannel(channel)
}

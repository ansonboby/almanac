package com.ansonboby.almanac.data.reminder

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import dagger.hilt.android.qualifiers.ApplicationContext
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Schedules the daily "ink the day" reminder via WorkManager (PRD 4: reminders).
 * A single unique periodic work runs every 24h, first firing at the next
 * user-chosen time. Re-scheduling with [ExistingPeriodicWorkPolicy.UPDATE]
 * keeps only one reminder active when the time changes.
 */
@Singleton
class ReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val workManager by lazy { WorkManager.getInstance(context) }

    fun schedule(hour: Int, minute: Int) {
        val now = System.currentTimeMillis()
        val delay = initialDelayMillis(hour, minute, now)
        val request = PeriodicWorkRequestBuilder<ReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .build()
        workManager.enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }

    /**
     * Milliseconds from [now] until the next occurrence of [hour]:[minute].
     * If that time has already passed today, the next occurrence is tomorrow.
     */
    companion object {
        const val WORK_NAME = "almanac_daily_reminder"

        internal fun initialDelayMillis(hour: Int, minute: Int, now: Long): Long {
            val target = Calendar.getInstance().apply { timeInMillis = now }.apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            if (target.timeInMillis <= now) {
                target.add(Calendar.DAY_OF_MONTH, 1)
            }
            return target.timeInMillis - now
        }
    }

    fun cancel() {
        workManager.cancelUniqueWork(WORK_NAME)
    }
}

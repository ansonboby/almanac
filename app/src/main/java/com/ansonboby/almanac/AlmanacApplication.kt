package com.ansonboby.almanac

import android.app.Application
import com.ansonboby.almanac.data.reminder.ensureReminderChannel
import dagger.hilt.android.HiltAndroidApp

/** Application entry point. Hosts the Hilt dependency graph for the whole app. */
@HiltAndroidApp
class AlmanacApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ensureReminderChannel(this)
    }
}

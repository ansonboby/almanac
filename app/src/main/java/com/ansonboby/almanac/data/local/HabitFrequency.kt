package com.ansonboby.almanac.data.local

import com.ansonboby.almanac.R

/** How often a habit is expected. Stored as [Habit.frequency] by [key]. */
sealed interface HabitFrequency {
    val key: String
    val labelRes: Int
    /** Whether the habit is expected on the given weekday (1=Mon … 7=Sun). */
    fun isDueOn(weekday: Int): Boolean

    data object Daily : HabitFrequency {
        override val key = "daily"
        override val labelRes = R.string.habit_freq_daily
        override fun isDueOn(weekday: Int) = true
    }
    data object Weekdays : HabitFrequency {
        override val key = "weekdays"
        override val labelRes = R.string.habit_freq_weekdays
        override fun isDueOn(weekday: Int) = weekday in 1..5
    }
    data object Weekends : HabitFrequency {
        override val key = "weekends"
        override val labelRes = R.string.habit_freq_weekends
        override fun isDueOn(weekday: Int) = weekday in 6..7
    }
    data object Custom : HabitFrequency {
        override val key = "custom"
        override val labelRes = R.string.habit_freq_custom
        override fun isDueOn(weekday: Int) = true // resolved against customDays in repo
    }

    companion object {
        fun fromKey(key: String?, customDays: String?): HabitFrequency = when (key) {
            Daily.key -> Daily
            Weekdays.key -> Weekdays
            Weekends.key -> Weekends
            Custom.key -> Custom
            else -> Daily
        }
    }
}

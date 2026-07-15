package com.ansonboby.almanac.data.local

/**
 * A [Habit] paired with its live status for the daily checklist: whether it was
 * completed on [day] and its current [streak] (consecutive due-days logged).
 */
data class HabitWithStatus(
    val habit: Habit,
    val isDoneToday: Boolean,
    val streak: Int,
)

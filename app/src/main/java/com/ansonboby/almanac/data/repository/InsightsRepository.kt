package com.ansonboby.almanac.data.repository

import com.ansonboby.almanac.data.local.AlmanacDatabase
import com.ansonboby.almanac.data.local.Entry
import com.ansonboby.almanac.data.local.Habit
import com.ansonboby.almanac.data.local.HabitFrequency
import com.ansonboby.almanac.data.util.LocalDateUtil
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Read-only aggregates for the Insights screen (PRD: Insights): mood trend,
 * entry frequency, and per-habit consistency. All heavy lifting is in the pure
 * [computeMoodTrend] / [computeFrequency] / [computeConsistency] helpers so the
 * math is unit-testable without Room.
 */
@Singleton
class InsightsRepository @Inject constructor(
    private val entryRepository: EntryRepository,
    private val habitRepository: HabitRepository,
    db: AlmanacDatabase,
) {
    private val entryDao = db.entryDao()
    private val habitDao = db.habitDao()
    /** All habits (active + archived) with their consistency stats over [periodDays]. */
    suspend fun habitConsistency(periodDays: Int): List<HabitConsistency> {
        val end = LocalDateUtil.todayLocalDay()
        val start = end - (periodDays - 1)
        val habits = habitDao.loadAllHabits()
        val logs = habitDao.logsInRange(start, end)
        val byHabit = logs.groupBy({ it.habitId }, { it.epochDayLocal })
        return habits.map { habit ->
            val logged = byHabit[habit.id].orEmpty().toSet()
            HabitConsistency(
                habit = habit,
                rate = computeConsistency(habit, logged, start, end),
                streak = computeStreak(habit, logged, end),
                loggedCount = logged.size,
            )
        }
    }

    fun moodTrend(entries: List<Entry>, start: Int, end: Int): List<DayMood> =
        computeMoodTrend(entries, start, end)

    fun entryFrequency(entries: List<Entry>, start: Int, end: Int): List<DayCount> =
        computeFrequency(entries, start, end)
}

data class DayMood(
    val day: Int,
    val avgScore: Float,
    val count: Int,
)

data class DayCount(
    val day: Int,
    val count: Int,
)

data class HabitConsistency(
    val habit: Habit,
    /** 0f..1f fraction of due-days in range that were logged. */
    val rate: Float,
    val streak: Int,
    val loggedCount: Int,
)

/**
 * Average mood score per day over [start]..[end]. Days without a mood entry are
 * emitted with [DayMood.count] = 0 so the chart can place gaps.
 */
fun computeMoodTrend(entries: List<Entry>, start: Int, end: Int): List<DayMood> {
    val byDay = entries.filter { it.moodScore != null }
        .groupBy { it.epochDayLocal }
    return (start..end).map { day ->
        val dayEntries = byDay[day]
        if (dayEntries.isNullOrEmpty()) {
            DayMood(day, 0f, 0)
        } else {
            val avg = dayEntries.mapNotNull { it.moodScore }.average().toFloat()
            DayMood(day, avg, dayEntries.size)
        }
    }
}

/** Entry count per day over [start]..[end] (all entry kinds). */
fun computeFrequency(entries: List<Entry>, start: Int, end: Int): List<DayCount> {
    val byDay = entries.groupBy { it.epochDayLocal }
    return (start..end).map { day -> DayCount(day, byDay[day]?.size ?: 0) }
}

/**
 * Fraction of due-days in [start]..[end] that were logged. Days the habit isn't
 * due are excluded from the denominator (same rule as the streak).
 */
fun computeConsistency(
    habit: Habit,
    loggedDays: Set<Int>,
    start: Int,
    end: Int,
): Float {
    val freq = HabitFrequency.fromKey(habit.frequency, habit.customDays)
    val customSet = habit.customDays?.split(',')
        .orEmpty().mapNotNull { it.trim().toIntOrNull() }.toSet()
    var due = 0
    var done = 0
    for (day in start..end) {
        val wd = LocalDateUtil.dayOfWeek(day)
        val isDue = when (freq) {
            HabitFrequency.Custom -> wd in customSet
            else -> freq.isDueOn(wd)
        }
        if (!isDue) continue
        due += 1
        if (day in loggedDays) done += 1
    }
    return if (due == 0) 0f else done.toFloat() / due.toFloat()
}

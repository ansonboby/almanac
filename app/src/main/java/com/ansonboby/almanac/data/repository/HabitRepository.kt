package com.ansonboby.almanac.data.repository

import com.ansonboby.almanac.data.local.AlmanacDatabase
import com.ansonboby.almanac.data.local.Habit
import com.ansonboby.almanac.data.local.HabitFrequency
import com.ansonboby.almanac.data.local.HabitLog
import com.ansonboby.almanac.data.local.HabitWithStatus
import com.ansonboby.almanac.data.util.LocalDateUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Habits feature (PRD: Habits). Owns the daily checklist state, the "stamp"
 * toggle, and streak math. Heavy aggregation lives in the pure [computeStreak]
 * helper so it can be unit-tested without Room.
 */
@Singleton
class HabitRepository @Inject constructor(
    db: AlmanacDatabase,
) {
    private val dao = db.habitDao()
    /** Active habits paired with today's done-state + current streak. */
    fun habitsForDay(day: Int = LocalDateUtil.todayLocalDay()): Flow<List<HabitWithStatus>> =
        dao.observeActiveHabits().combine(dao.observeLogsForDay(day)) { habits, logs ->
            val loggedIds = logs.map { it.habitId }.toSet()
            val loggedDays = logs.map { it.epochDayLocal }.toSet()
            habits.map { habit ->
                HabitWithStatus(
                    habit = habit,
                    isDoneToday = habit.id in loggedIds,
                    streak = computeStreak(habit, loggedDays, day),
                )
            }
        }

    /** Due-weekday bitmask (1=Mon … 7=Sun) for a habit, used by repo + UI. */
    fun dueWeekdays(habit: Habit): Set<Int> {
        val freq = HabitFrequency.fromKey(habit.frequency, habit.customDays)
        return when (freq) {
            HabitFrequency.Custom -> parseCustomDays(habit.customDays)
            else -> (1..7).toSet().filter { freq.isDueOn(it) }.toSet()
        }
    }

    private fun parseCustomDays(csv: String?): Set<Int> =
        csv?.split(',').orEmpty().mapNotNull { it.trim().toIntOrNull() }.toSet()

    fun archivedHabits(): Flow<List<Habit>> = dao.observeArchivedHabits()

    /** Toggle today's completion. Returns the new done-state. */
    suspend fun toggleToday(habitId: Long, day: Int = LocalDateUtil.todayLocalDay()): Boolean {
        val already = dao.loggedIdsForDay(day).contains(habitId)
        return if (already) {
            dao.deleteLogForDay(habitId, day)
            false
        } else {
            dao.insertLog(
                HabitLog(
                    habitId = habitId,
                    epochDayLocal = day,
                    loggedAt = System.currentTimeMillis(),
                ),
            )
            true
        }
    }

    suspend fun addHabit(habit: Habit): Long = dao.insertHabit(habit)

    suspend fun updateHabit(habit: Habit) = dao.updateHabit(habit)

    suspend fun archiveHabit(habit: Habit, archived: Boolean = true) =
        dao.updateHabit(habit.copy(archived = archived))

    /** Remove all habits + logs (purge local ledger). */
    suspend fun deleteAll() {
        dao.deleteAllLogs()
        dao.deleteAllHabits()
    }
}

/**
 * Current streak = consecutive due-days (ending today or yesterday) that were
 * logged. Days the habit isn't due are skipped, not counted against it. Pure +
 * deterministic — see [HabitRepositoryTest].
 */
fun computeStreak(habit: Habit, loggedDays: Set<Int>, today: Int): Int {
    val freq = HabitFrequency.fromKey(habit.frequency, habit.customDays)
    val customSet = habit.customDays?.split(',')
        .orEmpty().mapNotNull { it.trim().toIntOrNull() }.toSet()
    val isDue: (Int) -> Boolean = { d ->
        val wd = LocalDateUtil.dayOfWeek(d)
        when (freq) {
            HabitFrequency.Custom -> wd in customSet
            else -> freq.isDueOn(wd)
        }
    }

    var day = today
    // Allow today to be incomplete without breaking the streak.
    if (!(isDue(day) && day in loggedDays)) day -= 1

    var streak = 0
    while (day >= LocalDateUtil.minDay()) {
        if (!isDue(day)) {
            day -= 1
            continue
        }
        if (day in loggedDays) {
            streak += 1
            day -= 1
        } else {
            break
        }
    }
    return streak
}

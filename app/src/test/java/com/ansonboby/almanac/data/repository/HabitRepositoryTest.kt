package com.ansonboby.almanac.data.repository

import com.ansonboby.almanac.data.local.Habit
import com.ansonboby.almanac.data.local.HabitFrequency
import com.ansonboby.almanac.data.util.LocalDateUtil
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Streak math is the trickiest piece of habit logic (today may be incomplete,
 * non-due days must be skipped). Covered directly via [computeStreak].
 */
class HabitRepositoryTest {

    companion object {
        // Anchor a known Mon..Sun week near "today" so days sit above
        // LocalDateUtil.minDay() (2000-01-01) — the streak scan's lower bound.
        private val MON: Int = run {
            val today = LocalDateUtil.todayLocalDay()
            today - ((LocalDateUtil.dayOfWeek(today) - 1 + 7) % 7)
        }
        private val TUE = MON + 1
        private val WED = MON + 2
        private val THU = MON + 3
        private val FRI = MON + 4
        private val SAT = MON + 5
        private val SUN = MON + 6
    }

    @Test
    fun dailyStreakCountsConsecutiveDueDaysEndingToday() {
        val habit = habit(HabitFrequency.Daily.key)
        val streak = computeStreak(habit, setOf(WED, THU, FRI), today = FRI)
        assertEquals(3, streak)
    }

    @Test
    fun dailyStreakToleratesIncompleteToday() {
        // Today (Fri) not yet logged; yesterday..previous should still count.
        val habit = habit(HabitFrequency.Daily.key)
        val streak = computeStreak(habit, setOf(TUE, WED, THU), today = FRI)
        assertEquals(3, streak)
    }

    @Test
    fun dailyStreakBreaksOnFirstMissedDueDay() {
        val habit = habit(HabitFrequency.Daily.key)
        // Logged Tue, Mon; Wed (due) missed -> streak is just today's run (Tue).
        val streak = computeStreak(habit, setOf(MON, TUE), today = TUE)
        assertEquals(2, streak)
    }

    @Test
    fun weekdaysStreakSkipsNonDueDays() {
        val habit = habit(HabitFrequency.Weekdays.key)
        // Due Mon-Fri. Logged Mon, Tue, Wed, Thu. Fri missed -> 4.
        val streak = computeStreak(habit, setOf(MON, TUE, WED, THU), today = FRI)
        assertEquals(4, streak)
    }

    @Test
    fun customStreakRespectsCsvDays() {
        // Due Mon(1) & Thu(4). Logged both in the latest week.
        val habit = habit(HabitFrequency.Custom.key, customDays = "1,4")
        val streak = computeStreak(habit, setOf(MON, THU), today = FRI)
        assertEquals(2, streak)
    }

    private fun habit(frequency: String, customDays: String? = null) = Habit(
        title = "test",
        frequency = frequency,
        customDays = customDays,
        createdAt = 0L,
    )
}

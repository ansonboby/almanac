package com.ansonboby.almanac.data.repository

import com.ansonboby.almanac.data.local.Entry
import com.ansonboby.almanac.data.local.EntryType
import com.ansonboby.almanac.data.local.Habit
import com.ansonboby.almanac.data.local.HabitFrequency
import com.ansonboby.almanac.data.util.LocalDateUtil
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Pure-logic coverage for the Insights aggregates. These functions are
 * deterministic and take plain in-memory data, so they need no Room.
 */
class InsightsRepositoryTest {

    companion object {
        // 1970-01-01 is Thursday (weekday 4). Anchor a known week:
        // day 4 = Mon, 5 = Tue, 6 = Wed, 7 = Thu, 8 = Fri, 9 = Sat, 10 = Sun.
        private const val MON = 4
        private const val TUE = 5
        private const val WED = 6
        private const val THU = 7
        private const val FRI = 8
        private const val SAT = 9
        private const val SUN = 10

        private fun entry(day: Int, moodScore: Int? = null, type: EntryType = EntryType.TEXT, text: String? = null, tags: String? = null) =
            Entry(
                epochDayLocal = day,
                createdAt = day.toLong(),
                type = type,
                textContent = text,
                moodScore = moodScore,
                tags = tags,
            )
    }

    @Test
    fun dayOfWeekAnchorsAreStable() {
        assertEquals(1, LocalDateUtil.dayOfWeek(MON))
        assertEquals(7, LocalDateUtil.dayOfWeek(SUN))
    }

    @Test
    fun moodTrendAveragesWithinDayAndEmitsZeroForEmpty() {
        val entries = listOf(
            entry(MON, moodScore = 2),
            entry(MON, moodScore = -2),
            entry(TUE, moodScore = 1),
            entry(WED, moodScore = null), // text-only, excluded from mood
        )
        val trend = computeMoodTrend(entries, MON, WED)
        assertEquals(3, trend.size)
        assertEquals(0f, trend[0].avgScore) // (2 + -2) / 2
        assertEquals(2, trend[0].count)
        assertEquals(1f, trend[1].avgScore)
        assertEquals(1, trend[1].count)
        assertEquals(0, trend[2].count) // no mood entry Wed
        assertEquals(0f, trend[2].avgScore)
    }

    @Test
    fun frequencyCountsEveryEntryKind() {
        val entries = listOf(
            entry(MON, type = EntryType.PHOTO),
            entry(MON, type = EntryType.TEXT),
            entry(TUE, type = EntryType.MOOD, moodScore = 0),
        )
        val freq = computeFrequency(entries, MON, WED)
        assertEquals(3, freq.size)
        assertEquals(2, freq[0].count)
        assertEquals(1, freq[1].count)
        assertEquals(0, freq[2].count)
    }

    @Test
    fun consistencyDailyAllLoggedIsOne() {
        val habit = habit(HabitFrequency.Daily.key)
        val rate = computeConsistency(habit, setOf(MON, TUE, WED), MON, WED)
        assertEquals(1f, rate)
    }

    @Test
    fun consistencyWeekdaysExcludesWeekendFromDenominator() {
        // Due Mon-Fri (5 days); logged Mon-Thu (4) -> 0.8f
        val habit = habit(HabitFrequency.Weekdays.key)
        val rate = computeConsistency(habit, setOf(MON, TUE, WED, THU), MON, SUN)
        assertEquals(0.8f, rate)
    }

    @Test
    fun consistencyCustomUsesCsvDays() {
        // Custom on Mon(1) & Thu(4) only, within Mon..Sun.
        val habit = habit(HabitFrequency.Custom.key, customDays = "1,4")
        val rate = computeConsistency(habit, setOf(MON, THU), MON, SUN)
        assertEquals(1f, rate)
    }

    @Test
    fun consistencyNoDueDaysInRangeIsZero() {
        val habit = habit(HabitFrequency.Weekends.key)
        // Range covers only Mon, so no due days -> 0f, not divide-by-zero.
        val rate = computeConsistency(habit, emptySet(), MON, MON)
        assertEquals(0f, rate)
    }

    private fun habit(frequency: String, customDays: String? = null) = Habit(
        title = "test",
        frequency = frequency,
        customDays = customDays,
        createdAt = 0L,
    )
}

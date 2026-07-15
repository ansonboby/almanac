package com.ansonboby.almanac.data.reminder

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.concurrent.TimeUnit

/** Pure logic in [ReminderScheduler.initialDelayMillis] — the tricky part of daily reminders. */
class ReminderSchedulerTest {

    private fun at(hour: Int, minute: Int, second: Int): Long {
        val cal = Calendar.getInstance().apply {
            set(2026, Calendar.JULY, 16, hour, minute, second)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    @Test
    fun `future time today yields delay within 24h`() {
        val now = at(9, 0, 0)
        val delay = ReminderScheduler.initialDelayMillis(20, 0, now)
        val expected = TimeUnit.HOURS.toMillis(11)
        assertEquals(expected, delay)
        assertTrue(delay in 1 until TimeUnit.DAYS.toMillis(1))
    }

    @Test
    fun `past time today rolls to tomorrow`() {
        val now = at(21, 0, 0)
        val delay = ReminderScheduler.initialDelayMillis(20, 0, now)
        // 20:00 already passed -> next is tomorrow 20:00 => 23h until midnight + 20h
        val expected = TimeUnit.HOURS.toMillis(23)
        assertEquals(expected, delay)
        assertTrue(delay >= TimeUnit.HOURS.toMillis(20))
    }

    @Test
    fun `exact current time rolls to tomorrow`() {
        val now = at(20, 0, 0)
        val delay = ReminderScheduler.initialDelayMillis(20, 0, now)
        assertEquals(TimeUnit.DAYS.toMillis(1), delay)
    }

    @Test
    fun `minute precision`() {
        val now = at(8, 0, 0)
        val delay = ReminderScheduler.initialDelayMillis(8, 30, now)
        assertEquals(TimeUnit.MINUTES.toMillis(30), delay)
    }
}

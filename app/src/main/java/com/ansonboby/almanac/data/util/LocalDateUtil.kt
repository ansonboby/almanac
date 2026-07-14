package com.ansonboby.almanac.data.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/** Local-day helpers. [epochDayLocal] matches `LocalDate.toEpochDay()` so the DB
 *  and the UI agree on "which day" in the device timezone (PRD 5). */
object LocalDateUtil {

    fun todayLocalDay(): Int = LocalDate.now().toEpochDay().toInt()

    fun localDay(date: LocalDate): Int = date.toEpochDay().toInt()

    fun toLocalDate(epochDayLocal: Int): LocalDate =
        LocalDate.ofEpochDay(epochDayLocal.toLong())

    fun startOfMonth(epochDayLocal: Int): Int {
        val d = toLocalDate(epochDayLocal)
        return localDay(d.withDayOfMonth(1))
    }

    fun endOfMonth(epochDayLocal: Int): Int {
        val d = toLocalDate(epochDayLocal)
        return localDay(d.withDayOfMonth(d.lengthOfMonth()))
    }

    fun nowMillis(): Long = Instant.now().toEpochMilli()

    /** "14 JUL" — short stamp date (DESIGN.md: dates in mono). */
    fun stampDate(epochDayLocal: Int): String =
        toLocalDate(epochDayLocal).format(DateTimeFormatter.ofPattern("dd MMM"))
            .uppercase()

    /** "1872" style 4-digit year for the specimen/year context if needed. */
    fun year(epochDayLocal: Int): Int = toLocalDate(epochDayLocal).year

    /** Human day label: "Today" / "Yesterday" / "Mon, 14 Jul". */
    fun dayLabel(epochDayLocal: Int): String {
        val d = toLocalDate(epochDayLocal)
        val today = LocalDate.now()
        return when (epochDayLocal) {
            localDay(today) -> "Today"
            localDay(today.minus(1, ChronoUnit.DAYS)) -> "Yesterday"
            else -> d.format(DateTimeFormatter.ofPattern("EEE, d MMM"))
        }
    }

    fun monthLabel(epochDayLocal: Int): String =
        toLocalDate(epochDayLocal).format(DateTimeFormatter.ofPattern("MMMM yyyy"))

    fun monthShort(epochDayLocal: Int): String =
        toLocalDate(epochDayLocal).format(DateTimeFormatter.ofPattern("MMM"))

    fun dayOfMonth(epochDayLocal: Int): Int = toLocalDate(epochDayLocal).dayOfMonth
}

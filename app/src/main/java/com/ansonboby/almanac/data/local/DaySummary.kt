package com.ansonboby.almanac.data.local

import androidx.room.ColumnInfo

/**
 * Lightweight per-day aggregate for the Month stamp-sheet (PRD 6: "a grid of
 * stamps — brass-inked where a day has an entry, tinted by that day's mood").
 */
data class DaySummary(
    @ColumnInfo(name = "day") val day: Int,
    @ColumnInfo(name = "moodScore") val moodScore: Int?,
    @ColumnInfo(name = "count") val count: Int,
)

package com.ansonboby.almanac.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A repeated practice the user tends to daily (PRD: Habits). Field Ledger voice
 * calls these "practices" / "observances" — a naturalist's daily discipline, not
 * a gamified checkbox. Streaks are shown in mono as a counter (DESIGN.md).
 *
 * [frequency] is a [HabitFrequency] key; for [HabitFrequency.Custom] the due
 * weekdays live in [customDays] as a CSV bitmask (1=Mon … 7=Sun).
 */
@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    @ColumnInfo(name = "description") val description: String? = null,
    @ColumnInfo(name = "frequency") val frequency: String = HabitFrequency.Daily.key,
    /** CSV bitmask of due weekdays for [HabitFrequency.Custom], e.g. "2,4,6". */
    @ColumnInfo(name = "custom_days") val customDays: String? = null,
    /** Field Ledger accent: "brass" | "moss" | "dusty_rose". */
    @ColumnInfo(name = "tint") val tint: String = "brass",
    @ColumnInfo(name = "archived") val archived: Boolean = false,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "position") val position: Int = 0,
)

/**
 * One completion of a [Habit] on a given local day. A habit is "done today"
 * when a log with [epochDayLocal] == today exists. Soft-unlogged by deletion.
 */
@Entity(
    tableName = "habit_logs",
    foreignKeys = [
        ForeignKey(
            entity = Habit::class,
            parentColumns = ["id"],
            childColumns = ["habit_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("habit_id"), Index("epoch_day_local")],
)
data class HabitLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "habit_id") val habitId: Long,
    @ColumnInfo(name = "epoch_day_local") val epochDayLocal: Int,
    @ColumnInfo(name = "logged_at") val loggedAt: Long,
    @ColumnInfo(name = "note") val note: String? = null,
)

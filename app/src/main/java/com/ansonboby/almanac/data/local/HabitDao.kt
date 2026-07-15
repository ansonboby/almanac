package com.ansonboby.almanac.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.ColumnInfo
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: Habit): Long

    @Update
    suspend fun updateHabit(habit: Habit)

    @Delete
    suspend fun deleteHabit(habit: Habit)

    @Query("SELECT * FROM habits WHERE archived = 0 ORDER BY position ASC, created_at ASC")
    fun observeActiveHabits(): Flow<List<Habit>>

    @Query("SELECT * FROM habits WHERE archived = 1 ORDER BY position ASC, created_at ASC")
    fun observeArchivedHabits(): Flow<List<Habit>>

    @Query("SELECT * FROM habits ORDER BY position ASC, created_at ASC")
    suspend fun loadAllHabits(): List<Habit>

    @Query("SELECT * FROM habits WHERE id = :id")
    suspend fun loadHabit(id: Long): Habit?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: HabitLog): Long

    @Query("DELETE FROM habit_logs WHERE habit_id = :habitId AND epoch_day_local = :day")
    suspend fun deleteLogForDay(habitId: Long, day: Int)

    @Query("SELECT * FROM habit_logs WHERE habit_id = :habitId ORDER BY epoch_day_local ASC")
    fun observeLogs(habitId: Long): Flow<List<HabitLog>>

    @Query("SELECT * FROM habit_logs WHERE epoch_day_local = :day")
    fun observeLogsForDay(day: Int): Flow<List<HabitLog>>

    @Query("SELECT DISTINCT habit_id FROM habit_logs WHERE epoch_day_local = :day")
    suspend fun loggedIdsForDay(day: Int): List<Long>

    /** Wipe every habit + log (purge). */
    @Query("DELETE FROM habit_logs")
    suspend fun deleteAllLogs()

    @Query("DELETE FROM habits")
    suspend fun deleteAllHabits()

    @Query(
        "SELECT epoch_day_local FROM habit_logs " +
            "WHERE habit_id = :habitId AND epoch_day_local BETWEEN :start AND :end",
    )
    suspend fun loggedDaysBetween(habitId: Long, start: Int, end: Int): List<Int>

    /** All (habitId, day) pairs in a range — used for habit consistency in Insights. */
    @Query(
        "SELECT habit_id, epoch_day_local FROM habit_logs " +
            "WHERE epoch_day_local BETWEEN :start AND :end",
    )
    suspend fun logsInRange(start: Int, end: Int): List<HabitLogDay>
}

/** Minimal projection for range-wide log scans (keeps the cursor light). */
data class HabitLogDay(
    @ColumnInfo(name = "habit_id") val habitId: Long,
    @ColumnInfo(name = "epoch_day_local") val epochDayLocal: Int,
)


package com.ansonboby.almanac.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface EntryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: Entry): Long

    @Update
    suspend fun update(entry: Entry)

    @Delete
    suspend fun delete(entry: Entry)

    /** Live entries for one day, oldest first within the day. */
    @Query(
        "SELECT * FROM entries WHERE epoch_day_local = :day " +
            "AND deleted = 0 ORDER BY created_at ASC",
    )
    fun observeDay(day: Int): Flow<List<Entry>>

    /** All live entries, newest first — used for the running archival counter. */
    @Query("SELECT * FROM entries WHERE deleted = 0 ORDER BY created_at ASC")
    fun observeAll(): Flow<List<Entry>>

    @Query("SELECT * FROM entries WHERE id = :id AND deleted = 0")
    fun observeById(id: Long): Flow<Entry?>

    /** One row, for edit screens that want a suspend read. */
    @Query("SELECT * FROM entries WHERE id = :id AND deleted = 0")
    suspend fun loadById(id: Long): Entry?

    /** Month aggregate: which days have entries + the day's dominant mood. */
    @Query(
        "SELECT epoch_day_local AS day, " +
            "MAX(mood_score) AS moodScore, COUNT(*) AS count " +
            "FROM entries WHERE deleted = 0 AND epoch_day_local BETWEEN :start AND :end " +
            "GROUP BY epoch_day_local",
    )
    fun observeMonth(start: Int, end: Int): Flow<List<DaySummary>>

    @Query("SELECT COUNT(*) FROM entries WHERE deleted = 0")
    suspend fun countLive(): Int

    /** Wipe every entry (purge). */
    @Query("DELETE FROM entries")
    suspend fun deleteAll()

    /** Raw live entries in a day range — backs month search/filter. */
    @Query(
        "SELECT * FROM entries WHERE deleted = 0 AND epoch_day_local BETWEEN :start AND :end " +
            "ORDER BY epoch_day_local ASC, created_at ASC",
    )
    fun observeMonthEntries(start: Int, end: Int): Flow<List<Entry>>
}

package com.ansonboby.almanac.data.local

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * PHASE 0 PLACEHOLDER — remove in Phase 1 with [PlaceholderEntity].
 */
@Dao
interface PlaceholderDao {
    @Query("SELECT COUNT(*) FROM placeholder")
    fun count(): Flow<Int>
}

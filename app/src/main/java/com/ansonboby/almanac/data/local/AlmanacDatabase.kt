package com.ansonboby.almanac.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * The app's single Room database (offline-first, single source of truth, PRD 2).
 *
 * Phase 0 ships only [PlaceholderEntity] to prove the wiring compiles; Phase 1
 * introduces the real `Entry` / `Habit` / `Tag` schema and bumps the version.
 */
@Database(
    entities = [PlaceholderEntity::class],
    version = 1,
    // Phase 1 will enable schema export with a configured schema directory.
    exportSchema = false,
)
abstract class AlmanacDatabase : RoomDatabase() {
    abstract fun placeholderDao(): PlaceholderDao

    companion object {
        const val NAME = "almanac.db"
    }
}

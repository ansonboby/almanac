package com.ansonboby.almanac.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * The app's single Room database (PRD 5). Phase 0 used a throwaway placeholder
 * entity; this is the real schema. [androidx.room.RoomDatabase.exportSchema] is
 * off to keep the repo lean — turn on + version-migrate when the schema changes.
 */
@Database(entities = [Entry::class], version = 1, exportSchema = false)
abstract class AlmanacDatabase : RoomDatabase() {
    abstract fun entryDao(): EntryDao
}

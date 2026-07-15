package com.ansonboby.almanac.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * The app's single Room database (PRD 5). v1 held only [Entry]; v2 adds the
 * Habits feature ([Habit] + [HabitLog]). [exportSchema] is off to keep the repo
 * lean — add a `schemas/` dir + version-migrate when the schema changes again.
 */
@Database(
    entities = [Entry::class, Habit::class, HabitLog::class],
    version = 2,
    exportSchema = false,
)
abstract class AlmanacDatabase : RoomDatabase() {
    abstract fun entryDao(): EntryDao
    abstract fun habitDao(): HabitDao

    companion object {
        /** v1 -> v2: create the habits + habit_logs tables without dropping entries. */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS habits (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        title TEXT NOT NULL,
                        description TEXT,
                        frequency TEXT NOT NULL DEFAULT 'daily',
                        custom_days TEXT,
                        tint TEXT NOT NULL DEFAULT 'brass',
                        archived INTEGER NOT NULL DEFAULT 0,
                        created_at INTEGER NOT NULL,
                        position INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS habit_logs (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        habit_id INTEGER NOT NULL,
                        epoch_day_local INTEGER NOT NULL,
                        logged_at INTEGER NOT NULL,
                        note TEXT,
                        FOREIGN KEY(habit_id) REFERENCES habits(id) ON DELETE CASCADE
                    )
                    """.trimIndent(),
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_habit_logs_habit_id ON habit_logs(habit_id)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_habit_logs_epoch_day_local ON habit_logs(epoch_day_local)")
            }
        }
    }
}

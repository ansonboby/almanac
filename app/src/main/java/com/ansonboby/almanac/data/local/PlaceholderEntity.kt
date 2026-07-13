package com.ansonboby.almanac.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * PHASE 0 PLACEHOLDER — remove in Phase 1.
 *
 * Room requires at least one entity to generate a database. This exists only so
 * the Room/KSP/Hilt wiring compiles and is verifiably injectable end-to-end.
 * Phase 1 replaces it with the real `Entry` schema (PRD 5).
 */
@Entity(tableName = "placeholder")
data class PlaceholderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val createdAt: Long = 0,
)

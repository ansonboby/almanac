package com.ansonboby.almanac.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One logged piece of content tied to a day (PRD 5). A day is a small ordered
 * list of [Entry] rows sharing [epochDayLocal], so "a day with three photos and
 * a paragraph" is just four rows. Keeps the schema simple while supporting the
 * unified timeline.
 */
@Entity(tableName = "entries")
data class Entry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** Local calendar day this belongs to, in the device timezone. */
    @ColumnInfo(name = "epoch_day_local") val epochDayLocal: Int,
    /** Instant the entry was first written. Also the sort key within a day. */
    @ColumnInfo(name = "created_at") val createdAt: Long,
    /** PHOTO | TEXT | MOOD | CHECK_IN — an entry carries one primary content kind. */
    @ColumnInfo(name = "type") val type: EntryType,
    @ColumnInfo(name = "text_content") val textContent: String? = null,
    /** App-private content URI (scoped storage), see FileStorage. */
    @ColumnInfo(name = "photo_uri") val photoUri: String? = null,
    /** -2..+2, mapped to a weather-report mood glyph (PRD 3.3). */
    @ColumnInfo(name = "mood_score") val moodScore: Int? = null,
    @ColumnInfo(name = "location_name") val locationName: String? = null,
    @ColumnInfo(name = "lat") val lat: Double? = null,
    @ColumnInfo(name = "lng") val lng: Double? = null,
    /** Comma-joined tag labels (field-guide register, e.g. "Aquilegia formosa"). */
    @ColumnInfo(name = "tags") val tags: String? = null,
    /** Running archival number shown as "№ 442" (DESIGN.md voice). */
    @ColumnInfo(name = "archival_no") val archivalNo: Long = 0,
    /** Soft-delete flag so deletions never lose data silently. */
    @ColumnInfo(name = "deleted") val deleted: Boolean = false,
)

enum class EntryType {
    PHOTO, TEXT, MOOD, CHECK_IN,
}

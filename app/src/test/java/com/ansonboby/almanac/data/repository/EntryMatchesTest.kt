package com.ansonboby.almanac.data.repository

import com.ansonboby.almanac.data.local.Entry
import com.ansonboby.almanac.data.local.EntryType
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Filter logic for Today/Month search (PRD: search across text + tags). */
class EntryMatchesTest {

    private fun entry(text: String? = null, tags: String? = null, type: EntryType = EntryType.TEXT) =
        Entry(
            epochDayLocal = 4,
            createdAt = 4L,
            type = type,
            textContent = text,
            tags = tags,
        )

    @Test
    fun blankQueryMatchesAll() {
        val e = entry("anything")
        assertTrue(e.matches("", EntryFilter.ALL))
        assertTrue(e.matches("   ", EntryFilter.ALL))
    }

    @Test
    fun queryMatchesTextCaseInsensitively() {
        val e = entry(text = "Saw an Aquilegia formosa")
        assertTrue(e.matches("aquilegia", EntryFilter.ALL))
        assertFalse(e.matches("moss", EntryFilter.ALL))
    }

    @Test
    fun queryMatchesTags() {
        val e = entry(text = "walk", tags = "botany, field-note")
        assertTrue(e.matches("botany", EntryFilter.ALL))
        assertTrue(e.matches("FIELD-NOTE", EntryFilter.ALL))
    }

    @Test
    fun typeFilterNarrowsByKind() {
        val photo = entry(type = EntryType.PHOTO)
        val text = entry(type = EntryType.TEXT)
        assertTrue(photo.matches("", EntryFilter.PHOTO))
        assertFalse(text.matches("", EntryFilter.PHOTO))
        assertTrue(text.matches("", EntryFilter.ALL))
    }

    @Test
    fun queryAndFilterCombine() {
        val e = entry(text = "moss study", tags = "botany", type = EntryType.TEXT)
        assertTrue(e.matches("moss", EntryFilter.TEXT))
        assertFalse(e.matches("moss", EntryFilter.PHOTO))
        assertFalse(e.matches("river", EntryFilter.TEXT))
    }
}

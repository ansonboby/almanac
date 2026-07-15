package com.ansonboby.almanac.data.repository

import com.ansonboby.almanac.data.local.EntryType

/**
 * Timeline filter surfaced on the Today / Month views. [ALL] shows everything;
 * the rest narrow to one [EntryType]. Kept in the repository package so the
 * ViewModel (ui) can depend on it without the data layer reaching into ui.
 */
enum class EntryFilter(val entryType: EntryType?) {
    ALL(null),
    PHOTO(EntryType.PHOTO),
    TEXT(EntryType.TEXT),
    MOOD(EntryType.MOOD),
    NOTE(EntryType.CHECK_IN),
}

package com.ansonboby.almanac.data.repository

import com.ansonboby.almanac.data.local.AlmanacDatabase
import com.ansonboby.almanac.data.local.DaySummary
import com.ansonboby.almanac.data.local.Entry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The only layer ViewModels talk to. Wraps the Entry DAO, owns the running
 * archival number (DESIGN.md voice: "№ 442"), and exposes Flow queries so the UI
 * stays live against Room (PRD 7: no data loss, real persistence).
 */
@Singleton
class EntryRepository @Inject constructor(
    db: AlmanacDatabase,
) {
    private val dao = db.entryDao()
    fun dayEntries(day: Int): Flow<List<Entry>> = dao.observeDay(day)

    /** Day's entries, narrowed by a text query + [EntryFilter] (search/filter). */
    fun dayEntries(
        day: Int,
        query: String,
        filter: EntryFilter,
    ): Flow<List<Entry>> = dao.observeDay(day).map { list ->
        list.filter { it.matches(query, filter) }
    }

    fun entry(id: Long): Flow<Entry?> = dao.observeById(id)

    /** Every live entry, newest first — backs the Insights aggregates. */
    fun allEntries(): Flow<List<Entry>> = dao.observeAll()

    fun month(centerDay: Int): Flow<List<DaySummary>> = month(centerDay, "", EntryFilter.ALL)

    /** Month grid, per-day counts narrowed by a text query + [EntryFilter]. */
    fun month(
        centerDay: Int,
        query: String,
        filter: EntryFilter,
    ): Flow<List<DaySummary>> {
        val start = com.ansonboby.almanac.data.util.LocalDateUtil.startOfMonth(centerDay)
        val end = com.ansonboby.almanac.data.util.LocalDateUtil.endOfMonth(centerDay)
        val noFilter = query.isBlank() && filter == EntryFilter.ALL
        if (noFilter) {
            return dao.observeMonth(start, end).map { raw ->
                raw.map { DaySummary(day = it.day, moodScore = it.moodScore, count = it.count) }
            }
        }
        return dao.observeMonthEntries(start, end).map { entries ->
            entries.filter { it.matches(query, filter) }
                .groupBy { it.epochDayLocal }
                .map { (day, dayEntries) ->
                    val mood = dayEntries.firstOrNull { it.moodScore != null }?.moodScore
                    DaySummary(
                        day = day,
                        moodScore = mood,
                        count = dayEntries.size,
                    )
                }
        }
    }

    /** Inserts a new entry, assigning the next running archival number. */
    suspend fun create(entry: Entry): Long {
        val nextNo = (dao.countLive() + 1).toLong()
        return dao.insert(entry.copy(archivalNo = nextNo))
    }

    suspend fun update(entry: Entry) = dao.update(entry)

    suspend fun delete(entry: Entry) = dao.delete(entry)
}

/** Pure filter used by [EntryRepository.dayEntries]; unit-testable on its own. */
fun Entry.matches(query: String, filter: EntryFilter): Boolean {
    if (filter.entryType != null && type != filter.entryType) return false
    if (query.isBlank()) return true
    val q = query.lowercase().trim()
    return (textContent?.lowercase()?.contains(q) == true) ||
        (tags?.lowercase()?.contains(q) == true)
}

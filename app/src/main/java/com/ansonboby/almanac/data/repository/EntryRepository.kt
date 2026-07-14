package com.ansonboby.almanac.data.repository

import com.ansonboby.almanac.data.local.DaySummary
import com.ansonboby.almanac.data.local.Entry
import com.ansonboby.almanac.data.local.EntryDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The only layer ViewModels talk to. Wraps [EntryDao], owns the running archival
 * number (DESIGN.md voice: "№ 442"), and exposes Flow queries so the UI stays
 * live against Room (PRD 7: no data loss, real persistence).
 */
@Singleton
class EntryRepository @Inject constructor(
    private val dao: EntryDao,
) {
    fun dayEntries(day: Int): Flow<List<Entry>> = dao.observeDay(day)

    fun entry(id: Long): Flow<Entry?> = dao.observeById(id)

    fun month(centerDay: Int): Flow<List<DaySummary>> {
        val start = com.ansonboby.almanac.data.util.LocalDateUtil.startOfMonth(centerDay)
        val end = com.ansonboby.almanac.data.util.LocalDateUtil.endOfMonth(centerDay)
        return dao.observeMonth(start, end)
    }

    /** Inserts a new entry, assigning the next running archival number. */
    suspend fun create(entry: Entry): Long {
        val nextNo = (dao.countLive() + 1).toLong()
        return dao.insert(entry.copy(archivalNo = nextNo))
    }

    suspend fun update(entry: Entry) = dao.update(entry)

    suspend fun delete(entry: Entry) = dao.delete(entry)
}

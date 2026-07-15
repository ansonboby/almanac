package com.ansonboby.almanac.ui.navigation

/** All destinations in the app. */
sealed class Destination(val route: String) {
    data object Onboarding : Destination("onboarding")
    data object Today : Destination("today")
    data object Month : Destination("month")
    data object Habits : Destination("habits")
    data object Insights : Destination("insights")
    data object Settings : Destination("settings")
    data object Specimen : Destination("specimen")
    data object NewEntry : Destination("new_entry")
    data object EntryDetail : Destination("entry_detail/{entryId}") {
        fun create(entryId: Long) = "entry_detail/$entryId"
    }

    companion object {
        val bottomNav = listOf(Today, Month, Habits, Insights, Settings)
    }
}

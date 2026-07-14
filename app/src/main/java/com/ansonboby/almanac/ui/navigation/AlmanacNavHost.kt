package com.ansonboby.almanac.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ansonboby.almanac.ui.entry.EntryDetailScreen
import com.ansonboby.almanac.ui.entry.NewEntryScreen
import com.ansonboby.almanac.ui.insights.InsightsScreen
import com.ansonboby.almanac.ui.month.MonthScreen
import com.ansonboby.almanac.ui.settings.SettingsScreen
import com.ansonboby.almanac.ui.today.TodayScreen

/**
 * Navigation shell for the single-Activity app (PRD 6): bottom nav across
 * Today / Month / Insights / Settings, with New Entry and Entry Detail as
 * overlay routes above the active tab. The onboarding gate lives in MainActivity.
 */
@Composable
fun AlmanacNavHost(
    darkTheme: Boolean,
    onToggleTheme: () -> Unit,
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val current = navBackStackEntry?.destination
    val showBottomBar = current?.hierarchy?.none { it.route in listOf(Destination.NewEntry.route, Destination.EntryDetail.route) } ?: true

    Scaffold(
        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomBar) {
                AlmanacBottomBar(
                    current = Destination.bottomNav.firstOrNull { dest ->
                        current?.hierarchy?.any { it.route == dest.route } == true
                    } ?: Destination.Today,
                    onNavigate = { dest ->
                        navController.navigate(dest.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Destination.Today.route,
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
            composable(Destination.Today.route) {
                TodayScreen(
                    onNewEntry = { navController.navigate(Destination.NewEntry.route) },
                    onOpenEntry = { id -> navController.navigate(Destination.EntryDetail.create(id)) },
                    onToggleTheme = onToggleTheme,
                )
            }
            composable(Destination.Month.route) {
                MonthScreen(
                    onOpenDay = { day -> navController.navigate(Destination.EntryDetail.create(day.toLong())) },
                    onToggleTheme = onToggleTheme,
                )
            }
            composable(Destination.Insights.route) {
                InsightsScreen(onToggleTheme = onToggleTheme)
            }
            composable(Destination.Settings.route) {
                SettingsScreen(onToggleTheme = onToggleTheme)
            }
            composable(Destination.NewEntry.route) {
                NewEntryScreen(
                    onDone = { navController.popBackStack() },
                    onToggleTheme = onToggleTheme,
                )
            }
            composable(
                Destination.EntryDetail.route,
                arguments = listOf(navArgument("entryId") { type = androidx.navigation.NavType.LongType }),
            ) {
                EntryDetailScreen(
                    onBack = { navController.popBackStack() },
                    onToggleTheme = onToggleTheme,
                )
            }
        }
    }
}

package com.ansonboby.almanac.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ansonboby.almanac.ui.specimen.StyleSpecimenScreen

/**
 * Navigation shell for the single-Activity app (PRD 6).
 *
 * Phase 0 has a single destination — the Style Specimen sanity screen. Phase 1
 * expands this into the Today / Month / Insights / Settings destinations.
 */
object Routes {
    const val SPECIMEN = "specimen"
}

@Composable
fun AlmanacNavHost(
    darkTheme: Boolean,
    onToggleTheme: () -> Unit,
) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.SPECIMEN) {
        composable(Routes.SPECIMEN) {
            StyleSpecimenScreen(
                darkTheme = darkTheme,
                onToggleTheme = onToggleTheme,
            )
        }
    }
}

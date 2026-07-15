package com.ansonboby.almanac

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ansonboby.almanac.data.datastore.PreferencesManager
import com.ansonboby.almanac.ui.navigation.AlmanacNavHost
import com.ansonboby.almanac.ui.onboarding.OnboardingScreen
import com.ansonboby.almanac.ui.theme.AlmanacTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/** The single Activity for Almanac (single-Activity architecture, PRD 2). */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var preferences: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val onboardingComplete by preferences.onboardingComplete
                .collectAsStateWithLifecycle(initialValue = false)
            val themeMode by preferences.themeMode
                .collectAsStateWithLifecycle(initialValue = 1)

            // Theme is authoritative from preferences (Settings is the source of
            // truth). 1 = Ink (dark), 2 = Parchment (light). Default to Ink.
            val darkTheme = themeMode != 2
            val scope = rememberCoroutineScope()

            AlmanacTheme(darkTheme = darkTheme) {
                if (onboardingComplete) {
                    AlmanacNavHost(
                        darkTheme = darkTheme,
                        onToggleTheme = {
                            scope.launch { preferences.setThemeMode(if (darkTheme) 2 else 1) }
                        },
                    )
                } else {
                    OnboardingScreen(
                        onFinish = {
                            scope.launch { preferences.setOnboardingComplete(true) }
                        },
                        onToggleTheme = {
                            scope.launch { preferences.setThemeMode(if (darkTheme) 2 else 1) }
                        },
                    )
                }
            }
        }
    }
}

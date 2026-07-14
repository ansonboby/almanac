package com.ansonboby.almanac

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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

            // 1 = Ink (dark), 2 = Parchment (light). Default to Ink (PRD 3.1).
            var darkTheme by remember { mutableStateOf(themeMode != 2) }
            val scope = rememberCoroutineScope()

            AlmanacTheme(darkTheme = darkTheme) {
                if (onboardingComplete) {
                    AlmanacNavHost(
                        darkTheme = darkTheme,
                        onToggleTheme = {
                            darkTheme = !darkTheme
                            val next = if (darkTheme) 1 else 2
                            scope.launch { preferences.setThemeMode(next) }
                        },
                    )
                } else {
                    OnboardingScreen(
                        onFinish = {
                            scope.launch { preferences.setOnboardingComplete(true) }
                        },
                        onToggleTheme = { darkTheme = !darkTheme },
                    )
                }
            }
        }
    }
}

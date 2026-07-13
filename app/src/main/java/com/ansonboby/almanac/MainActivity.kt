package com.ansonboby.almanac

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.ansonboby.almanac.ui.navigation.AlmanacNavHost
import com.ansonboby.almanac.ui.theme.AlmanacTheme
import dagger.hilt.android.AndroidEntryPoint

/** The single Activity for Almanac (single-Activity architecture, PRD 2). */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val systemDark = isSystemInDarkTheme()
            // Phase 0: local override so the specimen screen can demo both themes.
            var darkTheme by rememberSaveable { mutableStateOf(systemDark) }
            AlmanacTheme(darkTheme = darkTheme) {
                AlmanacNavHost(
                    darkTheme = darkTheme,
                    onToggleTheme = { darkTheme = !darkTheme },
                )
            }
        }
    }
}

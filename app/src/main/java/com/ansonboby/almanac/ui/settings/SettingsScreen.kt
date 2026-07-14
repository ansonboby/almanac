package com.ansonboby.almanac.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ansonboby.almanac.R
import com.ansonboby.almanac.ui.components.ThemeToggleChip
import com.ansonboby.almanac.ui.theme.AlmanacTypography
import com.ansonboby.almanac.ui.theme.FieldLedgerPalette
import com.ansonboby.almanac.ui.theme.StampType

/** Placeholder for Phase 2+ (PRD 6). Theme toggle + colophon live here now. */
@Composable
fun SettingsScreen(onToggleTheme: () -> Unit) {
    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(stringResource(R.string.settings_placeholder_title), style = AlmanacTypography.displaySmall, color = MaterialTheme.colorScheme.onBackground)
            Text(stringResource(R.string.settings_placeholder_body), style = AlmanacTypography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            ThemeToggleChip(onToggleTheme = onToggleTheme)
            Column(Modifier.fillMaxWidth().padding(top = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(R.string.finis_opus), style = StampType.counter, color = FieldLedgerPalette.Brass)
                Text(
                    "Authenticated Ledger — The Naturalist's Almanac — Est. MMXXIV",
                    style = StampType.metadata,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

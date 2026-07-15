package com.ansonboby.almanac.ui.navigation

import androidx.compose.foundation.combinedClickable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ansonboby.almanac.R
import com.ansonboby.almanac.ui.navigation.CogIcon
import com.ansonboby.almanac.ui.navigation.HabitIcon
import com.ansonboby.almanac.ui.navigation.InsightsIcon
import com.ansonboby.almanac.ui.navigation.LedgerIcon
import com.ansonboby.almanac.ui.navigation.StampSheetIcon
import com.ansonboby.almanac.ui.theme.AlmanacTypography
import com.ansonboby.almanac.ui.theme.FieldLedgerPalette

/** Bottom navigation with the five primary destinations (PRD 6). */
@Composable
fun AlmanacBottomBar(
    current: Destination,
    onNavigate: (Destination) -> Unit,
    onOpenSpecimen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        tonalElevation = 0.dp,
    ) {
        Destination.bottomNav.forEach { dest ->
            val selected = current == dest
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(dest) },
                icon = {
                    val tint = if (selected) FieldLedgerPalette.Brass else FieldLedgerPalette.Moss
                    when (dest) {
                        Destination.Today -> LedgerIcon(tint = tint)
                        Destination.Month -> StampSheetIcon(tint = tint)
                        Destination.Habits -> HabitIcon(tint = tint)
                        Destination.Insights -> InsightsIcon(tint = tint)
                        Destination.Settings -> CogIcon(tint = tint)
                        else -> LedgerIcon(tint = tint)
                    }
                },
                label = {
                    Text(
                        text = stringResource(labelFor(dest)),
                        style = AlmanacTypography.labelSmall,
                        color = if (selected) FieldLedgerPalette.Brass else FieldLedgerPalette.Moss,
                    )
                },
                modifier = if (dest == Destination.Settings) {
                    Modifier.combinedClickable(
                        onClick = { onNavigate(dest) },
                        onLongClick = onOpenSpecimen,
                    )
                } else {
                    Modifier
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.surfaceVariant,
                    selectedIconColor = FieldLedgerPalette.Brass,
                    selectedTextColor = FieldLedgerPalette.Brass,
                    unselectedIconColor = FieldLedgerPalette.Moss,
                    unselectedTextColor = FieldLedgerPalette.Moss,
                ),
            )
        }
    }
}

private fun labelFor(dest: Destination) = when (dest) {
    Destination.Today -> R.string.nav_today
    Destination.Month -> R.string.nav_month
    Destination.Habits -> R.string.nav_habits
    Destination.Insights -> R.string.nav_insights
    Destination.Settings -> R.string.nav_settings
    else -> R.string.nav_today
}

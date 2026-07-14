package com.ansonboby.almanac.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ansonboby.almanac.R
import com.ansonboby.almanac.ui.theme.FieldLedgerPalette
import com.ansonboby.almanac.ui.theme.StampType

/** Mono chip that flips the Ink/Parchment theme (DESIGN.md: mono for metadata).
 *  Accessibility label is provided via clickable's onClickLabel. */
@Composable
fun ThemeToggleChip(onToggleTheme: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(0.dp))
            .border(1.dp, FieldLedgerPalette.Moss)
            .clickable(onClickLabel = stringResource(R.string.cd_theme_toggle)) { onToggleTheme() }
            .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        Text(
            text = "◐ THEME",
            style = StampType.counter,
            color = FieldLedgerPalette.Brass,
        )
    }
}

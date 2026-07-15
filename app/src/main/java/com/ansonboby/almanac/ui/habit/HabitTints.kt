package com.ansonboby.almanac.ui.habit

import androidx.compose.ui.graphics.Color
import com.ansonboby.almanac.ui.theme.FieldLedgerPalette

/** Field Ledger accents a habit may carry (DESIGN.md: brass/moss/dusty-rose). */
val HABIT_TINTS = listOf("brass", "moss", "dusty_rose")

fun habitTintColor(tint: String): Color = when (tint) {
    "moss" -> FieldLedgerPalette.Moss
    "dusty_rose" -> FieldLedgerPalette.DustyRose
    else -> FieldLedgerPalette.Brass
}

package com.ansonboby.almanac.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Field Ledger palette (PRD 3.1).
 *
 * These are the fixed brand tokens. They are the single source of truth for
 * color in the app — the Material 3 [androidx.compose.material3.ColorScheme]s in
 * Theme.kt are built from them, and Material You dynamic color is never used.
 */
object FieldLedgerPalette {
    /** Deep bottle-green charcoal. Primary dark background. */
    val Ink = Color(0xFF22261F)

    /** Card / surface on dark; primary background in light mode. */
    val Parchment = Color(0xFFE9E4D3)

    /** Primary accent — active states, habit streaks, primary buttons. */
    val Moss = Color(0xFF6B7A5A)

    /** Secondary accent — date-stamp ink, tags, location pins. */
    val Brass = Color(0xFFB8934A)

    /** Mood / warmth accent, used sparingly, always alongside moss or brass. */
    val DustyRose = Color(0xFFC08B7A)

    /** Body text on parchment surfaces. */
    val InkText = Color(0xFF1A1C16)

    /** Body text on ink surfaces. */
    val ParchmentText = Color(0xFFF5F2E8)
}

/** A slightly raised ink tone for elevated surfaces on the dark theme. */
val InkElevated = Color(0xFF2C3128)

/** A slightly recessed parchment tone for cards/dividers on the light theme. */
val ParchmentShade = Color(0xFFDBD5C0)

/** A muted, earthy red for error states — kept in the Field Ledger register. */
val Color_Error = Color(0xFF9E4B3D)

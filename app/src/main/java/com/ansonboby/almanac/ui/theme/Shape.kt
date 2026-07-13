package com.ansonboby.almanac.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Field Ledger shapes. Corners are deliberately restrained — the ledger-line
 * layout replaces most card chrome (PRD 3.5), so we avoid the friendly-SaaS
 * heavily-rounded look. Small, crisp radii only.
 */
val AlmanacShapes = Shapes(
    extraSmall = RoundedCornerShape(2.dp),
    small = RoundedCornerShape(3.dp),
    medium = RoundedCornerShape(4.dp),
    large = RoundedCornerShape(6.dp),
    extraLarge = RoundedCornerShape(8.dp),
)

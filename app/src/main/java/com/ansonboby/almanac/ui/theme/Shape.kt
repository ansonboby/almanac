package com.ansonboby.almanac.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Field Ledger shapes. The design system (DESIGN.md / Stitch design.md) is
 * explicitly anti-rounded: "sharp (0px)", no SaaS cards, depth comes from
 * hairlines and tonal layering, not elevation. So the global Material shape
 * scale is essentially square. The one sanctioned exception is the signature
 * stamp, which is allowed a slight deterministic rotation — handled in the
 * DateStamp component, not here.
 */
val AlmanacShapes = Shapes(
    extraSmall = RoundedCornerShape(0.dp),
    small = RoundedCornerShape(0.dp),
    medium = RoundedCornerShape(0.dp),
    large = RoundedCornerShape(0.dp),
    extraLarge = RoundedCornerShape(0.dp),
)

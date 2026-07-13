package com.ansonboby.almanac.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.ansonboby.almanac.ui.theme.FieldLedgerPalette as P

/**
 * "Ink" — the dark, default/hero theme (PRD 3.1). The app is most often opened
 * in the evening, so this is what most users see first.
 */
private val InkColorScheme = darkColorScheme(
    primary = P.Moss,
    onPrimary = P.ParchmentText,
    secondary = P.Brass,
    onSecondary = P.Ink,
    tertiary = P.DustyRose,
    onTertiary = P.Ink,
    background = P.Ink,
    onBackground = P.ParchmentText,
    surface = P.Ink,
    onSurface = P.ParchmentText,
    surfaceVariant = InkElevated,
    onSurfaceVariant = P.Parchment,
    outline = P.Moss,
    outlineVariant = InkElevated,
    error = Color_Error,
    onError = P.ParchmentText,
)

/**
 * "Parchment" — the light alternate theme. A full theme, not an afterthought:
 * surface/background pairing swaps, accents (moss / brass / rose) stay.
 */
private val ParchmentColorScheme = lightColorScheme(
    primary = P.Moss,
    onPrimary = P.ParchmentText,
    secondary = P.Brass,
    onSecondary = P.InkText,
    tertiary = P.DustyRose,
    onTertiary = P.InkText,
    background = P.Parchment,
    onBackground = P.InkText,
    surface = P.Parchment,
    onSurface = P.InkText,
    surfaceVariant = ParchmentShade,
    onSurfaceVariant = P.InkText,
    outline = P.Moss,
    outlineVariant = ParchmentShade,
    error = Color_Error,
    onError = P.ParchmentText,
)

@Composable
fun AlmanacTheme(
    darkTheme: Boolean = androidx.compose.foundation.isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    // No Material You dynamic color, ever (PRD 3.5). Fixed schemes only.
    val colorScheme = if (darkTheme) InkColorScheme else ParchmentColorScheme

    // Edge-to-edge is enabled in MainActivity; the system bars stay transparent
    // and we only control icon appearance so they stay legible on Ink/Parchment.
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AlmanacTypography,
        shapes = AlmanacShapes,
        content = content,
    )
}

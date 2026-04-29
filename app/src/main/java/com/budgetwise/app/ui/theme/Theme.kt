package com.budgetwise.app.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * BudgetWise static light colour scheme.
 *
 * Decision: Dynamic Colour (Android 12+ wallpaper-derived colours) is intentionally
 * DISABLED. Dynamic Colour would override CoralAlert with wallpaper-derived values,
 * breaking the visual language (a green wallpaper would make "over-budget" appear green).
 * The hardcoded colours here are semantically meaningful and must not be overridden.
 */
private val BudgetWiseLightColorScheme = lightColorScheme(
    primary          = TealPrimary,
    onPrimary        = SurfaceWhite,
    primaryContainer = GreenSecondary,
    onPrimaryContainer = NavyTertiary,

    secondary        = GreenSecondary,
    onSecondary      = NavyTertiary,
    secondaryContainer = TealPrimary.copy(alpha = 0.12f),

    tertiary         = NavyTertiary,
    onTertiary       = SurfaceWhite,

    error            = CoralAlert,
    onError          = SurfaceWhite,
    errorContainer   = CoralAlert.copy(alpha = 0.12f),
    onErrorContainer = CoralAlert,

    background       = BackgroundLight,
    onBackground     = OnBackgroundDark,

    surface          = SurfaceWhite,
    onSurface        = OnBackgroundDark,
    surfaceVariant   = BackgroundLight,
    onSurfaceVariant = TextMuted,

    outline          = TextMuted.copy(alpha = 0.5f)
)

/**
 * Root Compose theme for BudgetWise.
 *
 * Wraps all screens in MaterialTheme with:
 * - Static lightColorScheme (Dynamic Colour disabled — see above)
 * - BudgetWiseTypography (11-style scale)
 * - SideEffect to set the status bar colour to TealPrimary for visual consistency
 *
 * Usage: Wrap the Scaffold in MainActivity.setContent{}
 */
@Composable
fun BudgetWiseTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            // Set status bar background to TealPrimary and use light icons
            val window = (view.context as Activity).window
            window.statusBarColor = TealPrimary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = BudgetWiseLightColorScheme,
        typography  = BudgetWiseTypography,
        content     = content
    )
}

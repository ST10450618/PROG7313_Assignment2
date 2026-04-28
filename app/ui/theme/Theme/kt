
package com.budgetwise.app.ui.theme

import android.app.Activity
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val BudgetWiseColorScheme = lightColorScheme(
    primary          = TealPrimary,
    onPrimary        = Color.White,
    primaryContainer = GreenSecondary.copy(alpha = 0.15f),
    secondary        = GreenSecondary,
    onSecondary      = Color.White,
    tertiary         = NavyTertiary,
    onTertiary       = Color.White,
    error            = CoralAlert,
    onError          = Color.White,
    background       = Color(0xFFF8FFFE),
    onBackground     = Color(0xFF0F1F1E),
    surface          = Color(0xFFF8FFFE),
    onSurface        = Color(0xFF0F1F1E),
    outline          = TealPrimary.copy(alpha = 0.4f),
)

/**
 * BudgetWise Material Design 3 theme.
 *
 * Using a statically defined colour scheme (rather than Dynamic Colour) ensures
 * consistent brand presentation across all devices regardless of the user's
 * Android 12+ wallpaper-derived dynamic palette. This is critical for a financial
 * app where colour carries semantic meaning (coral = overspent, teal = healthy).
 */
@Composable
fun BudgetWiseTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = BudgetWiseColorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }
    MaterialTheme(
        colorScheme = BudgetWiseColorScheme,
        typography  = BudgetWiseTypography,
        content     = content
    )
}
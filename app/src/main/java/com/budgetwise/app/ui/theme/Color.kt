package com.budgetwise.app.ui.theme

import androidx.compose.ui.graphics.Color

// =============================================================================
// BudgetWise Brand Colours — Semantic Assignments
// =============================================================================
// CRITICAL: These colours carry semantic meaning. Do NOT swap them.
// Dynamic Colour is intentionally disabled in Theme.kt so Android wallpaper-derived
// colours cannot override these semantics (e.g. CoralAlert must always mean "error/over-budget").

/** Primary brand colour — teal.
 *  Used on: TopAppBars, FABs, primary buttons, on-track progress bar,
 *  category colour circles (default), selected bottom nav item. */
val TealPrimary       = Color(0xFF1B998B)

/** Secondary accent — bright green.
 *  Used on: secondary accents, success indicator tinting. */
val GreenSecondary    = Color(0xFF06D6A0)

/** Tertiary — dark navy.
 *  Used on: dark surfaces, tertiary colour slots. */
val NavyTertiary      = Color(0xFF0F4C5C)

/** Alert colour — coral/red.
 *  Used on: error messages, over-budget progress bar, delete buttons,
 *  max goal label in GoalsScreen, over-budget status card. */
val CoralAlert        = Color(0xFFE16162)

/** Warning / highlight — amber yellow.
 *  Used on: warnings, under-budget progress bar, min goal label in GoalsScreen,
 *  receipt photo camera icon in AddExpenseScreen. */
val YellowHighlight   = Color(0xFFFDD05C)

// =============================================================================
// Neutral / Background Colours
// =============================================================================

/** Screen background — very light teal-tinted white. */
val BackgroundLight   = Color(0xFFF8FFFE)

/** Primary text colour — near-black dark teal. */
val OnBackgroundDark  = Color(0xFF0F1F1E)

/** Card surface colour — white. */
val SurfaceWhite      = Color(0xFFFFFFFF)

/** Muted / secondary text — medium grey. */
val TextMuted         = Color(0xFF6B7280)

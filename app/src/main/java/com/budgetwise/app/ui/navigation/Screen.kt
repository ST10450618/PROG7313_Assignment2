package com.budgetwise.app.ui.navigation

/**
 * Sealed class defining all navigation routes in BudgetWise.
 *
 * Navigation zones (from the handover document):
 *
 * AUTH ZONE (bottom bar hidden):
 *   Login, Register
 *
 * MAIN ZONE (bottom bar visible — 5 tabs):
 *   Home, ExpenseList, Categories, Goals, Reports
 *
 * FORM ZONE (bottom bar hidden — full-screen form):
 *   AddExpense
 *
 * Back stack rules are implemented in BudgetWiseNavGraph composable functions
 * and in MainActivity (bottom bar visibility).
 */
sealed class Screen(val route: String) {

    // -------------------------------------------------------------------------
    // AUTH ZONE
    // -------------------------------------------------------------------------

    /** Login screen — start destination when no session is active. */
    object Login    : Screen("login")

    /** Registration screen — navigated from Login via "Create account" link. */
    object Register : Screen("register")

    // -------------------------------------------------------------------------
    // MAIN ZONE (bottom nav tabs)
    // -------------------------------------------------------------------------

    /**
     * Home screen — start destination when a session is active.
     * Shows live month total, quick-action grid, tips card, logout button.
     */
    object Home        : Screen("home")

    /**
     * Expense list screen — period-filtered list with DatePicker filter.
     * Accessible from bottom nav tab 2.
     */
    object ExpenseList : Screen("expense_list")

    /**
     * Categories screen — manage expense categories with colour picker.
     * Accessible from bottom nav tab 3.
     */
    object Categories  : Screen("categories")

    /**
     * Goals screen — set monthly min/max goals, view spending status.
     * Accessible from bottom nav tab 4.
     */
    object Goals       : Screen("goals")

    /**
     * Reports screen — category spending breakdown for a selected period.
     * Accessible from bottom nav tab 5.
     */
    object Reports     : Screen("reports")

    // -------------------------------------------------------------------------
    // FORM ZONE
    // -------------------------------------------------------------------------

    /**
     * Add expense screen — full form with all 7 fields + camera.
     * Launched from HomeScreen quick-action tile or ExpenseListScreen FAB.
     * Bottom bar hidden while on this screen.
     */
    object AddExpense  : Screen("add_expense")
}

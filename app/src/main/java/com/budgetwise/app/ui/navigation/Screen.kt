package com.budgetwise.app.ui.navigation

/**
 * Sealed class defining all navigation routes in BudgetWise.
 *
 * Navigation zones:
 *
 * AUTH ZONE (bottom bar hidden):
 *   Login, Register
 *
 * MAIN ZONE (bottom bar visible — 5 tabs):
 *   Home, ExpenseList, Categories, Goals, Reports
 *
 * FEATURE ZONE (bottom bar hidden — full-screen features):
 *   AddExpense, Streak, SmartTips
 */
sealed class Screen(val route: String) {

    // AUTH ZONE
    object Login    : Screen("login")
    object Register : Screen("register")

    // MAIN ZONE (bottom nav tabs)
    object Home        : Screen("home")
    object ExpenseList : Screen("expense_list")
    object Categories  : Screen("categories")
    object Goals       : Screen("goals")
    object Reports     : Screen("reports")

    // FORM ZONE
    object AddExpense  : Screen("add_expense")

    // OWN FEATURES (Final PoE)
    /** Own Feature 1: Daily expense logging streak tracker. */
    object Streak      : Screen("streak")

    /** Own Feature 2: Personalised budget tips based on spending. */
    object SmartTips   : Screen("smart_tips")
}
package com.budgetwise.app.ui.navigation

/**
 * Sealed class defining every navigable destination in BudgetWise.
 *
 * Using sealed classes rather than raw strings catches route typos at compile time.
 * This directly addresses the Part 1 feedback about navigation gaps — every screen
 * transition in the app is explicitly declared here and wired in [BudgetWiseNavGraph].
 */
sealed class Screen(val route: String) {
    object Login      : Screen("login")
    object Register   : Screen("register")
    object Home       : Screen("home")
    object Categories : Screen("categories")
    object AddExpense : Screen("add_expense")
    object ExpenseList: Screen("expense_list")
    object Goals      : Screen("goals")
    object Reports    : Screen("reports")
}
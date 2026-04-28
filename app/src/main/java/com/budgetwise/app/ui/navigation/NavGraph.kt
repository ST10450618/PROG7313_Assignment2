package com.budgetwise.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.budgetwise.app.ui.auth.LoginScreen
import com.budgetwise.app.ui.auth.RegisterScreen
import com.budgetwise.app.ui.category.CategoryScreen
import com.budgetwise.app.ui.expense.AddExpenseScreen
import com.budgetwise.app.ui.expense.ExpenseListScreen
import com.budgetwise.app.ui.goals.GoalsScreen
import com.budgetwise.app.ui.home.HomeScreen
import com.budgetwise.app.ui.reports.ReportScreen

/**
 * Central navigation graph — every route is defined here.
 *
 * Addressing Part 1 feedback: all navigation transitions are explicit with no dead ends.
 * Auth zone screens (Login, Register, AddExpense) hide the bottom bar via MainActivity.
 * All main zone screens are reachable from the bottom nav without additional taps.
 */
@Composable
fun BudgetWiseNavGraph(
    navController    : NavHostController,
    startDestination : String,
    modifier         : Modifier = Modifier
) {
    NavHost(navController = navController, startDestination = startDestination, modifier = modifier) {

        // ── AUTH ZONE ──────────────────────────────────────────────────────
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess    = { navController.navigate(Screen.Home.route) { popUpTo(Screen.Login.route) { inclusive = true } } },
                onNavigateToRegister = { navController.navigate(Screen.Register.route) }
            )
        }
        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = { navController.navigate(Screen.Home.route) { popUpTo(Screen.Login.route) { inclusive = true } } },
                onNavigateBack    = { navController.popBackStack() }
            )
        }

        // ── MAIN ZONE ──────────────────────────────────────────────────────
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToAddExpense = { navController.navigate(Screen.AddExpense.route) },
                onNavigateToExpenses   = { navController.navigate(Screen.ExpenseList.route) },
                onNavigateToReports    = { navController.navigate(Screen.Reports.route) },
                onNavigateToGoals      = { navController.navigate(Screen.Goals.route) },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Categories.route) { CategoryScreen() }
        composable(Screen.AddExpense.route) {
            AddExpenseScreen(
                onNavigateBack  = { navController.popBackStack() },
                onExpenseSaved  = { navController.popBackStack() }
            )
        }
        composable(Screen.ExpenseList.route) {
            ExpenseListScreen(onAddExpense = { navController.navigate(Screen.AddExpense.route) })
        }
        composable(Screen.Goals.route)   { GoalsScreen() }
        composable(Screen.Reports.route) { ReportScreen() }
    }
}

/** Bottom navigation bar — rendered by MainActivity for all main zone screens. */
@Composable
fun BudgetWiseBottomBar(navController: NavHostController) {
    val items = listOf(
        Triple(Screen.Home,        "Home",      Icons.Filled.Home),
        Triple(Screen.ExpenseList, "Expenses",  Icons.Filled.ReceiptLong),
        Triple(Screen.Categories,  "Categories",Icons.Filled.Category),
        Triple(Screen.Goals,       "Goals",     Icons.Filled.TrackChanges),
        Triple(Screen.Reports,     "Reports",   Icons.Filled.BarChart),
    )
    val entry by navController.currentBackStackEntryAsState()
    val current = entry?.destination?.route

    NavigationBar {
        items.forEach { (screen, label, icon) ->
            NavigationBarItem(
                selected = current == screen.route,
                onClick  = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState    = true
                    }
                },
                icon  = { Icon(icon, contentDescription = label) },
                label = { Text(label) }
            )
        }
    }
}
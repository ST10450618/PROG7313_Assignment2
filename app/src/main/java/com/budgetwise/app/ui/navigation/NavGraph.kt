package com.budgetwise.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.NavHostController
import com.budgetwise.app.ui.auth.LoginScreen
import com.budgetwise.app.ui.auth.RegisterScreen
import com.budgetwise.app.ui.category.CategoryScreen
import com.budgetwise.app.ui.expense.AddExpenseScreen
import com.budgetwise.app.ui.expense.ExpenseListScreen
import com.budgetwise.app.ui.goals.GoalsScreen
import com.budgetwise.app.ui.home.HomeScreen
import com.budgetwise.app.ui.reports.ReportScreen
import com.budgetwise.app.ui.streak.StreakScreen
import com.budgetwise.app.ui.tips.SmartTipsScreen
import com.budgetwise.app.ui.theme.OnBackgroundDark
import com.budgetwise.app.ui.theme.TealPrimary
import com.budgetwise.app.ui.theme.TextMuted

/**
 * Root NavHost — wires all screens to their route strings.
 *
 * Final PoE additions:
 *  - Streak screen (Own Feature 1) — accessible from HomeScreen
 *  - SmartTips screen (Own Feature 2) — accessible from HomeScreen
 */
@Composable
fun BudgetWiseNavGraph(
    navController    : NavHostController,
    startDestination : String,
    modifier         : Modifier = Modifier
) {
    NavHost(
        navController    = navController,
        startDestination = startDestination,
        modifier         = modifier
    ) {
        // AUTH ZONE
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // MAIN ZONE
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToAddExpense = { navController.navigate(Screen.AddExpense.route) },
                onNavigateToExpenses   = { navController.navigate(Screen.ExpenseList.route) },
                onNavigateToCategories = { navController.navigate(Screen.Categories.route) },
                onNavigateToGoals      = { navController.navigate(Screen.Goals.route) },
                onNavigateToStreak     = { navController.navigate(Screen.Streak.route) },
                onNavigateToSmartTips  = { navController.navigate(Screen.SmartTips.route) },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.ExpenseList.route) {
            ExpenseListScreen(onNavigateToAddExpense = { navController.navigate(Screen.AddExpense.route) })
        }

        composable(Screen.Categories.route) { CategoryScreen() }
        composable(Screen.Goals.route)      { GoalsScreen() }
        composable(Screen.Reports.route)    { ReportScreen() }

        // FORM ZONE
        composable(Screen.AddExpense.route) {
            AddExpenseScreen(onNavigateBack = { navController.popBackStack() })
        }

        // OWN FEATURES — Final PoE
        composable(Screen.Streak.route) {
            StreakScreen()
        }

        composable(Screen.SmartTips.route) {
            SmartTipsScreen()
        }
    }
}

// =============================================================================
// Bottom Navigation Bar (unchanged — 5 main tabs)
// =============================================================================

private data class BottomNavItem(
    val screen: Screen,
    val label : String,
    val icon  : @Composable () -> Unit
)

@Composable
fun BudgetWiseBottomBar(navController: NavController) {
    val navItems = listOf(
        BottomNavItem(Screen.Home,        "Home")       { Icon(Icons.Filled.Home,        contentDescription = "Home") },
        BottomNavItem(Screen.ExpenseList, "Expenses")   { Icon(Icons.Filled.List,         contentDescription = "Expenses") },
        BottomNavItem(Screen.Categories,  "Categories") { Icon(Icons.Filled.Category,     contentDescription = "Categories") },
        BottomNavItem(Screen.Goals,       "Goals")      { Icon(Icons.Filled.TrackChanges, contentDescription = "Goals") },
        BottomNavItem(Screen.Reports,     "Reports")    { Icon(Icons.Filled.BarChart,      contentDescription = "Reports") }
    )

    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route

    NavigationBar(containerColor = Color.White) {
        navItems.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.screen.route,
                onClick  = {
                    navController.navigate(item.screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState    = true
                    }
                },
                icon   = item.icon,
                label  = { Text(item.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor   = TealPrimary,
                    selectedTextColor   = TealPrimary,
                    unselectedIconColor = TextMuted,
                    unselectedTextColor = TextMuted,
                    indicatorColor      = TealPrimary.copy(alpha = 0.12f)
                )
            )
        }
    }
}
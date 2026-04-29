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
import com.budgetwise.app.ui.theme.OnBackgroundDark
import com.budgetwise.app.ui.theme.TealPrimary
import com.budgetwise.app.ui.theme.TextMuted

/**
 * Root NavHost that wires all 8 screens to their route strings.
 *
 * Back stack rules (from handover doc Section 7):
 * - Login → Home: popUpTo(Login) inclusive=true → back exits app
 * - Register → Home: popUpTo(Login) inclusive=true → clears both auth screens
 * - Logout: popUpTo(0) inclusive=true → clears entire stack
 * - Bottom bar tabs: saveState + restoreState + launchSingleTop
 *
 * @param navController  The NavHostController from MainActivity.
 * @param startDestination  "login" or "home" determined by session check in MainActivity.
 * @param modifier  Padding from Scaffold (accounts for bottom nav bar height).
 */
@Composable
fun BudgetWiseNavGraph(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier
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
                        // Clear both Login AND Register from back stack
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // MAIN ZONE
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToAddExpense = {
                    navController.navigate(Screen.AddExpense.route)
                },
                onNavigateToExpenses = {
                    navController.navigate(Screen.ExpenseList.route)
                },
                onNavigateToCategories = {
                    navController.navigate(Screen.Categories.route)
                },
                onNavigateToGoals = {
                    navController.navigate(Screen.Goals.route)
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        // Clear entire back stack on logout
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.ExpenseList.route) {
            ExpenseListScreen(
                onNavigateToAddExpense = {
                    navController.navigate(Screen.AddExpense.route)
                }
            )
        }

        composable(Screen.Categories.route) {
            CategoryScreen()
        }

        composable(Screen.Goals.route) {
            GoalsScreen()
        }

        composable(Screen.Reports.route) {
            ReportScreen()
        }

        // FORM ZONE
        composable(Screen.AddExpense.route) {
            AddExpenseScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

// =============================================================================
// Bottom Navigation Bar
// =============================================================================

/**
 * Data class representing a single bottom navigation tab item.
 */
private data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val icon: @Composable () -> Unit
)

/**
 * Material3 NavigationBar with 5 tabs for the main zone.
 *
 * Tab configuration:
 *   1. Home         — house icon
 *   2. Expenses     — list icon
 *   3. Categories   — category icon
 *   4. Goals        — track changes icon
 *   5. Reports      — bar chart icon
 *
 * Navigation behaviour:
 *   - saveState = true, restoreState = true: preserves scroll/state on tab switch
 *   - launchSingleTop = true: avoids duplicate destinations on repeated tap
 *   - popUpTo(startDestination) { saveState = true }: keeps back stack clean
 */
@Composable
fun BudgetWiseBottomBar(navController: NavController) {
    val navItems = listOf(
        BottomNavItem(Screen.Home,        "Home")       { Icon(Icons.Filled.Home,         contentDescription = "Home") },
        BottomNavItem(Screen.ExpenseList, "Expenses")   { Icon(Icons.Filled.List,          contentDescription = "Expenses") },
        BottomNavItem(Screen.Categories,  "Categories") { Icon(Icons.Filled.Category,      contentDescription = "Categories") },
        BottomNavItem(Screen.Goals,       "Goals")      { Icon(Icons.Filled.TrackChanges,  contentDescription = "Goals") },
        BottomNavItem(Screen.Reports,     "Reports")    { Icon(Icons.Filled.BarChart,       contentDescription = "Reports") }
    )

    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route

    NavigationBar(
        containerColor = Color.White
    ) {
        navItems.forEach { item ->
            val selected = currentRoute == item.screen.route
            NavigationBarItem(
                selected = selected,
                onClick  = {
                    navController.navigate(item.screen.route) {
                        // Pop up to the graph's start destination to avoid stacking destinations
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState    = true
                    }
                },
                icon    = item.icon,
                label   = { Text(item.label) },
                colors  = NavigationBarItemDefaults.colors(
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

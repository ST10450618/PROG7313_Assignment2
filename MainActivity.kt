package com.budgetwise.app

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.budgetwise.app.ui.navigation.BudgetWiseBottomBar
import com.budgetwise.app.ui.navigation.BudgetWiseNavGraph
import com.budgetwise.app.ui.navigation.Screen
import com.budgetwise.app.ui.theme.BudgetWiseTheme
import com.budgetwise.app.utils.SessionManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

private const val TAG = "MainActivity"

/**
 * Single Activity — all navigation happens via Jetpack Navigation Compose.
 * @AndroidEntryPoint enables Hilt injection into this Activity.
 *
 * The start destination is determined synchronously at launch by reading the
 * DataStore session. runBlocking is acceptable here because it executes before
 * setContent is called and the user is not yet interacting with the UI.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Determine whether the user has an active session
        val startDestination = runBlocking {
            val userId = sessionManager.userId.first()
            if (userId == SessionManager.NO_USER) {
                Log.d(TAG, "No active session — routing to Login")
                Screen.Login.route
            } else {
                Log.d(TAG, "Active session found (userId=$userId) — routing to Home")
                Screen.Home.route
            }
        }

        setContent {
            BudgetWiseTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                // Routes where the bottom navigation bar must be hidden
                val bottomBarHiddenRoutes = setOf(
                    Screen.Login.route,
                    Screen.Register.route,
                    Screen.AddExpense.route
                )

                Scaffold(
                    bottomBar = {
                        if (currentRoute !in bottomBarHiddenRoutes) {
                            BudgetWiseBottomBar(navController = navController)
                        }
                    }
                ) { paddingValues ->
                    BudgetWiseNavGraph(
                        navController = navController,
                        startDestination = startDestination,
                        modifier = Modifier.padding(paddingValues)
                    )
                }
            }
        }
    }
}
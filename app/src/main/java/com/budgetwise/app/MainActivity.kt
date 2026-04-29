package com.budgetwise.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
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

/**
 * The single Activity for the BudgetWise app (Single Activity Architecture).
 *
 * @AndroidEntryPoint enables Hilt field injection in this Activity. This annotation
 * is MANDATORY — without it, @Inject fields are never populated and the app crashes.
 *
 * Responsibilities:
 * 1. Determine the start destination by reading DataStore once on startup (runBlocking).
 * 2. Set up the Scaffold + NavHost via BudgetWiseNavGraph.
 * 3. Control bottom bar visibility based on the current route.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    /**
     * SessionManager injected by Hilt.
     * Used in onCreate() to determine whether the user is already logged in.
     */
    @Inject
    lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        /**
         * Decision 4 (handover doc): runBlocking for start destination.
         *
         * We block the main thread for ONE DataStore read before setContent{} runs.
         * This is ~1–2ms from the in-memory DataStore cache.
         * Purpose: prevent the Login→Home navigation flicker on every cold start
         * for an already-authenticated user.
         *
         * This is acceptable ONLY here in onCreate(), never in a ViewModel or composable.
         */
        val startDestination = runBlocking {
            val userId = sessionManager.userId.first()
            if (userId != SessionManager.NO_USER) Screen.Home.route
            else Screen.Login.route
        }

        setContent {
            BudgetWiseTheme {
                val navController = rememberNavController()

                // Observe current route to control bottom bar visibility
                val currentBackStack by navController.currentBackStackEntryAsState()
                val currentRoute = currentBackStack?.destination?.route

                /**
                 * Bottom bar visibility rules:
                 * HIDDEN on: Login, Register (auth zone), AddExpense (form zone)
                 * VISIBLE on: Home, ExpenseList, Categories, Goals, Reports (main zone)
                 */
                val bottomBarHiddenRoutes = setOf(
                    Screen.Login.route,
                    Screen.Register.route,
                    Screen.AddExpense.route
                )
                val showBottomBar = currentRoute !in bottomBarHiddenRoutes

                Scaffold(
                    modifier   = Modifier.fillMaxSize(),
                    bottomBar  = {
                        if (showBottomBar) {
                            BudgetWiseBottomBar(navController = navController)
                        }
                    }
                ) { innerPadding ->
                    BudgetWiseNavGraph(
                        navController      = navController,
                        startDestination   = startDestination,
                        modifier           = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

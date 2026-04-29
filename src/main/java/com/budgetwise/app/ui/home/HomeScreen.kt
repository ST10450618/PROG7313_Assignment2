package com.budgetwise.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.budgetwise.app.ui.theme.BackgroundLight
import com.budgetwise.app.ui.theme.NavyTertiary
import com.budgetwise.app.ui.theme.TealPrimary

/**
 * Home Screen — the main dashboard shown after login.
 *
 * Layout:
 * 1. TealPrimary welcome card: "Hello, [username]" + live ZAR total for current month
 * 2. 2×2 quick-action grid: Add Expense, My Expenses, Categories, Goals
 * 3. Tips card with budgeting advice
 * 4. Logout icon in top app bar (triggers confirmation AlertDialog)
 */
@Composable
fun HomeScreen(
    onNavigateToAddExpense: () -> Unit,
    onNavigateToExpenses:   () -> Unit,
    onNavigateToCategories: () -> Unit,
    onNavigateToGoals:      () -> Unit,
    onLogout:               () -> Unit,
    viewModel:              HomeViewModel = hiltViewModel()
) {
    val username    by viewModel.username.collectAsStateWithLifecycle()
    val monthTotal  by viewModel.monthTotal.collectAsStateWithLifecycle()
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Logout confirmation dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title            = { Text("Log Out") },
            text             = { Text("Are you sure you want to log out of BudgetWise?") },
            confirmButton    = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    viewModel.logout()
                    onLogout()
                }) {
                    Text("Log Out", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton    = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title  = { Text("BudgetWise", color = Color.White) },
                actions = {
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(
                            imageVector        = Icons.Filled.ExitToApp,
                            contentDescription = "Log Out",
                            tint               = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = TealPrimary)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundLight)
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ---- Welcome / Month Total Card ----
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(16.dp),
                colors   = CardDefaults.cardColors(containerColor = TealPrimary)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text  = "Hello, ${username.ifBlank { "there" }} 👋",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                    Text(
                        text  = viewModel.currentMonthLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text  = "Total Spent",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                    Text(
                        text  = "R %.2f".format(monthTotal),
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }

            // ---- Quick Action Grid (2×2) ----
            Text(
                text  = "Quick Actions",
                style = MaterialTheme.typography.titleMedium,
                color = NavyTertiary
            )

            Row(
                modifier            = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionCard(
                    icon    = Icons.Filled.AddCircle,
                    label   = "Add Expense",
                    onClick = onNavigateToAddExpense,
                    modifier = Modifier.weight(1f)
                )
                QuickActionCard(
                    icon    = Icons.Filled.List,
                    label   = "My Expenses",
                    onClick = onNavigateToExpenses,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier            = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionCard(
                    icon    = Icons.Filled.Category,
                    label   = "Categories",
                    onClick = onNavigateToCategories,
                    modifier = Modifier.weight(1f)
                )
                QuickActionCard(
                    icon    = Icons.Filled.BarChart,
                    label   = "Goals",
                    onClick = onNavigateToGoals,
                    modifier = Modifier.weight(1f)
                )
            }

            // ---- Tips Card ----
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(12.dp),
                colors   = CardDefaults.cardColors(containerColor = TealPrimary.copy(alpha = 0.08f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text  = "💡 Budgeting Tip",
                        style = MaterialTheme.typography.titleSmall,
                        color = NavyTertiary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text  = "Track every expense — even small ones. " +
                                "Small daily purchases can add up to significant amounts over a month. " +
                                "Set a minimum and maximum goal in Goals to stay on track.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Individual quick-action tile for the 2×2 grid.
 */
@Composable
private fun QuickActionCard(
    icon:     ImageVector,
    label:    String,
    onClick:  () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape  = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = label,
                tint               = TealPrimary,
                modifier           = Modifier.size(32.dp)
            )
            Text(
                text  = label,
                style = MaterialTheme.typography.bodySmall,
                color = NavyTertiary
            )
        }
    }
}

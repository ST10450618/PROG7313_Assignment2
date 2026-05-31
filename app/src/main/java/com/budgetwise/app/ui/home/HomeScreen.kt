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
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lightbulb
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
import com.budgetwise.app.ui.theme.YellowHighlight

/**
 * Home Screen — Final PoE version.
 *
 * Added two new quick-action tiles for the own features:
 *  - "My Streak" → StreakScreen (Own Feature 1)
 *  - "Smart Tips" → SmartTipsScreen (Own Feature 2)
 */
@Composable
fun HomeScreen(
    onNavigateToAddExpense : () -> Unit,
    onNavigateToExpenses   : () -> Unit,
    onNavigateToCategories : () -> Unit,
    onNavigateToGoals      : () -> Unit,
    onNavigateToStreak     : () -> Unit,      // Own Feature 1
    onNavigateToSmartTips  : () -> Unit,      // Own Feature 2
    onLogout               : () -> Unit,
    viewModel              : HomeViewModel = hiltViewModel()
) {
    val username   by viewModel.username.collectAsStateWithLifecycle()
    val monthTotal by viewModel.monthTotal.collectAsStateWithLifecycle()
    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title            = { Text("Log Out") },
            text             = { Text("Are you sure you want to log out of BudgetWise?") },
            confirmButton    = {
                TextButton(onClick = { showLogoutDialog = false; viewModel.logout(); onLogout() }) {
                    Text("Log Out", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { showLogoutDialog = false }) { Text("Cancel") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title  = { Text("BudgetWise", color = Color.White) },
                actions = {
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(Icons.Filled.ExitToApp, contentDescription = "Log Out", tint = Color.White)
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
            // ── Welcome / Month Total Card ────────────────────────────────
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
                        "Hello, ${username.ifBlank { "there" }} 👋",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                    Text(
                        viewModel.currentMonthLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text("Total Spent", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.75f))
                    Text(
                        "R %.2f".format(monthTotal),
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }

            // ── Quick Actions ─────────────────────────────────────────────
            Text("Quick Actions", style = MaterialTheme.typography.titleMedium, color = NavyTertiary)

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickActionCard(Icons.Filled.AddCircle,          "Add Expense",  onNavigateToAddExpense,  Modifier.weight(1f))
                QuickActionCard(Icons.Filled.List,               "My Expenses",  onNavigateToExpenses,    Modifier.weight(1f))
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickActionCard(Icons.Filled.Category,           "Categories",   onNavigateToCategories,  Modifier.weight(1f))
                QuickActionCard(Icons.Filled.BarChart,           "Goals",        onNavigateToGoals,       Modifier.weight(1f))
            }

            // Own Feature tiles
            Text("My Features", style = MaterialTheme.typography.titleMedium, color = NavyTertiary)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickActionCard(
                    icon     = Icons.Filled.LocalFireDepartment,
                    label    = "My Streak",
                    onClick  = onNavigateToStreak,
                    modifier = Modifier.weight(1f),
                    tint     = YellowHighlight
                )
                QuickActionCard(
                    icon     = Icons.Filled.Lightbulb,
                    label    = "Smart Tips",
                    onClick  = onNavigateToSmartTips,
                    modifier = Modifier.weight(1f),
                    tint     = TealPrimary
                )
            }

            // ── Tips Card ─────────────────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(12.dp),
                colors   = CardDefaults.cardColors(containerColor = TealPrimary.copy(alpha = 0.08f))
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("💡 Budgeting Tip", style = MaterialTheme.typography.titleSmall, color = NavyTertiary)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Track every expense — even small ones. Small daily purchases can add up " +
                                "to significant amounts over a month. Set a minimum and maximum goal in " +
                                "Goals to stay on track.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun QuickActionCard(
    icon    : ImageVector,
    label   : String,
    onClick : () -> Unit,
    modifier: Modifier = Modifier,
    tint    : androidx.compose.ui.graphics.Color = TealPrimary
) {
    Card(
        modifier  = modifier.clip(RoundedCornerShape(12.dp)).clickable(onClick = onClick),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier            = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(32.dp))
            Text(label, style = MaterialTheme.typography.bodySmall, color = NavyTertiary)
        }
    }
}
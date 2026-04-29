package com.budgetwise.app.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.budgetwise.app.ui.theme.TealPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToAddExpense: () -> Unit,
    onNavigateToExpenses  : () -> Unit,
    onNavigateToReports   : () -> Unit,
    onNavigateToGoals     : () -> Unit,
    onLogout              : () -> Unit,
    viewModel             : HomeViewModel = hiltViewModel()
) {
    val username   by viewModel.username.collectAsStateWithLifecycle()
    val monthTotal by viewModel.monthTotal.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title  = { Text("BudgetWise", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = TealPrimary, titleContentColor = Color.White, actionIconContentColor = Color.White),
                actions = {
                    IconButton(onClick = { viewModel.logout(); onLogout() }) {
                        Icon(Icons.Filled.Logout, "Logout")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier            = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Welcome + Monthly Summary Card ────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors   = CardDefaults.cardColors(containerColor = TealPrimary),
                shape    = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Welcome back,", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.bodyLarge)
                    Text(username.ifBlank { "User" }, color = Color.White, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(16.dp))
                    Text("This month's spending", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.bodyMedium)
                    Text("R ${"%.2f".format(monthTotal)}", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                }
            }

            Text("Quick Actions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                HomeActionCard(Icons.Filled.AddCircle,   "Add Expense",    onNavigateToAddExpense, Modifier.weight(1f))
                HomeActionCard(Icons.Filled.ReceiptLong, "View Expenses",  onNavigateToExpenses,   Modifier.weight(1f))
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                HomeActionCard(Icons.Filled.BarChart,     "Reports",       onNavigateToReports,    Modifier.weight(1f))
                HomeActionCard(Icons.Filled.TrackChanges, "Goals",         onNavigateToGoals,      Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun HomeActionCard(icon: ImageVector, label: String, onClick: () -> Unit, modifier: Modifier) {
    Card(onClick = onClick, modifier = modifier, shape = RoundedCornerShape(12.dp), elevation = CardDefaults.cardElevation(3.dp)) {
        Column(Modifier.padding(16.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, label, tint = TealPrimary, modifier = Modifier.size(36.dp))
            Spacer(Modifier.height(8.dp))
            Text(label, style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center)
        }
    }
}
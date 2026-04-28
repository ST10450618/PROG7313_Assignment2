package com.budgetwise.app.ui.expense

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.budgetwise.app.ui.theme.TealPrimary

/**
 * TODO (James): Implement the Expense List screen.
 *
 * Required features per spec:
 *  - Date-range filter header (sequential DatePickerDialogs: pick start → pick end)
 *  - Period total shown in header card
 *  - LazyColumn of ExpenseCards (amount, description, date, category colour dot)
 *  - Tap card with photoUri → show full-screen photo viewer Dialog
 *  - Delete icon on card → AlertDialog confirmation → ExpenseViewModel.deleteExpense()
 *  - FAB → navigate to AddExpense via onAddExpense()
 *
 * ViewModel: ExpenseViewModel (inject via hiltViewModel())
 * Default filter: current month start → end (initialised in FilterState)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseListScreen(onAddExpense: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title  = { Text("Expenses") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor    = TealPrimary,
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddExpense, containerColor = TealPrimary) {
                Icon(Icons.Filled.Add, contentDescription = "Add Expense", tint = Color.White)
            }
        }
    ) { padding ->
        Box(
            Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Expenses", style = MaterialTheme.typography.titleMedium)
                Text(
                    "TODO (James): implement expense list with date filter",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}

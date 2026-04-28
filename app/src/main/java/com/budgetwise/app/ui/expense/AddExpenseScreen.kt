package com.budgetwise.app.ui.expense

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.budgetwise.app.ui.theme.TealPrimary

/**
 * TODO (James): Implement the Add Expense screen.
 *
 * Required fields (all mandatory per spec):
 *  - Amount (Double, > 0)
 *  - Description (non-blank)
 *  - Date (DatePickerDialog)
 *  - Start time (TimePickerDialog — custom AlertDialog wrapper)
 *  - End time   (TimePickerDialog — must be > startTime)
 *  - Category   (ExposedDropdownMenuBox — pull from ExpenseViewModel.categories)
 *  - Photo      (optional — CameraX via FileProvider, see handover doc §9)
 *
 * ViewModel: ExpenseViewModel (inject via hiltViewModel())
 * On save success → call onExpenseSaved()
 * On back pressed → call onNavigateBack()
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    onNavigateBack: () -> Unit,
    onExpenseSaved: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Expense") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor            = TealPrimary,
                    titleContentColor         = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
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
                Text("Add Expense", style = MaterialTheme.typography.titleMedium)
                Text(
                    "TODO (James): implement expense form",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}

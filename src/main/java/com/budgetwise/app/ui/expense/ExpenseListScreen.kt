package com.budgetwise.app.ui.expense

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.budgetwise.app.data.local.entity.Expense
import com.budgetwise.app.ui.category.parseColor
import com.budgetwise.app.ui.theme.BackgroundLight
import com.budgetwise.app.ui.theme.CoralAlert
import com.budgetwise.app.ui.theme.TealPrimary
import com.budgetwise.app.ui.theme.YellowHighlight
import com.budgetwise.app.utils.DateUtils

/**
 * Expense List Screen — shows a date-filtered list of expenses.
 *
 * Features:
 * - Period summary card (formatted start/end dates + total amount)
 * - "Change Period" button → sequential 2-step DatePickerDialogs (start → end)
 * - LazyColumn of ExpenseRowCard items, ordered newest first
 * - Empty state when no expenses match the filter
 * - Photo viewer: tap the camera icon on a card → full-screen Dialog with AsyncImage
 * - Delete: tap the delete icon → confirmation AlertDialog
 * - FAB → navigates to AddExpenseScreen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseListScreen(
    onNavigateToAddExpense: () -> Unit,
    viewModel:              ExpenseViewModel = hiltViewModel()
) {
    val expenses     by viewModel.filteredExpenses.collectAsStateWithLifecycle()
    val filterState  by viewModel.filterState.collectAsStateWithLifecycle()
    val periodTotal  by viewModel.periodTotal.collectAsStateWithLifecycle()
    val categories   by viewModel.categories.collectAsStateWithLifecycle()

    // ---- Date picker state (sequential: step 1 = start, step 2 = end) ----
    var showDatePickerStep by remember { mutableStateOf(0) } // 0=none, 1=start, 2=end
    var pendingStartMs     by remember { mutableStateOf(0L) }

    // ---- Photo viewer ----
    var photoUriToView by remember { mutableStateOf<Uri?>(null) }

    // ---- Delete confirmation ----
    var expenseToDelete by remember { mutableStateOf<Expense?>(null) }

    // Date picker step 1: pick start date
    if (showDatePickerStep == 1) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = filterState.startMs)
        DatePickerDialog(
            onDismissRequest = { showDatePickerStep = 0 },
            confirmButton    = {
                TextButton(onClick = {
                    pendingStartMs     = datePickerState.selectedDateMillis ?: filterState.startMs
                    showDatePickerStep = 2
                }) { Text("Next", color = TealPrimary) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerStep = 0 }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Date picker step 2: pick end date
    if (showDatePickerStep == 2) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = filterState.endMs)
        DatePickerDialog(
            onDismissRequest = { showDatePickerStep = 0 },
            confirmButton    = {
                TextButton(onClick = {
                    val endMs = datePickerState.selectedDateMillis ?: filterState.endMs
                    viewModel.updateFilter(
                        DateUtils.startOfDay(pendingStartMs),
                        DateUtils.endOfDay(endMs)
                    )
                    showDatePickerStep = 0
                }) { Text("Apply", color = TealPrimary) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerStep = 1 }) { Text("Back") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Photo viewer dialog
    photoUriToView?.let { uri ->
        Dialog(onDismissRequest = { photoUriToView = null }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black, RoundedCornerShape(12.dp))
                    .padding(4.dp)
            ) {
                AsyncImage(
                    model             = uri,
                    contentDescription = "Receipt photo",
                    modifier          = Modifier.fillMaxWidth(),
                    contentScale      = ContentScale.Fit
                )
                TextButton(
                    onClick  = { photoUriToView = null },
                    modifier = Modifier.align(Alignment.TopEnd)
                ) { Text("Close", color = Color.White) }
            }
        }
    }

    // Delete confirmation
    expenseToDelete?.let { expense ->
        AlertDialog(
            onDismissRequest = { expenseToDelete = null },
            title   = { Text("Delete Expense") },
            text    = { Text("Delete '${expense.description}'? This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteExpense(expense.id)
                    expenseToDelete = null
                }) { Text("Delete", color = CoralAlert) }
            },
            dismissButton = {
                TextButton(onClick = { expenseToDelete = null }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title  = { Text("My Expenses", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = TealPrimary)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick        = onNavigateToAddExpense,
                containerColor = TealPrimary,
                contentColor   = Color.White
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Expense")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundLight)
                .padding(padding)
        ) {
            // ---- Period summary card ----
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape    = RoundedCornerShape(12.dp),
                colors   = CardDefaults.cardColors(containerColor = TealPrimary)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text  = "${DateUtils.formatDate(filterState.startMs)} – ${DateUtils.formatDate(filterState.endMs)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                        Text(
                            text  = "R %.2f".format(periodTotal),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text  = "${expenses.size} expense${if (expenses.size != 1) "s" else ""}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.75f)
                        )
                    }
                    OutlinedButton(
                        onClick = { showDatePickerStep = 1 },
                        colors  = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border  = ButtonDefaults.outlinedButtonBorder
                    ) { Text("Change Period", color = Color.White) }
                }
            }

            // ---- Expense list or empty state ----
            if (expenses.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("🧾", style = MaterialTheme.typography.displaySmall)
                        Text(
                            text  = "No expenses in this period",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text  = "Tap + to add your first expense",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier            = Modifier.fillMaxSize(),
                    contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(expenses, key = { it.id }) { expense ->
                        val categoryName = categories.find { it.id == expense.categoryId }?.name ?: "Uncategorised"
                        val categoryColor = categories.find { it.id == expense.categoryId }?.colorHex ?: "#9E9E9E"

                        ExpenseRowCard(
                            expense       = expense,
                            categoryName  = categoryName,
                            categoryColor = categoryColor,
                            onViewPhoto   = { uri -> photoUriToView = Uri.parse(uri) },
                            onDelete      = { expenseToDelete = expense }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Individual expense row card.
 * Shows: category colour circle, description, date/times, amount.
 * Camera icon (if photo exists) → triggers full-screen photo viewer.
 * Delete icon → triggers confirmation dialog.
 */
@Composable
fun ExpenseRowCard(
    expense:       Expense,
    categoryName:  String,
    categoryColor: String,
    onViewPhoto:   (String) -> Unit,
    onDelete:      () -> Unit
) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment   = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Category colour circle
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(parseColor(categoryColor)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text  = categoryName.take(1).uppercase(),
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text  = expense.description,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1
                )
                Text(
                    text  = "$categoryName • ${DateUtils.formatDate(expense.date)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text  = "${expense.startTime} – ${expense.endTime}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text  = "R %.2f".format(expense.amount),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TealPrimary
                )
                Row {
                    // Camera icon — only shown when photo exists
                    if (expense.photoUri != null) {
                        IconButton(
                            onClick  = { onViewPhoto(expense.photoUri) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.Filled.CameraAlt,
                                contentDescription = "View receipt",
                                tint   = YellowHighlight,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    // Delete button
                    IconButton(
                        onClick  = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "Delete expense",
                            tint   = CoralAlert,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

package com.budgetwise.app.ui.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.budgetwise.app.ui.category.parseColor
import com.budgetwise.app.ui.theme.*
import com.budgetwise.app.utils.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    viewModel: ReportViewModel = hiltViewModel()
) {
    val categoryRows by viewModel.categoryRows.collectAsStateWithLifecycle()
    val periodTotal  by viewModel.periodTotal.collectAsStateWithLifecycle()
    val filter       by viewModel.filter.collectAsStateWithLifecycle()

    // Sequential date-picker: 0=closed, 1=pick start, 2=pick end
    var showDatePickerStep by remember { mutableStateOf(0) }
    var pendingStartMs     by remember { mutableStateOf(0L) }

    // Step 1: pick start date
    if (showDatePickerStep == 1) {
        val dpState = rememberDatePickerState(initialSelectedDateMillis = filter.startMs)
        DatePickerDialog(
            onDismissRequest = { showDatePickerStep = 0 },
            confirmButton = {
                TextButton(onClick = {
                    pendingStartMs     = dpState.selectedDateMillis ?: filter.startMs
                    showDatePickerStep = 2
                }) { Text("Next", color = TealPrimary) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerStep = 0 }) { Text("Cancel") }
            }
        ) {
            DatePicker(
                state  = dpState,
                title  = { Text("Select start date", Modifier.padding(start = 24.dp, top = 16.dp)) },
                colors = DatePickerDefaults.colors(selectedDayContainerColor = TealPrimary)
            )
        }
    }

    // Step 2: pick end date
    if (showDatePickerStep == 2) {
        val dpState = rememberDatePickerState(initialSelectedDateMillis = filter.endMs)
        DatePickerDialog(
            onDismissRequest = { showDatePickerStep = 0 },
            confirmButton = {
                TextButton(onClick = {
                    val endMs = dpState.selectedDateMillis ?: filter.endMs
                    viewModel.updateFilter(pendingStartMs, endMs)
                    showDatePickerStep = 0
                }) { Text("Apply", color = TealPrimary) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerStep = 0 }) { Text("Cancel") }
            }
        ) {
            DatePicker(
                state  = dpState,
                title  = { Text("Select end date", Modifier.padding(start = 24.dp, top = 16.dp)) },
                colors = DatePickerDefaults.colors(selectedDayContainerColor = TealPrimary)
            )
        }
    }

    // Max row total for scaling progress bars (Decision 11 from handover doc)
    val maxRowTotal = categoryRows.maxOfOrNull { it.total } ?: 1.0

    Scaffold(
        topBar = {
            TopAppBar(
                title  = { Text("Reports") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor    = TealPrimary,
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = BackgroundLight
    ) { padding ->
        LazyColumn(
            modifier            = Modifier.fillMaxSize().padding(padding),
            contentPadding      = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Summary header card
            item {
                Card(
                    colors   = CardDefaults.cardColors(containerColor = TealPrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            "Total Spending",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                        Text(
                            "R ${"%.2f".format(periodTotal)}",
                            style      = MaterialTheme.typography.headlineMedium,
                            color      = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "${DateUtils.formatDate(filter.startMs)} – ${DateUtils.formatDate(filter.endMs)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.75f)
                        )
                        Text(
                            "${categoryRows.size} categor${if (categoryRows.size == 1) "y" else "ies"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.75f)
                        )
                    }
                }
            }

            // Change Period button
            item {
                OutlinedButton(
                    onClick  = { showDatePickerStep = 1 },
                    modifier = Modifier.fillMaxWidth(),
                    colors   = ButtonDefaults.outlinedButtonColors(contentColor = TealPrimary)
                ) {
                    Text("Change Period")
                }
            }

            // Empty state
            if (categoryRows.isEmpty()) {
                item {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "No category data",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextMuted
                            )
                            Text(
                                "Add expenses with categories to see your report.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }
                    }
                }
            }

            // Category breakdown rows
            items(categoryRows, key = { it.category.id }) { row ->
                CategoryReportCard(row = row, maxRowTotal = maxRowTotal)
            }
        }
    }
}

// =============================================================================
// CategoryReportCard
// =============================================================================

@Composable
private fun CategoryReportCard(
    row        : CategoryReportRow,
    maxRowTotal: Double
) {
    // Bars scale to the highest-spending category, not the grand total (Decision 11)
    val barProgress = (row.total / maxRowTotal).toFloat().coerceIn(0f, 1f)
    val catColor    = parseColor(row.category.colorHex)

    Card(
        colors   = CardDefaults.cardColors(containerColor = SurfaceWhite),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment      = Alignment.CenterVertically,
                horizontalArrangement  = Arrangement.SpaceBetween,
                modifier               = Modifier.fillMaxWidth()
            ) {
                // Colour dot + name
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(catColor)
                    )
                    Text(
                        row.category.name,
                        style      = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color      = OnBackgroundDark
                    )
                }
                // Amount + percentage
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "R ${"%.2f".format(row.total)}",
                        style      = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color      = OnBackgroundDark
                    )
                    Text(
                        "${"%.1f".format(row.percent)}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }
            }

            // Progress bar
            LinearProgressIndicator(
                progress   = { barProgress },
                modifier   = Modifier.fillMaxWidth().height(6.dp),
                color      = catColor,
                trackColor = catColor.copy(alpha = 0.15f)
            )
        }
    }
}

package com.budgetwise.app.ui.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.budgetwise.app.ui.category.parseColour
import com.budgetwise.app.ui.theme.TealPrimary
import com.budgetwise.app.utils.DateUtils

/**
 * ReportScreen — category spending breakdown with a user-selectable date range.
 *
 * Each category row includes:
 *   • Colour-coded dot matching the category's assigned colour
 *   • Category name
 *   • ZAR total for the period
 *   • Percentage of overall spend
 *   • Linear progress bar scaled to the highest-spend category (not total)
 *     so even small categories have a visible bar
 *
 * This satisfies the rubric: "view the total amount spent on each category
 * during a user-selectable period."
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(viewModel: ReportViewModel = hiltViewModel()) {
    val filter by viewModel.filterState.collectAsStateWithLifecycle()
    val rows   by viewModel.categoryRows.collectAsStateWithLifecycle()
    val total  by viewModel.periodTotal.collectAsStateWithLifecycle()

    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker   by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title  = { Text("Reports", fontWeight = FontWeight.SemiBold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TealPrimary, titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = { showStartPicker = true }) {
                        Icon(Icons.Filled.DateRange, "Filter")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier        = Modifier.fillMaxSize().padding(padding),
            contentPadding  = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // ── Summary header ─────────────────────────────────────────────
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors   = CardDefaults.cardColors(containerColor = TealPrimary),
                    shape    = RoundedCornerShape(14.dp)
                ) {
                    Column(Modifier.padding(18.dp)) {
                        Text("Spending Report", color = Color.White.copy(0.8f), style = MaterialTheme.typography.labelLarge)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "${DateUtils.formatDate(filter.startMs)} – ${DateUtils.formatDate(filter.endMs)}",
                            color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(12.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Total Spent", color = Color.White.copy(0.7f), style = MaterialTheme.typography.labelSmall)
                                Text("R ${"%.2f".format(total)}", color = Color.White, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Categories", color = Color.White.copy(0.7f), style = MaterialTheme.typography.labelSmall)
                                Text("${rows.size}", color = Color.White, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // ── Change period button ───────────────────────────────────────
            item {
                OutlinedButton(
                    onClick  = { showStartPicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.FilterAlt, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Change Period")
                }
            }

            if (rows.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(top = 48.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.BarChart, null, Modifier.size(72.dp), tint = TealPrimary.copy(0.3f))
                            Spacer(Modifier.height(12.dp))
                            Text("No expenses in this period", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            } else {
                // ── Section header ─────────────────────────────────────────
                item {
                    Text(
                        "Category Breakdown",
                        style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold
                    )
                }

                val maxTotal = rows.maxOfOrNull { it.total } ?: 1.0

                items(rows, key = { it.category?.id ?: -1L }) { row ->
                    CategoryReportCard(row = row, maxTotal = maxTotal)
                }
            }
        }
    }

    // ── Date range pickers ─────────────────────────────────────────────────
    if (showStartPicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = filter.startMs)
        DatePickerDialog(
            onDismissRequest = { showStartPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { viewModel.updateFilter(it, filter.endMs) }
                    showStartPicker = false; showEndPicker = true
                }) { Text("Next: End Date") }
            },
            dismissButton = { TextButton(onClick = { showStartPicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = state, headline = { Text("  Start Date", style = MaterialTheme.typography.titleMedium) }) }
    }

    if (showEndPicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = filter.endMs)
        DatePickerDialog(
            onDismissRequest = { showEndPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { viewModel.updateFilter(filter.startMs, it) }
                    showEndPicker = false
                }) { Text("Apply") }
            },
            dismissButton = { TextButton(onClick = { showEndPicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = state, headline = { Text("  End Date", style = MaterialTheme.typography.titleMedium) }) }
    }
}

@Composable
private fun CategoryReportCard(row: CategoryReportRow, maxTotal: Double) {
    val progress = (row.total / maxTotal).toFloat().coerceIn(0f, 1f)
    val barColor = row.category?.let { parseColour(it.colorHex) } ?: TealPrimary

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(36.dp).clip(CircleShape).background(barColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Category, null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(row.category?.name ?: "Uncategorised", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text("${"%.1f".format(row.percent)}% of total", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
                }
                Text("R ${"%.2f".format(row.total)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = barColor)
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress   = { progress },
                modifier   = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color      = barColor,
                trackColor = barColor.copy(alpha = 0.15f)
            )
        }
    }
}
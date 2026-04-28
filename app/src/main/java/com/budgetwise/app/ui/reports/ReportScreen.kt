package com.budgetwise.app.ui.reports

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.budgetwise.app.ui.theme.TealPrimary

/**
 * TODO (Seth): Implement the Reports screen.
 *
 * Required features per spec:
 *  - Summary header card (period total, formatted date range)
 *  - Sequential DatePickerDialogs for custom period selection
 *  - LazyColumn of CategoryReportCards, each showing:
 *      · Category colour dot + name
 *      · Spend total (R x.xx)
 *      · Percentage of period total
 *      · LinearProgressIndicator relative to largest category
 *
 * ViewModel: ReportViewModel (inject via hiltViewModel())
 * Default period: current month (set in ReportFilterState default values)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title  = { Text("Reports") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor    = TealPrimary,
                    titleContentColor = Color.White
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
                Text("Reports", style = MaterialTheme.typography.titleMedium)
                Text(
                    "TODO (Seth): implement category breakdown report",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}

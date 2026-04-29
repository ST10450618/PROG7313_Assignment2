package com.budgetwise.app.ui.goals

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.budgetwise.app.ui.theme.CoralAlert
import com.budgetwise.app.ui.theme.TealPrimary
import com.budgetwise.app.ui.theme.YellowHighlight
import com.budgetwise.app.utils.DateUtils

/**
 * GoalsScreen — set and monitor monthly min/max spending targets.
 *
 * The progress bar visually fills from 0 to [maxGoal]. The fill colour changes:
 *  • Green  (TealPrimary)  → between min and max (on track)
 *  • Yellow (YellowHighlight) → below minimum (under-spending)
 *  • Coral  (CoralAlert)   → above maximum (overspending)
 *
 * This colour-coded system was identified as a key feature in the Part A analysis
 * of YNAB's green/yellow/red category indicators, adapted for the two-boundary model.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(viewModel: GoalsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val goal    by viewModel.currentGoal.collectAsStateWithLifecycle()
    val total   by viewModel.monthTotal.collectAsStateWithLifecycle()
    val status  by viewModel.spendingStatus.collectAsStateWithLifecycle()
    val snack    = remember { SnackbarHostState() }

    var minStr by remember { mutableStateOf(goal?.minGoal?.let { "%.2f".format(it) } ?: "") }
    var maxStr by remember { mutableStateOf(goal?.maxGoal?.let { "%.2f".format(it) } ?: "") }

    // Pre-fill fields when goal loads from DB
    LaunchedEffect(goal) {
        goal?.let {
            minStr = "%.2f".format(it.minGoal)
            maxStr = "%.2f".format(it.maxGoal)
        }
    }

    LaunchedEffect(uiState.success, uiState.error) {
        (uiState.success ?: uiState.error)?.let { snack.showSnackbar(it); viewModel.clearMessages() }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snack) },
        topBar = {
            TopAppBar(
                title  = { Text("Monthly Goals", fontWeight = FontWeight.SemiBold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TealPrimary, titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Month label ───────────────────────────────────────────────
            Text(
                DateUtils.formatMonthYear(viewModel.currentMonth, viewModel.currentYear),
                style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TealPrimary
            )

            // ── Status card ───────────────────────────────────────────────
            val (statusColor, statusIcon, statusMsg) = when (status) {
                SpendingStatus.NO_GOAL   -> Triple(MaterialTheme.colorScheme.surfaceVariant, Icons.Filled.Info, "No goal set for this month yet")
                SpendingStatus.UNDER_MIN -> Triple(YellowHighlight.copy(0.25f), Icons.Filled.TrendingDown, "Below minimum — you're underspending your budget")
                SpendingStatus.ON_TRACK  -> Triple(TealPrimary.copy(0.15f), Icons.Filled.CheckCircle, "On track — spending is within your goal range")
                SpendingStatus.OVER_MAX  -> Triple(CoralAlert.copy(0.20f), Icons.Filled.Warning, "Over budget — you've exceeded your maximum goal")
            }
            val statusIconTint = when (status) {
                SpendingStatus.UNDER_MIN -> YellowHighlight
                SpendingStatus.ON_TRACK  -> TealPrimary
                SpendingStatus.OVER_MAX  -> CoralAlert
                else                     -> MaterialTheme.colorScheme.onSurface.copy(0.5f)
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors   = CardDefaults.cardColors(containerColor = statusColor),
                shape    = RoundedCornerShape(14.dp)
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(statusIcon, null, tint = statusIconTint, modifier = Modifier.size(36.dp))
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(statusMsg, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(4.dp))
                        Text("Current spending: R ${"%.2f".format(total)}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            // ── Progress bar ──────────────────────────────────────────────
            goal?.let { g ->
                val progress = (total / g.maxGoal).toFloat().coerceIn(0f, 1f)
                val barColor = when (status) {
                    SpendingStatus.OVER_MAX  -> CoralAlert
                    SpendingStatus.UNDER_MIN -> YellowHighlight
                    else                     -> TealPrimary
                }
                Column {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("R ${"%.0f".format(g.minGoal)} min", style = MaterialTheme.typography.labelSmall, color = YellowHighlight, fontWeight = FontWeight.Bold)
                        Text("R ${"%.0f".format(g.maxGoal)} max", style = MaterialTheme.typography.labelSmall, color = CoralAlert, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(14.dp),
                        color    = barColor,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "${"%.0f".format(progress * 100)}% of maximum used",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
                    )
                }
            }

            HorizontalDivider()

            // ── Goal input form ───────────────────────────────────────────
            Text("Set / Update Goal", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

            OutlinedTextField(
                value         = minStr,
                onValueChange = { if (it.matches(Regex("^\\d*\\.?\\d{0,2}$"))) minStr = it },
                label         = { Text("Minimum Monthly Spend (ZAR)") },
                leadingIcon   = { Text("R", Modifier.padding(start = 14.dp), fontWeight = FontWeight.Bold, color = YellowHighlight) },
                singleLine    = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                supportingText = { Text("The least you expect to spend this month") },
                modifier      = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value         = maxStr,
                onValueChange = { if (it.matches(Regex("^\\d*\\.?\\d{0,2}$"))) maxStr = it },
                label         = { Text("Maximum Monthly Spend (ZAR)") },
                leadingIcon   = { Text("R", Modifier.padding(start = 14.dp), fontWeight = FontWeight.Bold, color = CoralAlert) },
                singleLine    = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                supportingText = { Text("Your hard spending cap — going over alerts you") },
                modifier      = Modifier.fillMaxWidth()
            )

            Button(
                onClick  = { viewModel.saveGoal(minStr, maxStr) },
                modifier = Modifier.fillMaxWidth().height(54.dp)
            ) {
                Icon(Icons.Filled.Save, null)
                Spacer(Modifier.width(8.dp))
                Text("Save Goal", fontSize = 16.sp)
            }

            // ── Tips card ─────────────────────────────────────────────────
            Card(
                colors = CardDefaults.cardColors(containerColor = TealPrimary.copy(0.08f)),
                shape  = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(14.dp)) {
                    Text("💡 Budgeting Tips", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = TealPrimary)
                    Spacer(Modifier.height(6.dp))
                    Text("• Set your minimum to cover essential monthly expenses\n• Set your maximum to include a savings buffer\n• Review and adjust your goals monthly", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
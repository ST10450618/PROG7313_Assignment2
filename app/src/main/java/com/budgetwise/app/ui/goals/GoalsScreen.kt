package com.budgetwise.app.ui.goals

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.budgetwise.app.ui.theme.*
import com.budgetwise.app.utils.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(
    viewModel: GoalsViewModel = hiltViewModel()
) {
    val currentGoal    by viewModel.currentGoal.collectAsStateWithLifecycle()
    val monthTotal     by viewModel.monthTotal.collectAsStateWithLifecycle()
    val spendingStatus by viewModel.spendingStatus.collectAsStateWithLifecycle()
    val uiMessage      by viewModel.uiMessage.collectAsStateWithLifecycle()

    var minStr by remember { mutableStateOf("") }
    var maxStr by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }

    // Pre-populate fields when an existing goal loads from Room
    LaunchedEffect(currentGoal) {
        currentGoal?.let { goal ->
            if (minStr.isBlank()) minStr = goal.minGoal.toBigDecimal().stripTrailingZeros().toPlainString()
            if (maxStr.isBlank()) maxStr = goal.maxGoal.toBigDecimal().stripTrailingZeros().toPlainString()
        }
    }

    // Show snackbar on save or error
    LaunchedEffect(uiMessage) {
        uiMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    val monthLabel = DateUtils.formatMonthYear(DateUtils.currentMonth(), DateUtils.currentYear())

    Scaffold(
        topBar = {
            TopAppBar(
                title  = { Text("Goals — $monthLabel") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor    = TealPrimary,
                    titleContentColor = Color.White
                )
            )
        },
        snackbarHost   = { SnackbarHost(snackbarHostState) },
        containerColor = BackgroundLight
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Status Card
            GoalStatusCard(
                status     = spendingStatus,
                monthTotal = monthTotal,
                maxGoal    = currentGoal?.maxGoal ?: 0.0
            )

            // Progress Bar (only when a goal exists)
            if (currentGoal != null) {
                GoalProgressBar(
                    monthTotal = monthTotal,
                    minGoal    = currentGoal!!.minGoal,
                    maxGoal    = currentGoal!!.maxGoal,
                    status     = spendingStatus
                )
            }

            // Min Goal input
            OutlinedTextField(
                value         = minStr,
                onValueChange = { if (it.matches(Regex("""^\d{0,8}(\.\d{0,2})?$"""))) minStr = it },
                label         = { Text("Minimum Monthly Goal (R)") },
                placeholder   = { Text("e.g. 500.00") },
                prefix        = { Text("R ", color = YellowHighlight, fontWeight = FontWeight.Bold) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth()
            )

            // Max Goal input
            OutlinedTextField(
                value         = maxStr,
                onValueChange = { if (it.matches(Regex("""^\d{0,8}(\.\d{0,2})?$"""))) maxStr = it },
                label         = { Text("Maximum Monthly Goal (R)") },
                placeholder   = { Text("e.g. 3000.00") },
                prefix        = { Text("R ", color = CoralAlert, fontWeight = FontWeight.Bold) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth()
            )

            // Save button
            Button(
                onClick  = { viewModel.saveGoal(minStr, maxStr) },
                modifier = Modifier.fillMaxWidth(),
                colors   = ButtonDefaults.buttonColors(containerColor = TealPrimary)
            ) {
                Text("Save Goal", modifier = Modifier.padding(vertical = 4.dp))
            }

            // Tips card
            Card(
                colors   = CardDefaults.cardColors(containerColor = SurfaceWhite),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        "Tips for smart budgeting",
                        style      = MaterialTheme.typography.titleSmall,
                        color      = OnBackgroundDark,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Set a minimum to avoid under-spending on essentials.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                    Text(
                        "Set a maximum to cap discretionary spending.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                    Text(
                        "Revisit your goals each month as expenses change.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
            }
        }
    }
}

// =============================================================================
// GoalStatusCard
// =============================================================================

@Composable
private fun GoalStatusCard(
    status    : SpendingStatus,
    monthTotal: Double,
    maxGoal   : Double
) {
    data class CardData(val bg: Color, val headline: String, val subline: String)

    val data = when (status) {
        SpendingStatus.NO_GOAL   -> CardData(
            bg       = TextMuted.copy(alpha = 0.12f),
            headline = "No goal set yet",
            subline  = "Enter a minimum and maximum below to start tracking."
        )
        SpendingStatus.UNDER_MIN -> CardData(
            bg       = YellowHighlight.copy(alpha = 0.20f),
            headline = "Spending below minimum",
            subline  = "You've spent R ${"%.2f".format(monthTotal)} — below your minimum goal."
        )
        SpendingStatus.ON_TRACK  -> CardData(
            bg       = TealPrimary.copy(alpha = 0.15f),
            headline = "On track!",
            subline  = "You've spent R ${"%.2f".format(monthTotal)} — within your budget range."
        )
        SpendingStatus.OVER_MAX  -> CardData(
            bg       = CoralAlert.copy(alpha = 0.15f),
            headline = "Over budget!",
            subline  = "You've spent R ${"%.2f".format(monthTotal)} — R ${"%.2f".format(monthTotal - maxGoal)} over your maximum."
        )
    }

    Card(
        colors   = CardDefaults.cardColors(containerColor = data.bg),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                data.headline,
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color      = OnBackgroundDark
            )
            Spacer(Modifier.height(4.dp))
            Text(
                data.subline,
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )
        }
    }
}

// =============================================================================
// GoalProgressBar
// =============================================================================

@Composable
private fun GoalProgressBar(
    monthTotal: Double,
    minGoal   : Double,
    maxGoal   : Double,
    status    : SpendingStatus
) {
    val progress = if (maxGoal > 0) (monthTotal / maxGoal).toFloat().coerceIn(0f, 1f) else 0f

    val barColor = when (status) {
        SpendingStatus.ON_TRACK  -> TealPrimary
        SpendingStatus.UNDER_MIN -> YellowHighlight
        SpendingStatus.OVER_MAX  -> CoralAlert
        SpendingStatus.NO_GOAL   -> TealPrimary
    }

    Card(
        colors   = CardDefaults.cardColors(containerColor = SurfaceWhite),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Monthly Spending Progress",
                style = MaterialTheme.typography.titleSmall,
                color = OnBackgroundDark
            )
            LinearProgressIndicator(
                progress   = { progress },
                modifier   = Modifier.fillMaxWidth().height(12.dp),
                color      = barColor,
                trackColor = barColor.copy(alpha = 0.20f)
            )
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Min: R ${"%.2f".format(minGoal)}",
                    style      = MaterialTheme.typography.labelSmall,
                    color      = YellowHighlight,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    "${"%.0f".format(progress * 100)}%",
                    style      = MaterialTheme.typography.labelSmall,
                    color      = barColor,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Max: R ${"%.2f".format(maxGoal)}",
                    style      = MaterialTheme.typography.labelSmall,
                    color      = CoralAlert,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

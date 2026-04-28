package com.budgetwise.app.ui.goals

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.budgetwise.app.ui.theme.TealPrimary

/**
 * TODO (Seth): Implement the Goals screen.
 *
 * Required features per spec:
 *  - Colour-coded status card driven by GoalsViewModel.spendingStatus:
 *      NO_GOAL   → grey        "Set a goal to track your spending"
 *      UNDER_MIN → YellowHighlight "Spending less than your minimum"
 *      ON_TRACK  → TealPrimary     "On track!"
 *      OVER_MAX  → CoralAlert      "Over budget!"
 *  - LinearProgressIndicator (monthTotal / maxGoal, clamped 0f–1f)
 *  - Two OutlinedTextFields: Min Goal (R), Max Goal (R)
 *  - Save button → GoalsViewModel.saveGoal(minStr, maxStr)
 *  - Tips card at bottom
 *
 * ViewModel: GoalsViewModel (inject via hiltViewModel())
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title  = { Text("Goals") },
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
                Text("Goals", style = MaterialTheme.typography.titleMedium)
                Text(
                    "TODO (Seth): implement goals with progress bar",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}

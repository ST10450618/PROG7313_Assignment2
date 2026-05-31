package com.budgetwise.app.ui.tips

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.budgetwise.app.ui.theme.*

/**
 * Smart Tips Screen — Own Feature 2.
 *
 * Generates personalised budgeting tips based on the user's actual
 * spending data:
 *  - Identifies top spending category
 *  - Warns when over budget
 *  - Encourages when on track
 *  - Suggests saving targets based on remaining budget
 *
 * Tips are dynamic — they update as the user's expense data changes.
 * Documented in README as Own Feature 2.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartTipsScreen(viewModel: SmartTipsViewModel = hiltViewModel()) {
    val tips by viewModel.tips.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title  = { Text("Smart Tips", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = TealPrimary)
            )
        },
        containerColor = BackgroundLight
    ) { padding ->
        if (tips.isEmpty()) {
            Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = TealPrimary)
                    Spacer(Modifier.height(12.dp))
                    Text("Analysing your spending...", color = TextMuted)
                }
            }
        } else {
            LazyColumn(
                modifier            = Modifier.fillMaxSize().padding(padding),
                contentPadding      = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        "Personalised for you this month",
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color      = OnBackgroundDark
                    )
                }

                itemsIndexed(tips) { _, tip ->
                    TipCard(tip = tip)
                }

                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun TipCard(tip: BudgetTip) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(12.dp),
        colors   = CardDefaults.cardColors(containerColor = tip.bgColor)
    ) {
        Row(
            modifier  = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                tip.icon,
                contentDescription = null,
                tint     = tip.iconColor,
                modifier = Modifier.size(28.dp).padding(top = 2.dp)
            )
            Column {
                Text(
                    tip.title,
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color      = OnBackgroundDark
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    tip.body,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }
        }
    }
}

/** Data class representing a single personalised budget tip card. */
data class BudgetTip(
    val title    : String,
    val body     : String,
    val icon     : ImageVector,
    val iconColor: Color,
    val bgColor  : Color
)
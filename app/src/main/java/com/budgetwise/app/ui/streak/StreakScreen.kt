package com.budgetwise.app.ui.streak

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.budgetwise.app.ui.theme.*

/**
 * Streak Screen — Own Feature 1.
 *
 * Displays the user's consecutive daily expense-logging streak.
 * Motivation: gamification element that encourages users to log expenses
 * every day, making budgeting into a habit.
 *
 * Features:
 *  - Large flame icon + streak count
 *  - Milestone badges at 3, 7, 14, 30 days
 *  - Best-streak record
 *  - Streak data synced to Firestore (persists across reinstalls)
 *
 * Documented in README as Own Feature 1.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StreakScreen(viewModel: StreakViewModel = hiltViewModel()) {
    val streak    by viewModel.currentStreak.collectAsStateWithLifecycle()
    val bestStreak by viewModel.bestStreak.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title  = { Text("My Streak", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = TealPrimary)
            )
        },
        containerColor = BackgroundLight
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // ── Main streak display ───────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(20.dp),
                colors   = CardDefaults.cardColors(
                    containerColor = if (streak > 0) TealPrimary else TextMuted.copy(alpha = 0.15f)
                )
            ) {
                Column(
                    modifier            = Modifier.fillMaxWidth().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Filled.LocalFireDepartment,
                        contentDescription = "Streak",
                        tint   = if (streak > 0) YellowHighlight else TextMuted,
                        modifier = Modifier.size(72.dp)
                    )
                    Text(
                        text  = "$streak",
                        fontSize   = 64.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color      = if (streak > 0) Color.White else TextMuted
                    )
                    Text(
                        text  = if (streak == 1) "day streak" else "day streak",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (streak > 0) Color.White.copy(alpha = 0.85f) else TextMuted
                    )
                    if (streak == 0) {
                        Text(
                            "Log an expense today to start your streak!",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                }
            }

            // ── Best streak ───────────────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(12.dp),
                colors   = CardDefaults.cardColors(containerColor = SurfaceWhite)
            ) {
                Row(
                    modifier            = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment   = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Filled.Star, null, tint = YellowHighlight, modifier = Modifier.size(28.dp))
                    Column {
                        Text("Best Streak", style = MaterialTheme.typography.labelMedium, color = TextMuted)
                        Text(
                            "$bestStreak days",
                            style      = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color      = OnBackgroundDark
                        )
                    }
                }
            }

            // ── Milestone badges ──────────────────────────────────────────
            Text(
                "Milestone Badges",
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color      = OnBackgroundDark,
                modifier   = Modifier.align(Alignment.Start)
            )

            val milestones = listOf(
                Triple(3,  "🔥", "Hot Start"),
                Triple(7,  "⭐", "Week Warrior"),
                Triple(14, "🏆", "Fortnight Champion"),
                Triple(30, "💎", "Monthly Master")
            )

            milestones.chunked(2).forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEach { (days, emoji, name) ->
                        val unlocked = bestStreak >= days
                        Card(
                            modifier = Modifier.weight(1f),
                            shape    = RoundedCornerShape(12.dp),
                            colors   = CardDefaults.cardColors(
                                containerColor = if (unlocked) TealPrimary.copy(alpha = 0.12f) else TextMuted.copy(alpha = 0.07f)
                            )
                        ) {
                            Column(
                                modifier            = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    emoji,
                                    fontSize = 32.sp,
                                    color    = if (unlocked) Color.Unspecified else Color.Gray
                                )
                                Text(
                                    name,
                                    style  = MaterialTheme.typography.labelMedium,
                                    color  = if (unlocked) OnBackgroundDark else TextMuted,
                                    fontWeight = if (unlocked) FontWeight.SemiBold else FontWeight.Normal
                                )
                                Text(
                                    "$days days",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (unlocked) TealPrimary else TextMuted
                                )
                                if (!unlocked) {
                                    Text(
                                        "${days - bestStreak} more to go",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── How it works ──────────────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(12.dp),
                colors   = CardDefaults.cardColors(containerColor = TealPrimary.copy(alpha = 0.07f))
            ) {
                Column(Modifier.padding(14.dp)) {
                    Text("How streaks work", fontWeight = FontWeight.SemiBold, color = TealPrimary)
                    Spacer(Modifier.height(6.dp))
                    Text("• Log at least one expense each day to maintain your streak.", style = MaterialTheme.typography.bodySmall)
                    Text("• Miss a day and your streak resets to 0.", style = MaterialTheme.typography.bodySmall)
                    Text("• Your best streak is saved forever — even if you reset.", style = MaterialTheme.typography.bodySmall)
                    Text("• Streak data is backed up online so it survives reinstalls.", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
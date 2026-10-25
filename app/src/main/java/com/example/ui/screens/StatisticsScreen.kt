package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Fireplace
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.components.AuroraGlassCard
import com.example.ui.components.AuroraStatCard
import com.example.ui.components.WeeklyDistanceBarChart
import com.example.ui.theme.AuroraBackground
import com.example.ui.theme.AuroraBorder
import com.example.ui.theme.AuroraBorderSubtle
import com.example.ui.theme.AuroraMint
import com.example.ui.theme.AuroraSurfaceCard
import com.example.ui.theme.AuroraTextPrimary
import com.example.ui.theme.AuroraTextSecondary
import com.example.ui.theme.AuroraViolet
import com.example.ui.theme.AuroraWarning
import com.example.util.GpsMetricsCalculator

@Composable
fun StatisticsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val records by viewModel.personalRecords.collectAsState()
    val weeklyDistances by viewModel.weeklyDistances.collectAsState()
    val dailyGoalKm by viewModel.dailyGoalKm.collectAsState()
    val unitSystem by viewModel.unitSystem.collectAsState()

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AuroraBackground)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .testTag("statistics_screen")
    ) {
        // Title
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            Text(
                text = "SPORTS ANALYTICS",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                ),
                color = AuroraMint
            )
            Text(
                text = "Personal records and cumulative performance",
                style = MaterialTheme.typography.labelSmall,
                color = AuroraTextSecondary
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // All-Time Total Distance Highlight Banner
        AuroraGlassCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = AuroraBorder,
            backgroundColor = AuroraSurfaceCard
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "LIFETIME OUTDOOR DISTANCE",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp),
                    color = AuroraTextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = GpsMetricsCalculator.formatDistanceValueOnly(records.totalDistanceMeters, unitSystem),
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 48.sp
                        ),
                        color = AuroraMint
                    )
                    Spacer(modifier = Modifier.padding(horizontal = 3.dp))
                    Text(
                        text = GpsMetricsCalculator.getDistanceUnit(unitSystem).uppercase(),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = AuroraTextSecondary,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "${records.totalWalksCount} total completed walks • ${records.totalSteps} steps taken",
                    style = MaterialTheme.typography.labelSmall,
                    color = AuroraViolet
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3 Lifetime Totals
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AuroraStatCard(
                title = "Total Time",
                value = GpsMetricsCalculator.formatDuration(records.longestDurationSeconds),
                icon = Icons.Default.Timer,
                accentColor = AuroraViolet,
                modifier = Modifier.weight(1f)
            )
            AuroraStatCard(
                title = "Total Energy",
                value = "${records.totalCalories}",
                unit = "kcal",
                icon = Icons.Default.Fireplace,
                accentColor = AuroraWarning,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Personal Records Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "PERSONAL RECORDS",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = AuroraTextPrimary
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Record cards
        AuroraGlassCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = AuroraBorderSubtle
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Longest walk
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = AuroraMint
                        )
                        Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                        Column {
                            Text("Longest Walk", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = AuroraTextPrimary)
                            Text("Single continuous route", style = MaterialTheme.typography.labelSmall, color = AuroraTextSecondary)
                        }
                    }
                    Text(
                        text = GpsMetricsCalculator.formatDistance(records.longestWalkMeters, unitSystem),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = AuroraMint
                    )
                }

                // Fastest pace
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Default.FlashOn,
                            contentDescription = null,
                            tint = AuroraViolet
                        )
                        Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                        Column {
                            Text("Best Pace", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = AuroraTextPrimary)
                            Text("Fastest sustained walk", style = MaterialTheme.typography.labelSmall, color = AuroraTextSecondary)
                        }
                    }
                    Text(
                        text = GpsMetricsCalculator.formatPace(records.fastestPaceSecPerKm, unitSystem),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = AuroraViolet
                    )
                }

                // Longest Duration
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = AuroraWarning
                        )
                        Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                        Column {
                            Text("Longest Duration", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = AuroraTextPrimary)
                            Text("Max continuous elapsed time", style = MaterialTheme.typography.labelSmall, color = AuroraTextSecondary)
                        }
                    }
                    Text(
                        text = GpsMetricsCalculator.formatDuration(records.longestDurationSeconds),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = AuroraWarning
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Weekly Distribution Chart
        AuroraGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 80.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "THIS WEEK'S PACE & DISTANCE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = AuroraTextSecondary
                )
                Spacer(modifier = Modifier.height(16.dp))
                WeeklyDistanceBarChart(
                    dailyDistancesKm = weeklyDistances,
                    dailyGoalKm = dailyGoalKm,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

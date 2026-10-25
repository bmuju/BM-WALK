package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Fireplace
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.TrackingStatus
import com.example.data.model.UnitSystem
import com.example.ui.MainViewModel
import com.example.ui.Screen
import com.example.ui.components.AuroraGlassCard
import com.example.ui.components.AuroraGlowButton
import com.example.ui.components.AuroraStatCard
import com.example.ui.components.DailyGoalArc
import com.example.ui.components.WeeklyDistanceBarChart
import com.example.ui.theme.AuroraBackground
import com.example.ui.theme.AuroraBorder
import com.example.ui.theme.AuroraMint
import com.example.ui.theme.AuroraSurfaceCard
import com.example.ui.theme.AuroraTextPrimary
import com.example.ui.theme.AuroraTextSecondary
import com.example.ui.theme.AuroraViolet
import com.example.ui.theme.AuroraWarning
import com.example.util.GpsMetricsCalculator
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onStartWorkout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val unitSystem by viewModel.unitSystem.collectAsState()
    val dailyGoalKm by viewModel.dailyGoalKm.collectAsState()
    val dailyStepsGoal by viewModel.dailyStepsGoal.collectAsState()
    val todayStats by viewModel.todayStats.collectAsState()
    val weeklyDistances by viewModel.weeklyDistances.collectAsState()
    val allWalks by viewModel.allWalks.collectAsState()
    val trackingStatus by viewModel.trackingStatus.collectAsState()

    val (todayDistanceMeters, todaySteps, timeCal) = todayStats
    val (todayDuration, todayCalories) = timeCal

    val isWorkoutActive = trackingStatus == TrackingStatus.TRACKING || trackingStatus == TrackingStatus.PAUSED || trackingStatus == TrackingStatus.ACQUIRING

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AuroraBackground)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .testTag("home_screen")
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.bm_walk_logo),
                        contentDescription = "BM WALK Logo",
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "BM × WALK",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        ),
                        color = AuroraMint
                    )
                    Text(
                        text = "MOVE BEYOND ORDINARY",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        ),
                        color = AuroraViolet
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(AuroraSurfaceCard)
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    text = "GPS READY",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = AuroraMint
                    )
                )
            }
        }

        // Active workout notice banner if in progress
        if (isWorkoutActive) {
            AuroraGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .clickable { viewModel.navigateTo(Screen.LiveWorkout) },
                borderColor = AuroraWarning
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(AuroraWarning)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "WORKOUT IN PROGRESS",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = AuroraWarning
                        )
                        Text(
                            text = "Tap to view live map and statistics",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AuroraTextPrimary
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = AuroraMint
                    )
                }
            }
        }

        // Daily Goal Ring Card
        AuroraGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DAILY TARGET PROGRESS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = AuroraTextSecondary
                    )
                    Text(
                        text = "TODAY",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = AuroraMint,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                DailyGoalArc(
                    currentMeters = todayDistanceMeters,
                    goalMeters = dailyGoalKm * 1000.0,
                    currentSteps = todaySteps,
                    stepsGoal = dailyStepsGoal
                )
            }
        }

        // 4 Grid Stats Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AuroraStatCard(
                title = "Distance",
                value = GpsMetricsCalculator.formatDistanceValueOnly(todayDistanceMeters, unitSystem),
                unit = GpsMetricsCalculator.getDistanceUnit(unitSystem),
                icon = Icons.Default.DirectionsWalk,
                accentColor = AuroraMint,
                modifier = Modifier.weight(1f)
            )
            AuroraStatCard(
                title = "Duration",
                value = GpsMetricsCalculator.formatDuration(todayDuration),
                icon = Icons.Default.Timer,
                accentColor = AuroraViolet,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AuroraStatCard(
                title = "Calories",
                value = "$todayCalories",
                unit = "kcal",
                icon = Icons.Default.Fireplace,
                accentColor = AuroraWarning,
                modifier = Modifier.weight(1f)
            )
            val avgPace = GpsMetricsCalculator.calculateAvgPaceSecondsPerKm(todayDistanceMeters, todayDuration)
            AuroraStatCard(
                title = "Avg Pace",
                value = GpsMetricsCalculator.formatPace(avgPace, unitSystem),
                icon = Icons.Default.Speed,
                accentColor = AuroraMint,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Start Walking Big Glow Button
        AuroraGlowButton(
            text = if (isWorkoutActive) "RESUME LIVE WORKOUT" else "START WALKING",
            onClick = {
                if (isWorkoutActive) {
                    viewModel.navigateTo(Screen.LiveWorkout)
                } else {
                    onStartWorkout()
                }
            },
            icon = Icons.Default.DirectionsWalk,
            isPulsing = true,
            modifier = Modifier.fillMaxWidth(),
            testTag = "start_walking_button"
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Weekly Activity Chart Card
        AuroraGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "WEEKLY ACTIVITY",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = AuroraTextSecondary
                    )
                    Text(
                        text = "Goal: ${dailyGoalKm.toInt()} km/day",
                        style = MaterialTheme.typography.labelSmall,
                        color = AuroraViolet
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                WeeklyDistanceBarChart(
                    dailyDistancesKm = weeklyDistances,
                    dailyGoalKm = dailyGoalKm,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Recent Workouts header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "RECENT WORKOUTS",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = AuroraTextPrimary
            )
            if (allWalks.isNotEmpty()) {
                Text(
                    text = "View All",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = AuroraMint
                    ),
                    modifier = Modifier
                        .clickable { viewModel.navigateTo(Screen.History) }
                        .padding(4.dp)
                )
            }
        }

        if (allWalks.isEmpty()) {
            AuroraGlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = AuroraBorder
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsWalk,
                        contentDescription = null,
                        tint = AuroraMint.copy(alpha = 0.5f),
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No walks recorded yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = AuroraTextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Lace up your shoes, step outside and tap Start Walking to begin your first tracked journey.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AuroraTextSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            val recentWalk = allWalks.first()
            AuroraGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.navigateTo(Screen.History) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = recentWalk.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = AuroraTextPrimary
                        )
                        val dateStr = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault())
                            .format(Date(recentWalk.startTimeMillis))
                        Text(
                            text = dateStr,
                            style = MaterialTheme.typography.labelSmall,
                            color = AuroraTextSecondary
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = GpsMetricsCalculator.formatDistance(recentWalk.distanceMeters, unitSystem),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = AuroraMint
                            )
                        )
                        Text(
                            text = "${GpsMetricsCalculator.formatDuration(recentWalk.durationSeconds)} • ${recentWalk.calories} kcal",
                            style = MaterialTheme.typography.labelSmall,
                            color = AuroraTextSecondary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

package com.example.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Fireplace
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.WalkEntity
import com.example.ui.MainViewModel
import com.example.ui.components.AuroraGlassCard
import com.example.ui.components.AuroraGlowButton
import com.example.ui.components.AuroraStatCard
import com.example.ui.components.RoutePreviewCanvas
import com.example.ui.theme.AuroraBackground
import com.example.ui.theme.AuroraBorder
import com.example.ui.theme.AuroraBorderSubtle
import com.example.ui.theme.AuroraError
import com.example.ui.theme.AuroraMint
import com.example.ui.theme.AuroraSurface
import com.example.ui.theme.AuroraSurfaceCard
import com.example.ui.theme.AuroraSurfaceVariant
import com.example.ui.theme.AuroraTextPrimary
import com.example.ui.theme.AuroraTextSecondary
import com.example.ui.theme.AuroraViolet
import com.example.ui.theme.AuroraWarning
import com.example.util.GpsMetricsCalculator
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WorkoutSummaryScreen(
    walk: WalkEntity,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val unitSystem by viewModel.unitSystem.collectAsState()
    var title by remember { mutableStateOf(walk.title) }
    var notes by remember { mutableStateOf("") }

    val routePoints = remember { walk.parsePoints() }
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AuroraBackground)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .testTag("workout_summary_screen")
    ) {
        Text(
            text = "WORKOUT SUMMARY",
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            ),
            color = AuroraMint
        )

        val dateStr = SimpleDateFormat("EEEE, MMMM d, yyyy • h:mm a", Locale.getDefault())
            .format(Date(walk.startTimeMillis))
        Text(
            text = dateStr,
            style = MaterialTheme.typography.labelSmall,
            color = AuroraTextSecondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Route Map Preview
        RoutePreviewCanvas(
            points = routePoints,
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            backgroundColor = AuroraSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Primary Oversized Distance Banner
        AuroraGlassCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = AuroraBorder,
            backgroundColor = AuroraSurfaceCard
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "TOTAL DISTANCE",
                    style = MaterialTheme.typography.labelSmall,
                    color = AuroraTextSecondary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = GpsMetricsCalculator.formatDistanceValueOnly(walk.distanceMeters, unitSystem),
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 48.sp
                        ),
                        color = AuroraMint
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = GpsMetricsCalculator.getDistanceUnit(unitSystem).uppercase(),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = AuroraTextSecondary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 4 Secondary Metrics
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AuroraStatCard(
                title = "Duration",
                value = GpsMetricsCalculator.formatDuration(walk.durationSeconds),
                icon = Icons.Default.Timer,
                accentColor = AuroraViolet,
                modifier = Modifier.weight(1f)
            )
            AuroraStatCard(
                title = "Avg Pace",
                value = GpsMetricsCalculator.formatPace(walk.avgPaceSecondsPerKm, unitSystem),
                icon = Icons.Default.Speed,
                accentColor = AuroraMint,
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
                value = "${walk.calories}",
                unit = "kcal",
                icon = Icons.Default.Fireplace,
                accentColor = AuroraWarning,
                modifier = Modifier.weight(1f)
            )
            AuroraStatCard(
                title = "Steps",
                value = "${walk.steps}",
                icon = Icons.Default.DirectionsWalk,
                accentColor = AuroraMint,
                modifier = Modifier.weight(1f)
            )
        }

        if (walk.avgHeartRateBpm > 0) {
            Spacer(modifier = Modifier.height(12.dp))
            val zone = com.example.data.model.HeartRateZone.fromBpm(walk.avgHeartRateBpm)
            AuroraGlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = AuroraViolet.copy(alpha = 0.5f),
                backgroundColor = AuroraSurfaceCard
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(AuroraViolet.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.Favorite,
                                contentDescription = null,
                                tint = AuroraViolet,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "HEART RATE ANALYTICS",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = AuroraTextSecondary
                            )
                            Text(
                                text = "Avg: ${walk.avgHeartRateBpm} BPM • Peak: ${walk.maxHeartRateBpm} BPM",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = AuroraTextPrimary
                            )
                        }
                    }
                    Text(
                        text = zone.label.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = AuroraViolet
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Edit Walk Name
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Walk Title", color = AuroraTextSecondary) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("summary_title_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AuroraMint,
                unfocusedBorderColor = AuroraBorderSubtle,
                focusedTextColor = AuroraTextPrimary,
                unfocusedTextColor = AuroraTextPrimary,
                focusedContainerColor = AuroraSurfaceCard,
                unfocusedContainerColor = AuroraSurfaceCard
            ),
            shape = RoundedCornerShape(14.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Notes Input
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Notes (Weather, terrain, thoughts)", color = AuroraTextSecondary) },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .testTag("summary_notes_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AuroraMint,
                unfocusedBorderColor = AuroraBorderSubtle,
                focusedTextColor = AuroraTextPrimary,
                unfocusedTextColor = AuroraTextPrimary,
                focusedContainerColor = AuroraSurfaceCard,
                unfocusedContainerColor = AuroraSurfaceCard
            ),
            shape = RoundedCornerShape(14.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Save Button
        AuroraGlowButton(
            text = "SAVE WALK TO HISTORY",
            onClick = {
                viewModel.saveWorkout(walk, title, notes)
            },
            icon = Icons.Default.Check,
            modifier = Modifier.fillMaxWidth(),
            testTag = "save_walk_button"
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Discard Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .clickable { viewModel.discardWorkout() }
                .padding(vertical = 14.dp)
                .testTag("discard_walk_button"),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = AuroraError,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "DISCARD WORKOUT",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = AuroraError,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

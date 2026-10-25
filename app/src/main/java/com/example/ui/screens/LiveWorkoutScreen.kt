package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BleConnectionState
import com.example.data.model.HeartRateZone
import com.example.data.model.TrackingStatus
import com.example.ui.MainViewModel
import com.example.ui.Screen
import com.example.ui.components.AuroraGlassCard
import com.example.ui.components.AuroraMapView
import com.example.ui.components.BleHeartRateDialog
import com.example.ui.components.GpsAccuracyBadge
import com.example.ui.components.LiveHeartRateBadge
import com.example.ui.theme.AuroraBackground
import com.example.ui.theme.AuroraBorder
import com.example.ui.theme.AuroraBorderSubtle
import com.example.ui.theme.AuroraError
import com.example.ui.theme.AuroraMint
import com.example.ui.theme.AuroraSurface
import com.example.ui.theme.AuroraSurfaceCard
import com.example.ui.theme.AuroraTextPrimary
import com.example.ui.theme.AuroraTextSecondary
import com.example.ui.theme.AuroraViolet
import com.example.ui.theme.AuroraWarning
import com.example.util.GpsMetricsCalculator

@Composable
fun LiveWorkoutScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val liveMetrics by viewModel.liveMetrics.collectAsState()
    val status by viewModel.trackingStatus.collectAsState()
    val unitSystem by viewModel.unitSystem.collectAsState()

    val bleState by viewModel.bleConnectionState.collectAsState()
    val connectedHrDeviceName by viewModel.connectedHrDeviceName.collectAsState()
    val scannedHrDevices by viewModel.scannedHrDevices.collectAsState()
    val currentBpm by viewModel.currentHeartRate.collectAsState()
    val hrZone by viewModel.heartRateZone.collectAsState()
    val isBleDemo by viewModel.isBleDemoMode.collectAsState()

    var showStopDialog by remember { mutableStateOf(false) }
    var showBleDialog by remember { mutableStateOf(false) }

    val isPaused = status == TrackingStatus.PAUSED
    val isAcquiring = status == TrackingStatus.ACQUIRING

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AuroraBackground)
            .testTag("live_workout_screen")
    ) {
        // Full screen interactive Leaflet Map
        AuroraMapView(
            currentLocation = liveMetrics.currentLocation,
            routePoints = liveMetrics.routePoints,
            modifier = Modifier.fillMaxSize()
        )

        // Top Floating HUD
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // GPS Accuracy + Heart Rate Badges
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GpsAccuracyBadge(
                        accuracyMeters = liveMetrics.gpsAccuracyMeters,
                        isAcquiring = isAcquiring
                    )
                    LiveHeartRateBadge(
                        currentBpm = currentBpm,
                        zone = hrZone,
                        connectionState = bleState,
                        isDemo = isBleDemo,
                        onClick = { showBleDialog = true }
                    )
                }

                // Minimize / Back to Home HUD button
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(AuroraSurface.copy(alpha = 0.85f))
                        .border(1.dp, AuroraBorderSubtle, CircleShape)
                        .clickable { viewModel.navigateTo(Screen.Home) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Minimize",
                        tint = AuroraTextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            AnimatedVisibility(visible = isAcquiring) {
                AuroraGlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    borderColor = AuroraWarning
                ) {
                    Text(
                        text = "Acquiring precision GPS fix... Tracking begins automatically on good signal.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AuroraWarning,
                        modifier = Modifier.padding(12.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            AnimatedVisibility(visible = isPaused) {
                AuroraGlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    borderColor = AuroraWarning
                ) {
                    Text(
                        text = "WORKOUT PAUSED • Active time and distance are halted",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = AuroraWarning,
                        modifier = Modifier.padding(10.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Bottom Sports Analytics Panel & Controls
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(16.dp)
        ) {
            // Main Glass HUD Card
            AuroraGlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = AuroraBorder,
                backgroundColor = AuroraSurface.copy(alpha = 0.92f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Oversized Distance Stat
                    val distanceFormatted = GpsMetricsCalculator.formatDistanceValueOnly(
                        liveMetrics.distanceMeters,
                        unitSystem
                    )
                    val distanceUnit = GpsMetricsCalculator.getDistanceUnit(unitSystem)

                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = distanceFormatted,
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 50.sp
                            ),
                            color = AuroraMint
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = distanceUnit.uppercase(),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = AuroraTextSecondary,
                            modifier = Modifier.padding(bottom = 10.dp)
                        )
                    }

                    // Live Heart Rate Ribbon (if connected)
                    if (currentBpm != null) {
                        val zoneColor = when (hrZone) {
                            HeartRateZone.REST -> AuroraMint
                            HeartRateZone.FAT_BURN -> Color(0xFF48CAE4)
                            HeartRateZone.AEROBIC -> AuroraViolet
                            HeartRateZone.THRESHOLD -> AuroraWarning
                            HeartRateZone.PEAK -> AuroraError
                            null -> AuroraMint
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(zoneColor.copy(alpha = 0.15f))
                                .border(1.dp, zoneColor.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                                .clickable { showBleDialog = true }
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "Pulse",
                                tint = zoneColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "$currentBpm BPM",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold
                                ),
                                color = zoneColor
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "• ${hrZone?.label ?: "Cardio"}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = AuroraTextPrimary
                            )
                            val avgHr = liveMetrics.avgHeartRateBpm
                            if (avgHr != null && avgHr > 0) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "• Avg: $avgHr",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AuroraTextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Secondary Metrics Row (Time, Pace, Calories, Steps)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "TIME",
                                style = MaterialTheme.typography.labelSmall,
                                color = AuroraTextSecondary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = GpsMetricsCalculator.formatDuration(liveMetrics.durationSeconds),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = AuroraTextPrimary
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "AVG PACE",
                                style = MaterialTheme.typography.labelSmall,
                                color = AuroraTextSecondary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = GpsMetricsCalculator.formatPace(liveMetrics.avgPaceSecondsPerKm, unitSystem),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = AuroraTextPrimary
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "CALORIES",
                                style = MaterialTheme.typography.labelSmall,
                                color = AuroraTextSecondary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${liveMetrics.calories} kcal",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = AuroraViolet
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "STEPS",
                                style = MaterialTheme.typography.labelSmall,
                                color = AuroraTextSecondary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${liveMetrics.steps}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = AuroraMint
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Workout Action Buttons (Pause / Resume and Stop)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Pause / Resume Button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isPaused) AuroraMint else AuroraSurfaceCard)
                                .border(
                                    1.dp,
                                    if (isPaused) AuroraMint else AuroraBorderSubtle,
                                    RoundedCornerShape(20.dp)
                                )
                                .clickable {
                                    if (isPaused) {
                                        viewModel.resumeWorkout()
                                    } else {
                                        viewModel.pauseWorkout()
                                    }
                                }
                                .testTag("pause_resume_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                    contentDescription = if (isPaused) "Resume" else "Pause",
                                    tint = if (isPaused) AuroraBackground else AuroraMint,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isPaused) "RESUME" else "PAUSE",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    ),
                                    color = if (isPaused) AuroraBackground else AuroraTextPrimary
                                )
                            }
                        }

                        // Finish / Stop Button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(AuroraError.copy(alpha = 0.2f))
                                .border(1.dp, AuroraError.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                                .clickable { showStopDialog = true }
                                .testTag("finish_workout_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Stop,
                                    contentDescription = "Stop",
                                    tint = AuroraError,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "FINISH",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    ),
                                    color = AuroraError
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // BLE Heart Rate Pairing Dialog
    if (showBleDialog) {
        BleHeartRateDialog(
            connectionState = bleState,
            connectedDeviceName = connectedHrDeviceName,
            scannedDevices = scannedHrDevices,
            isDemoMode = isBleDemo,
            onStartScan = { viewModel.startBleScan() },
            onStopScan = { viewModel.stopBleScan() },
            onConnect = { addr -> viewModel.connectBleDevice(addr) },
            onDisconnect = { viewModel.disconnectBleDevice() },
            onStartVirtual = { viewModel.startVirtualHrSensor() },
            onDismiss = { showBleDialog = false }
        )
    }

    // Confirmation Dialog before ending workout
    if (showStopDialog) {
        AlertDialog(
            onDismissRequest = { showStopDialog = false },
            title = {
                Text(
                    text = "Finish Workout?",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = AuroraTextPrimary
                )
            },
            text = {
                Text(
                    text = "Are you ready to complete this walk and save your metrics?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AuroraTextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showStopDialog = false
                        viewModel.finishWorkout()
                    }
                ) {
                    Text(
                        text = "FINISH & SAVE",
                        color = AuroraMint,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showStopDialog = false }) {
                    Text("KEEP WALKING", color = AuroraTextSecondary)
                }
            },
            containerColor = AuroraSurface,
            shape = RoundedCornerShape(24.dp)
        )
    }
}

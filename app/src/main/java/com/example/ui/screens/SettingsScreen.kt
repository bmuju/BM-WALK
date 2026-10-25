package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UnitSystem
import com.example.ui.MainViewModel
import com.example.ui.components.AuroraGlassCard
import com.example.ui.components.BleHeartRateDialog
import com.example.ui.theme.AuroraBackground
import com.example.ui.theme.AuroraBorder
import com.example.ui.theme.AuroraBorderSubtle
import com.example.ui.theme.AuroraError
import com.example.ui.theme.AuroraMint
import com.example.ui.theme.AuroraSurface
import com.example.ui.theme.AuroraSurfaceCard
import com.example.ui.theme.AuroraTextMuted
import com.example.ui.theme.AuroraTextPrimary
import com.example.ui.theme.AuroraTextSecondary
import com.example.ui.theme.AuroraViolet

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val unitSystem by viewModel.unitSystem.collectAsState()
    val dailyGoalKm by viewModel.dailyGoalKm.collectAsState()
    val dailyStepsGoal by viewModel.dailyStepsGoal.collectAsState()
    val userWeightKg by viewModel.userWeightKg.collectAsState()
    val gpsThreshold by viewModel.gpsAccuracyThreshold.collectAsState()
    val bleState by viewModel.bleConnectionState.collectAsState()
    val connectedHrDeviceName by viewModel.connectedHrDeviceName.collectAsState()
    val scannedHrDevices by viewModel.scannedHrDevices.collectAsState()
    val isBleDemo by viewModel.isBleDemoMode.collectAsState()

    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var showBleDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AuroraBackground)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .testTag("settings_screen")
    ) {
        // Title
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            Text(
                text = "SETTINGS",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                ),
                color = AuroraMint
            )
            Text(
                text = "Preferences, measurement units and privacy controls",
                style = MaterialTheme.typography.labelSmall,
                color = AuroraTextSecondary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Measurement Units
        AuroraGlassCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = AuroraBorderSubtle
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Text(
                    text = "MEASUREMENT SYSTEM",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = AuroraTextSecondary
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilterChip(
                        selected = unitSystem == UnitSystem.METRIC,
                        onClick = { viewModel.updateUnitSystem(UnitSystem.METRIC) },
                        label = { Text("Metric (km, min/km)") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AuroraMint,
                            selectedLabelColor = AuroraBackground,
                            containerColor = AuroraSurfaceCard,
                            labelColor = AuroraTextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = unitSystem == UnitSystem.METRIC,
                            borderColor = AuroraBorderSubtle,
                            selectedBorderColor = AuroraMint
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = unitSystem == UnitSystem.IMPERIAL,
                        onClick = { viewModel.updateUnitSystem(UnitSystem.IMPERIAL) },
                        label = { Text("Imperial (mi, min/mi)") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AuroraMint,
                            selectedLabelColor = AuroraBackground,
                            containerColor = AuroraSurfaceCard,
                            labelColor = AuroraTextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = unitSystem == UnitSystem.IMPERIAL,
                            borderColor = AuroraBorderSubtle,
                            selectedBorderColor = AuroraMint
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Daily Walking Goal
        AuroraGlassCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = AuroraBorderSubtle
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Text(
                    text = "DAILY DISTANCE TARGET",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = AuroraTextSecondary
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(3.0, 5.0, 8.0, 10.0).forEach { goal ->
                        FilterChip(
                            selected = dailyGoalKm == goal,
                            onClick = { viewModel.updateDailyGoalKm(goal) },
                            label = { Text("${goal.toInt()} km") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AuroraViolet,
                                selectedLabelColor = AuroraBackground,
                                containerColor = AuroraSurfaceCard,
                                labelColor = AuroraTextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = dailyGoalKm == goal,
                                borderColor = AuroraBorderSubtle,
                                selectedBorderColor = AuroraViolet
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Daily Steps Goal
        AuroraGlassCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = AuroraBorderSubtle
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Text(
                    text = "DAILY STEPS TARGET",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = AuroraTextSecondary
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(5000, 7500, 10000, 15000).forEach { steps ->
                        FilterChip(
                            selected = dailyStepsGoal == steps,
                            onClick = { viewModel.updateDailyStepsGoal(steps) },
                            label = { Text(if (steps >= 1000) "${steps / 1000}k" else "$steps") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AuroraMint,
                                selectedLabelColor = AuroraBackground,
                                containerColor = AuroraSurfaceCard,
                                labelColor = AuroraTextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = dailyStepsGoal == steps,
                                borderColor = AuroraBorderSubtle,
                                selectedBorderColor = AuroraMint
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // User Body Weight (for accurate calorie burn)
        AuroraGlassCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = AuroraBorderSubtle
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Text(
                    text = "ESTIMATED BODY WEIGHT (FOR ENERGY CALCULATION)",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = AuroraTextSecondary
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(55f, 65f, 75f, 85f, 95f).forEach { weight ->
                        FilterChip(
                            selected = userWeightKg.toInt() == weight.toInt(),
                            onClick = { viewModel.updateUserWeight(weight) },
                            label = { Text("${weight.toInt()}kg") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AuroraMint,
                                selectedLabelColor = AuroraBackground,
                                containerColor = AuroraSurfaceCard,
                                labelColor = AuroraTextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = userWeightKg.toInt() == weight.toInt(),
                                borderColor = AuroraBorderSubtle,
                                selectedBorderColor = AuroraMint
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // GPS Outlier Rejection Threshold
        AuroraGlassCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = AuroraBorderSubtle
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Text(
                    text = "GPS ACCURACY SENSITIVITY",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = AuroraTextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Discards inaccurate fixes and standing jitter to ensure pure walking distances.",
                    style = MaterialTheme.typography.bodySmall,
                    color = AuroraTextSecondary
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        15.0f to "Strict (15m)",
                        35.0f to "Standard (35m)",
                        50.0f to "Relaxed (50m)"
                    ).forEach { (threshold, label) ->
                        FilterChip(
                            selected = gpsThreshold == threshold,
                            onClick = { viewModel.updateGpsThreshold(threshold) },
                            label = { Text(label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AuroraViolet,
                                selectedLabelColor = AuroraBackground,
                                containerColor = AuroraSurfaceCard,
                                labelColor = AuroraTextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = gpsThreshold == threshold,
                                borderColor = AuroraBorderSubtle,
                                selectedBorderColor = AuroraViolet
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // System Location Settings link
        AuroraGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                },
            borderColor = AuroraBorderSubtle
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = AuroraMint,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Android Location Permissions",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = AuroraTextPrimary
                    )
                    Text(
                        text = "Manage precision GPS permissions in system settings",
                        style = MaterialTheme.typography.labelSmall,
                        color = AuroraTextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Bluetooth Low Energy (BLE) Heart Rate Monitor
        AuroraGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showBleDialog = true },
            borderColor = if (bleState == com.example.data.model.BleConnectionState.CONNECTED) AuroraMint else AuroraBorderSubtle
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                androidx.compose.material3.Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.Favorite,
                    contentDescription = null,
                    tint = if (bleState == com.example.data.model.BleConnectionState.CONNECTED) AuroraMint else AuroraViolet,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Bluetooth Heart Rate Monitor",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = AuroraTextPrimary
                    )
                    Text(
                        text = when (bleState) {
                            com.example.data.model.BleConnectionState.CONNECTED -> "Connected to ${connectedHrDeviceName ?: "Sensor"}"
                            com.example.data.model.BleConnectionState.CONNECTING -> "Connecting..."
                            com.example.data.model.BleConnectionState.SCANNING -> "Scanning for sensors..."
                            com.example.data.model.BleConnectionState.DISCONNECTED -> "Pair chest strap, arm band, or smart watch"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = if (bleState == com.example.data.model.BleConnectionState.CONNECTED) AuroraMint else AuroraTextSecondary
                    )
                }
                androidx.compose.material3.Text(
                    text = if (bleState == com.example.data.model.BleConnectionState.CONNECTED) "ACTIVE" else "PAIR",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (bleState == com.example.data.model.BleConnectionState.CONNECTED) AuroraMint else AuroraViolet
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Privacy Guarantee Card
        AuroraGlassCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = AuroraBorder
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = AuroraMint,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.padding(horizontal = 6.dp))
                    Text(
                        text = "PRIVACY & DATA OWNERSHIP",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = AuroraMint
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "All workout metrics, routes, and personal records remain strictly stored on your Android device (in local Room database). No accounts, tracking servers, or location upload. Map tiles are loaded securely from OpenStreetMap and CartoDB.",
                    style = MaterialTheme.typography.bodySmall,
                    color = AuroraTextSecondary,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Danger Zone: Clear all history
        AuroraGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showClearHistoryDialog = true },
            borderColor = AuroraError.copy(alpha = 0.5f),
            backgroundColor = AuroraSurfaceCard
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteSweep,
                    contentDescription = null,
                    tint = AuroraError,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Delete All Workout History",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = AuroraError
                        )
                    )
                    Text(
                        text = "Permanently remove all recorded walks and reset analytics",
                        style = MaterialTheme.typography.labelSmall,
                        color = AuroraTextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // App Info & Credits
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "BM × WALK",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp
                ),
                color = AuroraMint
            )
            Text(
                text = "MOVE BEYOND ORDINARY",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                ),
                color = AuroraViolet
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Version 1.0.0 (Build 1) • Production Release",
                style = MaterialTheme.typography.labelSmall,
                color = AuroraTextMuted
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Made by BM-UJWAL",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = AuroraTextSecondary,
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
    }

    // Confirmation dialog for clearing all history
    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = {
                Text(
                    text = "Clear All Walk History?",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = AuroraTextPrimary
                )
            },
            text = {
                Text(
                    text = "This will permanently delete all past walks and routes from your device. This cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AuroraTextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllHistory()
                        showClearHistoryDialog = false
                    }
                ) {
                    Text("DELETE ALL", color = AuroraError, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) {
                    Text("CANCEL", color = AuroraTextSecondary)
                }
            },
            containerColor = AuroraSurface,
            shape = RoundedCornerShape(24.dp)
        )
    }

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
}

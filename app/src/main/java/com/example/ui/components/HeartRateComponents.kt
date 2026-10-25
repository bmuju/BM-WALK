package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.SensorsOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BleConnectionState
import com.example.data.model.BleDeviceItem
import com.example.data.model.HeartRateZone
import com.example.ui.theme.AuroraBackground
import com.example.ui.theme.AuroraBorder
import com.example.ui.theme.AuroraBorderSubtle
import com.example.ui.theme.AuroraError
import com.example.ui.theme.AuroraMint
import com.example.ui.theme.AuroraSurface
import com.example.ui.theme.AuroraSurfaceCard
import com.example.ui.theme.AuroraSurfaceVariant
import com.example.ui.theme.AuroraTextMuted
import com.example.ui.theme.AuroraTextPrimary
import com.example.ui.theme.AuroraTextSecondary
import com.example.ui.theme.AuroraViolet
import com.example.ui.theme.AuroraWarning

@Composable
fun LiveHeartRateBadge(
    currentBpm: Int?,
    zone: HeartRateZone?,
    connectionState: BleConnectionState,
    isDemo: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isConnected = connectionState == BleConnectionState.CONNECTED && currentBpm != null

    // Heart beat animation duration tuned to BPM (default ~800ms)
    val beatDurationMs = if (isConnected && currentBpm != null && currentBpm > 40) {
        (60000 / currentBpm).coerceIn(350, 1500)
    } else 800

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_beat")
    val heartScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isConnected) 1.22f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(beatDurationMs / 2, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "heart_scale"
    )

    val (accentColor, labelText) = when {
        !isConnected && connectionState == BleConnectionState.CONNECTING ->
            Pair(AuroraViolet, "Connecting HR...")
        !isConnected && connectionState == BleConnectionState.SCANNING ->
            Pair(AuroraMint, "Scanning HR...")
        !isConnected ->
            Pair(AuroraTextMuted, "Connect HRM")
        else -> {
            val color = when (zone) {
                HeartRateZone.REST -> AuroraMint
                HeartRateZone.FAT_BURN -> Color(0xFF48CAE4)
                HeartRateZone.AEROBIC -> AuroraViolet
                HeartRateZone.THRESHOLD -> AuroraWarning
                HeartRateZone.PEAK -> AuroraError
                null -> AuroraMint
            }
            Pair(color, "${zone?.label ?: "Active"} • $currentBpm BPM")
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(AuroraSurface.copy(alpha = 0.9f))
            .border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag("live_heart_rate_badge")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = "Heart Rate",
                tint = accentColor,
                modifier = Modifier
                    .size(16.dp)
                    .scale(heartScale)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = labelText,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = AuroraTextPrimary
            )
            if (isDemo) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "DEMO",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                    color = AuroraWarning
                )
            }
        }
    }
}

@Composable
fun BleHeartRateDialog(
    connectionState: BleConnectionState,
    connectedDeviceName: String?,
    scannedDevices: List<BleDeviceItem>,
    isDemoMode: Boolean,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    onConnect: (String) -> Unit,
    onDisconnect: () -> Unit,
    onStartVirtual: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Bluetooth,
                        contentDescription = null,
                        tint = AuroraMint,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "HEART RATE SENSOR",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = AuroraMint
                    )
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = AuroraTextSecondary
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Connect a Bluetooth Low Energy (BLE) chest strap, arm band, or smart watch for real-time pulse analytics.",
                    style = MaterialTheme.typography.bodySmall,
                    color = AuroraTextSecondary
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Connection status card
                AuroraGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = if (connectionState == BleConnectionState.CONNECTED) AuroraMint else AuroraBorderSubtle,
                    backgroundColor = AuroraSurfaceCard
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (connectionState == BleConnectionState.CONNECTED)
                                            AuroraMint.copy(alpha = 0.2f)
                                        else AuroraSurfaceVariant
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (connectionState == BleConnectionState.CONNECTED)
                                        Icons.Default.BluetoothConnected
                                    else Icons.Default.Sensors,
                                    contentDescription = null,
                                    tint = if (connectionState == BleConnectionState.CONNECTED) AuroraMint else AuroraTextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = when (connectionState) {
                                        BleConnectionState.CONNECTED -> connectedDeviceName ?: "Connected"
                                        BleConnectionState.CONNECTING -> "Connecting..."
                                        BleConnectionState.SCANNING -> "Scanning for sensors..."
                                        BleConnectionState.DISCONNECTED -> "No sensor connected"
                                    },
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = AuroraTextPrimary
                                )
                                Text(
                                    text = if (isDemoMode) "Virtual HRM Active" else "BLE GATT Service 0x180D",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isDemoMode) AuroraWarning else AuroraTextMuted
                                )
                            }
                        }

                        if (connectionState == BleConnectionState.CONNECTED) {
                            TextButton(onClick = onDisconnect) {
                                Text("DISCONNECT", color = AuroraError, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Scan / Refresh controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DISCOVERED MONITORS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = AuroraTextSecondary
                    )

                    if (connectionState == BleConnectionState.SCANNING) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                strokeWidth = 2.dp,
                                color = AuroraMint
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            TextButton(onClick = onStopScan) {
                                Text("STOP", color = AuroraWarning, fontSize = 11.sp)
                            }
                        }
                    } else {
                        TextButton(onClick = onStartScan) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.BluetoothSearching,
                                    contentDescription = null,
                                    tint = AuroraMint,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("SCAN BLE", color = AuroraMint, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Scanned devices list
                if (scannedDevices.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(AuroraSurfaceVariant.copy(alpha = 0.5f))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (connectionState == BleConnectionState.SCANNING)
                                "Searching for advertising BLE heart rate devices..."
                            else "No devices found. Tap 'SCAN BLE' or try the Virtual HR Sensor below.",
                            style = MaterialTheme.typography.bodySmall,
                            color = AuroraTextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(scannedDevices, key = { it.address }) { device ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(AuroraSurfaceCard)
                                    .clickable { onConnect(device.address) }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = device.name,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = AuroraTextPrimary
                                    )
                                    Text(
                                        text = "${device.address} • RSSI: ${device.rssi} dBm",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = AuroraTextSecondary
                                    )
                                }
                                Text(
                                    text = "CONNECT",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = AuroraMint
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Demo / Virtual HR Sensor Button for testing without physical peripheral
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(AuroraViolet.copy(alpha = 0.15f))
                        .border(1.dp, AuroraViolet.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .clickable { onStartVirtual() }
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Sensors,
                            contentDescription = null,
                            tint = AuroraViolet,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isDemoMode) "RESTART VIRTUAL HR SENSOR" else "START VIRTUAL HR SENSOR (DEMO)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = AuroraViolet
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("DONE", color = AuroraMint, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = AuroraSurface,
        shape = RoundedCornerShape(24.dp)
    )
}

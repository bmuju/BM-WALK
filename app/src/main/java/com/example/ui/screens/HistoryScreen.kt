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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.WalkEntity
import com.example.ui.MainViewModel
import com.example.ui.components.AuroraGlassCard
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
import com.example.util.GpsMetricsCalculator
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class HistoryFilter {
    ALL,
    THIS_WEEK,
    THIS_MONTH
}

@Composable
fun HistoryScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val allWalks by viewModel.allWalks.collectAsState()
    val unitSystem by viewModel.unitSystem.collectAsState()

    var filter by remember { mutableStateOf(HistoryFilter.ALL) }
    var selectedWalkForDetail by remember { mutableStateOf<WalkEntity?>(null) }
    var walkToDelete by remember { mutableStateOf<WalkEntity?>(null) }

    val filteredWalks = remember(allWalks, filter) {
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance()
        when (filter) {
            HistoryFilter.ALL -> allWalks
            HistoryFilter.THIS_WEEK -> {
                cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                val weekStart = cal.timeInMillis
                allWalks.filter { it.startTimeMillis >= weekStart }
            }
            HistoryFilter.THIS_MONTH -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                val monthStart = cal.timeInMillis
                allWalks.filter { it.startTimeMillis >= monthStart }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AuroraBackground)
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .testTag("history_screen")
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "WORKOUT HISTORY",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    ),
                    color = AuroraMint
                )
                Text(
                    text = "${allWalks.size} total walks recorded on-device",
                    style = MaterialTheme.typography.labelSmall,
                    color = AuroraTextSecondary
                )
            }
        }

        // Filter chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HistoryFilter.values().forEach { f ->
                val label = when (f) {
                    HistoryFilter.ALL -> "All Walks"
                    HistoryFilter.THIS_WEEK -> "This Week"
                    HistoryFilter.THIS_MONTH -> "This Month"
                }
                FilterChip(
                    selected = filter == f,
                    onClick = { filter = f },
                    label = { Text(label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AuroraMint,
                        selectedLabelColor = AuroraBackground,
                        containerColor = AuroraSurfaceCard,
                        labelColor = AuroraTextSecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = filter == f,
                        borderColor = AuroraBorderSubtle,
                        selectedBorderColor = AuroraMint
                    )
                )
            }
        }

        if (filteredWalks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 60.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                AuroraGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = AuroraBorder
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsWalk,
                            contentDescription = null,
                            tint = AuroraMint.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No walks found in this period",
                            style = MaterialTheme.typography.titleMedium,
                            color = AuroraTextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Completed walks and their GPS routes will be preserved privately here.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AuroraTextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(filteredWalks, key = { it.id }) { walk ->
                    val points = remember(walk.id) { walk.parsePoints() }
                    val dateFormatted = SimpleDateFormat("EEE, MMM d, yyyy • h:mm a", Locale.getDefault())
                        .format(Date(walk.startTimeMillis))

                    AuroraGlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedWalkForDetail = walk }
                            .testTag("walk_item_${walk.id}")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = walk.title,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = AuroraTextPrimary
                                    )
                                    Text(
                                        text = dateFormatted,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = AuroraTextSecondary
                                    )
                                }

                                IconButton(
                                    onClick = { walkToDelete = walk },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = AuroraError.copy(alpha = 0.7f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Route Canvas preview and stats row side-by-side
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RoutePreviewCanvas(
                                    points = points,
                                    modifier = Modifier
                                        .size(90.dp)
                                        .clip(RoundedCornerShape(12.dp)),
                                    backgroundColor = AuroraSurfaceVariant
                                )

                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Distance:", style = MaterialTheme.typography.labelSmall, color = AuroraTextSecondary)
                                        Text(
                                            GpsMetricsCalculator.formatDistance(walk.distanceMeters, unitSystem),
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = AuroraMint
                                        )
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Time:", style = MaterialTheme.typography.labelSmall, color = AuroraTextSecondary)
                                        Text(
                                            GpsMetricsCalculator.formatDuration(walk.durationSeconds),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = AuroraTextPrimary
                                        )
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Pace:", style = MaterialTheme.typography.labelSmall, color = AuroraTextSecondary)
                                        Text(
                                            GpsMetricsCalculator.formatPace(walk.avgPaceSecondsPerKm, unitSystem),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = AuroraTextPrimary
                                        )
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Calories:", style = MaterialTheme.typography.labelSmall, color = AuroraTextSecondary)
                                        Text(
                                            "${walk.calories} kcal",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = AuroraViolet
                                        )
                                    }
                                    if (walk.avgHeartRateBpm > 0) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Heart Rate:", style = MaterialTheme.typography.labelSmall, color = AuroraTextSecondary)
                                            Text(
                                                "${walk.avgHeartRateBpm} bpm",
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                color = AuroraMint
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }

    // Full Walk Details Dialog
    selectedWalkForDetail?.let { walk ->
        val points = remember(walk.id) { walk.parsePoints() }
        val dateFormatted = SimpleDateFormat("EEEE, MMMM d, yyyy • h:mm a", Locale.getDefault())
            .format(Date(walk.startTimeMillis))

        AlertDialog(
            onDismissRequest = { selectedWalkForDetail = null },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = walk.title,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = AuroraMint
                    )
                    IconButton(onClick = { selectedWalkForDetail = null }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = AuroraTextSecondary
                        )
                    }
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = dateFormatted,
                        style = MaterialTheme.typography.labelSmall,
                        color = AuroraTextSecondary
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    RoutePreviewCanvas(
                        points = points,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        backgroundColor = AuroraSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Distance", style = MaterialTheme.typography.labelSmall, color = AuroraTextSecondary)
                            Text(
                                GpsMetricsCalculator.formatDistance(walk.distanceMeters, unitSystem),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = AuroraMint
                            )
                        }
                        Column {
                            Text("Duration", style = MaterialTheme.typography.labelSmall, color = AuroraTextSecondary)
                            Text(
                                GpsMetricsCalculator.formatDuration(walk.durationSeconds),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = AuroraTextPrimary
                            )
                        }
                        Column {
                            Text("Avg Pace", style = MaterialTheme.typography.labelSmall, color = AuroraTextSecondary)
                            Text(
                                GpsMetricsCalculator.formatPace(walk.avgPaceSecondsPerKm, unitSystem),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = AuroraTextPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Calories", style = MaterialTheme.typography.labelSmall, color = AuroraTextSecondary)
                            Text("${walk.calories} kcal", style = MaterialTheme.typography.bodyLarge, color = AuroraViolet)
                        }
                        Column {
                            Text("Steps", style = MaterialTheme.typography.labelSmall, color = AuroraTextSecondary)
                            Text("${walk.steps}", style = MaterialTheme.typography.bodyLarge, color = AuroraTextPrimary)
                        }
                        Column {
                            Text("GPS Fixes", style = MaterialTheme.typography.labelSmall, color = AuroraTextSecondary)
                            Text("${points.size} pts", style = MaterialTheme.typography.bodyLarge, color = AuroraTextSecondary)
                        }
                    }

                    if (walk.avgHeartRateBpm > 0) {
                        Spacer(modifier = Modifier.height(10.dp))
                        val zone = com.example.data.model.HeartRateZone.fromBpm(walk.avgHeartRateBpm)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Avg Heart Rate", style = MaterialTheme.typography.labelSmall, color = AuroraTextSecondary)
                                Text("${walk.avgHeartRateBpm} BPM", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), color = AuroraMint)
                            }
                            Column {
                                Text("Peak Heart Rate", style = MaterialTheme.typography.labelSmall, color = AuroraTextSecondary)
                                Text("${walk.maxHeartRateBpm} BPM", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), color = AuroraError)
                            }
                            Column {
                                Text("Dominant Zone", style = MaterialTheme.typography.labelSmall, color = AuroraTextSecondary)
                                Text(zone.label, style = MaterialTheme.typography.bodyLarge, color = AuroraViolet)
                            }
                        }
                    }

                    if (walk.notes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text("Notes:", style = MaterialTheme.typography.labelSmall, color = AuroraTextSecondary)
                        Text(walk.notes, style = MaterialTheme.typography.bodyMedium, color = AuroraTextPrimary)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedWalkForDetail = null }) {
                    Text("CLOSE", color = AuroraMint, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = AuroraSurface,
            shape = RoundedCornerShape(24.dp)
        )
    }

    // Delete Confirmation Dialog
    walkToDelete?.let { walk ->
        AlertDialog(
            onDismissRequest = { walkToDelete = null },
            title = {
                Text(
                    text = "Delete Walk?",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = AuroraTextPrimary
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete '${walk.title}'? This action cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AuroraTextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteWalk(walk)
                        walkToDelete = null
                        if (selectedWalkForDetail?.id == walk.id) {
                            selectedWalkForDetail = null
                        }
                    }
                ) {
                    Text("DELETE", color = AuroraError, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { walkToDelete = null }) {
                    Text("CANCEL", color = AuroraTextSecondary)
                }
            },
            containerColor = AuroraSurface,
            shape = RoundedCornerShape(24.dp)
        )
    }
}

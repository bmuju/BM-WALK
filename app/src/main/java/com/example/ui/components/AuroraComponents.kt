package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AuroraBackground
import com.example.ui.theme.AuroraBorder
import com.example.ui.theme.AuroraBorderSubtle
import com.example.ui.theme.AuroraError
import com.example.ui.theme.AuroraGradient
import com.example.ui.theme.AuroraMint
import com.example.ui.theme.AuroraSurface
import com.example.ui.theme.AuroraSurfaceCard
import com.example.ui.theme.AuroraSurfaceTranslucent
import com.example.ui.theme.AuroraSurfaceVariant
import com.example.ui.theme.AuroraTextMuted
import com.example.ui.theme.AuroraTextPrimary
import com.example.ui.theme.AuroraTextSecondary
import com.example.ui.theme.AuroraViolet
import com.example.ui.theme.AuroraWarning

@Composable
fun AuroraGlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    borderColor: Color = AuroraBorder,
    backgroundColor: Color = AuroraSurfaceTranslucent,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier
            .border(
                border = BorderStroke(1.dp, borderColor),
                shape = RoundedCornerShape(cornerRadius)
            ),
        shape = RoundedCornerShape(cornerRadius),
        color = backgroundColor,
        tonalElevation = 2.dp
    ) {
        content()
    }
}

@Composable
fun AuroraStatCard(
    title: String,
    value: String,
    unit: String = "",
    icon: ImageVector? = null,
    accentColor: Color = AuroraMint,
    modifier: Modifier = Modifier
) {
    AuroraGlassCard(
        modifier = modifier,
        borderColor = AuroraBorderSubtle,
        backgroundColor = AuroraSurfaceCard
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = AuroraTextSecondary,
                    letterSpacing = 1.sp
                )
                if (icon != null) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = AuroraTextPrimary
                )
                if (unit.isNotBlank()) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = unit,
                        style = MaterialTheme.typography.labelSmall,
                        color = AuroraTextSecondary,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun GpsAccuracyBadge(
    accuracyMeters: Float,
    isAcquiring: Boolean,
    modifier: Modifier = Modifier
) {
    val (statusColor, label) = when {
        isAcquiring -> Pair(AuroraWarning, "Acquiring GPS...")
        accuracyMeters <= 0f -> Pair(AuroraTextMuted, "Searching...")
        accuracyMeters <= 12f -> Pair(AuroraMint, "GPS Strong (±${accuracyMeters.toInt()}m)")
        accuracyMeters <= 25f -> Pair(AuroraMint.copy(alpha = 0.8f), "GPS Good (±${accuracyMeters.toInt()}m)")
        accuracyMeters <= 40f -> Pair(AuroraWarning, "GPS Fair (±${accuracyMeters.toInt()}m)")
        else -> Pair(AuroraError, "GPS Weak (±${accuracyMeters.toInt()}m)")
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(AuroraSurface.copy(alpha = 0.85f))
            .border(1.dp, statusColor.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(statusColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = AuroraTextPrimary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun AuroraGlowButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    isPulsing: Boolean = false,
    testTag: String = "primary_button"
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isPulsing) 1.03f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        modifier = modifier
            .scale(pulseScale)
            .clip(RoundedCornerShape(28.dp))
            .background(AuroraGradient)
            .clickable(onClick = onClick)
            .testTag(testTag)
            .padding(vertical = 18.dp, horizontal = 28.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = AuroraBackground,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
            }
            Text(
                text = text.uppercase(),
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    letterSpacing = 1.2.sp
                ),
                color = AuroraBackground
            )
        }
    }
}

@Composable
fun DailyGoalArc(
    currentMeters: Double,
    goalMeters: Double,
    currentSteps: Int,
    stepsGoal: Int,
    modifier: Modifier = Modifier
) {
    val distanceProgress = (currentMeters / goalMeters.coerceAtLeast(100.0)).toFloat().coerceIn(0f, 1.25f)
    val stepsProgress = (currentSteps.toFloat() / stepsGoal.coerceAtLeast(1000)).coerceIn(0f, 1.25f)

    Box(
        modifier = modifier
            .size(190.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 12.dp.toPx()
            val innerStrokeWidth = 8.dp.toPx()
            val center = Offset(size.width / 2f, size.height / 2f)
            val outerRadius = (size.width / 2f) - (strokeWidth / 2f) - 4.dp.toPx()
            val innerRadius = outerRadius - strokeWidth - 6.dp.toPx()

            // Outer Track (Distance)
            drawCircle(
                color = AuroraSurfaceVariant,
                radius = outerRadius,
                center = center,
                style = Stroke(width = strokeWidth)
            )

            // Inner Track (Steps)
            drawCircle(
                color = AuroraSurfaceVariant.copy(alpha = 0.5f),
                radius = innerRadius,
                center = center,
                style = Stroke(width = innerStrokeWidth)
            )

            // Outer Progress Arc (Distance: Mint)
            drawArc(
                brush = Brush.sweepGradient(
                    listOf(AuroraMint, AuroraViolet, AuroraMint)
                ),
                startAngle = -90f,
                sweepAngle = distanceProgress * 360f,
                useCenter = false,
                topLeft = Offset(center.x - outerRadius, center.y - outerRadius),
                size = Size(outerRadius * 2, outerRadius * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Inner Progress Arc (Steps: Violet)
            drawArc(
                color = AuroraViolet,
                startAngle = -90f,
                sweepAngle = stepsProgress * 360f,
                useCenter = false,
                topLeft = Offset(center.x - innerRadius, center.y - innerRadius),
                size = Size(innerRadius * 2, innerRadius * 2),
                style = Stroke(width = innerStrokeWidth, cap = StrokeCap.Round)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val km = (currentMeters / 1000.0)
            Text(
                text = String.format("%.2f", km),
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 32.sp
                ),
                color = AuroraMint
            )
            Text(
                text = "OF ${(goalMeters / 1000.0).toInt()} KM",
                style = MaterialTheme.typography.labelSmall,
                color = AuroraTextSecondary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "$currentSteps steps",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = AuroraViolet
            )
        }
    }
}

@Composable
fun WeeklyDistanceBarChart(
    dailyDistancesKm: List<Pair<String, Double>>,
    dailyGoalKm: Double,
    modifier: Modifier = Modifier
) {
    val maxDist = (dailyDistancesKm.maxOfOrNull { it.second } ?: 1.0).coerceAtLeast(dailyGoalKm * 1.1)

    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
        ) {
            val width = size.width
            val height = size.height
            val barCount = dailyDistancesKm.size
            if (barCount == 0) return@Canvas

            val barSpacing = width / barCount
            val barWidth = barSpacing * 0.45f

            // Draw goal line
            val goalY = height - ((dailyGoalKm / maxDist) * height).toFloat()
            drawLine(
                color = AuroraBorder,
                start = Offset(0f, goalY),
                end = Offset(width, goalY),
                strokeWidth = 1.5.dp.toPx(),
                cap = StrokeCap.Round
            )

            dailyDistancesKm.forEachIndexed { index, pair ->
                val dist = pair.second
                val barHeight = ((dist / maxDist) * height).toFloat().coerceAtLeast(4f)
                val barX = (index * barSpacing) + (barSpacing - barWidth) / 2f
                val barY = height - barHeight

                val isMetGoal = dist >= dailyGoalKm
                val barBrush = if (isMetGoal) {
                    Brush.verticalGradient(listOf(AuroraMint, AuroraViolet))
                } else {
                    Brush.verticalGradient(
                        listOf(AuroraMint.copy(alpha = 0.6f), AuroraSurfaceVariant)
                    )
                }

                drawRoundRect(
                    brush = barBrush,
                    topLeft = Offset(barX, barY),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            dailyDistancesKm.forEach { (day, _) ->
                Text(
                    text = day,
                    style = MaterialTheme.typography.labelSmall,
                    color = AuroraTextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.data.model.GeoPoint
import com.example.ui.theme.AuroraMint
import com.example.ui.theme.AuroraSurface
import com.example.ui.theme.AuroraViolet

@Composable
fun RoutePreviewCanvas(
    points: List<GeoPoint>,
    modifier: Modifier = Modifier,
    backgroundColor: Color = AuroraSurface
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .padding(8.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Draw subtle coordinate grid background
            val gridSpacing = 28.dp.toPx()
            var x = 0f
            while (x < width) {
                drawLine(
                    color = Color(0x159BB7C8),
                    start = Offset(x, 0f),
                    end = Offset(x, height),
                    strokeWidth = 1f
                )
                x += gridSpacing
            }
            var y = 0f
            while (y < height) {
                drawLine(
                    color = Color(0x159BB7C8),
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1f
                )
                y += gridSpacing
            }

            if (points.size < 2) {
                // If 0 or 1 point, draw a subtle pulsing locator radar
                drawCircle(
                    color = AuroraMint.copy(alpha = 0.2f),
                    radius = 24.dp.toPx(),
                    center = Offset(width / 2f, height / 2f)
                )
                drawCircle(
                    color = AuroraMint,
                    radius = 5.dp.toPx(),
                    center = Offset(width / 2f, height / 2f)
                )
                return@Canvas
            }

            val lats = points.map { it.latitude }
            val lons = points.map { it.longitude }
            val minLat = lats.minOrNull() ?: 0.0
            val maxLat = lats.maxOrNull() ?: 0.0
            val minLon = lons.minOrNull() ?: 0.0
            val maxLon = lons.maxOrNull() ?: 0.0

            val latSpan = (maxLat - minLat).coerceAtLeast(0.0001)
            val lonSpan = (maxLon - minLon).coerceAtLeast(0.0001)

            val paddingPx = 20.dp.toPx()
            val drawWidth = width - (paddingPx * 2)
            val drawHeight = height - (paddingPx * 2)

            // Scale keeping aspect ratio
            val scale = (drawWidth / lonSpan).coerceAtMost(drawHeight / latSpan)
            val scaledWidth = (lonSpan * scale).toFloat()
            val scaledHeight = (latSpan * scale).toFloat()

            val offsetX = paddingPx + (drawWidth - scaledWidth) / 2f
            val offsetY = paddingPx + (drawHeight - scaledHeight) / 2f

            fun toCanvasOffset(p: GeoPoint): Offset {
                val nx = ((p.longitude - minLon) * scale).toFloat()
                // Invert latitude because canvas Y increases downwards
                val ny = ((maxLat - p.latitude) * scale).toFloat()
                return Offset(offsetX + nx, offsetY + ny)
            }

            val path = Path()
            val first = toCanvasOffset(points.first())
            path.moveTo(first.x, first.y)

            for (i in 1 until points.size) {
                val pt = toCanvasOffset(points[i])
                path.lineTo(pt.x, pt.y)
            }

            // Glow path underneath
            drawPath(
                path = path,
                color = AuroraViolet.copy(alpha = 0.4f),
                style = Stroke(
                    width = 8.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            // Sharp mint path
            drawPath(
                path = path,
                color = AuroraMint,
                style = Stroke(
                    width = 3.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            // Start marker (Violet)
            val startOffset = toCanvasOffset(points.first())
            drawCircle(
                color = AuroraViolet,
                radius = 5.dp.toPx(),
                center = startOffset
            )
            drawCircle(
                color = Color.White,
                radius = 2.dp.toPx(),
                center = startOffset
            )

            // End marker (Mint with outer ring)
            val endOffset = toCanvasOffset(points.last())
            drawCircle(
                color = AuroraMint.copy(alpha = 0.3f),
                radius = 9.dp.toPx(),
                center = endOffset
            )
            drawCircle(
                color = AuroraMint,
                radius = 5.dp.toPx(),
                center = endOffset
            )
        }
    }
}

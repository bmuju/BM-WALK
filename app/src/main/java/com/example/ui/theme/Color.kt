package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val AuroraBackground = Color(0xFF081B2B)
val AuroraSurface = Color(0xFF102D42)
val AuroraSurfaceVariant = Color(0xFF163852)
val AuroraSurfaceTranslucent = Color(0xE6102D42)
val AuroraSurfaceCard = Color(0xFF123249)

val AuroraBorder = Color(0x3352F5D1)
val AuroraBorderSubtle = Color(0x269BB7C8)

val AuroraMint = Color(0xFF52F5D1)
val AuroraMintGlow = Color(0x3352F5D1)
val AuroraViolet = Color(0xFF8A78FF)
val AuroraVioletGlow = Color(0x338A78FF)

val AuroraTextPrimary = Color(0xFFF2FAFF)
val AuroraTextSecondary = Color(0xFF9BB7C8)
val AuroraTextMuted = Color(0xFF6B8B9E)

val AuroraWarning = Color(0xFFFFB74D)
val AuroraSuccess = Color(0xFF52F5D1)
val AuroraError = Color(0xFFFF5370)

val AuroraGradient = Brush.horizontalGradient(
    colors = listOf(AuroraMint, AuroraViolet)
)

val AuroraCardGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF14364F), Color(0xFF0E273A))
)

val AuroraGlowGradient = Brush.radialGradient(
    colors = listOf(Color(0x4052F5D1), Color(0x00081B2B))
)

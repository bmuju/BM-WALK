package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.AuroraBackground
import com.example.ui.theme.AuroraMint
import com.example.ui.theme.AuroraTextPrimary
import com.example.ui.theme.AuroraTextSecondary
import com.example.ui.theme.AuroraViolet
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onContinue: () -> Unit
) {
    val scale = remember { Animatable(0.85f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        alpha.animateTo(1f, animationSpec = tween(800, easing = FastOutSlowInEasing))
        scale.animateTo(1f, animationSpec = tween(800, easing = FastOutSlowInEasing))
        delay(1400)
        onContinue()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AuroraBackground)
            .clickable { onContinue() }
            .testTag("splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // BM × WALK Logo
            Box(
                modifier = Modifier
                    .size(170.dp)
                    .scale(scale.value)
                    .clip(RoundedCornerShape(32.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.bm_walk_logo),
                    contentDescription = "BM × WALK Logo",
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "BM × WALK",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                ),
                color = AuroraMint
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "MOVE BEYOND ORDINARY",
                style = MaterialTheme.typography.labelLarge.copy(
                    letterSpacing = 3.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = AuroraViolet
            )

            Spacer(modifier = Modifier.height(36.dp))

            Text(
                text = "Premium Outdoor Walking Companion",
                style = MaterialTheme.typography.bodyMedium,
                color = AuroraTextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(80.dp))

            Text(
                text = "Made by BM-UJWAL",
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 1.sp
                ),
                color = AuroraTextSecondary.copy(alpha = 0.6f)
            )
        }
    }
}

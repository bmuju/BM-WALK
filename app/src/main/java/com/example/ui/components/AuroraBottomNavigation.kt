package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.Screen
import com.example.ui.theme.AuroraMint
import com.example.ui.theme.AuroraSurface
import com.example.ui.theme.AuroraTextMuted
import com.example.ui.theme.AuroraTextSecondary

enum class NavTab(val title: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    WORKOUT("Track", Icons.Default.DirectionsWalk),
    HISTORY("History", Icons.Default.History),
    STATS("Stats", Icons.Default.BarChart),
    SETTINGS("Settings", Icons.Default.Settings)
}

@Composable
fun AuroraBottomNavigation(
    currentScreen: Screen,
    onTabSelected: (NavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeTab = when (currentScreen) {
        Screen.Home -> NavTab.HOME
        Screen.LiveWorkout -> NavTab.WORKOUT
        Screen.History -> NavTab.HISTORY
        Screen.Statistics -> NavTab.STATS
        Screen.Settings -> NavTab.SETTINGS
        else -> NavTab.HOME
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(AuroraSurface.copy(alpha = 0.95f))
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavTab.values().forEach { tab ->
                val isSelected = tab == activeTab
                val tint by animateColorAsState(
                    targetValue = if (isSelected) AuroraMint else AuroraTextMuted,
                    label = "tab_tint"
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onTabSelected(tab) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("nav_tab_${tab.name.lowercase()}")
                ) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.title,
                        tint = tint,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = tab.title,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        ),
                        color = if (isSelected) AuroraMint else AuroraTextSecondary
                    )
                }
            }
        }
    }
}

package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.ui.MainViewModel
import com.example.ui.Screen
import com.example.ui.components.AuroraBottomNavigation
import com.example.ui.components.NavTab
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LiveWorkoutScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.StatisticsScreen
import com.example.ui.screens.WorkoutSummaryScreen
import com.example.ui.theme.AuroraBackground
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                MainAppContent(
                    viewModel = viewModel,
                    hasLocationPermission = { checkLocationPermissions() }
                )
            }
        }
    }

    private fun checkLocationPermissions(): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseLocation = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fineLocation || coarseLocation
    }
}

@Composable
fun MainAppContent(
    viewModel: MainViewModel,
    hasLocationPermission: () -> Boolean
) {
    val currentScreen by viewModel.currentScreen.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            viewModel.startWorkout()
        }
    }

    fun startWalkingWithPermissions() {
        val permissionsToRequest = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissionsToRequest.add(Manifest.permission.BLUETOOTH_SCAN)
            permissionsToRequest.add(Manifest.permission.BLUETOOTH_CONNECT)
        }

        if (hasLocationPermission()) {
            viewModel.startWorkout()
        } else {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    // Handle Android system back gesture gracefully
    when (currentScreen) {
        Screen.Splash -> {}
        Screen.Home -> {}
        Screen.LiveWorkout -> {
            BackHandler {
                // Return to home dashboard while keeping tracking active in background
                viewModel.navigateTo(Screen.Home)
            }
        }
        is Screen.WorkoutSummary -> {
            BackHandler {
                viewModel.discardWorkout()
            }
        }
        Screen.History, Screen.Statistics, Screen.Settings -> {
            BackHandler {
                viewModel.navigateTo(Screen.Home)
            }
        }
    }

    if (currentScreen is Screen.Splash) {
        SplashScreen(
            onContinue = { viewModel.navigateTo(Screen.Home) }
        )
    } else {
        val showBottomBar = currentScreen !is Screen.LiveWorkout && currentScreen !is Screen.WorkoutSummary

        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    AuroraBottomNavigation(
                        currentScreen = currentScreen,
                        onTabSelected = { tab ->
                            when (tab) {
                                NavTab.HOME -> viewModel.navigateTo(Screen.Home)
                                NavTab.WORKOUT -> startWalkingWithPermissions()
                                NavTab.HISTORY -> viewModel.navigateTo(Screen.History)
                                NavTab.STATS -> viewModel.navigateTo(Screen.Statistics)
                                NavTab.SETTINGS -> viewModel.navigateTo(Screen.Settings)
                            }
                        }
                    )
                }
            },
            containerColor = AuroraBackground,
            contentWindowInsets = WindowInsets.safeDrawing
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AuroraBackground)
                    .padding(paddingValues)
            ) {
                when (val screen = currentScreen) {
                    Screen.Splash -> {}
                    Screen.Home -> {
                        HomeScreen(
                            viewModel = viewModel,
                            onStartWorkout = { startWalkingWithPermissions() }
                        )
                    }
                    Screen.LiveWorkout -> {
                        LiveWorkoutScreen(
                            viewModel = viewModel
                        )
                    }
                    is Screen.WorkoutSummary -> {
                        WorkoutSummaryScreen(
                            walk = screen.walkEntity,
                            viewModel = viewModel
                        )
                    }
                    Screen.History -> {
                        HistoryScreen(
                            viewModel = viewModel
                        )
                    }
                    Screen.Statistics -> {
                        StatisticsScreen(
                            viewModel = viewModel
                        )
                    }
                    Screen.Settings -> {
                        SettingsScreen(
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
    }
}

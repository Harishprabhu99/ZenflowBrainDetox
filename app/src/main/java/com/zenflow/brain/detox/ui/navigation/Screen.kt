package com.zenflow.brain.detox.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Apps
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    data object Auth : Screen("auth", "Authentication")
    data object Home : Screen("home", "Home", Icons.Default.Home)
    data object AppSelection : Screen("apps", "Apps", Icons.Default.Apps)
    data object Dashboard : Screen("dashboard", "Progress", Icons.Default.BarChart)
    data object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    data object TimerSettings : Screen("timer/{packageName}", "Timer") {
        fun createRoute(packageName: String) = "timer/$packageName"
    }
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.AppSelection,
    Screen.Dashboard,
    Screen.Settings,
)

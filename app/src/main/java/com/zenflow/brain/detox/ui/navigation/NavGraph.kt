package com.zenflow.brain.detox.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zenflow.brain.detox.MainViewModel
import com.zenflow.brain.detox.di.appViewModel
import com.zenflow.brain.detox.ui.screens.apps.AppSelectionScreen
import com.zenflow.brain.detox.ui.screens.auth.AuthScreen
import com.zenflow.brain.detox.ui.screens.dashboard.DashboardScreen
import com.zenflow.brain.detox.ui.screens.home.HomeScreen
import com.zenflow.brain.detox.ui.screens.settings.SettingsScreen
import com.zenflow.brain.detox.ui.screens.timer.TimerSettingsScreen

@Composable
fun BrainDetoxNavHost(
    mainViewModel: MainViewModel = appViewModel { MainViewModel(it) }
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in bottomNavItems.map { it.route }
    val startDestination by mainViewModel.startDestination.collectAsStateWithLifecycle()

    if (startDestination == null) return // Or show splash

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            selected = currentRoute == screen.route,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                screen.icon?.let { Icon(it, contentDescription = screen.title) }
                            },
                            label = { Text(screen.title) },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination!!,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Screen.Auth.route) {
                AuthScreen(
                    onAuthenticated = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Auth.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToApps = { navController.navigate(Screen.AppSelection.route) },
                    onNavigateToTimer = { pkg ->
                        navController.navigate(Screen.TimerSettings.createRoute(pkg))
                    },
                )
            }
            composable(Screen.AppSelection.route) {
                AppSelectionScreen(
                    onNavigateToTimer = { pkg ->
                        navController.navigate(Screen.TimerSettings.createRoute(pkg))
                    },
                )
            }
            composable(Screen.Dashboard.route) {
                DashboardScreen()
            }
            composable(Screen.Settings.route) {
                SettingsScreen()
            }
            composable(
                route = Screen.TimerSettings.route,
                arguments = listOf(navArgument("packageName") { type = NavType.StringType }),
            ) { backStackEntry ->
                val packageName = backStackEntry.arguments?.getString("packageName") ?: return@composable
                TimerSettingsScreen(
                    packageName = packageName,
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}

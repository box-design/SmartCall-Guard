package com.smartcall.guard.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.smartcall.guard.ui.screen.BlacklistManageScreen
import com.smartcall.guard.ui.screen.BlockStatsScreen
import com.smartcall.guard.ui.screen.HomeScreen
import com.smartcall.guard.ui.screen.RulesScreen
import com.smartcall.guard.ui.screen.LogsScreen
import com.smartcall.guard.ui.screen.SettingsScreen
import com.smartcall.guard.ui.screen.WhitelistManageScreen

sealed class Screen(val route: String, val title: String, val selectedIcon: ImageVector, val unselectedIcon: ImageVector) {
    object Home : Screen("home", "首页", Icons.Filled.Home, Icons.Outlined.Home)
    object Rules : Screen("rules", "规则", Icons.Filled.List, Icons.Outlined.List)
    object Logs : Screen("logs", "记录", Icons.Filled.Notifications, Icons.Outlined.Notifications)
    object Settings : Screen("settings", "设置", Icons.Filled.Settings, Icons.Outlined.Settings)
}

val bottomNavItems = listOf(Screen.Home, Screen.Rules, Screen.Logs, Screen.Settings)

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = if (currentDestination?.hierarchy?.any { it.route == screen.route } == true)
                                    screen.selectedIcon else screen.unselectedIcon,
                                contentDescription = screen.title
                            )
                        },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) { HomeScreen(navController = navController) }
            composable(Screen.Rules.route) { RulesScreen() }
            composable(Screen.Logs.route) { LogsScreen() }
            composable(Screen.Settings.route) { SettingsScreen(navController = navController) }
            composable("blacklist_manage") {
                BlacklistManageScreen(onBack = { navController.popBackStack() })
            }
            composable("whitelist_manage") {
                WhitelistManageScreen(onBack = { navController.popBackStack() })
            }
            composable("block_stats") {
                BlockStatsScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}

package com.example.smarthome

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DevicesOther
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.smarthome.screens.devices.DevicesScreen
import com.example.smarthome.screens.home.HomeScreen
import com.example.smarthome.screens.profile.ProfileScreen
import com.example.smarthome.screens.scenarios.ScenarioEditorScreen
import com.example.smarthome.screens.scenarios.ScenariosScreen
import com.example.smarthome.ui.theme.UjinCyan
import com.example.smarthome.ui.theme.UjinSurface

private data class BottomTab(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

private val bottomTabs = listOf(
    BottomTab("home", "Главная", Icons.Filled.Home),
    BottomTab("devices", "Устройства", Icons.Filled.DevicesOther),
    BottomTab("scenarios", "Сценарии", Icons.Filled.AutoAwesome),
    BottomTab("profile", "Профиль", Icons.Filled.Person),
)

@Composable
fun MainScreen(
    onLogout: () -> Unit = {},
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute == null || bottomTabs.any { it.route == currentRoute }

    val navigateToTab: (String) -> Unit = { route ->
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = UjinSurface,
                    tonalElevation = 0.dp,
                ) {
                    bottomTabs.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.route,
                            onClick = { navigateToTab(tab.route) },
                            icon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.label,
                                )
                            },
                            label = {
                                Text(
                                    text = tab.label,
                                    fontSize = 11.sp,
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = UjinSurface,
                                selectedTextColor = UjinCyan,
                                indicatorColor = UjinCyan,
                                unselectedIconColor = Color(0xFF8B949E),
                                unselectedTextColor = Color(0xFF8B949E),
                            ),
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(padding),
        ) {
            composable("home") {
                HomeScreen(
                    onScenariosClick = { navigateToTab("scenarios") },
                )
            }
            composable("devices") { DevicesScreen() }
            composable("scenarios") {
                ScenariosScreen(
                    onAddScenario = { navController.navigate("scenario_editor") },
                    onOpenScenario = { id -> navController.navigate("scenario_editor?scenarioId=$id") },
                )
            }
            composable(
                route = "scenario_editor?scenarioId={scenarioId}",
                arguments = listOf(navArgument("scenarioId") { nullable = true; defaultValue = null }),
            ) { entry ->
                ScenarioEditorScreen(
                    scenarioId = entry.arguments?.getString("scenarioId"),
                    onBack = { navController.popBackStack() },
                )
            }
            composable("profile") {
                ProfileScreen(onLogout = onLogout)
            }
        }
    }
}

package com.example.smarthome.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.smarthome.screens.home.HomeScreen

@Composable
fun NavGraph() {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = "home") {
        composable("home") {
            HomeScreen(
                onDeviceClick = { id -> nav.navigate("device/$id") },
                onScenariosClick = { nav.navigate("scenarios") },
            )
        }
    }
}
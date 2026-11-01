package com.example.smarthome.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.smarthome.MainScreen
import com.example.smarthome.screens.auth.LoginScreen
import com.example.smarthome.screens.auth.OtpScreen
import com.example.smarthome.screens.auth.RegisterScreen
import com.example.smarthome.screens.auth.SplashScreen

@Composable
fun NavGraph() {
    val nav = rememberNavController()

    NavHost(navController = nav, startDestination = "splash") {
        composable("splash") {
            SplashScreen(
                onLoginClick = { nav.navigate("login") },
                onRegisterClick = { nav.navigate("register") },
            )
        }

        composable("register") {
            RegisterScreen(
                onBackClick = { nav.popBackStack() },
                onCodeSent = { nav.navigate("otp") },
            )
        }

        composable("login") {
            LoginScreen(
                onBackClick = { nav.popBackStack() },
                onCodeSent = { nav.navigate("otp") },
            )
        }

        composable("otp") {
            OtpScreen(
                onBackClick = { nav.popBackStack() },
                onLoginSuccess = {
                    nav.navigate("main") {
                        popUpTo("splash") { inclusive = true }
                    }
                },
            )
        }

        composable("main") {
            MainScreen(
                onLogout = {
                    nav.navigate("login") {
                        popUpTo("main") { inclusive = true }
                    }
                },
            )
        }
    }
}
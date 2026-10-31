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
    
    // Стартовая точка теперь "splash"
    NavHost(navController = nav, startDestination = "splash") {
        
        // 1. Стартовый экран
        composable("splash") {
            SplashScreen(
                onLoginClick = { nav.navigate("login") },
                onRegisterClick = { nav.navigate("register") }
            )
        }
        
        // 2. Экран Регистрации
        composable("register") {
            RegisterScreen(
                onBackClick = { nav.popBackStack() } // Возврат на старт
            )
        }

        // 3. Экран Авторизации (номер телефона)
        composable("login") {
            LoginScreen(
                onBackClick = { nav.popBackStack() }, // Возврат на старт
                onCodeSent = { nav.navigate("otp") } // Переход на ввод кода
            )
        }

        // 4. Экран Ввода Кода (OTP)
        composable("otp") {
            OtpScreen(
                onBackClick = { nav.popBackStack() }, // Возврат на ввод номера
                onLoginSuccess = {
                    // При успехе переходим на главный экран и очищаем стек
                    nav.navigate("main") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        // 5. Главный экран с нижней навигацией
        composable("main") {
            MainScreen()
        }
    }
}
package com.example.smarthome.screens.scenarios

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.runtime.Composable
import com.example.smarthome.ui.components.EmptyTabScreen

@Composable
fun ScenariosScreen() {
    EmptyTabScreen(
        title = "Сценарии",
        subtitle = "Здесь будут быстрые и популярные сценарии",
        icon = Icons.Filled.AutoAwesome,
    )
}

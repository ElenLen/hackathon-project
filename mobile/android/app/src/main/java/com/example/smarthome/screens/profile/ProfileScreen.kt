package com.example.smarthome.screens.profile

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import com.example.smarthome.ui.components.EmptyTabScreen

@Composable
fun ProfileScreen() {
    EmptyTabScreen(
        title = "Профиль",
        subtitle = "Здесь будут данные профиля и настройки",
        icon = Icons.Filled.Person,
    )
}

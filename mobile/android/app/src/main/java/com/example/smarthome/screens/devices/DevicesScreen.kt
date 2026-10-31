package com.example.smarthome.screens.devices

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DevicesOther
import androidx.compose.runtime.Composable
import com.example.smarthome.ui.components.EmptyTabScreen

@Composable
fun DevicesScreen() {
    EmptyTabScreen(
        title = "Устройства",
        subtitle = "Здесь появится список всех устройств квартиры",
        icon = Icons.Filled.DevicesOther,
    )
}

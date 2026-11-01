package com.example.smarthome.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.DevicesOther
import androidx.compose.material.icons.filled.DoorFront
import androidx.compose.material.icons.filled.ElectricMeter
import androidx.compose.material.icons.filled.Elevator
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.ui.graphics.vector.ImageVector

fun familyIcon(family: String): ImageVector = when (family.lowercase()) {
    "leak" -> Icons.Filled.WaterDrop
    "barrier" -> Icons.Filled.DoorFront
    "thermostat" -> Icons.Filled.Thermostat
    "counter" -> Icons.Filled.ElectricMeter
    "light" -> Icons.Filled.Lightbulb
    "ventilation" -> Icons.Filled.Air
    "fire" -> Icons.Filled.LocalFireDepartment
    "elevator" -> Icons.Filled.Elevator
    "hub" -> Icons.Filled.Hub
    else -> Icons.Filled.DevicesOther
}

package com.example.smarthome.screens.devices

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Doorbell
import androidx.compose.material.icons.filled.Garage
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smarthome.data.dto.DeviceOut
import com.example.smarthome.ui.theme.UjinCyan
import com.example.smarthome.ui.theme.UjinGreen
import com.example.smarthome.ui.theme.UjinRed
import com.example.smarthome.ui.theme.UjinSurface
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull

@Composable
fun DevicesScreen(
    viewModel: DevicesViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.message) {
        state.message?.let { message ->
            showAddDialog = false
            snackbarHostState.showSnackbar(message)
            viewModel.consumeMessage()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.padding(start = 25.dp, end = 25.dp, top = 24.dp),
            ) {
                Text(
                    text = "Устройства",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = devicesCountText(state.devices.size),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.height(20.dp))

            when {
                state.loading && state.devices.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = UjinCyan)
                    }
                }

                state.error != null && state.devices.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = state.error ?: "Ошибка загрузки",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            TextButton(onClick = viewModel::loadDevices) {
                                Text("Повторить", color = UjinCyan)
                            }
                        }
                    }
                }

                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(
                            start = 25.dp,
                            end = 25.dp,
                            bottom = 16.dp,
                        ),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(state.devices, key = { it.id }) { device ->
                            DeviceCard(device = device)
                        }
                    }
                }
            }

            Button(
                onClick = { showAddDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 25.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = UjinCyan,
                    contentColor = UjinSurface,
                ),
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Добавить устройство",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Spacer(Modifier.height(12.dp))
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 84.dp),
        )
    }

    if (showAddDialog) {
        AddDeviceDialog(
            saving = state.saving,
            error = state.error,
            onDismiss = {
                showAddDialog = false
                viewModel.consumeError()
            },
            onConfirm = { title, family -> viewModel.addDevice(title, family) },
        )
    }
}

@Composable
private fun DeviceCard(
    device: DeviceOut,
    modifier: Modifier = Modifier,
) {
    val online = device.lastValues["online"]?.booleanOrNull != false
    val (status, rawStatusColor) = device.status()
    val statusColor = if (rawStatusColor == Color.Gray) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        rawStatusColor
    }
    val accentColor = if (online) UjinCyan else MaterialTheme.colorScheme.onSurfaceVariant

    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(accentColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = device.icon(),
                        contentDescription = device.title,
                        tint = accentColor,
                        modifier = Modifier.size(24.dp),
                    )
                }

                Spacer(Modifier.weight(1f))

                Box(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (online) UjinGreen else UjinRed),
                )
            }

            Column(
                modifier = Modifier.align(Alignment.BottomStart),
            ) {
                Text(
                    text = device.familyLabel(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = device.title.ifBlank { "Устройство" },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 19.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = status,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = statusColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

private fun DeviceOut.icon(): ImageVector = familyIconFor(family)

fun familyIconFor(family: String): ImageVector = when (family.lowercase()) {
    "leak", "water", "flood" -> Icons.Filled.WaterDrop
    "thermostat", "climate" -> Icons.Filled.Thermostat
    "light", "lamp" -> Icons.Filled.Lightbulb
    "ventilation", "fan" -> Icons.Filled.Air
    "door", "lock" -> Icons.Filled.MeetingRoom
    "intercom", "doorphone" -> Icons.Filled.Doorbell
    "barrier", "gate", "parking" -> Icons.Filled.Garage
    "camera", "cctv" -> Icons.Filled.Videocam
    else -> Icons.Filled.MeetingRoom
}

private fun DeviceOut.familyLabel(): String = when (family.lowercase()) {
    "leak", "water", "flood" -> "ДАТЧИК"
    "thermostat", "climate" -> "КЛИМАТ"
    "light", "lamp" -> "ОСВЕЩЕНИЕ"
    "ventilation", "fan" -> "ВЕНТИЛЯЦИЯ"
    "door", "lock" -> "ДОСТУП"
    "intercom", "doorphone" -> "ДОМОФОН"
    "barrier", "gate", "parking" -> "ПАРКОВКА"
    "camera", "cctv" -> "КАМЕРА"
    else -> "УСТРОЙСТВО"
}

private fun DeviceOut.status(): Pair<String, Color> {
    if (lastValues["online"]?.booleanOrNull == false) {
        return "Офлайн" to UjinRed
    }

    return when (family.lowercase()) {
        "leak", "water", "flood" -> {
            if (lastValues["leak"]?.booleanOrNull == true) {
                "Протечка" to UjinRed
            } else {
                "Нет протечек" to UjinGreen
            }
        }

        "thermostat", "climate" -> {
            val temperature = lastValues["temperature"]?.content
                ?: lastValues["target"]?.content
            if (temperature != null) {
                "$temperature°C" to UjinCyan
            } else {
                "Онлайн" to UjinGreen
            }
        }

        "light", "lamp" -> {
            val on = lastValues["on"]?.booleanOrNull == true
            val brightness = lastValues["brightness"]?.intOrNull
            if (on) {
                if (brightness != null) "Включено · $brightness%" to UjinCyan
                else "Включено" to UjinCyan
            } else {
                "Выключено" to Color.Gray
            }
        }

        "ventilation", "fan" -> {
            val speed = lastValues["speed"]?.intOrNull
            if (speed != null) "Скорость $speed%" to UjinCyan
            else "Онлайн" to UjinGreen
        }

        "door", "lock" -> {
            if (lastValues["open"]?.booleanOrNull == true) {
                "Открыто" to UjinCyan
            } else {
                "Закрыто" to UjinGreen
            }
        }

        "intercom", "doorphone" -> {
            if (lastValues["call"]?.booleanOrNull == true) {
                "Вызов" to UjinCyan
            } else {
                "Нет вызова" to Color.Gray
            }
        }

        "barrier", "gate", "parking" -> {
            val open = lastValues["open_cmd"]?.intOrNull ?: 0
            if (open > 0) "Открыт" to UjinCyan else "Закрыт" to UjinGreen
        }

        "camera", "cctv" -> "Онлайн" to UjinGreen

        else -> {
            val value = lastValues.values.firstOrNull()?.doubleOrNull
            if (value != null) "$value" to UjinCyan else "Онлайн" to UjinGreen
        }
    }
}

private fun devicesCountText(count: Int): String {
    val word = when {
        count % 10 == 1 && count % 100 != 11 -> "устройство"
        count % 10 in 2..4 && count % 100 !in 12..14 -> "устройства"
        else -> "устройств"
    }
    return "$count $word"
}

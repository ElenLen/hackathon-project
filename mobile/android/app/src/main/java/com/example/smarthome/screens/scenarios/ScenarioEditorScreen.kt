package com.example.smarthome.screens.scenarios

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DevicesOther
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smarthome.data.RepositoryProvider
import com.example.smarthome.data.dto.DeviceOut
import com.example.smarthome.ui.theme.UjinCyan
import com.example.smarthome.ui.theme.UjinSurface

@Composable
fun ScenarioEditorScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
) {
    var devices by remember { mutableStateOf<List<DeviceOut>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            devices = RepositoryProvider.repository.getDevices()
        } catch (e: Exception) {
            error = e.message ?: "Не удалось загрузить устройства"
        } finally {
            loading = false
        }
    }

    var name by rememberSaveable { mutableStateOf("") }
    val conditions = remember { mutableStateListOf<ScenarioRule>() }
    val actions = remember { mutableStateListOf<ScenarioRule>() }
    var dialogKind by remember { mutableStateOf<RuleKind?>(null) }

    val canSave = name.isNotBlank() && conditions.isNotEmpty() && actions.isNotEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, start = 8.dp, end = 25.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Назад",
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
            Spacer(Modifier.width(4.dp))
            Text(
                text = "Новый сценарий",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        when {
            loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = UjinCyan)
                }
            }

            error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = error ?: "Ошибка загрузки",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(start = 25.dp, end = 25.dp, top = 8.dp),
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Название сценария") },
                        placeholder = { Text("Например, Ушёл из дома") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                    )

                    Spacer(Modifier.height(24.dp))

                    SectionHeader(
                        title = "Условия",
                        subtitle = "Когда это произойдёт",
                    )
                    Spacer(Modifier.height(10.dp))

                    if (conditions.isEmpty()) {
                        Hint("Добавьте хотя бы одно условие запуска")
                    } else {
                        conditions.forEach { rule ->
                            RuleCard(
                                rule = rule,
                                onRemove = { conditions.remove(rule) },
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                    }

                    AddRuleButton(
                        text = "Добавить условие",
                        onClick = { dialogKind = RuleKind.CONDITION },
                    )

                    Spacer(Modifier.height(24.dp))

                    SectionHeader(
                        title = "Действия",
                        subtitle = "Что должно произойти с устройствами",
                    )
                    Spacer(Modifier.height(10.dp))

                    if (actions.isEmpty()) {
                        Hint("Добавьте хотя бы одно действие")
                    } else {
                        actions.forEach { rule ->
                            RuleCard(
                                rule = rule,
                                onRemove = { actions.remove(rule) },
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                    }

                    AddRuleButton(
                        text = "Добавить действие",
                        onClick = { dialogKind = RuleKind.ACTION },
                    )

                    Spacer(Modifier.height(16.dp))
                }
            }
        }

        if (!canSave && !loading && error == null) {
            Text(
                text = "Для сохранения добавьте название, условие и действие",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 25.dp, vertical = 6.dp),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }

        Button(
            onClick = {
                UserScenarioStore.add(
                    UserScenario(
                        id = "user-${System.currentTimeMillis()}",
                        name = name.trim(),
                        conditions = conditions.toList(),
                        actions = actions.toList(),
                    ),
                )
                onSaved()
            },
            enabled = canSave,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 25.dp)
                .height(59.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = UjinCyan,
                contentColor = UjinSurface,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        ) {
            Text(
                text = "Сохранить сценарий",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Spacer(Modifier.height(12.dp))
    }

    dialogKind?.let { kind ->
        RuleDialog(
            kind = kind,
            devices = devices,
            onDismiss = { dialogKind = null },
            onConfirm = { rule ->
                if (kind == RuleKind.CONDITION) conditions.add(rule) else actions.add(rule)
                dialogKind = null
            },
        )
    }
}

private enum class RuleKind {
    CONDITION,
    ACTION,
}

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String,
) {
    Column {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = subtitle,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun Hint(text: String) {
    Text(
        text = text,
        fontSize = 13.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(vertical = 4.dp),
    )
}

@Composable
private fun AddRuleButton(
    text: String,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, UjinCyan),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = UjinCyan),
    ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun RuleCard(
    rule: ScenarioRule,
    onRemove: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, top = 10.dp, bottom = 10.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(UjinCyan.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.DevicesOther,
                    contentDescription = null,
                    tint = UjinCyan,
                    modifier = Modifier.size(20.dp),
                )
            }

            Spacer(Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = rule.deviceTitle,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = if (rule.operator == null) {
                        "${signalLabel(rule.signal)} = ${rule.value}"
                    } else {
                        "${signalLabel(rule.signal)} ${rule.operator} ${rule.value}"
                    },
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Удалить",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Composable
private fun RuleDialog(
    kind: RuleKind,
    devices: List<DeviceOut>,
    onDismiss: () -> Unit,
    onConfirm: (ScenarioRule) -> Unit,
) {
    var selectedDeviceId by remember { mutableStateOf(devices.firstOrNull()?.id) }
    val selectedDevice = devices.firstOrNull { it.id == selectedDeviceId }
    val signals = selectedDevice?.lastValues?.keys?.toList().orEmpty()

    var selectedSignal by remember(selectedDeviceId) {
        mutableStateOf(signals.firstOrNull())
    }
    var operator by remember { mutableStateOf("=") }
    var value by remember(selectedDeviceId, selectedSignal) {
        mutableStateOf(
            selectedDevice?.lastValues?.get(selectedSignal)?.content.orEmpty(),
        )
    }

    val canConfirm = selectedDevice != null && selectedSignal != null && value.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (kind == RuleKind.CONDITION) "Новое условие" else "Новое действие",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                DialogLabel("Устройство")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(devices, key = { it.id }) { device ->
                        FilterChip(
                            selected = device.id == selectedDeviceId,
                            onClick = { selectedDeviceId = device.id },
                            label = {
                                Text(
                                    text = device.title.ifBlank { device.id },
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            },
                        )
                    }
                }

                DialogLabel("Сигнал")
                if (signals.isEmpty()) {
                    Text(
                        text = "Нет доступных сигналов",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(signals) { signal ->
                            FilterChip(
                                selected = signal == selectedSignal,
                                onClick = { selectedSignal = signal },
                                label = { Text(signalLabel(signal)) },
                            )
                        }
                    }
                }

                if (kind == RuleKind.CONDITION) {
                    DialogLabel("Оператор")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("=", ">", "<").forEach { option ->
                            FilterChip(
                                selected = option == operator,
                                onClick = { operator = option },
                                label = { Text(option) },
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Значение") },
                    placeholder = { Text("true, false, 25, 0") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                )

                Text(
                    text = "Например: true, false, 25 или 0",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = canConfirm,
                onClick = {
                    val device = selectedDevice ?: return@TextButton
                    val signal = selectedSignal ?: return@TextButton
                    onConfirm(
                        ScenarioRule(
                            deviceId = device.id,
                            deviceTitle = device.title.ifBlank { device.id },
                            signal = signal,
                            operator = if (kind == RuleKind.CONDITION) operator else null,
                            value = value.trim(),
                        ),
                    )
                },
            ) {
                Text("Добавить")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp),
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
    )
}

@Composable
private fun DialogLabel(text: String) {
    Text(
        text = text,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurface,
    )
}

private fun signalLabel(signal: String): String = when (signal) {
    "leak" -> "Протечка"
    "temperature" -> "Температура"
    "target" -> "Целевая температура"
    "on" -> "Включение"
    "brightness" -> "Яркость"
    "speed" -> "Скорость"
    "open" -> "Открытие"
    "open_cmd" -> "Команда открытия"
    "online" -> "Онлайн"
    "call" -> "Вызов"
    else -> signal
}

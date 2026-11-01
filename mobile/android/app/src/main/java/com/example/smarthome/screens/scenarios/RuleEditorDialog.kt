package com.example.smarthome.screens.scenarios

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smarthome.data.dto.ActionSpec
import com.example.smarthome.data.dto.DeviceOut
import com.example.smarthome.data.dto.RuleSpec
import com.example.smarthome.data.dto.SignalDeclaration
import com.example.smarthome.data.dto.guessDeclaration
import com.example.smarthome.ui.theme.UjinCyan
import com.example.smarthome.ui.theme.UjinGreen
import com.example.smarthome.ui.theme.UjinSurface
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull

enum class RuleEditorKind(val title: String) {
    TRIGGER("Триггер"),
    CONDITION("Условие"),
    ACTION("Действие"),
}

data class RuleDraft(
    val deviceId: String = "",
    val signal: String = "",
    val operator: String = "eq",
    val value: JsonPrimitive = JsonPrimitive(0),
)

fun RuleSpec.toDraft(): RuleDraft = RuleDraft(deviceId, signal, operator, value)

fun ActionSpec.toDraft(): RuleDraft = RuleDraft(deviceId, signal, "eq", value)

private data class OperatorOption(val code: String, val label: String)

private val operatorOptions = listOf(
    OperatorOption("eq", "="),
    OperatorOption("ne", "≠"),
    OperatorOption("gt", ">"),
    OperatorOption("gte", "≥"),
    OperatorOption("lt", "<"),
    OperatorOption("lte", "≤"),
    OperatorOption("changed", "изменилось"),
)

/** Шторы: единственный сигнал open, значения 0/1. */
private const val CURTAIN_FAMILY = "curtain"
private const val CURTAIN_SIGNAL = "open"
private const val CURTAIN_CLOSE = 0
private const val CURTAIN_OPEN = 1

private fun DeviceOut.isCurtain(): Boolean = family.lowercase() == CURTAIN_FAMILY

@Composable
fun RuleEditorDialog(
    kind: RuleEditorKind,
    initial: RuleDraft,
    devices: List<DeviceOut>,
    declarations: Map<String, List<SignalDeclaration>>,
    onDismiss: () -> Unit,
    onConfirm: (RuleDraft) -> Unit,
    onDelete: (() -> Unit)? = null,
) {
    var selectedDeviceId by remember {
        mutableStateOf(initial.deviceId.ifBlank { devices.firstOrNull()?.id.orEmpty() })
    }
    val selectedDevice = devices.firstOrNull { it.id == selectedDeviceId }
    val writable = kind == RuleEditorKind.ACTION
    val availableSignals = remember(selectedDevice, declarations, writable) {
        signalsFor(selectedDevice, declarations, writable)
    }
    var selectedSignal by remember(selectedDeviceId) {
        mutableStateOf(
            if (availableSignals.any { it.name == initial.signal }) initial.signal
            else availableSignals.firstOrNull()?.name.orEmpty(),
        )
    }
    val declaration = availableSignals.firstOrNull { it.name == selectedSignal }
    var operator by remember { mutableStateOf(initial.operator.ifBlank { "eq" }) }
    var value by remember(selectedDeviceId, selectedSignal) {
        mutableStateOf(
            if (selectedSignal == initial.signal) {
                initial.value
            } else {
                declaration?.defaultValue(selectedDevice?.lastValues?.get(selectedSignal))
                    ?: JsonPrimitive(0)
            },
        )
    }

    val isCurtain = selectedDevice?.isCurtain() == true

    val needsValue = kind == RuleEditorKind.ACTION || operator != "changed"
    val canConfirm = selectedDevice != null &&
            selectedSignal.isNotBlank() &&
            (!needsValue || value.content.isNotBlank())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Настроить ${kind.title.lowercase()}",
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

                if (isCurtain) {
                    // Шторы: только Открыть/Закрыть, без сигнала и значения
                    DialogLabel("Действие со шторами")
                    val currentOpen = if (kind == RuleEditorKind.ACTION) {
                        value.content == CURTAIN_OPEN.toString()
                    } else {
                        initial.value.content == CURTAIN_OPEN.toString()
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Button(
                            onClick = {
                                selectedSignal = CURTAIN_SIGNAL
                                operator = "eq"
                                value = JsonPrimitive(CURTAIN_OPEN)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (currentOpen) UjinCyan else UjinCyan.copy(alpha = 0.18f),
                                contentColor = if (currentOpen) UjinSurface else UjinCyan,
                            ),
                        ) {
                            Text("Открыть", fontWeight = FontWeight.SemiBold)
                        }
                        Button(
                            onClick = {
                                selectedSignal = CURTAIN_SIGNAL
                                operator = "eq"
                                value = JsonPrimitive(CURTAIN_CLOSE)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!currentOpen) UjinCyan else UjinCyan.copy(alpha = 0.18f),
                                contentColor = if (!currentOpen) UjinSurface else UjinCyan,
                            ),
                        ) {
                            Text("Закрыть", fontWeight = FontWeight.SemiBold)
                        }
                    }
                } else {
                    DialogLabel("Сигнал")
                    if (availableSignals.isEmpty()) {
                        Text(
                            text = "Нет подходящих сигналов",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(availableSignals, key = { it.name }) { signal ->
                                FilterChip(
                                    selected = signal.name == selectedSignal,
                                    onClick = { selectedSignal = signal.name },
                                    label = { Text(signal.title.ifBlank { signal.name }) },
                                )
                            }
                        }
                    }

                    if (kind != RuleEditorKind.ACTION) {
                        DialogLabel("Сравнение")
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(operatorOptions, key = { it.code }) { option ->
                                FilterChip(
                                    selected = option.code == operator,
                                    onClick = { operator = option.code },
                                    label = { Text(option.label) },
                                )
                            }
                        }
                    }

                    DialogLabel("Значение")
                    if (operator == "changed" && kind != RuleEditorKind.ACTION) {
                        Text(
                            text = "Сценарий сработает при любом изменении сигнала",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        ValueEditor(
                            declaration = declaration,
                            value = value,
                            onValueChange = { value = it },
                        )
                    }

                    if (declaration != null) {
                        Text(
                            text = "${declaration.name} · ${declaration.management} · ${declaration.control}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = canConfirm,
                onClick = {
                    val draft = if (isCurtain) {
                        RuleDraft(
                            deviceId = selectedDeviceId,
                            signal = CURTAIN_SIGNAL,
                            operator = "eq",
                            value = value,
                        )
                    } else {
                        RuleDraft(
                            deviceId = selectedDeviceId,
                            signal = selectedSignal,
                            operator = if (kind == RuleEditorKind.ACTION) "eq" else operator,
                            value = if (needsValue) value else JsonPrimitive(1),
                        )
                    }
                    onConfirm(draft)
                },
            ) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            Row {
                if (onDelete != null) {
                    TextButton(onClick = onDelete) {
                        Text("Удалить", color = MaterialTheme.colorScheme.error)
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Отмена", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp),
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

@Composable
private fun ValueEditor(
    declaration: SignalDeclaration?,
    value: JsonPrimitive,
    onValueChange: (JsonPrimitive) -> Unit,
) {
    if (declaration == null) {
        OutlinedTextField(
            value = value.content,
            onValueChange = { onValueChange(primitiveFromText(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Значение") },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
        )
        return
    }

    when (declaration.control) {
        "switch" -> {
            val checked = value.booleanOrNull ?: (value.content == "1")
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = declaration.label(value),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Switch(
                    checked = checked,
                    onCheckedChange = { onValueChange(JsonPrimitive(if (it) 1 else 0)) },
                )
            }
        }

        "range", "counter" -> {
            val min = declaration.min ?: 0.0
            val max = declaration.max ?: if (declaration.control == "counter") 1000.0 else 100.0
            val current = (value.doubleOrNull ?: min).toFloat().coerceIn(min.toFloat(), max.toFloat())
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(declaration.title, fontSize = 14.sp)
                    Text(
                        text = declaration.label(value),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Slider(
                    value = current,
                    onValueChange = { onValueChange(JsonPrimitive(it.toDouble())) },
                    valueRange = min.toFloat()..max.toFloat(),
                )
            }
        }

        "list", "combolist" -> {
            val layout = declaration.layout
            if (layout.isNullOrEmpty()) {
                OutlinedTextField(
                    value = value.content,
                    onValueChange = { onValueChange(primitiveFromText(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Значение") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                )
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(layout.entries.toList(), key = { it.key }) { entry ->
                        FilterChip(
                            selected = entry.key == value.content,
                            onClick = {
                                onValueChange(
                                    entry.key.toIntOrNull()?.let { JsonPrimitive(it) }
                                        ?: JsonPrimitive(entry.key),
                                )
                            },
                            label = { Text(entry.value) },
                        )
                    }
                }
            }
        }

        "button", "push_time" -> {
            OutlinedButton(
                onClick = { onValueChange(JsonPrimitive(1)) },
                shape = RoundedCornerShape(14.dp),
            ) {
                Text("Отправить 1")
            }
        }

        else -> {
            OutlinedTextField(
                value = value.content,
                onValueChange = { onValueChange(primitiveFromText(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Значение") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
            )
        }
    }
}

private fun primitiveFromText(text: String): JsonPrimitive {
    if (text.isBlank()) return JsonPrimitive("")
    return text.toIntOrNull()?.let { JsonPrimitive(it) }
        ?: text.toDoubleOrNull()?.let { JsonPrimitive(it) }
        ?: JsonPrimitive(text)
}

private fun signalsFor(
    device: DeviceOut?,
    declarations: Map<String, List<SignalDeclaration>>,
    writable: Boolean,
): List<SignalDeclaration> {
    if (device == null) return emptyList()
    val declared = declarations[device.id].orEmpty()
        .filter { if (writable) it.writable else it.readable }
    if (declared.isNotEmpty()) return declared
    return device.lastValues
        .map { (name, value) -> guessDeclaration(name, value) }
        .filter { if (writable) it.writable else it.readable }
}
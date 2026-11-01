package com.example.smarthome.screens.devices

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smarthome.ui.theme.UjinCyan
import com.example.smarthome.ui.theme.UjinSurface

data class DeviceFamilyOption(
    val family: String,
    val label: String,
    val icon: ImageVector,
)

private val familyOptionValues = listOf(
    "thermostat" to "Термостат",
    "light" to "Освещение",
    "ventilation" to "Вентиляция",
    "door" to "Дверь",
    "intercom" to "Домофон",
    "barrier" to "Шлагбаум",
    "camera" to "Камера",
    "counter" to "Счётчик",
    "fire" to "Пожарный",
    "elevator" to "Лифт",
    "hub" to "Хаб",
    "other" to "Другое",
)

fun deviceFamilyOptions(): List<DeviceFamilyOption> =
    familyOptionValues.map { (family, label) ->
        DeviceFamilyOption(family = family, label = label, icon = familyIconFor(family))
    }

@Composable
fun AddDeviceDialog(
    saving: Boolean,
    error: String?,
    onDismiss: () -> Unit,
    onConfirm: (title: String, family: String) -> Unit,
) {
    val options = remember { deviceFamilyOptions() }
    var title by remember { mutableStateOf("") }
    var family by remember { mutableStateOf(options.first().family) }
    val canConfirm = title.isNotBlank() && !saving

    AlertDialog(
        onDismissRequest = { if (!saving) onDismiss() },
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                text = "Новое устройство",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Название") },
                    placeholder = { Text("Например, Кухня") },
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    text = "Тип устройства",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(Modifier.height(8.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 240.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(options, key = { it.family }) { option ->
                        val selected = option.family == family
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (selected) UjinCyan.copy(alpha = 0.16f)
                                    else MaterialTheme.colorScheme.surfaceVariant,
                                )
                                .clickable(enabled = !saving) { family = option.family }
                                .padding(vertical = 10.dp, horizontal = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (selected) UjinCyan.copy(alpha = 0.22f)
                                        else UjinSurface,
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = option.icon,
                                    contentDescription = option.label,
                                    tint = if (selected) UjinCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = option.label,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = if (selected) UjinCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                if (error != null) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = error,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(title, family) },
                enabled = canConfirm,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = UjinCyan,
                    contentColor = UjinSurface,
                ),
            ) {
                if (saving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = UjinSurface,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text("Добавить", fontWeight = FontWeight.SemiBold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !saving) {
                Text("Отмена", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        shape = RoundedCornerShape(20.dp),
    )
}
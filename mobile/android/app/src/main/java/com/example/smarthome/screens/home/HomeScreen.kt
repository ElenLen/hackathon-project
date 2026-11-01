package com.example.smarthome.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Doorbell
import androidx.compose.material.icons.filled.Garage
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.example.smarthome.data.dto.ScenarioOut
import com.example.smarthome.ui.components.ErrorBanner
import com.example.smarthome.ui.components.LoadingBox
import com.example.smarthome.ui.theme.UjinCyan
import com.example.smarthome.ui.theme.UjinGreen
import com.example.smarthome.ui.theme.UjinRed

/**
 * Ключевые слова, по которым скрываются устройства/сценарии,
 * относящиеся к протечкам и трубам.
 */
private val HIDDEN_KEYWORDS = listOf(
    "протечк",
    "труб",
    "leak",
    "pipe",
)

private fun DeviceOut.isHiddenDevice(): Boolean {
    val haystack = buildString {
        append(title)
        append(' ')
        append(id)
    }.lowercase()
    return HIDDEN_KEYWORDS.any { haystack.contains(it) }
}

private fun ScenarioOut.isHiddenScenario(): Boolean {
    val haystack = buildString {
        append(name)
        append(' ')
        append(id)
    }.lowercase()
    return HIDDEN_KEYWORDS.any { haystack.contains(it) }
}

@Composable
fun HomeScreen(
    onScenariosClick: () -> Unit,
    viewModel: HomeViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.message) {
        state.message?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.consumeMessage()
        }
    }

    // Скрываем устройства протечки/трубы и соответствующие сценарии
    val visibleProblemDevices = remember(state.problemDevices) {
        state.problemDevices.filterNot { it.isHiddenDevice() }
    }
    val visibleScenarios = remember(state.scenarios) {
        state.scenarios.filterNot { it.isHiddenScenario() }
    }
    val visibleAllGood = state.allGood || visibleProblemDevices.isEmpty()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background),
        ) {
            if (state.loading && state.devices.isEmpty() && state.scenarios.isEmpty()) {
                LoadingBox()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 20.dp,
                        end = 20.dp,
                        top = 24.dp,
                        bottom = 32.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    item { HomeHeader() }

                    state.error?.let { error ->
                        item { ErrorBanner(message = error) }
                    }

                    item {
                        StatusCard(
                            problemDevices = visibleProblemDevices,
                            allGood = visibleAllGood,
                        )
                    }

                    item { SectionTitle("Быстрое управление") }

                    item {
                        QuickActionsRow(
                            onAction = viewModel::onQuickAction,
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            SectionTitle("Сценарии")
                            TextButton(onClick = onScenariosClick) {
                                Text(
                                    text = "Все",
                                    color = UjinCyan,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                    }

                    if (visibleScenarios.isEmpty()) {
                        item {
                            EmptyHint("Пока нет доступных сценариев")
                        }
                    } else {
                        item { SubsectionTitle("Быстрые") }
                        item {
                            QuickScenariosRow(
                                scenarios = visibleScenarios.take(4),
                                busyScenarioId = state.busyScenarioId,
                                onRun = viewModel::runScenario,
                            )
                        }

                        item { SubsectionTitle("Популярные") }
                        items(visibleScenarios, key = { it.id }) { scenario ->
                            ScenarioRow(
                                scenario = scenario,
                                busy = state.busyScenarioId == scenario.id,
                                onToggle = { enabled ->
                                    viewModel.setScenarioEnabled(scenario.id, enabled)
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Моя квартира",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "Умный дом Ujin",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Apartment,
                contentDescription = null,
                tint = UjinCyan,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

@Composable
private fun StatusCard(
    problemDevices: List<DeviceOut>,
    allGood: Boolean,
) {
    val accent = if (allGood) UjinGreen else UjinRed
    val subtitle = if (allGood) {
        "Все устройства работают штатно"
    } else {
        val names = problemDevices.take(2).joinToString(", ") {
            it.title.ifBlank { it.id }
        }
        if (problemDevices.size > 2) {
            "$names и ещё ${problemDevices.size - 2}"
        } else {
            names
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = accent.copy(alpha = 0.12f),
        ),
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (allGood) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(26.dp),
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (allGood) "Всё хорошо" else "Есть проблемы",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            if (!allGood) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(accent)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = problemDevices.size.toString(),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickActionsRow(
    onAction: (QuickAction) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        QuickAction.entries.forEach { action ->
            QuickActionTile(
                action = action,
                modifier = Modifier.weight(1f),
                onClick = { onAction(action) },
            )
        }
    }
}

@Composable
private fun QuickActionTile(
    action: QuickAction,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val icon: ImageVector = when (action) {
        QuickAction.DOORS -> Icons.Filled.MeetingRoom
        QuickAction.INTERCOM -> Icons.Filled.Doorbell
        QuickAction.BARRIER -> Icons.Filled.Garage
        QuickAction.CAMERA -> Icons.Filled.Videocam
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = action.title,
                tint = UjinCyan,
                modifier = Modifier.size(24.dp),
            )
        }

        Spacer(Modifier.height(10.dp))

        Text(
            text = action.title,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = action.subtitle,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun QuickScenariosRow(
    scenarios: List<ScenarioOut>,
    busyScenarioId: String?,
    onRun: (String) -> Unit,
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(scenarios, key = { it.id }) { scenario ->
            val busy = busyScenarioId == scenario.id
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(
                        if (scenario.enabled) UjinCyan.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant,
                    )
                    .clickable(
                        enabled = !busy && busyScenarioId == null,
                        onClick = { onRun(scenario.id) },
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (busy) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = UjinCyan,
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Bolt,
                        contentDescription = null,
                        tint = if (scenario.enabled) UjinCyan else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(16.dp),
                    )
                }

                Spacer(Modifier.width(8.dp))

                Text(
                    text = scenario.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun ScenarioRow(
    scenario: ScenarioOut,
    busy: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center,
            ) {
                if (busy) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = UjinCyan,
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        tint = UjinCyan,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = scenario.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = if (scenario.enabled) "Активен" else "Выключен",
                    fontSize = 12.sp,
                    color = if (scenario.enabled) UjinGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.width(12.dp))

            Switch(
                checked = scenario.enabled,
                onCheckedChange = onToggle,
                enabled = !busy,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = UjinCyan,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = MaterialTheme.colorScheme.outlineVariant,
                ),
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
    )
}

@Composable
private fun SubsectionTitle(text: String) {
    Text(
        text = text,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun EmptyHint(text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(16.dp),
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
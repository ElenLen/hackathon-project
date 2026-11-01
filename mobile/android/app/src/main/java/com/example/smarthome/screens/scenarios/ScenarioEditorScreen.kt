package com.example.smarthome.screens.scenarios

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smarthome.data.dto.RuleSpec
import com.example.smarthome.data.dto.ScenarioDefinition
import com.example.smarthome.data.dto.emptyScenario
import com.example.smarthome.ui.theme.UjinCyan
import com.example.smarthome.ui.theme.UjinSurface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScenarioEditorScreen(
    scenarioId: String? = null,
    onBack: () -> Unit,
    viewModel: ScenariosViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val density = LocalDensity.current
    val screenWidth = with(density) {
        LocalConfiguration.current.screenWidthDp.dp.toPx()
    }

    LaunchedEffect(Unit) { viewModel.load() }
    LaunchedEffect(state.error) {
        val message = state.error ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        viewModel.consumeError()
    }

    val isNew = scenarioId.isNullOrBlank()
    var draft by remember { mutableStateOf<ScenarioDefinition?>(null) }
    var positions by remember { mutableStateOf<Map<String, Offset>>(emptyMap()) }
    var editTarget by remember { mutableStateOf<String?>(null) }
    var addKind by remember { mutableStateOf<RuleEditorKind?>(null) }

    LaunchedEffect(isNew, scenarioId, state.scenarios, state.loading) {
        if (state.loading || draft != null) return@LaunchedEffect
        val source = if (isNew) {
            emptyScenario()
        } else {
            state.scenarios.firstOrNull { it.id == scenarioId }
        } ?: return@LaunchedEffect
        draft = source
        positions = defaultPositions(source, screenWidth)
    }

    val scenario = draft
    val nodes = remember(scenario, state.devices, state.declarations) {
        scenario?.toGraphNodes(state.devices, state.declarations).orEmpty()
    }
    val edges = remember(scenario) { scenario?.toGraphEdges().orEmpty() }

    fun persist(result: ScenarioDefinition) {
        val final = result.withPositions(positions)
        viewModel.save(final, isNew) { onBack() }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isNew) "Новый сценарий" else "Редактор сценария",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад",
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val current = draft ?: return@IconButton
                            persist(current.copy(name = current.name.ifBlank { "Без названия" }))
                        },
                        enabled = draft != null && !state.saving,
                    ) {
                        if (state.saving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = UjinCyan,
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = "Сохранить",
                                tint = UjinCyan,
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when {
                state.loading || draft == null -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = UjinCyan,
                    )
                }

                else -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        OutlinedTextField(
                            value = scenario?.name.orEmpty(),
                            onValueChange = { value ->
                                val current = draft ?: return@OutlinedTextField
                                draft = current.copy(name = value)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 25.dp),
                            label = { Text("Название сценария") },
                            placeholder = { Text("Например, свет при протечке") },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                        )

                        Spacer(Modifier.height(8.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 25.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "Сценарий включён",
                                modifier = Modifier.weight(1f),
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Switch(
                                checked = scenario?.enabled ?: true,
                                onCheckedChange = { value ->
                                    val current = draft ?: return@Switch
                                    draft = current.copy(enabled = value)
                                },
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        ScenarioGraphBoard(
                            nodes = nodes,
                            edges = edges,
                            positions = positions,
                            onMove = { id, offset ->
                                positions = positions.toMutableMap().apply { put(id, offset) }
                            },
                            onNodeClick = { id -> editTarget = id },
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(horizontal = 16.dp),
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 25.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Button(
                                onClick = { addKind = RuleEditorKind.TRIGGER },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp),
                                shape = RoundedCornerShape(14.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = UjinCyan.copy(alpha = 0.18f),
                                    contentColor = UjinCyan,
                                ),
                            ) {
                                Text(
                                    text = "Триггер",
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                )
                            }
                            Button(
                                onClick = { addKind = RuleEditorKind.CONDITION },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp),
                                shape = RoundedCornerShape(14.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = UjinCyan.copy(alpha = 0.18f),
                                    contentColor = UjinCyan,
                                ),
                            ) {
                                Text(
                                    text = "Условие",
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                )
                            }
                            Button(
                                onClick = { addKind = RuleEditorKind.ACTION },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp),
                                shape = RoundedCornerShape(14.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = UjinCyan.copy(alpha = 0.18f),
                                    contentColor = UjinCyan,
                                ),
                            ) {
                                Text(
                                    text = "Действие",
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                )
                            }
                        }

                        Text(
                            text = "Перетащите блоки, чтобы выстроить цепочку. Нажмите на блок, чтобы изменить.",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 25.dp, vertical = 8.dp),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )

                        Button(
                            onClick = {
                                val current = draft ?: return@Button
                                persist(current.copy(name = current.name.ifBlank { "Без названия" }))
                            },
                            enabled = !state.saving,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 25.dp)
                                .height(59.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = UjinCyan,
                                contentColor = UjinSurface,
                            ),
                        ) {
                            Text(
                                text = if (isNew) "Создать сценарий" else "Сохранить изменения",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                    }
                }
            }
        }
    }

    addKind?.let { kind ->
        val base = draft
        if (base != null) {
            RuleEditorDialog(
                kind = kind,
                initial = when (kind) {
                    RuleEditorKind.TRIGGER -> base.takeIf { it.hasTrigger }?.trigger?.toDraft()
                        ?: RuleDraft()
                    RuleEditorKind.CONDITION -> RuleDraft()
                    RuleEditorKind.ACTION -> RuleDraft()
                },
                devices = state.devices,
                declarations = state.declarations,
                onDismiss = { addKind = null },
                onConfirm = { ruleDraft ->
                    val updated = when (kind) {
                        RuleEditorKind.TRIGGER -> base.copy(
                            trigger = RuleSpec(
                                id = "trigger",
                                deviceId = ruleDraft.deviceId,
                                signal = ruleDraft.signal,
                                operator = ruleDraft.operator,
                                value = ruleDraft.value,
                            ),
                        )
                        RuleEditorKind.CONDITION -> base.addConditionDraft(ruleDraft)
                        RuleEditorKind.ACTION -> base.addActionDraft(ruleDraft)
                    }
                    draft = updated
                    val extraId = when (kind) {
                        RuleEditorKind.TRIGGER -> null
                        RuleEditorKind.CONDITION -> updated.conditions.lastOrNull()?.id
                        RuleEditorKind.ACTION -> updated.actions.lastOrNull()?.id
                    }
                    if (extraId != null && !positions.containsKey(extraId)) {
                        val anchor = positions["trigger"] ?: Offset(16f, 20f)
                        val step = 92f + 28f
                        val sameKind = if (kind == RuleEditorKind.ACTION) {
                            updated.actions.size - 1
                        } else {
                            updated.conditions.size - 1
                        }
                        val x = if (kind == RuleEditorKind.ACTION) {
                            (screenWidth - 150f - 16f).coerceAtLeast(anchor.x + 150f + 24f)
                        } else {
                            anchor.x
                        }
                        positions = positions.toMutableMap().apply {
                            put(extraId, Offset(x, anchor.y + step * (sameKind + 1)))
                        }
                    }
                    addKind = null
                },
            )
        }
    }

    editTarget?.let { target ->
        val base = draft
        if (base != null) {
            val condition = base.conditions.firstOrNull { it.id == target }
            val action = base.actions.firstOrNull { it.id == target }
            val kind = when {
                target == "trigger" -> RuleEditorKind.TRIGGER
                condition != null -> RuleEditorKind.CONDITION
                else -> RuleEditorKind.ACTION
            }
            val initial = when {
                condition != null -> condition.toDraft()
                action != null -> action.toDraft()
                else -> base.trigger.toDraft()
            }
            RuleEditorDialog(
                kind = kind,
                initial = initial,
                devices = state.devices,
                declarations = state.declarations,
                onDismiss = { editTarget = null },
                onConfirm = { ruleDraft ->
                    val updated = when {
                        kind == RuleEditorKind.TRIGGER -> base.copy(
                            trigger = RuleSpec(
                                id = "trigger",
                                deviceId = ruleDraft.deviceId,
                                signal = ruleDraft.signal,
                                operator = ruleDraft.operator,
                                value = ruleDraft.value,
                            ),
                        )
                        condition != null -> base.updateConditionDraft(target, ruleDraft)
                        else -> base.updateActionDraft(target, ruleDraft)
                    }
                    draft = updated
                    editTarget = null
                },
                onDelete = {
                    draft = when {
                        kind == RuleEditorKind.TRIGGER -> base.copy(trigger = RuleSpec())
                        condition != null -> base.removeCondition(target)
                        else -> base.removeAction(target)
                    }
                    positions = positions.toMutableMap().apply { remove(target) }
                    editTarget = null
                },
            )
        }
    }
}
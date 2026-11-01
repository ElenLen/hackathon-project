package com.example.smarthome.screens.scenarios

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smarthome.data.RepositoryProvider
import com.example.smarthome.data.dto.ActionSpec
import com.example.smarthome.data.dto.DeviceOut
import com.example.smarthome.data.dto.RuleSpec
import com.example.smarthome.data.dto.ScenarioDefinition
import com.example.smarthome.data.dto.ScenarioGraphEdge
import com.example.smarthome.data.dto.ScenarioGraphNode
import com.example.smarthome.data.dto.ScenarioLayout
import com.example.smarthome.data.dto.ScenarioRun
import com.example.smarthome.data.dto.SignalDeclaration
import com.example.smarthome.data.dto.newRuleId
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonPrimitive

data class ScenarioUiState(
    val loading: Boolean = true,
    val saving: Boolean = false,
    val error: String? = null,
    val scenarios: List<ScenarioDefinition> = emptyList(),
    val devices: List<DeviceOut> = emptyList(),
    val declarations: Map<String, List<SignalDeclaration>> = emptyMap(),
    val runs: List<ScenarioRun> = emptyList(),
)

class ScenariosViewModel : ViewModel() {

    private val repository = RepositoryProvider.repository

    private val _state = MutableStateFlow(ScenarioUiState())
    val state: StateFlow<ScenarioUiState> = _state.asStateFlow()

    private var loaded = false

    fun load(force: Boolean = false) {
        if (loaded && !force) return
        loaded = true
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            try {
                // distinctBy — защита от дублей "dev-7" и т.п. в LazyGrid/LazyColumn
                val devices = repository.getDevices().distinctBy { it.id }
                val declarations = devices.map { device ->
                    async { device.id to repository.getDeclaration(device.id) }
                }.awaitAll().toMap()
                val scenarios = repository.getScenarioDefinitions()
                    .distinctBy { it.id }
                _state.update {
                    it.copy(
                        loading = false,
                        devices = devices,
                        declarations = declarations,
                        scenarios = scenarios.sortedBy { scenario -> scenario.name.lowercase() },
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(loading = false, error = e.message ?: "Не удалось загрузить сценарии")
                }
            }
        }
    }

    fun toggle(scenario: ScenarioDefinition, enabled: Boolean) {
        viewModelScope.launch {
            val previous = _state.value.scenarios
            _state.update { state ->
                state.copy(
                    scenarios = state.scenarios.map {
                        if (it.id == scenario.id) it.copy(enabled = enabled) else it
                    },
                )
            }
            try {
                repository.toggleScenario(scenario.id, enabled)
            } catch (e: Exception) {
                _state.update { it.copy(scenarios = previous, error = e.message) }
            }
        }
    }

    fun delete(scenario: ScenarioDefinition) {
        viewModelScope.launch {
            val previous = _state.value.scenarios
            _state.update { state ->
                state.copy(scenarios = state.scenarios.filterNot { it.id == scenario.id })
            }
            try {
                repository.deleteScenario(scenario.id)
            } catch (e: Exception) {
                _state.update { it.copy(scenarios = previous, error = e.message) }
            }
        }
    }

    fun test(scenario: ScenarioDefinition) {
        viewModelScope.launch {
            try {
                val run = repository.testScenario(scenario.id)
                _state.update { it.copy(runs = listOf(run) + it.runs) }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message ?: "Не удалось запустить сценарий") }
            }
        }
    }

    fun consumeError() {
        _state.update { it.copy(error = null) }
    }

    fun save(scenario: ScenarioDefinition, isNew: Boolean, onSaved: () -> Unit) {
        _state.update { it.copy(saving = true, error = null) }
        viewModelScope.launch {
            try {
                if (isNew) repository.createScenario(scenario) else repository.updateScenario(scenario)
                loaded = false
                load(force = true)
                _state.update { it.copy(saving = false) }
                onSaved()
            } catch (e: Exception) {
                _state.update {
                    it.copy(saving = false, error = e.message ?: "Не удалось сохранить сценарий")
                }
            }
        }
    }

    fun loadRuns(scenarioId: String) {
        viewModelScope.launch {
            try {
                val runs = repository.getRuns(scenarioId)
                _state.update { it.copy(runs = runs) }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }
}

fun defaultPositions(scenario: ScenarioDefinition, screenWidth: Float): Map<String, Offset> {
    val nodeWidth = 150f
    val nodeHeight = 92f
    val left = 16f
    val right = (screenWidth - nodeWidth - 16f).coerceAtLeast(left + nodeWidth + 24f)
    val top = 20f
    val step = nodeHeight + 28f
    val trigger = Offset(left, top)
    val conditions = scenario.conditions.mapIndexed { index, _ ->
        Offset(left, top + step * (index + 1))
    }
    val actions = scenario.actions.mapIndexed { index, _ ->
        Offset(right, top + step * (index + 1))
    }

    val stored = scenario.layout.nodes.associate { it.id to Offset(it.x, it.y) }
    val positions = linkedMapOf<String, Offset>()
    positions["trigger"] = stored["trigger"] ?: trigger
    scenario.conditions.forEachIndexed { index, rule ->
        positions[rule.id] = stored[rule.id] ?: conditions.getOrElse(index) { trigger }
    }
    scenario.actions.forEachIndexed { index, action ->
        positions[action.id] = stored[action.id] ?: actions.getOrElse(index) { trigger }
    }
    return positions
}

fun ScenarioDefinition.toGraphNodes(
    devices: List<DeviceOut>,
    declarations: Map<String, List<SignalDeclaration>>,
): List<ScenarioGraphNodeView> {
    val titles = devices.associate { it.id to it.title.ifBlank { it.id } }
    val nodes = mutableListOf<ScenarioGraphNodeView>()
    if (hasTrigger) {
        nodes += ScenarioGraphNodeView(
            id = "trigger",
            kind = GraphNodeKind.TRIGGER,
            badge = "ЕСЛИ",
            title = titles[trigger.deviceId] ?: trigger.deviceId,
            subtitle = graphRuleSummary(trigger, declarations),
        )
    }
    conditions.forEach { rule ->
        nodes += ScenarioGraphNodeView(
            id = rule.id,
            kind = GraphNodeKind.CONDITION,
            badge = "УСЛОВИЕ",
            title = titles[rule.deviceId] ?: rule.deviceId,
            subtitle = graphRuleSummary(rule, declarations),
        )
    }
    actions.forEach { action ->
        nodes += ScenarioGraphNodeView(
            id = action.id,
            kind = GraphNodeKind.ACTION,
            badge = "ТОГДА",
            title = titles[action.deviceId] ?: action.deviceId,
            subtitle = graphActionSummary(action, declarations),
        )
    }
    return nodes
}

fun ScenarioDefinition.toGraphEdges(): List<ScenarioGraphEdgeView> {
    if (!hasTrigger) return emptyList()
    val conditionIds = conditions.map { it.id }
    val edges = mutableListOf<ScenarioGraphEdgeView>()
    if (conditionIds.isEmpty()) {
        actions.forEach { edges += ScenarioGraphEdgeView("trigger", it.id) }
    } else {
        edges += ScenarioGraphEdgeView("trigger", conditionIds.first())
        conditionIds.zipWithNext().forEach { (from, to) ->
            edges += ScenarioGraphEdgeView(from, to)
        }
        val last = conditionIds.last()
        actions.forEach { edges += ScenarioGraphEdgeView(last, it.id) }
    }
    return edges
}

fun ScenarioDefinition.withPositions(positions: Map<String, Offset>): ScenarioDefinition {
    val nodes = positions.map { (id, offset) ->
        ScenarioGraphNode(
            id = id,
            type = when (id) {
                "trigger" -> "trigger"
                else -> if (conditions.any { it.id == id }) "condition" else "action"
            },
            x = offset.x,
            y = offset.y,
        )
    }
    val edges = toGraphEdges().map { ScenarioGraphEdge(it.from, it.to) }
    return copy(layout = ScenarioLayout(nodes = nodes, edges = edges))
}

fun ScenarioDefinition.addConditionDraft(draft: RuleDraft): ScenarioDefinition {
    val rule = RuleSpec(
        id = newRuleId("condition"),
        deviceId = draft.deviceId,
        signal = draft.signal,
        operator = draft.operator,
        value = draft.value,
    )
    return copy(conditions = conditions + rule)
}

fun ScenarioDefinition.updateConditionDraft(id: String, draft: RuleDraft): ScenarioDefinition {
    return copy(
        conditions = conditions.map {
            if (it.id == id) {
                it.copy(
                    deviceId = draft.deviceId,
                    signal = draft.signal,
                    operator = draft.operator,
                    value = draft.value,
                )
            } else {
                it
            }
        },
    )
}

fun ScenarioDefinition.addActionDraft(draft: RuleDraft): ScenarioDefinition {
    val action = ActionSpec(
        id = newRuleId("action"),
        deviceId = draft.deviceId,
        signal = draft.signal,
        value = draft.value,
    )
    return copy(actions = actions + action)
}

fun ScenarioDefinition.updateActionDraft(id: String, draft: RuleDraft): ScenarioDefinition {
    return copy(
        actions = actions.map {
            if (it.id == id) {
                it.copy(deviceId = draft.deviceId, signal = draft.signal, value = draft.value)
            } else {
                it
            }
        },
    )
}

fun ScenarioDefinition.removeCondition(id: String): ScenarioDefinition =
    copy(conditions = conditions.filterNot { it.id == id })

fun ScenarioDefinition.removeAction(id: String): ScenarioDefinition =
    copy(actions = actions.filterNot { it.id == id })

private fun signalTitle(
    deviceId: String,
    signal: String,
    declarations: Map<String, List<SignalDeclaration>>,
): String {
    val declaration = declarations[deviceId]?.firstOrNull { it.name == signal }
    return declaration?.title?.takeIf { it.isNotBlank() } ?: signal
}

private fun graphRuleSummary(
    rule: RuleSpec,
    declarations: Map<String, List<SignalDeclaration>>,
): String {
    val operator = when (rule.operator) {
        "eq" -> "="
        "ne" -> "≠"
        "gt" -> ">"
        "gte" -> "≥"
        "lt" -> "<"
        "lte" -> "≤"
        "changed" -> "изменилось"
        else -> rule.operator
    }
    val title = signalTitle(rule.deviceId, rule.signal, declarations)
    return if (rule.operator == "changed") "$title $operator"
    else "$title $operator ${rule.value.content}"
}

private fun graphActionSummary(
    action: ActionSpec,
    declarations: Map<String, List<SignalDeclaration>>,
): String {
    val title = signalTitle(action.deviceId, action.signal, declarations)
    val declaration = declarations[action.deviceId]?.firstOrNull { it.name == action.signal }
    val value = declaration?.label(action.value) ?: action.value.content
    return "$title = $value"
}

fun ScenarioDefinition.triggerSummary(
    declarations: Map<String, List<SignalDeclaration>>,
): String {
    if (!hasTrigger) return "Нет условия запуска"
    val head = graphRuleSummary(trigger, declarations)
    return if (conditions.isEmpty()) head else "$head · условий: ${conditions.size}"
}
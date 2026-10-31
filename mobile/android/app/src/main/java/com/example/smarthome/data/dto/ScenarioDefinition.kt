package com.example.smarthome.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonPrimitive
import java.util.UUID

@Serializable
data class RuleSpec(
    val id: String = "",
    @SerialName("device_id") val deviceId: String = "",
    val signal: String = "",
    val operator: String = "eq",
    val value: JsonPrimitive = JsonPrimitive(0),
)

@Serializable
data class ActionSpec(
    val id: String = "",
    @SerialName("device_id") val deviceId: String = "",
    val signal: String = "",
    val value: JsonPrimitive = JsonPrimitive(1),
)

@Serializable
data class ScenarioGraphNode(
    val id: String = "",
    val type: String = "trigger",
    val x: Float = 0f,
    val y: Float = 0f,
)

@Serializable
data class ScenarioGraphEdge(
    val from: String = "",
    val to: String = "",
)

@Serializable
data class ScenarioLayout(
    val nodes: List<ScenarioGraphNode> = emptyList(),
    val edges: List<ScenarioGraphEdge> = emptyList(),
)

@Serializable
data class ScenarioDefinition(
    val id: String = "",
    val name: String = "",
    val enabled: Boolean = true,
    val trigger: RuleSpec = RuleSpec(),
    val conditions: List<RuleSpec> = emptyList(),
    val actions: List<ActionSpec> = emptyList(),
    val layout: ScenarioLayout = ScenarioLayout(),
) {
    val hasTrigger: Boolean
        get() = trigger.deviceId.isNotBlank() && trigger.signal.isNotBlank()

    fun normalized(): ScenarioDefinition = copy(
        trigger = trigger.copy(id = "trigger"),
        conditions = conditions.map {
            if (it.id.isBlank()) it.copy(id = newRuleId("condition")) else it
        },
        actions = actions.map {
            if (it.id.isBlank()) it.copy(id = newRuleId("action")) else it
        },
    )
}

fun emptyScenario(): ScenarioDefinition = ScenarioDefinition(id = newRuleId("scenario"))

fun newRuleId(prefix: String): String =
    "$prefix-${UUID.randomUUID().toString().take(8)}"

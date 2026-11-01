package com.example.smarthome.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ScenarioRun(
    val id: Int,
    @SerialName("scenario_id") val scenarioId: String = "",
    @SerialName("triggered_at") val triggeredAt: String = "",
    @SerialName("trigger_snapshot") val triggerSnapshot: String? = null,
    @SerialName("actions_json") val actionsJson: String? = null,
    val ok: Int = 1,
)

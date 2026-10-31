package com.example.smarthome.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class ScenarioOut(
    val id: String,
    val name: String = "",
    val enabled: Boolean = true,
    val rule: JsonObject = JsonObject(emptyMap()),
    @SerialName("created_at") val createdAt: String = "",
)

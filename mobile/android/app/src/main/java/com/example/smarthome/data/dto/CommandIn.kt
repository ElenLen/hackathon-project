package com.example.smarthome.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonPrimitive

@Serializable
data class CommandIn(
    @SerialName("device_id") val deviceId: String,
    val signal: String,
    val value: JsonPrimitive,
)

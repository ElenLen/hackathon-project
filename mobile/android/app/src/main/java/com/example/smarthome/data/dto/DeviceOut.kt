package com.example.smarthome.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonPrimitive

@Serializable
data class DeviceOut(
    val id: String,
    @SerialName("ujin_sn") val ujinSn: String = "",
    val title: String = "",
    val family: String = "",
    @SerialName("apartment_id") val apartmentId: String = "",
    @SerialName("last_values") val lastValues: Map<String, JsonPrimitive> = emptyMap(),
    @SerialName("last_seen_at") val lastSeenAt: String? = null,
)

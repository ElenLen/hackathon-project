package com.example.smarthome.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SignalPoint(
    @SerialName("signal_name") val signalName: String = "",
    @SerialName("value_num") val valueNum: Double? = null,
    @SerialName("value_str") val valueStr: String? = null,
    val ts: String = "",
)

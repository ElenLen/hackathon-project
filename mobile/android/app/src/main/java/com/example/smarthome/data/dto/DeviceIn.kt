package com.example.smarthome.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeviceIn(
    val title: String,
    val family: String,
    @SerialName("apartment_id") val apartmentId: String = "",
)
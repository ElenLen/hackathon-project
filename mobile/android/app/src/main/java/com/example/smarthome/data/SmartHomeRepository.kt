package com.example.smarthome.data

import com.example.smarthome.data.dto.*
import kotlinx.serialization.json.JsonObject

interface SmartHomeRepository {
    suspend fun getDevices(): List<DeviceOut>
    suspend fun getDevice(id: String): DeviceOut
    suspend fun getSignals(deviceId: String, name: String?, limit: Int = 100): List<SignalPoint>
    suspend fun sendCommand(command: CommandIn): JsonObject
    suspend fun getScenarios(): List<ScenarioOut>
    suspend fun toggleScenario(id: String, enabled: Boolean): JsonObject
    suspend fun getRuns(id: String): List<ScenarioRun>
}
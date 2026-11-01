package com.example.smarthome.data

import com.example.smarthome.data.dto.*
import kotlinx.serialization.json.JsonObject

interface SmartHomeRepository {
    suspend fun getDevices(): List<DeviceOut>
    suspend fun getDevice(id: String): DeviceOut
    suspend fun getDeclaration(deviceId: String): List<SignalDeclaration>
    suspend fun getSignals(deviceId: String, name: String?, limit: Int = 100): List<SignalPoint>
    suspend fun sendCommand(command: CommandIn): JsonObject
    suspend fun addDevice(title: String, family: String, apartmentId: String = ""): DeviceOut

    suspend fun getScenarios(): List<ScenarioOut>
    suspend fun getScenarioDefinitions(): List<ScenarioDefinition>
    suspend fun createScenario(scenario: ScenarioDefinition): ScenarioDefinition
    suspend fun updateScenario(scenario: ScenarioDefinition): ScenarioDefinition
    suspend fun deleteScenario(id: String)
    suspend fun toggleScenario(id: String, enabled: Boolean): JsonObject
    suspend fun testScenario(id: String): ScenarioRun
    suspend fun getRuns(id: String): List<ScenarioRun>
}
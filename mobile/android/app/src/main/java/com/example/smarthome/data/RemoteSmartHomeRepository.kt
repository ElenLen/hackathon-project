package com.example.smarthome.data

import com.example.smarthome.data.dto.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement

class RemoteSmartHomeRepository(private val api: ApiService) : SmartHomeRepository {
    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        coerceInputValues = true
    }

    override suspend fun getDevices() = api.getDevices()
    override suspend fun getDevice(id: String) = api.getDevice(id)
    override suspend fun getDeclaration(deviceId: String) = api.getDeclaration(deviceId)
    override suspend fun getSignals(deviceId: String, name: String?, limit: Int) =
        api.getSignals(deviceId, name, limit)
    override suspend fun sendCommand(command: CommandIn) = api.sendCommand(command)

    override suspend fun getScenarios() = api.getScenarios()

    override suspend fun getScenarioDefinitions(): List<ScenarioDefinition> =
        api.getScenarios().map { it.toDefinition() }

    override suspend fun createScenario(scenario: ScenarioDefinition) =
        api.createScenario(scenario)

    override suspend fun updateScenario(scenario: ScenarioDefinition) =
        api.updateScenario(scenario.id, scenario)

    override suspend fun deleteScenario(id: String) {
        api.deleteScenario(id)
    }

    override suspend fun toggleScenario(id: String, enabled: Boolean) =
        api.toggleScenario(id, enabled)

    override suspend fun testScenario(id: String) = api.testScenario(id)
    override suspend fun getRuns(id: String) = api.getRuns(id)

    private fun ScenarioOut.toDefinition(): ScenarioDefinition {
        val parsed = runCatching {
            json.decodeFromJsonElement<ScenarioDefinition>(rule)
        }.getOrNull() ?: ScenarioDefinition()
        return parsed.copy(id = id, name = name, enabled = enabled)
    }
}

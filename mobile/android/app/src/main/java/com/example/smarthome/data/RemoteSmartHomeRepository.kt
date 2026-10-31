package com.example.smarthome.data

import com.example.smarthome.data.dto.*
import kotlinx.serialization.json.JsonObject

class RemoteSmartHomeRepository(private val api: ApiService) : SmartHomeRepository {
    override suspend fun getDevices() = api.getDevices()
    override suspend fun getDevice(id: String) = api.getDevice(id)
    override suspend fun getSignals(deviceId: String, name: String?, limit: Int) = 
        api.getSignals(deviceId, name, limit)
    override suspend fun sendCommand(command: CommandIn) = api.sendCommand(command)
    override suspend fun getScenarios() = api.getScenarios()
    override suspend fun toggleScenario(id: String, enabled: Boolean) = 
        api.toggleScenario(id, enabled)
    override suspend fun getRuns(id: String) = api.getRuns(id)
}
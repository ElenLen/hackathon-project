package com.example.smarthome.data.mock

import com.example.smarthome.data.SmartHomeRepository
import com.example.smarthome.data.dto.*
import kotlinx.coroutines.delay
import kotlinx.serialization.json.*
import kotlin.random.Random

class MockSmartHomeRepository : SmartHomeRepository {

    // Наша "база данных" в памяти
    private val devices = mutableMapOf<String, DeviceOut>(
        "dev-1" to DeviceOut(
            id = "dev-1", title = "Датчик протечки", family = "leak",
            lastValues = mapOf("leak" to JsonPrimitive(false)),
        ),
        "dev-2" to DeviceOut(
            id = "dev-2", title = "Термостат", family = "thermostat",
            lastValues = mapOf(
                "temperature" to JsonPrimitive(22.5),
                "target" to JsonPrimitive(23.0),
            ),
        ),
        "dev-3" to DeviceOut(
            id = "dev-3", title = "Свет в гостиной", family = "light",
            lastValues = mapOf(
                "on" to JsonPrimitive(true),
                "brightness" to JsonPrimitive(70),
            ),
        ),
        "dev-4" to DeviceOut(
            id = "dev-4", title = "Вентиляция", family = "ventilation",
            lastValues = mapOf("speed" to JsonPrimitive(30)),
        ),
        "dev-5" to DeviceOut(
            id = "dev-5", title = "Входная дверь", family = "door",
            lastValues = mapOf(
                "open" to JsonPrimitive(false),
                "online" to JsonPrimitive(true),
            ),
        ),
        "dev-6" to DeviceOut(
            id = "dev-6", title = "Домофон", family = "intercom",
            lastValues = mapOf(
                "call" to JsonPrimitive(false),
                "online" to JsonPrimitive(true),
            ),
        ),
        "dev-7" to DeviceOut(
            id = "dev-7", title = "Шлагбаум", family = "barrier",
            lastValues = mapOf(
                "open_cmd" to JsonPrimitive(0),
                "online" to JsonPrimitive(true),
            ),
        ),
    )

    private val scenarios = mutableMapOf<String, ScenarioOut>(
        "sc-1" to ScenarioOut(id = "sc-1", name = "Протечка → перекрыть воду", enabled = true),
        "sc-2" to ScenarioOut(id = "sc-2", name = "Ночной режим (свет 20%)", enabled = false),
        "sc-3" to ScenarioOut(id = "sc-3", name = "Ушёл из дома", enabled = true),
        "sc-4" to ScenarioOut(id = "sc-4", name = "Доброе утро", enabled = true),
        "sc-5" to ScenarioOut(id = "sc-5", name = "Экономия энергии", enabled = false),
    )

    override suspend fun getDevices(): List<DeviceOut> {
        delay(400) // Имитация загрузки из сети
        return devices.values.toList()
    }

    override suspend fun getDevice(id: String): DeviceOut {
        delay(200)
        return devices[id] ?: error("Устройство $id не найдено")
    }

    override suspend fun getSignals(deviceId: String, name: String?, limit: Int): List<SignalPoint> {
        delay(300)
        val dev = devices[deviceId] ?: return emptyList()
        val now = System.currentTimeMillis()
        // Генерируем фейковый график
        return (0 until minOf(limit, 10)).map { i ->
            val base = dev.lastValues.values.firstOrNull()?.content?.toDoubleOrNull() ?: 0.0
            SignalPoint(
                signalName = name ?: dev.lastValues.keys.firstOrNull() ?: "value",
                valueNum = base + Random.nextDouble(-2.0, 2.0), // Немного шума
                ts = ((now - i * 60_000L) / 1000).toString(),
            )
        }
    }

    override suspend fun sendCommand(command: CommandIn): JsonObject {
        delay(200)
        val dev = devices[command.deviceId] ?: error("Устройство не найдено")
        // Обновляем значение в памяти!
        devices[command.deviceId] = dev.copy(
            lastValues = dev.lastValues + (command.signal to command.value),
        )
        return buildJsonObject { put("status", "ok") }
    }

    override suspend fun getScenarios(): List<ScenarioOut> {
        delay(400)
        return scenarios.values.toList()
    }

    override suspend fun toggleScenario(id: String, enabled: Boolean): JsonObject {
        delay(200)
        scenarios[id] = scenarios[id]!!.copy(enabled = enabled)
        return buildJsonObject { put("status", "ok") }
    }

    override suspend fun getRuns(id: String): List<ScenarioRun> {
        delay(200)
        return emptyList() // Для демо можно вернуть пустой список
    }
}
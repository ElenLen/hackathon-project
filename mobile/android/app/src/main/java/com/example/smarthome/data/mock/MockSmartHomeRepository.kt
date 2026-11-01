package com.example.smarthome.data.mock

import com.example.smarthome.data.SmartHomeRepository
import com.example.smarthome.data.dto.*
import kotlinx.coroutines.delay
import kotlinx.serialization.json.*
import kotlin.math.abs
import kotlin.random.Random

class MockSmartHomeRepository : SmartHomeRepository {

    private val devices = mutableMapOf<String, DeviceOut>(
        "dev-2" to DeviceOut(
            id = "dev-2", title = "Термостат", family = "thermostat",
            lastValues = mapOf(
                "temperature" to JsonPrimitive(22.5),
                "target" to JsonPrimitive(23.0),
                "mode" to JsonPrimitive(3),
            ),
        ),
        "dev-3" to DeviceOut(
            id = "dev-3", title = "Свет в гостиной", family = "light",
            lastValues = mapOf(
                "on" to JsonPrimitive(0),
                "brightness" to JsonPrimitive(70),
            ),
        ),
        "dev-4" to DeviceOut(
            id = "dev-4", title = "Вентилятор", family = "ventilation",
            lastValues = mapOf(
                "speed" to JsonPrimitive(30),
                "co2" to JsonPrimitive(620),
            ),
        ),
        "dev-5" to DeviceOut(
            id = "dev-5", title = "Входная дверь", family = "door",
            lastValues = mapOf(
                "open" to JsonPrimitive(0),
                "online" to JsonPrimitive(1),
            ),
        ),
        "dev-6" to DeviceOut(
            id = "dev-6", title = "Домофон", family = "intercom",
            lastValues = mapOf(
                "call" to JsonPrimitive(0),
                "online" to JsonPrimitive(1),
            ),
        ),
        "dev-7" to DeviceOut(
            id = "dev-7", title = "Шлагбаум", family = "barrier",
            lastValues = mapOf(
                "open_cmd" to JsonPrimitive(0),
                "state" to JsonPrimitive(0),
                "obstacle" to JsonPrimitive(0),
            ),
        ),
    )

    private val declarations: Map<String, List<SignalDeclaration>> = mapOf(
        "thermostat" to listOf(
            declaration("temperature", "Температура", "graph", "r", "property", measure = "°C"),
            declaration("target", "Цель", "range", "rw", "setting", measure = "°C", min = 5.0, max = 35.0),
            declaration("mode", "Режим", "list", "rw", "capability", layout = mapOf("0" to "Выкл", "1" to "Обогрев", "2" to "Охлаждение", "3" to "Авто")),
        ),
        "light" to listOf(
            declaration("on", "Лампа", "switch", "rw", "capability", layout = mapOf("0" to "Выкл", "1" to "Вкл")),
            declaration("brightness", "Яркость", "range", "rw", "capability", measure = "%", min = 1.0, max = 100.0),
        ),
        "ventilation" to listOf(
            declaration("speed", "Скорость", "range", "rw", "capability", min = 0.0, max = 6.0),
            declaration("co2", "CO₂", "graph", "r", "property", measure = "ppm"),
        ),
        "door" to listOf(
            declaration("open", "Открытие", "switch", "rw", "capability", layout = mapOf("0" to "Закрыто", "1" to "Открыто")),
            declaration("online", "Связь", "list", "r", "property", layout = mapOf("0" to "Нет", "1" to "Есть")),
        ),
        "intercom" to listOf(
            declaration("call", "Вызов", "list", "r", "property", layout = mapOf("0" to "Нет", "1" to "Вызов")),
            declaration("online", "Связь", "list", "r", "property", layout = mapOf("0" to "Нет", "1" to "Есть")),
        ),
        "barrier" to listOf(
            declaration("open_cmd", "Открыть", "switch", "rw", "capability", layout = mapOf("0" to "Нет", "1" to "Открыть")),
            declaration("state", "Состояние", "list", "r", "property", layout = mapOf("0" to "Закрыт", "1" to "Открывается", "2" to "Открыт", "3" to "Закрывается", "4" to "Удержание")),
            declaration("hold_time", "Задержка", "counter", "rw", "setting", measure = "с", min = 0.0, max = 300.0),
            declaration("obstacle", "Препятствие", "list", "r", "property", layout = mapOf("0" to "Нет", "1" to "Есть")),
        ),
        "counter" to listOf(
            declaration("total", "Показания", "counter", "r", "property", measure = "кВт·ч"),
            declaration("power_w", "Мощность", "graph", "r", "property", measure = "Вт"),
            declaration("load_relay", "Нагрузка", "switch", "rw", "capability", layout = mapOf("0" to "Отключена", "1" to "Включена")),
            declaration("online", "Связь", "list", "r", "property", layout = mapOf("0" to "Нет", "1" to "Есть")),
        ),
        "fire" to listOf(
            declaration("smoke", "Задымлённость", "graph", "r", "property", measure = "%"),
            declaration("alarm", "Тревога", "list", "r", "property", layout = mapOf("0" to "Нет", "1" to "Есть")),
            declaration("battery", "Батарея", "graph", "r", "property", measure = "%"),
            declaration("threshold", "Порог", "counter", "rw", "setting", measure = "%", min = 10.0, max = 95.0),
        ),
        "elevator" to listOf(
            declaration("floor_current", "Текущий этаж", "counter", "r", "property", min = 1.0, max = 5.0),
            declaration("floor_destination", "Этаж назначения", "counter", "r", "property", min = 0.0, max = 5.0),
            declaration("direction", "Направление", "list", "r", "property", layout = mapOf("0" to "Стоит", "1" to "Вверх", "2" to "Вниз")),
            declaration("call_requested", "Вызов", "combolist", "w", "capability", min = 1.0, max = 5.0),
            declaration("emergency", "Авария", "list", "r", "property", layout = mapOf("0" to "Норма", "1" to "Авария")),
            declaration("door", "Дверь", "list", "r", "property", layout = mapOf("0" to "Закрыта", "1" to "Открыта")),
        ),
        "valve" to listOf(
            declaration("lamp", "Клапан", "switch", "rw", "capability", layout = mapOf("0" to "Закрыт", "1" to "Открыт")),
            declaration("brightness", "Положение", "range", "rw", "capability", measure = "%", min = 1.0, max = 100.0),
        ),
        "hub" to listOf(
            declaration("mute", "Без звука", "switch", "rw", "capability", layout = mapOf("0" to "Выкл", "1" to "Вкл")),
        ),
    )

    private val scenarios = mutableListOf<ScenarioDefinition>()
    private val runs = mutableListOf<ScenarioRun>()
    private var nextRunId = 1

    init {
        scenarios += ScenarioDefinition(
            id = "sc-2",
            name = "Жара → вентиляция",
            trigger = RuleSpec(id = "trigger", deviceId = "dev-2", signal = "temperature", operator = "gt", value = JsonPrimitive(26)),
            conditions = listOf(RuleSpec(id = "condition-1", deviceId = "dev-4", signal = "speed", operator = "lt", value = JsonPrimitive(6))),
            actions = listOf(ActionSpec(id = "action-2", deviceId = "dev-4", signal = "speed", value = JsonPrimitive(3))),
        )
        scenarios += ScenarioDefinition(
            id = "sc-3",
            name = "Открыть шлагбаум",
            enabled = false,
            trigger = RuleSpec(id = "trigger", deviceId = "dev-7", signal = "open_cmd", value = JsonPrimitive(1)),
            actions = listOf(ActionSpec(id = "action-3", deviceId = "dev-7", signal = "hold_time", value = JsonPrimitive(15))),
        )
    }

    override suspend fun getDevices(): List<DeviceOut> {
        delay(400)
        return devices.values.toList()
    }

    override suspend fun addDevice(title: String, family: String, apartmentId: String): DeviceOut {
        delay(300)
        val normalizedFamily = family.lowercase()
        val index = devices.size + 1
        val device = DeviceOut(
            id = "dev-$index",
            ujinSn = "ujin-${1000 + index}",
            title = title.ifBlank { "Новое устройство" },
            family = normalizedFamily,
            apartmentId = apartmentId.ifBlank { "apt-1" },
            lastValues = emptyMap(),
            lastSeenAt = null,
        )
        devices[device.id] = device
        return device
    }

    override suspend fun getDevice(id: String): DeviceOut {
        delay(200)
        return devices[id] ?: error("Устройство $id не найдено")
    }

    override suspend fun getDeclaration(deviceId: String): List<SignalDeclaration> {
        delay(100)
        val device = devices[deviceId] ?: return emptyList()
        return declarations[device.family.lowercase()] ?: device.lastValues.map { (name, value) ->
            guessDeclaration(name, value)
        }
    }

    override suspend fun getSignals(deviceId: String, name: String?, limit: Int): List<SignalPoint> {
        delay(300)
        val dev = devices[deviceId] ?: return emptyList()
        val now = System.currentTimeMillis()
        return (0 until minOf(limit, 10)).map { i ->
            val base = dev.lastValues.values.firstOrNull()?.content?.toDoubleOrNull() ?: 0.0
            SignalPoint(
                signalName = name ?: dev.lastValues.keys.firstOrNull() ?: "value",
                valueNum = base + Random.nextDouble(-2.0, 2.0),
                ts = ((now - i * 60_000L) / 1000).toString(),
            )
        }
    }

    override suspend fun sendCommand(command: CommandIn): JsonObject {
        delay(120)
        val dev = devices[command.deviceId] ?: error("Устройство не найдено")
        val previous = dev.lastValues[command.signal]
        devices[command.deviceId] = dev.copy(
            lastValues = dev.lastValues + (command.signal to command.value),
        )
        evaluate(devices[command.deviceId]!!, command.signal, command.value, previous)
        return buildJsonObject { put("status", "ok") }
    }

    override suspend fun getScenarios(): List<ScenarioOut> {
        delay(200)
        return scenarios.map { ScenarioOut(id = it.id, name = it.name, enabled = it.enabled) }
    }

    override suspend fun getScenarioDefinitions(): List<ScenarioDefinition> {
        delay(200)
        return scenarios.toList()
    }

    override suspend fun createScenario(scenario: ScenarioDefinition): ScenarioDefinition {
        delay(200)
        val id = scenario.id.ifBlank { newRuleId("scenario") }
        val created = scenario.copy(id = id).normalized()
        scenarios.removeAll { it.id == id }
        scenarios += created
        return created
    }

    override suspend fun updateScenario(scenario: ScenarioDefinition): ScenarioDefinition {
        delay(200)
        val index = scenarios.indexOfFirst { it.id == scenario.id }
        if (index < 0) {
            scenarios += scenario.normalized()
        } else {
            scenarios[index] = scenario.normalized()
        }
        return scenario
    }

    override suspend fun deleteScenario(id: String) {
        delay(150)
        scenarios.removeAll { it.id == id }
        runs.removeAll { it.scenarioId == id }
    }

    override suspend fun toggleScenario(id: String, enabled: Boolean): JsonObject {
        delay(150)
        val index = scenarios.indexOfFirst { it.id == id }
        if (index >= 0) {
            scenarios[index] = scenarios[index].copy(enabled = enabled)
        }
        return buildJsonObject { put("status", "ok") }
    }

    override suspend fun testScenario(id: String): ScenarioRun {
        delay(150)
        val scenario = scenarios.firstOrNull { it.id == id } ?: error("Сценарий не найден")
        val applied = scenario.actions.mapNotNull { action ->
            applyAction(action)?.let { "${action.deviceId}:${action.signal}=${action.value.content}" }
        }
        val run = ScenarioRun(
            id = nextRunId++,
            scenarioId = id,
            triggeredAt = System.currentTimeMillis().toString(),
            triggerSnapshot = "manual",
            actionsJson = applied.joinToString(", "),
            ok = 1,
        )
        runs += run
        return run
    }

    override suspend fun getRuns(id: String): List<ScenarioRun> {
        delay(150)
        return runs.filter { it.scenarioId == id }.reversed()
    }

    private fun evaluate(device: DeviceOut, signal: String, value: JsonPrimitive, previous: JsonPrimitive?) {
        scenarios.filter { it.enabled }.forEach { scenario ->
            val trigger = scenario.trigger
            if (trigger.deviceId != device.id || trigger.signal != signal) return@forEach
            if (!matches(value, trigger, previous)) return@forEach
            val conditionsOk = scenario.conditions.all { rule ->
                val current = devices[rule.deviceId]?.lastValues?.get(rule.signal)
                current != null && matches(current, rule, null)
            }
            if (!conditionsOk) return@forEach

            val applied = scenario.actions.mapNotNull { action ->
                applyAction(action)?.let { "${action.deviceId}:${action.signal}=${action.value.content}" }
            }
            runs += ScenarioRun(
                id = nextRunId++,
                scenarioId = scenario.id,
                triggeredAt = System.currentTimeMillis().toString(),
                triggerSnapshot = "${device.id}:$signal=${value.content}",
                actionsJson = applied.joinToString(", "),
                ok = 1,
            )
        }
    }

    private fun applyAction(action: ActionSpec): DeviceOut? {
        if (action.deviceId.isBlank() || action.signal.isBlank()) return null
        val target = devices[action.deviceId] ?: return null
        val updated = target.copy(lastValues = target.lastValues + (action.signal to action.value))
        devices[action.deviceId] = updated
        return updated
    }

    private fun matches(actual: JsonPrimitive, rule: RuleSpec, previous: JsonPrimitive?): Boolean {
        val expected = rule.value
        return when (rule.operator) {
            "eq" -> valuesEqual(actual, expected)
            "ne" -> !valuesEqual(actual, expected)
            "gt" -> compare(actual, expected) > 0
            "gte" -> compare(actual, expected) >= 0
            "lt" -> compare(actual, expected) < 0
            "lte" -> compare(actual, expected) <= 0
            "changed" -> previous != null && actual.content != previous.content
            else -> false
        }
    }

    private fun valuesEqual(a: JsonPrimitive, b: JsonPrimitive): Boolean {
        val an = a.content.toDoubleOrNull()
        val bn = b.content.toDoubleOrNull()
        if (an != null && bn != null) return abs(an - bn) < 0.0001
        return a.content.equals(b.content, ignoreCase = true)
    }

    private fun compare(a: JsonPrimitive, b: JsonPrimitive): Int {
        val an = a.content.toDoubleOrNull()
        val bn = b.content.toDoubleOrNull()
        if (an != null && bn != null) return an.compareTo(bn)
        return a.content.compareTo(b.content)
    }

    private fun declaration(
        name: String,
        title: String,
        control: String,
        management: String,
        kind: String,
        measure: String = "",
        min: Double? = null,
        max: Double? = null,
        layout: Map<String, String>? = null,
    ) = SignalDeclaration(
        name = name,
        title = title,
        control = control,
        management = management,
        kind = kind,
        group = if (kind == "setting") "setting" else if (management == "r") "property" else "capability",
        measure = measure,
        min = min,
        max = max,
        layout = layout,
    )
}
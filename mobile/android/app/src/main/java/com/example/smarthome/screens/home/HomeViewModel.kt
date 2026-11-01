package com.example.smarthome.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smarthome.data.RepositoryProvider
import com.example.smarthome.data.dto.DeviceOut
import com.example.smarthome.data.dto.ScenarioOut
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.booleanOrNull

enum class QuickAction(
    val title: String,
    val subtitle: String,
) {
    DOORS("Двери", "Открыть"),
    INTERCOM("Домофон", "Вызов"),
    BARRIER("Шлагбаум", "Открыть"),
    CAMERA("Камеры", "Просмотр"),
}

data class HomeState(
    val devices: List<DeviceOut> = emptyList(),
    val scenarios: List<ScenarioOut> = emptyList(),
    val loading: Boolean = true,
    val error: String? = null,
    val message: String? = null,
    val busyScenarioId: String? = null,
) {
    val problemDevices: List<DeviceOut>
        get() = devices.filter(::isProblemDevice)

    val allGood: Boolean
        get() = problemDevices.isEmpty()
}

class HomeViewModel : ViewModel() {
    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            while (true) {
                try {
                    val (devices, scenarios) = coroutineScope {
                        val devicesRequest = async { RepositoryProvider.repository.getDevices() }
                        val scenariosRequest = async { RepositoryProvider.repository.getScenarios() }
                        devicesRequest.await() to scenariosRequest.await()
                    }
                    _state.update {
                        it.copy(
                            devices = devices,
                            scenarios = scenarios,
                            error = null,
                            loading = false,
                        )
                    }
                } catch (e: Exception) {
                    _state.update {
                        it.copy(error = e.message ?: "Ошибка сети", loading = false)
                    }
                }
                delay(3000)
            }
        }
    }

    fun onQuickAction(action: QuickAction) {
        _state.update { it.copy(message = "«${action.title}» скоро появится") }
    }

    fun runScenario(id: String) {
        val snapshot = _state.value
        val scenario = snapshot.scenarios.firstOrNull { it.id == id } ?: return
        if (snapshot.busyScenarioId != null) return

        viewModelScope.launch {
            _state.update { it.copy(busyScenarioId = id) }
            try {
                RepositoryProvider.repository.toggleScenario(id, true)
                _state.update { current ->
                    current.copy(
                        scenarios = current.scenarios.map {
                            if (it.id == id) it.copy(enabled = true) else it
                        },
                        message = "Сценарий «${scenario.name}» запущен",
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(message = "Не удалось запустить: ${e.message ?: "ошибка"}")
                }
            } finally {
                _state.update { it.copy(busyScenarioId = null) }
            }
        }
    }

    fun setScenarioEnabled(id: String, enabled: Boolean) {
        val snapshot = _state.value
        val scenario = snapshot.scenarios.firstOrNull { it.id == id } ?: return
        if (snapshot.busyScenarioId != null) return

        viewModelScope.launch {
            _state.update { it.copy(busyScenarioId = id) }
            try {
                RepositoryProvider.repository.toggleScenario(id, enabled)
                _state.update { current ->
                    current.copy(
                        scenarios = current.scenarios.map {
                            if (it.id == id) it.copy(enabled = enabled) else it
                        },
                        message = if (enabled) {
                            "Сценарий «${scenario.name}» включён"
                        } else {
                            "Сценарий «${scenario.name}» выключен"
                        },
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(message = "Не удалось изменить сценарий: ${e.message ?: "ошибка"}")
                }
            } finally {
                _state.update { it.copy(busyScenarioId = null) }
            }
        }
    }

    fun consumeMessage() {
        _state.update { it.copy(message = null) }
    }
}

private val alarmSignals = setOf("leak", "smoke", "fire", "gas", "flood", "alarm")
private val onlineSignals = setOf("online", "connected", "reachable")
private val batterySignals = setOf("battery", "battery_level", "charge")

private val classPrefixes = setOf(
    "integrations_devices",
    "integrations",
    "parkcloud_device",
    "devices",
)

private fun isProblemDevice(device: DeviceOut): Boolean =
    device.lastValues.any { (name, value) ->
        when (normalizeSignalName(name)) {
            in alarmSignals -> isTruthy(value)
            in onlineSignals -> !isTruthy(value)
            in batterySignals -> (value.content.toDoubleOrNull() ?: 100.0) <= 10.0
            else -> false
        }
    }

private fun normalizeSignalName(name: String): String {
    var signal = name.trim().lowercase()
    var parts = signal.split("_")
    while (parts.size > 1) {
        val head = parts.first()
        val isClassPrefix = head in classPrefixes ||
            parts.take(2).joinToString("_") in classPrefixes
        if (!isClassPrefix) break
        val cut = if (parts.take(2).joinToString("_") in classPrefixes) 2 else 1
        parts = parts.drop(cut)
        signal = parts.joinToString("_")
    }
    return signal
}

private fun isTruthy(value: kotlinx.serialization.json.JsonPrimitive): Boolean {
    value.booleanOrNull?.let { return it }
    val text = value.content.trim().lowercase()
    return when (text) {
        "true", "1", "on", "yes", "open", "opened", "alarm" -> true
        "false", "0", "off", "no", "closed", "normal", "ok", "idle" -> false
        else -> text.toDoubleOrNull()?.let { it != 0.0 } ?: false
    }
}

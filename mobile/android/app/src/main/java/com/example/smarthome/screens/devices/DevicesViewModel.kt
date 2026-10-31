package com.example.smarthome.screens.devices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smarthome.data.RepositoryProvider
import com.example.smarthome.data.dto.DeviceOut
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DevicesUiState(
    val devices: List<DeviceOut> = emptyList(),
    val loading: Boolean = true,
    val saving: Boolean = false,
    val error: String? = null,
    val message: String? = null,
)

class DevicesViewModel : ViewModel() {
    private val _state = MutableStateFlow(DevicesUiState())
    val state: StateFlow<DevicesUiState> = _state.asStateFlow()

    init {
        loadDevices()
    }

    fun loadDevices() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            try {
                val devices = RepositoryProvider.repository.getDevices()
                _state.update { it.copy(devices = devices, loading = false) }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        loading = false,
                        error = e.message ?: "Не удалось загрузить устройства",
                    )
                }
            }
        }
    }

    fun addDevice(title: String, family: String) {
        if (_state.value.saving) return
        viewModelScope.launch {
            _state.update { it.copy(saving = true, error = null) }
            try {
                val device = RepositoryProvider.repository.addDevice(
                    title = title.trim(),
                    family = family,
                )
                _state.update { current ->
                    current.copy(
                        devices = current.devices + device,
                        saving = false,
                        message = "Устройство «${device.title}» добавлено",
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        saving = false,
                        error = e.message ?: "Не удалось добавить устройство",
                    )
                }
            }
        }
    }

    fun consumeMessage() {
        _state.update { it.copy(message = null) }
    }

    fun consumeError() {
        _state.update { it.copy(error = null) }
    }
}
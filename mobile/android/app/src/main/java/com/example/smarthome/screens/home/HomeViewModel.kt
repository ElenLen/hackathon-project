package com.example.smarthome.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smarthome.data.RepositoryProvider // <-- НОВЫЙ ИМПОРТ
import com.example.smarthome.data.dto.DeviceOut
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeState(
    val devices: List<DeviceOut> = emptyList(),
    val loading: Boolean = true,
    val error: String? = null,
)

class HomeViewModel : ViewModel() {
    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            while (true) {
                try {
                    // БЕРЕМ ДАННЫЕ ИЗ НАШЕГО ПРОВАЙДЕРА
                    val devices = RepositoryProvider.repository.getDevices()
                    _state.update { it.copy(devices = devices, error = null, loading = false) }
                } catch (e: Exception) {
                    _state.update { it.copy(error = e.message ?: "Ошибка сети", loading = false) }
                }
                delay(3000) // Обновляем каждые 3 секунды
            }
        }
    }
}
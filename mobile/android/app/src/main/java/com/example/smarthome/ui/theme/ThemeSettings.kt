package com.example.smarthome.ui.theme

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object ThemeSettings {
    private val _darkTheme = MutableStateFlow(false)
    val darkTheme: StateFlow<Boolean> = _darkTheme.asStateFlow()

    fun setDarkTheme(enabled: Boolean) {
        _darkTheme.value = enabled
    }
}

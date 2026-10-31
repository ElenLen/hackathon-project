package com.example.smarthome.screens.scenarios

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ScenarioRule(
    val deviceId: String,
    val deviceTitle: String,
    val signal: String,
    val operator: String? = null,
    val value: String,
) {
    val ruleText: String
        get() = if (operator == null) {
            "$signal = $value"
        } else {
            "$signal $operator $value"
        }
}

data class UserScenario(
    val id: String,
    val name: String,
    val conditions: List<ScenarioRule>,
    val actions: List<ScenarioRule>,
) {
    val summary: String
        get() = "${conditions.size} ${plural(conditions.size, "условие", "условия", "условий")} · " +
            "${actions.size} ${plural(actions.size, "действие", "действия", "действий")}"
}

private fun plural(count: Int, one: String, few: String, many: String): String = when {
    count % 10 == 1 && count % 100 != 11 -> one
    count % 10 in 2..4 && count % 100 !in 12..14 -> few
    else -> many
}

object UserScenarioStore {
    private val _scenarios = MutableStateFlow<List<UserScenario>>(emptyList())
    val scenarios: StateFlow<List<UserScenario>> = _scenarios.asStateFlow()

    fun add(scenario: UserScenario) {
        _scenarios.update { it + scenario }
    }
}

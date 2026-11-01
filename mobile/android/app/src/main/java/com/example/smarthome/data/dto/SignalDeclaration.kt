package com.example.smarthome.data.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonPrimitive

@Serializable
data class SignalDeclaration(
    val name: String = "",
    val title: String = "",
    val control: String = "text",
    val management: String = "r",
    val kind: String = "property",
    val group: String = "property",
    val measure: String = "",
    val min: Double? = null,
    val max: Double? = null,
    val layout: Map<String, String>? = null,
) {
    val readable: Boolean
        get() = management == "r" || management == "rw"

    val writable: Boolean
        get() = management == "rw" || management == "w"

    fun label(value: JsonPrimitive): String {
        val key = value.content
        layout?.get(key)?.let { return it }
        layout?.entries?.firstOrNull { it.key.equals(key, ignoreCase = true) }?.let { return it.value }
        val suffix = measure.takeIf { it.isNotBlank() }
        return if (suffix != null) "$key $suffix" else key
    }

    fun defaultValue(current: JsonPrimitive?): JsonPrimitive {
        current?.let { return it }
        layout?.keys?.firstOrNull()?.let { key ->
            return key.toIntOrNull()?.let { JsonPrimitive(it) } ?: JsonPrimitive(key)
        }
        return when (control) {
            "switch" -> JsonPrimitive(0)
            "range", "counter" -> JsonPrimitive(min ?: 0.0)
            else -> JsonPrimitive(0)
        }
    }
}

fun List<SignalDeclaration>.readable(): List<SignalDeclaration> = filter { it.readable }

fun List<SignalDeclaration>.writable(): List<SignalDeclaration> = filter { it.writable }

fun guessDeclaration(name: String, value: JsonPrimitive): SignalDeclaration {
    val numeric = value.content.toDoubleOrNull()
    val control = when {
        numeric == null -> "text"
        value.content == "0" || value.content == "1" -> "switch"
        else -> "graph"
    }
    return SignalDeclaration(
        name = name,
        title = name,
        control = control,
        management = "rw",
        kind = "property",
        group = "property",
    )
}

package com.example.smarthome.data

import com.example.smarthome.data.dto.CommandIn
import com.example.smarthome.data.dto.DeviceIn
import com.example.smarthome.data.dto.DeviceOut
import com.example.smarthome.data.dto.ScenarioDefinition
import com.example.smarthome.data.dto.ScenarioOut
import com.example.smarthome.data.dto.ScenarioRun
import com.example.smarthome.data.dto.SignalDeclaration
import com.example.smarthome.data.dto.SignalPoint
import kotlinx.serialization.json.JsonObject
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("health")
    suspend fun health(): Map<String, String>

    @GET("devices")
    suspend fun getDevices(): List<DeviceOut>

    @GET("devices/{id}")
    suspend fun getDevice(@Path("id") id: String): DeviceOut

    @GET("devices/{id}/declaration")
    suspend fun getDeclaration(@Path("id") id: String): List<SignalDeclaration>

    @GET("signals")
    suspend fun getSignals(
        @Query("device_id") deviceId: String,
        @Query("name") name: String? = null,
        @Query("limit") limit: Int = 100,
    ): List<SignalPoint>

    @POST("commands")
    suspend fun sendCommand(@Body command: CommandIn): JsonObject

    @POST("devices")
    suspend fun addDevice(@Body device: DeviceIn): DeviceOut

    @GET("scenarios")
    suspend fun getScenarios(): List<ScenarioOut>

    @POST("scenarios")
    suspend fun createScenario(@Body scenario: ScenarioDefinition): ScenarioDefinition

    @PUT("scenarios/{id}")
    suspend fun updateScenario(
        @Path("id") id: String,
        @Body scenario: ScenarioDefinition,
    ): ScenarioDefinition

    @DELETE("scenarios/{id}")
    suspend fun deleteScenario(@Path("id") id: String): JsonObject

    @PATCH("scenarios/{id}")
    suspend fun toggleScenario(
        @Path("id") id: String,
        @Query("enabled") enabled: Boolean,
    ): JsonObject

    @POST("scenarios/{id}/test")
    suspend fun testScenario(@Path("id") id: String): ScenarioRun

    @GET("scenarios/{id}/runs")
    suspend fun getRuns(@Path("id") id: String): List<ScenarioRun>
}

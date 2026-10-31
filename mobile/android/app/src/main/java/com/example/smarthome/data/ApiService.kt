package com.example.smarthome.data

import com.example.smarthome.data.dto.CommandIn
import com.example.smarthome.data.dto.DeviceOut
import com.example.smarthome.data.dto.ScenarioOut
import com.example.smarthome.data.dto.ScenarioRun
import com.example.smarthome.data.dto.SignalPoint
import kotlinx.serialization.json.JsonObject
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("health")
    suspend fun health(): Map<String, String>

    @GET("devices")
    suspend fun getDevices(): List<DeviceOut>

    @GET("devices/{id}")
    suspend fun getDevice(@Path("id") id: String): DeviceOut

    @GET("signals")
    suspend fun getSignals(
        @Query("device_id") deviceId: String,
        @Query("name") name: String? = null,
        @Query("limit") limit: Int = 100,
    ): List<SignalPoint>

    @POST("commands")
    suspend fun sendCommand(@Body command: CommandIn): JsonObject

    @GET("scenarios")
    suspend fun getScenarios(): List<ScenarioOut>

    @PATCH("scenarios/{id}")
    suspend fun toggleScenario(
        @Path("id") id: String,
        @Query("enabled") enabled: Boolean,
    ): JsonObject

    @GET("scenarios/{id}/runs")
    suspend fun getRuns(@Path("id") id: String): List<ScenarioRun>
}

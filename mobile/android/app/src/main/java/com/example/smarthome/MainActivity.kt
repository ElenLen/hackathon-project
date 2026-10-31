package com.example.smarthome

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.lifecycle.lifecycleScope
import com.example.smarthome.data.ApiClient
import com.example.smarthome.ui.theme.UjinTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            try {
                val devices = ApiClient.api.getDevices()
                Log.d("API", "devices=${devices.size}: ${devices.map { it.id }}")
            } catch (e: Exception) {
                Log.e("API", "FAILED: ${e.message}", e)
            }
        }

        setContent {
            UjinTheme {
                Text("Ujin — see Logcat")
            }
        }
    }
}
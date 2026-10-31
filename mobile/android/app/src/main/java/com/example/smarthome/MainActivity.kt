package com.example.smarthome

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.smarthome.ui.navigation.NavGraph
import com.example.smarthome.ui.theme.ThemeSettings
import com.example.smarthome.ui.theme.UjinTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val darkTheme by ThemeSettings.darkTheme.collectAsState()
            UjinTheme(darkTheme = darkTheme) { NavGraph() }
        }
    }
}
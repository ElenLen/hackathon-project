package com.example.smarthome.data

import com.example.smarthome.BuildConfig
import com.example.smarthome.data.mock.MockSmartHomeRepository

object RepositoryProvider {
    // Приложение будет использовать это свойство во всех ViewModel
    val repository: SmartHomeRepository by lazy {
        if (BuildConfig.USE_MOCK) {
            MockSmartHomeRepository()
        } else {
            RemoteSmartHomeRepository(ApiClient.api)
        }
    }
}
package com.example.mpazpro.data.remote

import com.example.mpazpro.BuildConfig

// Dirección del backend. Sale de build.gradle.kts (propiedad -PapiBaseUrl); por defecto es la del emulador.
object ApiConfig {
    val baseUrl: String = BuildConfig.API_BASE_URL.let { if (it.endsWith("/")) it else "$it/" }
}

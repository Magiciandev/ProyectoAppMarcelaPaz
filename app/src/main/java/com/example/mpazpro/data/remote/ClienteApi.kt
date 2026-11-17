package com.example.mpazpro.data.remote

import com.example.mpazpro.data.remote.api.AdminApi
import com.example.mpazpro.data.remote.api.AuthApi
import com.example.mpazpro.data.remote.api.ContenidosApi
import com.example.mpazpro.data.remote.api.EvaluacionesApi
import com.example.mpazpro.data.remote.api.ProgresoApi
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Un solo cliente HTTP y una sola URL base (el gateway) para los cinco servicios.
 * Se crea una vez en MainActivity y se reparte a los repositorios que lo necesiten.
 */
class ClienteApi(baseUrl: String, sesion: SesionStore) {

    private val http = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor(sesion))
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(http)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val auth: AuthApi by lazy { retrofit.create(AuthApi::class.java) }
    val contenidos: ContenidosApi by lazy { retrofit.create(ContenidosApi::class.java) }
    val evaluaciones: EvaluacionesApi by lazy { retrofit.create(EvaluacionesApi::class.java) }
    val progreso: ProgresoApi by lazy { retrofit.create(ProgresoApi::class.java) }
    val admin: AdminApi by lazy { retrofit.create(AdminApi::class.java) }
}

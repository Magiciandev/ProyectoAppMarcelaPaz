package com.example.mpazpro.data.remote

import okhttp3.Interceptor
import okhttp3.Response

/** Agrega «Authorization: Bearer <token>» a cada petición, salvo el login (que es público). */
class AuthInterceptor(private val sesion: SesionStore) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val token = sesion.token()
        val esLogin = original.url.encodedPath.endsWith("auth/login")
        val peticion = if (token == null || esLogin || original.header("Authorization") != null) {
            original
        } else {
            original.newBuilder().header("Authorization", "Bearer $token").build()
        }
        return chain.proceed(peticion)
    }
}

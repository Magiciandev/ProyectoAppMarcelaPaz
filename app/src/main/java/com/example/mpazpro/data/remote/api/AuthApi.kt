package com.example.mpazpro.data.remote.api

import com.example.mpazpro.data.remote.dto.LoginRequestDto
import com.example.mpazpro.data.remote.dto.LoginResponseDto
import com.example.mpazpro.data.remote.dto.UsuarioMeDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

// Una interfaz por servicio del backend. Todas salen por el mismo gateway (una sola URL base).
// Las rutas van SIN «/» inicial: Retrofit las une a la URL base, que termina en «/».
interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body cuerpo: LoginRequestDto): LoginResponseDto

    @GET("auth/me")
    suspend fun me(): UsuarioMeDto
}

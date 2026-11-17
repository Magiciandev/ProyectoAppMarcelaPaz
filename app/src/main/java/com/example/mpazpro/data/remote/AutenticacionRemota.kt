package com.example.mpazpro.data.remote

import com.example.mpazpro.data.remote.api.AuthApi
import com.example.mpazpro.data.remote.dto.LoginRequestDto
import com.example.mpazpro.data.remote.dto.LoginResponseDto

/** Inicia y cierra la sesión con el servidor. Si el login resulta, deja el token guardado para las demás llamadas. */
class AutenticacionRemota(
    private val api: AuthApi,
    private val sesion: SesionStore
) {
    suspend fun iniciarSesion(usuario: String, pin: String): ResultadoRemoto<LoginResponseDto> {
        val resultado = llamarApi { api.login(LoginRequestDto(usuario, pin)) }
        if (resultado is ResultadoRemoto.Ok) {
            sesion.guardar(resultado.valor, usuario)
        }
        return resultado
    }

    fun cerrarSesion() = sesion.cerrar()
}

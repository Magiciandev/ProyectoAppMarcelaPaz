package com.example.mpazpro.data.remote

import com.example.mpazpro.data.remote.dto.ErrorApiDto
import com.google.gson.Gson
import com.google.gson.JsonParseException
import java.io.IOException
import retrofit2.HttpException

/**
 * Resultado de una llamada al backend, sin excepciones a la vista:
 *  - Ok: respondió bien.
 *  - ErrorHttp: el servidor respondió con un error (por ejemplo 401 o 409), con el mensaje ya listo para mostrar.
 *  - SinConexion: no se pudo llegar al servidor (sin red, apagado o tiempo agotado).
 */
sealed interface ResultadoRemoto<out T> {
    data class Ok<T>(val valor: T) : ResultadoRemoto<T>
    data class ErrorHttp(val codigo: Int, val mensaje: String) : ResultadoRemoto<Nothing>
    data class SinConexion(val mensaje: String) : ResultadoRemoto<Nothing>
}

/** Ejecuta una llamada de Retrofit y convierte sus fallos en un [ResultadoRemoto]. */
suspend fun <T> llamarApi(bloque: suspend () -> T): ResultadoRemoto<T> =
    try {
        ResultadoRemoto.Ok(bloque())
    } catch (e: HttpException) {
        ResultadoRemoto.ErrorHttp(e.code(), ApiErrores.mensajeDe(e))
    } catch (e: JsonParseException) {
        ResultadoRemoto.ErrorHttp(0, "El servidor respondió algo inesperado")
    } catch (e: IOException) {
        ResultadoRemoto.SinConexion("No hay conexión con el servidor")
    }

object ApiErrores {
    private val gson = Gson()

    /** Usa el mensaje que mandó el backend; si no hay cuerpo (por ejemplo un 401 o 403), uno según el código. */
    fun mensajeDe(e: HttpException): String {
        val cuerpo = runCatching { e.response()?.errorBody()?.string() }.getOrNull()
        val delServidor = cuerpo
            ?.takeIf { it.isNotBlank() }
            ?.let { runCatching { gson.fromJson(it, ErrorApiDto::class.java)?.message }.getOrNull() }
        return delServidor?.takeIf { it.isNotBlank() } ?: mensajePorCodigo(e.code())
    }

    fun mensajePorCodigo(codigo: Int): String = when (codigo) {
        400 -> "Los datos enviados no son válidos"
        401 -> "Tu sesión no es válida o venció; ingresa de nuevo"
        403 -> "No tienes permiso para hacer esto"
        404 -> "No se encontró lo que buscabas"
        409 -> "No se puede hacer en el estado actual"
        in 500..599 -> "El servidor tuvo un problema; intenta de nuevo"
        else -> "No se pudo completar la operación"
    }
}

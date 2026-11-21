package com.example.mpazpro.data.repository

import com.example.mpazpro.data.remote.ResultadoRemoto

enum class ResultadoSincronizacion { COMPLETA, REINTENTAR }

/**
 * Lo que se envía en segundo plano, en orden de prioridad: primero los intentos de prueba y después el avance
 * (son los datos críticos para el docente; los contenidos nuevos esperan). Recibe las dos operaciones como función
 * para poder probar el orden y la decisión de reintentar sin red ni Android.
 */
class Sincronizador(
    private val enviarIntentos: suspend () -> ResultadoRemoto<*>,
    private val enviarAvance: suspend () -> ResultadoRemoto<*>
) {
    suspend fun ejecutar(): ResultadoSincronizacion {
        val intentos = enviarIntentos()
        // Sin red no tiene sentido intentar lo siguiente.
        if (intentos is ResultadoRemoto.SinConexion) return ResultadoSincronizacion.REINTENTAR
        val avance = enviarAvance()
        return if (conviene(intentos) || conviene(avance)) ResultadoSincronizacion.REINTENTAR else ResultadoSincronizacion.COMPLETA
    }

    /** Reintentar si no hubo red o el servidor falló (5xx). Un 4xx no mejora reintentando: se deja para el próximo envío. */
    private fun conviene(r: ResultadoRemoto<*>): Boolean =
        r is ResultadoRemoto.SinConexion || (r is ResultadoRemoto.ErrorHttp && r.codigo >= 500)
}

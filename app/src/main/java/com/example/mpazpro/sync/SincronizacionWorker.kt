package com.example.mpazpro.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.mpazpro.data.local.AppDatabase
import com.example.mpazpro.data.remote.AlmacenSharedPreferences
import com.example.mpazpro.data.remote.ApiConfig
import com.example.mpazpro.data.remote.ClienteApi
import com.example.mpazpro.data.remote.SesionStore
import com.example.mpazpro.data.repository.ActividadesServidor
import com.example.mpazpro.data.repository.BandejaIntentos
import com.example.mpazpro.data.repository.CacheEvaluacionRoom
import com.example.mpazpro.data.repository.ResultadoSincronizacion
import com.example.mpazpro.data.repository.Sincronizador

/**
 * Envía en segundo plano lo que el estudiante dejó pendiente: primero los intentos de prueba y luego el avance.
 * Corre solo con conexión y se reintenta con espera creciente si falla. Solo envía lo de quien tiene la sesión
 * abierta (el token es de esa persona): sin sesión de estudiante no hace nada.
 */
class SincronizacionWorker(contexto: Context, parametros: WorkerParameters) : CoroutineWorker(contexto, parametros) {

    override suspend fun doWork(): Result {
        val sesion = SesionStore(AlmacenSharedPreferences(applicationContext))
        val actual = sesion.actual() ?: return Result.success()
        if (actual.rol != ROL_ESTUDIANTE) return Result.success()

        val baseDeDatos = AppDatabase.obtener(applicationContext)
        // Los datos locales usan el id de Room, no el del servidor: se busca al usuario de la sesión por su nombre de usuario.
        val local = baseDeDatos.usuarioDao().buscarPorUsuario(actual.usuario) ?: return Result.success()

        val cliente = ClienteApi(ApiConfig.baseUrl, sesion)
        val cache = CacheEvaluacionRoom(baseDeDatos.cacheEvaluacionDao())
        val bandeja = BandejaIntentos(cliente.progreso, baseDeDatos.intentoPendienteDao(), alAceptar = cache::sumarIntentoAceptado)
        val avance = crearAvanceServidor(cliente.progreso, ActividadesServidor(cliente.evaluaciones, cache), baseDeDatos)

        val resultado = Sincronizador(
            enviarIntentos = { bandeja.enviar(local.id) },
            enviarAvance = { avance.sincronizar(local.id) }
        ).ejecutar()
        return if (resultado == ResultadoSincronizacion.COMPLETA) Result.success() else Result.retry()
    }

    private companion object {
        const val ROL_ESTUDIANTE = "ESTUDIANTE"
    }
}

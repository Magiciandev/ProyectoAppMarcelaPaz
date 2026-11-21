package com.example.mpazpro.data.repository

import com.example.mpazpro.data.local.IntentoPendienteDao
import com.example.mpazpro.data.model.IntentoPendiente
import com.example.mpazpro.data.remote.ResultadoRemoto
import com.example.mpazpro.data.remote.api.ProgresoApi
import com.example.mpazpro.data.remote.dto.IntentoSyncDto
import com.example.mpazpro.data.remote.dto.ResultadoIntentoDto
import com.example.mpazpro.data.remote.dto.SyncRequestDto
import com.example.mpazpro.data.remote.llamarApi
import com.google.gson.Gson
import java.time.Instant

/**
 * Los intentos de prueba del estudiante que esperan confirmación del servidor. Primero se guardan en Room y después
 * se envían a `POST /progreso/sync`; solo se borran cuando el servidor responde algo definitivo para ese intento.
 * Un fallo de red o del servidor los deja guardados para el siguiente intento de envío.
 */
class BandejaIntentos(
    private val api: ProgresoApi,
    private val dao: IntentoPendienteDao,
    private val ahora: () -> Instant = { Instant.now() },
    private val gson: Gson = Gson()
) {

    /** Guarda el intento antes de enviarlo. Repetir el mismo uuid no lo duplica. */
    suspend fun guardar(usuarioId: Long, intento: IntentoSyncDto) {
        dao.guardar(
            IntentoPendiente(
                uuid = intento.uuid,
                usuarioId = usuarioId,
                objetivoId = intento.objetivoId,
                numeroIntento = intento.numeroIntento,
                fechaHora = fechaEnMillis(intento.fechaHora),
                intentoJson = gson.toJson(intento)
            )
        )
    }

    /** Cuántos intentos de ese objetivo siguen sin confirmar (se descuentan de los que le quedan al estudiante). */
    suspend fun pendientesDe(usuarioId: Long, objetivoId: Long): Int = dao.contar(usuarioId, objetivoId)

    /**
     * Envía todos los pendientes en un solo lote, del más antiguo al más nuevo. Devuelve la respuesta del servidor
     * por uuid. Sin nada pendiente no llama al servidor. Los intentos sin respuesta quedan guardados.
     */
    suspend fun enviar(usuarioId: Long): ResultadoRemoto<Map<String, ResultadoIntentoDto>> {
        val pendientes = dao.pendientes(usuarioId)
        if (pendientes.isEmpty()) return ResultadoRemoto.Ok(emptyMap())
        val intentos = pendientes.map { gson.fromJson(it.intentoJson, IntentoSyncDto::class.java) }
        val uuidsPendientes = pendientes.map { it.uuid }.toSet()
        return llamarApi {
            val respuesta = api.sincronizar(SyncRequestDto(enviadoEn = ahora().toString(), intentos = intentos))
            val definitivos = respuesta.intentos
                .filter { it.uuid != null && it.uuid in uuidsPendientes && it.resultado in DEFINITIVOS }
                .associateBy { it.uuid!! }
            if (definitivos.isNotEmpty()) dao.borrar(definitivos.keys.toList())
            definitivos
        }
    }

    private fun fechaEnMillis(texto: String): Long =
        runCatching { Instant.parse(texto).toEpochMilli() }.getOrElse { ahora().toEpochMilli() }

    private companion object {
        val DEFINITIVOS = setOf("ACEPTADO", "DUPLICADO", "RECHAZADO")
    }
}

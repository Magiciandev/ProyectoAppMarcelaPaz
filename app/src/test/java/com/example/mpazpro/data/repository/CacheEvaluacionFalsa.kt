package com.example.mpazpro.data.repository

import com.example.mpazpro.data.remote.dto.AsignacionEstudianteDto
import com.example.mpazpro.data.remote.dto.PaqueteEvaluacionDto

/** Caché en memoria para las pruebas (hace lo mismo que la de Room, sin Android). Con `fallaAlGuardar` simula un disco con problemas. */
class CacheEvaluacionFalsa(private val fallaAlGuardar: Boolean = false) : CacheEvaluacion {
    val asignacionesPorUsuario = mutableMapOf<Long, List<AsignacionEstudianteDto>>()
    val paquetes = mutableMapOf<Long, PaqueteEvaluacionDto>()

    override suspend fun guardarAsignaciones(usuarioId: Long, asignaciones: List<AsignacionEstudianteDto>) {
        if (fallaAlGuardar) throw IllegalStateException("disco lleno")
        asignacionesPorUsuario[usuarioId] = asignaciones
    }

    override suspend fun asignaciones(usuarioId: Long) = asignacionesPorUsuario[usuarioId].orEmpty()

    override suspend fun asignacion(usuarioId: Long, objetivoId: Long) =
        asignaciones(usuarioId).firstOrNull { it.objetivoId == objetivoId }

    override suspend fun sumarIntentoAceptado(usuarioId: Long, objetivoId: Long) {
        asignacionesPorUsuario[usuarioId] = asignaciones(usuarioId).map {
            if (it.objetivoId == objetivoId) it.copy(intentosUsados = it.intentosUsados + 1) else it
        }
    }

    override suspend fun guardarPaquete(paquete: PaqueteEvaluacionDto) {
        if (fallaAlGuardar) throw IllegalStateException("disco lleno")
        paquetes[paquete.objetivoId] = paquete
    }

    override suspend fun paquete(objetivoId: Long) = paquetes[objetivoId]
}

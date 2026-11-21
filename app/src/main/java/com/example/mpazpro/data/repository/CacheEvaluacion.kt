package com.example.mpazpro.data.repository

import com.example.mpazpro.data.local.CacheEvaluacionDao
import com.example.mpazpro.data.model.CacheAsignacionEstudiante
import com.example.mpazpro.data.model.CachePaquete
import com.example.mpazpro.data.remote.dto.AsignacionEstudianteDto
import com.example.mpazpro.data.remote.dto.PaqueteEvaluacionDto
import com.google.gson.Gson

/**
 * Lo que la app guarda en el teléfono para poder practicar y rendir la prueba sin conexión.
 * Es una interfaz para poder probar los repositorios con una caché en memoria, sin Room.
 */
interface CacheEvaluacion {
    /** Guarda las asignaciones vigentes del estudiante (reemplaza las anteriores). */
    suspend fun guardarAsignaciones(usuarioId: Long, asignaciones: List<AsignacionEstudianteDto>)
    suspend fun asignaciones(usuarioId: Long): List<AsignacionEstudianteDto>
    suspend fun asignacion(usuarioId: Long, objetivoId: Long): AsignacionEstudianteDto?

    /** Un intento del estudiante fue aceptado por el servidor: se suma a los usados que se muestran sin conexión. */
    suspend fun sumarIntentoAceptado(usuarioId: Long, objetivoId: Long)

    suspend fun guardarPaquete(paquete: PaqueteEvaluacionDto)
    suspend fun paquete(objetivoId: Long): PaqueteEvaluacionDto?
}

/** La caché de verdad: Room, con el paquete guardado como JSON. */
class CacheEvaluacionRoom(
    private val dao: CacheEvaluacionDao,
    private val gson: Gson = Gson()
) : CacheEvaluacion {

    override suspend fun guardarAsignaciones(usuarioId: Long, asignaciones: List<AsignacionEstudianteDto>) {
        dao.reemplazarAsignaciones(usuarioId, asignaciones.map {
            CacheAsignacionEstudiante(
                usuarioId, it.objetivoId, it.asignacionId, it.fechaDesde, it.fechaHasta,
                it.limiteIntentos, it.fechaHabilitacion, it.fechaRendicion, it.intentosUsados
            )
        })
    }

    override suspend fun asignaciones(usuarioId: Long): List<AsignacionEstudianteDto> =
        dao.asignaciones(usuarioId).map { aDto(it) }

    override suspend fun asignacion(usuarioId: Long, objetivoId: Long): AsignacionEstudianteDto? =
        dao.asignacion(usuarioId, objetivoId)?.let { aDto(it) }

    private fun aDto(it: CacheAsignacionEstudiante) = AsignacionEstudianteDto(
        it.asignacionId, it.objetivoId, it.fechaDesde, it.fechaHasta,
        it.limiteIntentos, it.fechaHabilitacion, it.fechaRendicion, it.intentosUsados
    )

    override suspend fun sumarIntentoAceptado(usuarioId: Long, objetivoId: Long) =
        dao.sumarIntentoUsado(usuarioId, objetivoId)

    override suspend fun guardarPaquete(paquete: PaqueteEvaluacionDto) =
        dao.guardarPaquete(CachePaquete(paquete.objetivoId, gson.toJson(paquete)))

    // Si el texto guardado no se puede leer (por ejemplo, cambió el formato), es como no tener nada guardado.
    override suspend fun paquete(objetivoId: Long): PaqueteEvaluacionDto? =
        dao.paquete(objetivoId)?.json?.let { runCatching { gson.fromJson(it, PaqueteEvaluacionDto::class.java) }.getOrNull() }
}

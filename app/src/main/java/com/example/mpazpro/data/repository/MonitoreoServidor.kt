package com.example.mpazpro.data.repository

import com.example.mpazpro.data.remote.ResultadoRemoto
import com.example.mpazpro.data.remote.api.ProgresoApi
import com.example.mpazpro.data.remote.dto.EstudianteCursoDto
import com.example.mpazpro.data.remote.llamarApi
import java.time.Instant

/** Un estudiante tal como lo ve el docente al monitorear: lo que guarda el servidor, ya con las fechas convertidas. */
data class EstudianteMonitoreo(
    /** El id del usuario en el servidor (el mismo que usan resumen, intentos e informes). */
    val usuarioId: Long,
    val nombre: String,
    val curso: String,
    /** Promedio de avance de sus objetivos; nulo si el servidor aún no tiene avance suyo. */
    val avanceGlobal: Int?,
    val ultimaConexion: Instant?,
    val ultimaSincronizacion: Instant?
)

object MapeoMonitoreo {
    /** Las fechas llegan como texto ISO-8601 en UTC («Z»); si vienen vacías o mal escritas, quedan en null. */
    fun instante(texto: String?): Instant? =
        texto?.takeIf { it.isNotBlank() }?.let { runCatching { Instant.parse(it) }.getOrNull() }

    fun aFila(nombreCurso: String, dto: EstudianteCursoDto) = EstudianteMonitoreo(
        usuarioId = dto.usuarioId,
        nombre = dto.nombre,
        curso = nombreCurso,
        avanceGlobal = dto.avanceGlobal?.coerceIn(0, 100),
        ultimaConexion = instante(dto.ultimaConexion),
        ultimaSincronizacion = instante(dto.ultimaSincronizacion)
    )
}

/**
 * Monitoreo del docente (y de UTP) con datos del servidor: los estudiantes de cada curso suyo, con su avance y las
 * dos fechas de la decisión D11 (última conexión y última sincronización). El servidor decide qué cursos le tocan.
 */
class MonitoreoServidor(private val api: ProgresoApi) {

    suspend fun estudiantes(): ResultadoRemoto<List<EstudianteMonitoreo>> =
        llamarApi {
            api.cursos().flatMap { curso ->
                api.estudiantesDelCurso(curso.id).map { MapeoMonitoreo.aFila(curso.nombre, it) }
            }
        }
}

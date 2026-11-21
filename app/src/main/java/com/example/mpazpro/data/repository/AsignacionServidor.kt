package com.example.mpazpro.data.repository

import com.example.mpazpro.data.model.Asignacion
import com.example.mpazpro.data.remote.ResultadoRemoto
import com.example.mpazpro.data.remote.api.ProgresoApi
import com.example.mpazpro.data.remote.dto.AsignacionDto
import com.example.mpazpro.data.remote.dto.AsignacionEstudianteDto
import com.example.mpazpro.data.remote.dto.AsignacionRequestDto
import com.example.mpazpro.data.remote.llamarApi

/**
 * Asignar un objetivo a un curso en el servidor (ms-progreso). Solo el docente dueño del curso puede hacerlo, y el
 * objetivo debe estar autorizado o publicado por UTP: si no, el servidor responde con el motivo.
 */
class AsignacionServidor(
    private val api: ProgresoApi,
    // Solo la usa el estudiante (para ver sus asignaciones sin conexión); sin ella todo funciona como antes.
    private val cache: CacheEvaluacion? = null
) {

    /** Crea la asignación o, si el objetivo ya estaba asignado a ese curso, la actualiza. */
    suspend fun guardar(objetivoId: Long, asignacion: Asignacion, intentos: Int): ResultadoRemoto<AsignacionDto> =
        when (val resultado = llamarApi { guardarEnServidor(objetivoId, asignacion, intentos) }) {
            is ResultadoRemoto.Ok -> {
                val dto = resultado.valor
                if (dto == null) {
                    ResultadoRemoto.ErrorHttp(404, "El curso «${asignacion.curso}» no existe en el servidor")
                } else {
                    ResultadoRemoto.Ok(dto)
                }
            }
            is ResultadoRemoto.ErrorHttp -> resultado
            is ResultadoRemoto.SinConexion -> resultado
        }

    // Devuelve null si el servidor no tiene ese curso.
    private suspend fun guardarEnServidor(objetivoId: Long, asignacion: Asignacion, intentos: Int): AsignacionDto? {
        val cursoId = api.cursos()
            .firstOrNull { it.nombre.trim().equals(asignacion.curso.trim(), ignoreCase = true) }?.id
            ?: return null
        val cuerpo = AsignacionRequestDto(
            objetivoId = objetivoId,
            cursoId = cursoId,
            fechaDesde = MapeoAsignacion.fechaIso(asignacion.desde),
            fechaHasta = MapeoAsignacion.fechaIso(asignacion.hasta),
            limiteIntentos = intentos,
            disponible = asignacion.activa
        )
        // El servidor no deja crear dos veces la misma asignación (responde 409): si ya existe, se edita.
        val existente = api.asignaciones(cursoId).firstOrNull { it.objetivoId == objetivoId && it.cursoId == cursoId }
        return if (existente != null) api.actualizarAsignacion(existente.id, cuerpo) else api.crearAsignacion(cuerpo)
    }

    /** Estudiante: lo que le asignaron y está vigente (de su curso o individual), con los intentos que lleva usados. */
    suspend fun misAsignaciones(estudianteId: Long? = null): ResultadoRemoto<List<AsignacionEstudianteDto>> {
        val enLinea = llamarApi { api.misAsignaciones() }
        val guardado = cache
        if (guardado == null || estudianteId == null) return enLinea
        return when (enLinea) {
            // Con conexión se guarda lo que entregó el servidor; sin conexión se usa lo último guardado.
            is ResultadoRemoto.Ok -> enLinea.also { runCatching { guardado.guardarAsignaciones(estudianteId, enLinea.valor) } }
            is ResultadoRemoto.ErrorHttp -> enLinea
            is ResultadoRemoto.SinConexion -> guardado.asignaciones(estudianteId)
                .takeIf { it.isNotEmpty() }?.let { ResultadoRemoto.Ok(it) } ?: enLinea
        }
    }

    /** Estudiante: avisa al servidor que se conectó (el docente ve la última conexión de cada estudiante). */
    suspend fun registrarConexion(): ResultadoRemoto<Boolean> =
        llamarApi { api.conexion().isSuccessful }

    /** Las asignaciones de un objetivo en todos los cursos del docente, listas para mostrar. */
    suspend fun listar(objetivoId: Long, unidadId: Long): ResultadoRemoto<List<Asignacion>> =
        llamarApi {
            api.cursos().flatMap { curso ->
                api.asignaciones(curso.id)
                    .filter { it.objetivoId == objetivoId && it.cursoId == curso.id }
                    .map { MapeoAsignacion.aLocal(it, curso.nombre, unidadId) }
            }
        }
}

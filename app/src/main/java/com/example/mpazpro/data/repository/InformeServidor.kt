package com.example.mpazpro.data.repository

import com.example.mpazpro.data.model.FichaEstudiante
import com.example.mpazpro.data.model.MapeoInforme
import com.example.mpazpro.data.remote.ResultadoRemoto
import com.example.mpazpro.data.remote.api.ProgresoApi
import com.example.mpazpro.data.remote.dto.InformeDto
import com.example.mpazpro.data.remote.dto.InformeRequestDto
import com.example.mpazpro.data.remote.dto.ReinicioDto
import com.example.mpazpro.data.remote.dto.ReinicioRequestDto
import com.example.mpazpro.data.remote.dto.ValidacionRequestDto
import com.example.mpazpro.data.remote.llamarApi

/**
 * Informes y reinicio del docente contra el servidor (ms-progreso). El servidor calcula el resumen y el nivel; el
 * docente escribe la observación y valida (no hay diagnóstico automático). [nombresDeObjetivos] entrega los nombres
 * guardados en el catálogo del teléfono, porque el servidor responde solo con ids.
 */
class InformeServidor(
    private val api: ProgresoApi,
    private val nombresDeObjetivos: suspend () -> Map<Long, String> = { emptyMap() }
) {
    /** Resumen del estudiante y su informe más reciente (el servidor los entrega del más nuevo al más antiguo). */
    suspend fun cargar(estudianteId: Long): ResultadoRemoto<FichaEstudiante> = llamarApi {
        val resumen = api.resumen(estudianteId)
        val ultimo = api.informes(null, estudianteId).firstOrNull()
        val detalle = ultimo?.let { api.informe(it.id) }
        MapeoInforme.ficha(resumen, detalle, nombresDeObjetivos())
    }

    /** Emite un informe nuevo con lo que lleva el estudiante hoy; si aún no rinde ninguna prueba, el servidor responde 409. */
    suspend fun generar(estudianteId: Long): ResultadoRemoto<InformeDto> =
        llamarApi { api.generarInforme(InformeRequestDto(estudianteId)) }

    /** Guarda la observación sin validar el informe. */
    suspend fun guardarObservacion(informeId: Long, texto: String): ResultadoRemoto<InformeDto> =
        llamarApi { api.validarInforme(informeId, ValidacionRequestDto(observacion = texto.trim())) }

    /** Valida el informe (y, si se entrega, guarda la observación escrita). */
    suspend fun validar(informeId: Long, texto: String?): ResultadoRemoto<InformeDto> =
        llamarApi { api.validarInforme(informeId, ValidacionRequestDto(observacion = texto?.trim(), validar = true)) }

    /** Reinicia todos los objetivos del estudiante. El servidor archiva los intentos y el avance: no borra nada. */
    suspend fun reiniciar(estudianteId: Long, motivo: String? = null): ResultadoRemoto<ReinicioDto> =
        llamarApi { api.reiniciar(estudianteId, ReinicioRequestDto(objetivoId = null, motivo = motivo)) }
}

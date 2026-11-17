package com.example.mpazpro.data.remote.dto

// ---------------------------------------------------------------- estudiante

data class EstudianteMeDto(val usuarioId: Long, val nombre: String, val cursoId: Long, val cursoNombre: String? = null)

data class AsignacionEstudianteDto(
    val asignacionId: Long,
    val objetivoId: Long,
    val fechaDesde: String,
    val fechaHasta: String,
    val limiteIntentos: Int,
    val fechaHabilitacion: String? = null,
    val fechaRendicion: String? = null,
    val intentosUsados: Long = 0
)

data class AvanceObjetivoDto(
    val objetivoId: Long,
    val porcentajeAvance: Int,
    val intentosUsados: Long,
    val intentosRestantes: Long,
    val limiteIntentos: Int
)

data class AvanceDto(val objetivos: List<AvanceObjetivoDto> = emptyList(), val ultimoReinicio: String? = null)

// ---------------------------------------------------------------- sincronización

data class RespuestaSyncDto(
    val uuid: String,
    val preguntaId: Long,
    /** Texto JSON: la opción elegida, el orden elegido o los pares unidos. */
    val respuesta: String,
    val esCorrecta: Boolean,
    val puntajeObtenido: Double,
    val marcaTiempo: String
)

data class IntentoSyncDto(
    val uuid: String,
    val pruebaId: Long,
    val objetivoId: Long,
    val numeroIntento: Int,
    val puntajeObtenido: Double,
    val puntajeMaximo: Double,
    val fechaHora: String,
    val respuestas: List<RespuestaSyncDto> = emptyList()
)

data class ProgresoSyncDto(
    val objetivoId: Long,
    val contenidosCompletados: Int,
    val actividadesCompletadas: Int,
    val desafiosCompletados: Int,
    val porcentajeAvance: Int,
    val marcaTiempo: String
)

data class SyncRequestDto(
    val enviadoEn: String? = null,
    val intentos: List<IntentoSyncDto> = emptyList(),
    val progreso: List<ProgresoSyncDto> = emptyList()
)

/** resultado: ACEPTADO, DUPLICADO o RECHAZADO (con motivo). */
data class ResultadoIntentoDto(val uuid: String?, val resultado: String, val motivo: String? = null)

data class ResultadoProgresoDto(val objetivoId: Long?, val resultado: String, val motivo: String? = null)

data class MejorObjetivoDto(
    val objetivoId: Long,
    val mejorPorcentaje: Int? = null,
    val intentosAceptados: Long,
    val limiteIntentos: Int? = null
)

data class SyncResponseDto(
    val intentos: List<ResultadoIntentoDto> = emptyList(),
    val progreso: List<ResultadoProgresoDto> = emptyList(),
    val mejorPorObjetivo: List<MejorObjetivoDto> = emptyList(),
    val ultimoReinicio: String? = null
)

// ---------------------------------------------------------------- docente y UTP

data class CursoDto(val id: Long, val nombre: String, val docenteId: Long)

data class EstudianteCursoDto(
    val usuarioId: Long,
    val nombre: String,
    val avanceGlobal: Int? = null,
    val ultimaConexion: String? = null,
    val ultimaSincronizacion: String? = null
)

data class AsignacionRequestDto(
    val objetivoId: Long,
    val cursoId: Long? = null,
    val estudianteId: Long? = null,
    val fechaDesde: String,
    val fechaHasta: String,
    val limiteIntentos: Int? = null,
    val fechaHabilitacion: String? = null,
    val fechaRendicion: String? = null,
    val disponible: Boolean? = null
)

data class AsignacionDto(
    val id: Long,
    val objetivoId: Long,
    val cursoId: Long? = null,
    val estudianteId: Long? = null,
    val docenteId: Long,
    val disponible: Boolean,
    val fechaDesde: String,
    val fechaHasta: String,
    val limiteIntentos: Int,
    val fechaHabilitacion: String? = null,
    val fechaRendicion: String? = null
)

data class ResumenObjetivoDto(
    val objetivoId: Long,
    val porcentajeAvance: Int,
    val intentosAceptados: Long,
    val limiteIntentos: Int,
    val mejorPorcentaje: Int? = null,
    /** DOMINA, EN_VIAS o REFUERZO; nulo si todavía no rinde la prueba. */
    val nivel: String? = null
)

data class ResumenDto(
    val estudianteId: Long,
    val nombre: String,
    val objetivos: List<ResumenObjetivoDto> = emptyList(),
    val promedioGeneral: Int? = null,
    val nivelGeneral: String? = null
)

data class RespuestaDto(val preguntaId: Long, val respuesta: String, val esCorrecta: Boolean, val puntajeObtenido: Double)

data class IntentoDto(
    val id: Long,
    val uuid: String,
    val objetivoId: Long,
    val pruebaId: Long,
    val numeroIntento: Int,
    val puntajeObtenido: Double,
    val puntajeMaximo: Double,
    val porcentaje: Int,
    val fechaHora: String,
    val aceptado: Boolean,
    val motivoRechazo: String? = null,
    val respuestas: List<RespuestaDto> = emptyList()
)

data class InformeRequestDto(val estudianteId: Long, val objetivoId: Long? = null)

data class ValidacionRequestDto(val observacion: String? = null, val validar: Boolean? = null)

data class InformeObjetivoDto(val objetivoId: Long, val mejorPorcentaje: Int, val nivel: String, val intentosAceptados: Int)

data class InformeRespuestaDto(val objetivoId: Long, val preguntaId: Long, val esCorrecta: Boolean, val puntaje: Double)

data class InformeDto(
    val id: Long,
    val estudianteId: Long,
    val cursoId: Long,
    val objetivoId: Long? = null,
    val puntajeGlobal: Double,
    val nivel: String,
    val fechaEmision: String,
    val validado: Boolean,
    val validadoPor: Long? = null,
    val fechaValidacion: String? = null,
    val observacionDocente: String? = null,
    /** El desglose solo viene al pedir un informe en particular, no en el listado. */
    val porObjetivo: List<InformeObjetivoDto>? = null,
    val areasRefuerzo: List<Long>? = null,
    val respuestas: List<InformeRespuestaDto>? = null
)

data class ReinicioRequestDto(val objetivoId: Long? = null, val motivo: String? = null)

data class ReinicioDto(val fecha: String, val objetivoId: Long? = null)

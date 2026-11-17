package com.example.mpazpro.data.remote.dto

/**
 * Una opción solo trae los campos que usa su tipo de pregunta:
 * esCorrecta (SELECCION y COMPLETACION), posicionCorrecta (SECUENCIA) o parClave (ASOCIACION).
 */
data class OpcionDto(
    val id: Long? = null,
    val texto: String,
    val esCorrecta: Boolean? = null,
    val posicionCorrecta: Int? = null,
    val parClave: String? = null,
    val orden: Int? = null
)

data class PreguntaDto(
    val id: Long,
    val objetivoId: Long,
    val tipo: String,
    val enunciado: String,
    val explicacion: String? = null,
    val opciones: List<OpcionDto> = emptyList()
)

data class PreguntaRequestDto(
    val objetivoId: Long,
    val tipo: String,
    val enunciado: String,
    val explicacion: String? = null,
    val opciones: List<OpcionDto>
)

data class PreguntaEnActividadDto(val preguntaId: Long, val orden: Int? = null)

data class PreguntaEnPruebaDto(val preguntaId: Long, val ordenEnPrueba: Int? = null, val puntaje: Double? = null)

data class ActividadDto(
    val id: Long,
    val objetivoId: Long,
    val titulo: String,
    val tipo: String,
    val dificultad: Int,
    val orden: Int,
    val estado: String,
    /** Solo viene en el paquete; en los listados es nulo. */
    val preguntas: List<PreguntaEnActividadDto>? = null
)

data class ActividadRequestDto(
    val objetivoId: Long,
    val titulo: String,
    val tipo: String,
    val dificultad: Int? = null,
    val orden: Int? = null
)

data class PruebaDto(
    val id: Long,
    val objetivoId: Long,
    val titulo: String,
    val criteriosPuntuacion: String? = null,
    val limiteIntentosPorDefecto: Int,
    val estado: String,
    val preguntas: List<PreguntaEnPruebaDto> = emptyList()
)

data class PruebaRequestDto(
    val objetivoId: Long,
    val titulo: String,
    val criteriosPuntuacion: String? = null,
    val limiteIntentos: Int? = null
)

/** Actividades, preguntas (con solucionario) y prueba de un objetivo, para la caché y el modo sin conexión. */
data class PaqueteEvaluacionDto(
    val objetivoId: Long,
    val actividades: List<ActividadDto> = emptyList(),
    val preguntas: List<PreguntaDto> = emptyList(),
    val prueba: PruebaDto? = null
)

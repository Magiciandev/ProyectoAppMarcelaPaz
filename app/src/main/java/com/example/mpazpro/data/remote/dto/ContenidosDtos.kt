package com.example.mpazpro.data.remote.dto

data class AsignaturaDto(val id: Long, val nombre: String, val nivel: String)

data class UnidadDto(
    val id: Long,
    val asignaturaId: Long,
    val titulo: String,
    val orden: Int,
    val estado: String,
    val fechaInicio: String? = null,
    val fechaFin: String? = null,
    val creadoPor: Long? = null,
    val creadoEn: String? = null
)

data class UnidadRequestDto(
    val asignaturaId: Long? = null,
    val titulo: String,
    val orden: Int? = null,
    val fechaInicio: String? = null,
    val fechaFin: String? = null
)

data class ObjetivoDto(
    val id: Long,
    val unidadId: Long,
    val codigo: String,
    val descripcion: String,
    val ejeCurricular: String? = null,
    val textoReferencia: String? = null,
    val orden: Int,
    val estado: String
)

data class ObjetivoRequestDto(
    val codigo: String,
    val descripcion: String,
    val ejeCurricular: String? = null,
    val textoReferencia: String? = null,
    val orden: Int? = null
)

data class ContenidoDto(
    val id: Long,
    val objetivoId: Long,
    val titulo: String,
    val explicacion: String,
    val instrucciones: String? = null,
    val ejemplosGuiados: String? = null,
    val imagenUrl: String? = null,
    val archivoAudio: String? = null,
    val orden: Int,
    val responsableOrigen: String,
    val licencia: String
)

data class ContenidoRequestDto(
    val titulo: String,
    val explicacion: String,
    val instrucciones: String? = null,
    val ejemplosGuiados: String? = null,
    val imagenUrl: String? = null,
    val archivoAudio: String? = null,
    val orden: Int? = null,
    val responsableOrigen: String,
    val licencia: String
)

/** Un objetivo con todos sus contenidos: lo que se guarda en la caché de la app. */
data class PaqueteContenidosDto(val objetivo: ObjetivoDto, val contenidos: List<ContenidoDto>)

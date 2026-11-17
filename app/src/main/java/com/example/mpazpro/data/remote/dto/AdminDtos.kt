package com.example.mpazpro.data.remote.dto

/** accion: ENVIAR_REVISION, AUTORIZAR, RECHAZAR, PUBLICAR, DESHABILITAR o REABRIR. */
data class AprobacionRequestDto(
    val entidad: String = "OBJETIVO",
    val entidadId: Long,
    val accion: String,
    val observacion: String? = null
)

data class AprobacionResponseDto(
    val entidad: String,
    val entidadId: Long,
    val estadoAnterior: String,
    val estadoNuevo: String,
    val fecha: String
)

data class HistorialDto(
    val id: Long,
    val entidad: String,
    val entidadId: Long,
    val responsableUtpId: Long,
    val accion: String,
    val estadoAnterior: String,
    val estadoNuevo: String,
    val fecha: String,
    val observacion: String? = null
)

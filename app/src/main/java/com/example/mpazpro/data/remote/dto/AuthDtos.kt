package com.example.mpazpro.data.remote.dto

// DTO = «objeto de transferencia»: la forma exacta del JSON que viaja entre la app y el backend.
// Los nombres siguen el documento «Contrato de la API y modelo de datos v2».
// Todo lo que puede venir vacío es nulable; las fechas y horas viajan como texto ISO-8601 (UTC).

data class LoginRequestDto(val usuario: String, val pin: String)

data class LoginResponseDto(
    val token: String,
    val tipo: String? = null,
    val expiraEn: String,
    val usuarioId: Long,
    val nombre: String,
    val rol: String
)

data class UsuarioMeDto(val usuarioId: Long, val usuario: String, val nombre: String, val rol: String)

/** Cuerpo de error de todos los servicios (mismo formato en cada uno). */
data class ErrorApiDto(
    val timestamp: String? = null,
    val status: Int? = null,
    val error: String? = null,
    val message: String? = null,
    val path: String? = null
)

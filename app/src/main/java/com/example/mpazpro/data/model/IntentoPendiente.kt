package com.example.mpazpro.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Un intento de prueba ya terminado que todavía no se confirmó en el servidor. Se guarda en el teléfono antes de
 * enviarlo: así, si no hay conexión o la app se cierra, el resultado no se pierde. Se borra cuando el servidor
 * responde algo definitivo (aceptado, duplicado o rechazado con motivo).
 * `uuid` es el del intento (lo crea el dispositivo y hace los reenvíos idempotentes). `usuarioId` es el id local.
 * `fechaHora` va en milisegundos y ordena el envío; `intentoJson` es el intento completo, con sus respuestas.
 */
@Entity(tableName = "intento_pendiente")
data class IntentoPendiente(
    @PrimaryKey val uuid: String,
    val usuarioId: Long,
    val objetivoId: Long,
    val numeroIntento: Int,
    val fechaHora: Long,
    val intentoJson: String
)

package com.example.mpazpro.data.model

import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * Decisión D11: el docente ve la última conexión y la última sincronización de cada estudiante, y este mensaje le
 * ayuda a distinguir si el estudiante no avanzó porque no practicó o porque no se conectó a subir sus datos.
 * Los días se cuentan en el calendario del colegio (America/Santiago), igual que el resto de las fechas.
 */
object MensajeInactividad {
    /** Desde cuántos días se considera «inactivo» (en la conexión o en el envío de avances). */
    const val DIAS_DE_AVISO = 3L

    fun para(ultimaConexion: Instant?, ultimaSincronizacion: Instant?, ahora: Instant): String {
        // Si el servidor solo conoce el envío de avances, esa fecha también prueba que hubo conexión.
        val conexion = ultimaConexion ?: ultimaSincronizacion ?: return "Aún no se ha conectado."
        val diasConexion = diasDesde(conexion, ahora)
        if (diasConexion >= DIAS_DE_AVISO) return "Lleva $diasConexion días sin conectarse."
        if (ultimaSincronizacion == null) {
            return "Se conectó ${hace(diasConexion)}, pero aún no ha enviado avances."
        }
        val diasSync = diasDesde(ultimaSincronizacion, ahora)
        return if (diasSync >= DIAS_DE_AVISO) {
            "Se conectó ${hace(diasConexion)}, pero no envía avances desde ${hace(diasSync)}."
        } else {
            "Al día: se conectó ${hace(diasConexion)} y envió avances ${hace(diasSync)}."
        }
    }

    /** Días de calendario (en la zona del colegio) entre un instante y ahora; nunca negativo. */
    fun diasDesde(momento: Instant, ahora: Instant): Long =
        ChronoUnit.DAYS.between(ZonaColegio.fechaDe(momento), ZonaColegio.fechaDe(ahora)).coerceAtLeast(0)

    private fun hace(dias: Long): String = when (dias) {
        0L -> "hoy"
        1L -> "ayer"
        else -> "hace $dias días"
    }
}

/** Fecha y hora para mostrar al docente, en la zona del colegio. */
object FechaHoraColegio {
    private val FORMATO: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm").withZone(ZonaColegio.ZONA)

    fun formatear(momento: Instant): String = FORMATO.format(momento)
}

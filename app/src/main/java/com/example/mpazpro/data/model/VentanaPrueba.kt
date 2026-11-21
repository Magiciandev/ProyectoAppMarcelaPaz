package com.example.mpazpro.data.model

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

// La zona horaria del colegio. El servidor (ms-progreso) compara las fechas de calendario en esta misma zona,
// así que la app usa la misma para avisar antes de que el servidor rechace un intento.
object ZonaColegio {
    val ZONA: ZoneId = ZoneId.of("America/Santiago")

    fun hoy(): LocalDate = LocalDate.now(ZONA)

    /** El día de calendario en que cae un instante (el servidor guarda y recibe instantes en UTC, con «Z»). */
    fun fechaDe(momento: Instant): LocalDate = LocalDate.ofInstant(momento, ZONA)
}

// Si hoy se puede rendir la prueba de una asignación.
sealed interface DisponibilidadPrueba {
    object EnFecha : DisponibilidadPrueba
    data class AunNoDisponible(val desde: LocalDate) : DisponibilidadPrueba
    data class YaCerrada(val hasta: LocalDate) : DisponibilidadPrueba
}

/**
 * La misma regla que aplica el servidor al recibir un intento (ReglasProgreso en ms-progreso):
 * se puede rendir desde el día más tardío entre el inicio de la asignación y la habilitación de la prueba,
 * hasta el día más temprano entre el fin de la asignación y la rendición de la prueba, ambos días incluidos.
 */
object VentanaPrueba {

    fun desde(fechaDesde: LocalDate, habilitacion: LocalDate?): LocalDate =
        if (habilitacion != null && habilitacion.isAfter(fechaDesde)) habilitacion else fechaDesde

    fun hasta(fechaHasta: LocalDate, rendicion: LocalDate?): LocalDate =
        if (rendicion != null && rendicion.isBefore(fechaHasta)) rendicion else fechaHasta

    fun evaluar(
        fechaDesde: LocalDate,
        fechaHasta: LocalDate,
        habilitacion: LocalDate?,
        rendicion: LocalDate?,
        hoy: LocalDate
    ): DisponibilidadPrueba {
        val desde = desde(fechaDesde, habilitacion)
        val hasta = hasta(fechaHasta, rendicion)
        return when {
            hoy.isBefore(desde) -> DisponibilidadPrueba.AunNoDisponible(desde)
            hoy.isAfter(hasta) -> DisponibilidadPrueba.YaCerrada(hasta)
            else -> DisponibilidadPrueba.EnFecha
        }
    }
}

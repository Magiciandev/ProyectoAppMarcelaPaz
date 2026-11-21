package com.example.mpazpro.data.repository

import com.example.mpazpro.data.model.Asignacion
import com.example.mpazpro.data.remote.dto.AsignacionDto
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

// Traduce entre la asignación de la app (fechas en milisegundos) y la del servidor (fechas «2026-11-19»).
// El selector de fecha de Material 3 trabaja en UTC, así que aquí también se usa UTC para no correr el día.
object MapeoAsignacion {
    private const val FIN_DE_DIA = 86_399_999L

    /** Milisegundos del selector de fecha -> «2026-11-19». */
    fun fechaIso(millis: Long): String =
        Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate().toString()

    /** «2026-11-19» -> milisegundos al comienzo de ese día. */
    fun inicioDelDia(fechaIso: String): Long =
        LocalDate.parse(fechaIso).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

    /** «2026-11-19» -> milisegundos al final de ese día (la app guarda «hasta» como el fin del día). */
    fun finDelDia(fechaIso: String): Long = inicioDelDia(fechaIso) + FIN_DE_DIA

    /** Una asignación del servidor, como la muestra la pantalla de asignar (no se guarda en Room). */
    fun aLocal(dto: AsignacionDto, nombreCurso: String, unidadId: Long): Asignacion =
        Asignacion(
            id = dto.id,
            unidadId = unidadId,
            curso = nombreCurso,
            desde = inicioDelDia(dto.fechaDesde),
            hasta = finDelDia(dto.fechaHasta),
            activa = dto.disponible
        )
}

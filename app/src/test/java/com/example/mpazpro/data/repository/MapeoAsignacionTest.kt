package com.example.mpazpro.data.repository

import com.example.mpazpro.data.remote.dto.AsignacionDto
import java.time.LocalDate
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class MapeoAsignacionTest {

    // El selector de fecha entrega la medianoche UTC del día elegido.
    private fun medianocheUtc(anio: Int, mes: Int, dia: Int): Long =
        LocalDate.of(anio, mes, dia).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

    @Test fun la_fecha_elegida_se_envia_sin_correr_el_dia() {
        assertEquals("2026-11-19", MapeoAsignacion.fechaIso(medianocheUtc(2026, 11, 19)))
    }

    @Test fun el_fin_del_dia_sigue_siendo_el_mismo_dia() {
        val finDelDia = medianocheUtc(2026, 12, 19) + 86_399_999L

        assertEquals("2026-12-19", MapeoAsignacion.fechaIso(finDelDia))
    }

    @Test fun de_texto_a_milisegundos_y_de_vuelta() {
        val inicio = MapeoAsignacion.inicioDelDia("2026-11-19")

        assertEquals(medianocheUtc(2026, 11, 19), inicio)
        assertEquals(inicio + 86_399_999L, MapeoAsignacion.finDelDia("2026-11-19"))
        assertEquals("2026-11-19", MapeoAsignacion.fechaIso(inicio))
    }

    @Test fun la_asignacion_del_servidor_se_ve_como_una_local() {
        val dto = AsignacionDto(
            id = 5, objetivoId = 100, cursoId = 1, docenteId = 2, disponible = false,
            fechaDesde = "2026-11-19", fechaHasta = "2026-12-19", limiteIntentos = 3
        )

        val local = MapeoAsignacion.aLocal(dto, "2° A", unidadId = 9)

        assertEquals(5L, local.id)
        assertEquals(9L, local.unidadId)
        assertEquals("2° A", local.curso)
        assertEquals(medianocheUtc(2026, 11, 19), local.desde)
        assertEquals(medianocheUtc(2026, 12, 19) + 86_399_999L, local.hasta)
        assertFalse(local.activa)
    }
}

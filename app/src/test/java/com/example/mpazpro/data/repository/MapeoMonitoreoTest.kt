package com.example.mpazpro.data.repository

import com.example.mpazpro.data.remote.dto.EstudianteCursoDto
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MapeoMonitoreoTest {
    @Test fun convierte_las_fechas_iso_a_instantes() {
        val fila = MapeoMonitoreo.aFila(
            "2° A",
            EstudianteCursoDto(3, "Ana", 85, "2026-11-20T15:00:00Z", "2026-11-20T15:30:00.250Z")
        )
        assertEquals(3L, fila.usuarioId)
        assertEquals("2° A", fila.curso)
        assertEquals(85, fila.avanceGlobal)
        assertEquals(Instant.parse("2026-11-20T15:00:00Z"), fila.ultimaConexion)
        assertEquals(Instant.parse("2026-11-20T15:30:00.250Z"), fila.ultimaSincronizacion)
    }

    @Test fun lo_que_viene_vacio_o_mal_escrito_queda_en_null() {
        val fila = MapeoMonitoreo.aFila("2° B", EstudianteCursoDto(7, "Diego", null, "", "ayer"))
        assertNull(fila.avanceGlobal)
        assertNull(fila.ultimaConexion)
        assertNull(fila.ultimaSincronizacion)
    }

    @Test fun el_avance_se_mantiene_entre_0_y_100() {
        assertEquals(100, MapeoMonitoreo.aFila("2° A", EstudianteCursoDto(3, "Ana", 140)).avanceGlobal)
        assertEquals(0, MapeoMonitoreo.aFila("2° A", EstudianteCursoDto(3, "Ana", -5)).avanceGlobal)
    }
}

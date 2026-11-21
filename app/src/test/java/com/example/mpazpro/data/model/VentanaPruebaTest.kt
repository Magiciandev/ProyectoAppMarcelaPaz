package com.example.mpazpro.data.model

import java.time.Instant
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class VentanaPruebaTest {
    private fun d(texto: String) = LocalDate.parse(texto)

    private fun evaluar(hoy: String, desde: String = "2026-11-01", hasta: String = "2026-11-30", hab: String? = null, rend: String? = null) =
        VentanaPrueba.evaluar(d(desde), d(hasta), hab?.let(::d), rend?.let(::d), d(hoy))

    @Test fun dentro_de_la_asignacion_se_puede_rendir() {
        assertEquals(DisponibilidadPrueba.EnFecha, evaluar("2026-11-15"))
    }

    @Test fun el_primer_y_el_ultimo_dia_cuentan() {
        assertEquals(DisponibilidadPrueba.EnFecha, evaluar("2026-11-01"))
        assertEquals(DisponibilidadPrueba.EnFecha, evaluar("2026-11-30"))
    }

    @Test fun antes_del_inicio_o_despues_del_fin_no_se_puede() {
        assertEquals(DisponibilidadPrueba.AunNoDisponible(d("2026-11-01")), evaluar("2026-10-31"))
        assertEquals(DisponibilidadPrueba.YaCerrada(d("2026-11-30")), evaluar("2026-12-01"))
    }

    @Test fun la_habilitacion_posterior_al_inicio_manda() {
        assertEquals(DisponibilidadPrueba.AunNoDisponible(d("2026-11-10")), evaluar("2026-11-05", hab = "2026-11-10"))
        assertEquals(DisponibilidadPrueba.EnFecha, evaluar("2026-11-10", hab = "2026-11-10"))
    }

    @Test fun una_habilitacion_anterior_al_inicio_no_adelanta_nada() {
        assertEquals(DisponibilidadPrueba.AunNoDisponible(d("2026-11-01")), evaluar("2026-10-20", hab = "2026-10-15"))
    }

    @Test fun la_rendicion_anterior_al_fin_manda() {
        assertEquals(DisponibilidadPrueba.YaCerrada(d("2026-11-20")), evaluar("2026-11-21", rend = "2026-11-20"))
        assertEquals(DisponibilidadPrueba.EnFecha, evaluar("2026-11-20", rend = "2026-11-20"))
    }

    @Test fun una_rendicion_posterior_al_fin_no_alarga_la_asignacion() {
        assertEquals(DisponibilidadPrueba.YaCerrada(d("2026-11-30")), evaluar("2026-12-02", rend = "2026-12-10"))
    }

    @Test fun una_ventana_invertida_nunca_deja_rendir() {
        // Habilitación (20-11) posterior a la rendición (10-11): ningún día es válido.
        assertEquals(DisponibilidadPrueba.AunNoDisponible(d("2026-11-20")), evaluar("2026-11-15", hab = "2026-11-20", rend = "2026-11-10"))
        assertEquals(DisponibilidadPrueba.YaCerrada(d("2026-11-10")), evaluar("2026-11-25", hab = "2026-11-20", rend = "2026-11-10"))
        for (dia in 1..30) {
            val hoy = "2026-11-%02d".format(dia)
            assertNotEquals(DisponibilidadPrueba.EnFecha, evaluar(hoy, hab = "2026-11-20", rend = "2026-11-10"))
        }
    }

    // El servidor recibe el instante en UTC («Z») y lo pasa al día de calendario de Santiago. En noviembre Chile está en UTC-3.
    @Test fun un_instante_de_la_noche_en_utc_sigue_siendo_el_dia_anterior_en_santiago() {
        assertEquals(d("2026-11-20"), ZonaColegio.fechaDe(Instant.parse("2026-11-21T02:30:00Z"))) // 23:30 en Santiago
        assertEquals(d("2026-11-21"), ZonaColegio.fechaDe(Instant.parse("2026-11-21T03:30:00Z"))) // 00:30 en Santiago
    }

    @Test fun en_invierno_la_diferencia_es_de_cuatro_horas() {
        assertEquals(d("2026-07-14"), ZonaColegio.fechaDe(Instant.parse("2026-07-15T03:30:00Z"))) // 23:30 en Santiago
        assertEquals(d("2026-07-15"), ZonaColegio.fechaDe(Instant.parse("2026-07-15T04:30:00Z"))) // 00:30 en Santiago
    }
}

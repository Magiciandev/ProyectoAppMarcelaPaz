package com.example.mpazpro.data.model

import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test

class MensajeInactividadTest {
    // 21-nov-2026 12:00 en Santiago (verano: UTC-3).
    private val ahora = Instant.parse("2026-11-21T15:00:00Z")

    private fun i(texto: String) = Instant.parse(texto)

    @Test fun sin_ninguna_fecha_aun_no_se_ha_conectado() {
        assertEquals("Aún no se ha conectado.", MensajeInactividad.para(null, null, ahora))
    }

    @Test fun muchos_dias_sin_conectarse() {
        assertEquals("Lleva 10 días sin conectarse.",
            MensajeInactividad.para(i("2026-11-11T15:00:00Z"), i("2026-11-11T15:00:00Z"), ahora))
    }

    @Test fun el_limite_son_tres_dias() {
        assertEquals("Lleva 3 días sin conectarse.", MensajeInactividad.para(i("2026-11-18T15:00:00Z"), null, ahora))
        assertEquals("Se conectó hace 2 días, pero aún no ha enviado avances.",
            MensajeInactividad.para(i("2026-11-19T15:00:00Z"), null, ahora))
    }

    @Test fun se_conecto_pero_nunca_ha_enviado_avances() {
        assertEquals("Se conectó hoy, pero aún no ha enviado avances.", MensajeInactividad.para(i("2026-11-21T14:00:00Z"), null, ahora))
    }

    @Test fun se_conecto_ayer_pero_no_envia_avances_hace_dias() {
        assertEquals("Se conectó ayer, pero no envía avances desde hace 5 días.",
            MensajeInactividad.para(i("2026-11-20T15:00:00Z"), i("2026-11-16T15:00:00Z"), ahora))
    }

    @Test fun conexion_y_avances_recientes() {
        assertEquals("Al día: se conectó hoy y envió avances ayer.",
            MensajeInactividad.para(i("2026-11-21T14:00:00Z"), i("2026-11-20T15:00:00Z"), ahora))
    }

    @Test fun sin_conexion_registrada_se_usa_la_sincronizacion() {
        assertEquals("Al día: se conectó hoy y envió avances hoy.", MensajeInactividad.para(null, i("2026-11-21T14:00:00Z"), ahora))
    }

    @Test fun los_dias_se_cuentan_en_la_zona_del_colegio_no_en_utc() {
        // Santiago: 20-nov 23:00. Una conexión a las 02:59 UTC del 20-nov fue el 19-nov a las 23:59 en Santiago: ayer.
        val casiMedianoche = i("2026-11-21T02:00:00Z")
        assertEquals(1L, MensajeInactividad.diasDesde(i("2026-11-20T02:59:00Z"), casiMedianoche))
        // Dos instantes de días UTC distintos que en Santiago son el mismo día: 0 días.
        assertEquals(0L, MensajeInactividad.diasDesde(i("2026-11-20T04:00:00Z"), casiMedianoche))
    }

    @Test fun una_fecha_futura_por_desfase_de_reloj_no_da_dias_negativos() {
        assertEquals(0L, MensajeInactividad.diasDesde(i("2026-11-25T15:00:00Z"), ahora))
    }

    @Test fun la_fecha_se_muestra_en_hora_de_santiago() {
        assertEquals("21-11-2026 12:00", FechaHoraColegio.formatear(ahora))
    }
}

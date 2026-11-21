package com.example.mpazpro.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TransicionesUnidadTest {

    private val estados = listOf(
        EstadoUnidad.BORRADOR, EstadoUnidad.EN_REVISION, EstadoUnidad.AUTORIZADO,
        EstadoUnidad.PUBLICADO, EstadoUnidad.DESHABILITADO
    )

    @Test fun cada_accion_solo_se_permite_desde_su_estado() {
        AccionEstado.entries.forEach { accion ->
            estados.forEach { estado ->
                assertEquals("$accion desde $estado", estado == accion.desde, TransicionesUnidad.permite(estado, accion))
            }
        }
    }

    @Test fun un_borrador_solo_se_envia_a_revision() {
        assertEquals(listOf(AccionEstado.ENVIAR_REVISION), TransicionesUnidad.accionesDesde(EstadoUnidad.BORRADOR))
    }

    @Test fun una_unidad_en_revision_se_autoriza_o_se_rechaza() {
        assertEquals(
            setOf(AccionEstado.AUTORIZAR, AccionEstado.RECHAZAR),
            TransicionesUnidad.accionesDesde(EstadoUnidad.EN_REVISION).toSet()
        )
    }

    @Test fun una_unidad_autorizada_solo_se_publica() {
        assertEquals(listOf(AccionEstado.PUBLICAR), TransicionesUnidad.accionesDesde(EstadoUnidad.AUTORIZADO))
    }

    @Test fun una_unidad_publicada_solo_se_deshabilita() {
        assertEquals(listOf(AccionEstado.DESHABILITAR), TransicionesUnidad.accionesDesde(EstadoUnidad.PUBLICADO))
    }

    @Test fun una_unidad_deshabilitada_solo_se_reabre() {
        assertEquals(listOf(AccionEstado.REABRIR), TransicionesUnidad.accionesDesde(EstadoUnidad.DESHABILITADO))
    }

    @Test fun el_ciclo_completo_vuelve_a_borrador() {
        var estado = EstadoUnidad.BORRADOR
        listOf(
            AccionEstado.ENVIAR_REVISION, AccionEstado.AUTORIZAR, AccionEstado.PUBLICAR,
            AccionEstado.DESHABILITAR, AccionEstado.REABRIR
        ).forEach { accion ->
            assertTrue("$accion desde $estado", TransicionesUnidad.permite(estado, accion))
            estado = accion.hasta
        }
        assertEquals(EstadoUnidad.BORRADOR, estado)
        assertFalse(TransicionesUnidad.permite(EstadoUnidad.PUBLICADO, AccionEstado.AUTORIZAR))
    }

    @Test fun los_nombres_de_las_acciones_son_los_del_servidor() {
        assertEquals(
            setOf("ENVIAR_REVISION", "AUTORIZAR", "RECHAZAR", "PUBLICAR", "DESHABILITAR", "REABRIR"),
            AccionEstado.entries.map { it.name }.toSet()
        )
    }
}

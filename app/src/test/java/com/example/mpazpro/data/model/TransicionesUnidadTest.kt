package com.example.mpazpro.data.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TransicionesUnidadTest {

    @Test fun solo_un_borrador_se_puede_enviar_a_revision() {
        assertTrue(TransicionesUnidad.puedeEnviarARevision(EstadoUnidad.BORRADOR))
        assertFalse(TransicionesUnidad.puedeEnviarARevision(EstadoUnidad.EN_REVISION))
        assertFalse(TransicionesUnidad.puedeEnviarARevision(EstadoUnidad.PUBLICADO))
    }

    @Test fun solo_una_unidad_en_revision_se_aprueba_o_rechaza() {
        assertTrue(TransicionesUnidad.puedeAprobar(EstadoUnidad.EN_REVISION))
        assertTrue(TransicionesUnidad.puedeRechazar(EstadoUnidad.EN_REVISION))
        assertFalse(TransicionesUnidad.puedeAprobar(EstadoUnidad.BORRADOR))
        assertFalse(TransicionesUnidad.puedeAprobar(EstadoUnidad.PUBLICADO))
        assertFalse(TransicionesUnidad.puedeRechazar(EstadoUnidad.BORRADOR))
    }

    @Test fun solo_una_unidad_publicada_se_puede_deshabilitar() {
        assertTrue(TransicionesUnidad.puedeDeshabilitar(EstadoUnidad.PUBLICADO))
        assertFalse(TransicionesUnidad.puedeDeshabilitar(EstadoUnidad.BORRADOR))
        assertFalse(TransicionesUnidad.puedeDeshabilitar(EstadoUnidad.EN_REVISION))
    }
}

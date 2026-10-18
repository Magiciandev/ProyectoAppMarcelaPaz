package com.example.mpazpro.data.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TransicionesUnidadTest {

    @Test
    fun solo_un_borrador_se_puede_enviar_a_revision() {
        assertTrue(TransicionesUnidad.puedeEnviarARevision(EstadoUnidad.BORRADOR))
        assertFalse(TransicionesUnidad.puedeEnviarARevision(EstadoUnidad.EN_REVISION))
        assertFalse(TransicionesUnidad.puedeEnviarARevision(EstadoUnidad.PUBLICADO))
    }

    @Test
    fun una_unidad_publicada_no_se_puede_aprobar_otra_vez() {
        assertTrue(TransicionesUnidad.puedeAprobar(EstadoUnidad.BORRADOR))
        assertTrue(TransicionesUnidad.puedeAprobar(EstadoUnidad.EN_REVISION))
        assertFalse(TransicionesUnidad.puedeAprobar(EstadoUnidad.PUBLICADO))
    }

    @Test
    fun un_borrador_no_se_puede_devolver() {
        assertFalse(TransicionesUnidad.puedeDevolver(EstadoUnidad.BORRADOR))
        assertTrue(TransicionesUnidad.puedeDevolver(EstadoUnidad.EN_REVISION))
        assertTrue(TransicionesUnidad.puedeDevolver(EstadoUnidad.PUBLICADO))
    }
}

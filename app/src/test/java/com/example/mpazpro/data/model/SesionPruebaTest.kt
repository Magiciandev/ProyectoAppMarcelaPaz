package com.example.mpazpro.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SesionPruebaTest {

    private fun seleccion(id: Long, puntaje: Double = 1.0) = PreguntaPrueba(
        id, TipoPreguntaPrueba.SELECCION, "Pregunta $id", null,
        listOf(OpcionPrueba("correcta", true, null), OpcionPrueba("mala", false, null)), puntaje
    )

    private fun secuencia(id: Long, puntaje: Double = 2.0) = PreguntaPrueba(
        id, TipoPreguntaPrueba.SECUENCIA, "Ordena $id", null,
        listOf(OpcionPrueba("pa", false, 2), OpcionPrueba("so", false, 1)), puntaje
    )

    @Test fun la_seleccion_se_corrige_apenas_se_elige_y_no_se_puede_cambiar() {
        val s = SesionPrueba(listOf(seleccion(1)))

        s.elegir(1)
        s.elegir(0) // ya respondió: se ignora

        assertTrue(s.corregida)
        assertEquals(listOf(1), s.seleccion)
    }

    @Test fun no_se_avanza_sin_responder() {
        val s = SesionPrueba(listOf(seleccion(1), seleccion(2)))

        assertFalse(s.siguiente())

        assertEquals(0, s.indice)
    }

    @Test fun la_secuencia_se_arma_con_deshacer_y_solo_se_comprueba_completa() {
        val s = SesionPrueba(listOf(secuencia(1)))

        s.elegir(0)
        s.comprobar() // falta una opción: no corrige
        assertFalse(s.corregida)

        s.deshacer()
        s.elegir(1)
        s.elegir(1) // repetida: se ignora
        s.elegir(0)
        assertEquals(listOf(1, 0), s.seleccion)

        s.comprobar()
        assertTrue(s.corregida)
    }

    @Test fun recorre_toda_la_prueba_y_calcula_el_puntaje() {
        val s = SesionPrueba(listOf(seleccion(1, 1.0), seleccion(2, 1.0), secuencia(3, 2.0)))

        s.elegir(0); assertFalse(s.siguiente()) // acierta
        s.elegir(1); assertFalse(s.siguiente()) // se equivoca
        s.elegir(1); s.elegir(0); s.comprobar() // acierta la secuencia (so, pa)
        assertTrue(s.esUltima)
        assertTrue(s.siguiente())

        assertTrue(s.terminada)
        assertEquals(3.0, s.puntajeObtenido(), 0.0) // 1 + 0 + 2
        assertEquals(4.0, s.puntajeMaximo(), 0.0)
        assertEquals(listOf(listOf(0), listOf(1), listOf(1, 0)), s.respuestasDadas())
    }

    @Test fun terminada_la_prueba_no_cambia_nada() {
        val s = SesionPrueba(listOf(seleccion(1)))
        s.elegir(0)
        s.siguiente()

        s.elegir(1)
        assertTrue(s.siguiente())

        assertEquals(1, s.respuestasDadas().size)
        assertEquals(1.0, s.puntajeObtenido(), 0.0)
    }
}

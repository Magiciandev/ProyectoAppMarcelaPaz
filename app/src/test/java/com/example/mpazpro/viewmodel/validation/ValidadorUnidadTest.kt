package com.example.mpazpro.viewmodel.validation

import com.example.mpazpro.data.model.Asignaturas
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidadorUnidadTest {

    // --- Título ---
    @Test
    fun titulo_vacio_es_error() {
        assertNotNull(ValidadorUnidad.validarTitulo(""))
        assertNotNull(ValidadorUnidad.validarTitulo("     "))
    }

    @Test
    fun titulo_demasiado_corto_es_error() {
        assertNotNull(ValidadorUnidad.validarTitulo("Suma"))
    }

    @Test
    fun titulo_con_largo_minimo_es_valido() {
        assertNull(ValidadorUnidad.validarTitulo("Sumas"))
    }

    @Test
    fun titulo_demasiado_largo_es_error() {
        val mensaje = ValidadorUnidad.validarTitulo("a".repeat(ValidadorUnidad.TITULO_MAX + 1))
        assertNotNull(mensaje)
        assertTrue(mensaje!!.contains(ValidadorUnidad.TITULO_MAX.toString()))
    }

    // --- Objetivo de aprendizaje ---
    @Test
    fun objetivo_vacio_es_error() {
        assertNotNull(ValidadorUnidad.validarObjetivo(""))
    }

    @Test
    fun objetivo_con_formato_incorrecto_es_error() {
        assertNotNull(ValidadorUnidad.validarObjetivo("05"))
        assertNotNull(ValidadorUnidad.validarObjetivo("OA"))
        assertNotNull(ValidadorUnidad.validarObjetivo("OA123"))
        assertNotNull(ValidadorUnidad.validarObjetivo("OA 5"))
    }

    @Test
    fun objetivo_con_formato_correcto_es_valido() {
        assertNull(ValidadorUnidad.validarObjetivo("OA5"))
        assertNull(ValidadorUnidad.validarObjetivo("OA05"))
        assertNull(ValidadorUnidad.validarObjetivo("oa12"))
    }

    // --- Asignatura ---
    @Test
    fun asignatura_vacia_es_error() {
        assertNotNull(ValidadorUnidad.validarAsignatura("", Asignaturas.todas))
    }

    @Test
    fun asignatura_fuera_de_la_lista_es_error() {
        assertNotNull(ValidadorUnidad.validarAsignatura("Música", Asignaturas.todas))
    }

    @Test
    fun asignatura_de_la_lista_es_valida() {
        assertNull(ValidadorUnidad.validarAsignatura(Asignaturas.MATEMATICA, Asignaturas.todas))
    }

    // --- Contenido ---
    @Test
    fun contenido_vacio_o_corto_es_error() {
        assertNotNull(ValidadorUnidad.validarContenido(""))
        assertNotNull(ValidadorUnidad.validarContenido("Muy corto"))
    }

    @Test
    fun contenido_con_largo_valido_es_valido() {
        assertNull(ValidadorUnidad.validarContenido("a".repeat(ValidadorUnidad.CONTENIDO_MIN)))
        assertNull(ValidadorUnidad.validarContenido("a".repeat(ValidadorUnidad.CONTENIDO_MAX)))
    }

    @Test
    fun contenido_demasiado_largo_es_error() {
        assertNotNull(ValidadorUnidad.validarContenido("a".repeat(ValidadorUnidad.CONTENIDO_MAX + 1)))
    }
}

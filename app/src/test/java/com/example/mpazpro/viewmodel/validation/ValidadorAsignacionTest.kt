package com.example.mpazpro.viewmodel.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ValidadorAsignacionTest {
    @Test fun cursoVacio_esError() = assertEquals("Elige un curso", ValidadorAsignacion.validarCurso(""))
    @Test fun cursoDesconocido_esError() = assertEquals("Curso no válido", ValidadorAsignacion.validarCurso("3° A"))
    @Test fun cursoValido_noTieneError() = assertNull(ValidadorAsignacion.validarCurso("2° A"))
    @Test fun sinFechaDesde_esError() = assertNotNull(ValidadorAsignacion.validarDesde(null))
    @Test fun sinFechaHasta_esError() = assertNotNull(ValidadorAsignacion.validarHasta(10L, null))
    @Test fun hastaAnteriorADesde_esError() = assertNotNull(ValidadorAsignacion.validarHasta(10L, 5L))
    @Test fun hastaIgualADesde_esValido() = assertNull(ValidadorAsignacion.validarHasta(10L, 10L))
    @Test fun hastaPosterior_esValido() = assertNull(ValidadorAsignacion.validarHasta(10L, 20L))
    @Test fun intentosCero_esError() = assertNotNull(ValidadorAsignacion.validarIntentos(0))
    @Test fun intentosSeis_esError() = assertNotNull(ValidadorAsignacion.validarIntentos(6))
    @Test fun intentosNulo_esError() = assertNotNull(ValidadorAsignacion.validarIntentos(null))
    @Test fun intentosLimites_sonValidos() {
        assertNull(ValidadorAsignacion.validarIntentos(1))
        assertNull(ValidadorAsignacion.validarIntentos(5))
    }
    @Test fun sinConfirmar_esError() = assertNotNull(ValidadorAsignacion.validarConfirmacion(false))
    @Test fun confirmado_esValido() = assertNull(ValidadorAsignacion.validarConfirmacion(true))
}

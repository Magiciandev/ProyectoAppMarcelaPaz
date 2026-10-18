package com.example.mpazpro.viewmodel.validation

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ValidadorLoginTest {

    // --- Usuario ---
    @Test
    fun usuario_vacio_es_error() {
        assertNotNull(ValidadorLogin.validarUsuario(""))
        assertNotNull(ValidadorLogin.validarUsuario("   "))
    }

    @Test
    fun usuario_muy_corto_es_error() {
        assertNotNull(ValidadorLogin.validarUsuario("ab"))
    }

    @Test
    fun usuario_muy_largo_es_error() {
        assertNotNull(ValidadorLogin.validarUsuario("a".repeat(ValidadorLogin.USUARIO_MAX + 1)))
    }

    @Test
    fun usuario_con_simbolos_o_espacios_es_error() {
        assertNotNull(ValidadorLogin.validarUsuario("est 001"))
        assertNotNull(ValidadorLogin.validarUsuario("est-001"))
        assertNotNull(ValidadorLogin.validarUsuario("docente@colegio"))
    }

    @Test
    fun usuario_valido_no_tiene_error() {
        assertNull(ValidadorLogin.validarUsuario("utp"))
        assertNull(ValidadorLogin.validarUsuario("est001"))
        assertNull(ValidadorLogin.validarUsuario("  docente  "))
    }

    // --- PIN ---
    @Test
    fun pin_vacio_es_error() {
        assertNotNull(ValidadorLogin.validarPin(""))
    }

    @Test
    fun pin_con_letras_es_error() {
        assertNotNull(ValidadorLogin.validarPin("12a4"))
    }

    @Test
    fun pin_con_largo_incorrecto_es_error() {
        assertNotNull(ValidadorLogin.validarPin("123"))
        assertNotNull(ValidadorLogin.validarPin("12345"))
    }

    @Test
    fun pin_de_cuatro_digitos_es_valido() {
        assertNull(ValidadorLogin.validarPin("1234"))
        assertNull(ValidadorLogin.validarPin("0000"))
    }
}

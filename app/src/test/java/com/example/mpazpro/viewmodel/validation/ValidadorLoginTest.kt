package com.example.mpazpro.viewmodel.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ValidadorLoginTest {
    @Test fun usuarioVacio_esError() = assertEquals("Escribe tu usuario", ValidadorLogin.validarUsuario("  "))
    @Test fun usuarioCorto_esError() = assertNotNull(ValidadorLogin.validarUsuario("ab"))
    @Test fun usuarioConSimbolos_esError() = assertEquals("Usa solo letras y números", ValidadorLogin.validarUsuario("ana!"))
    @Test fun usuarioValido_noTieneError() = assertNull(ValidadorLogin.validarUsuario("ana"))
    @Test fun pinVacio_esError() = assertEquals("Escribe tu PIN", ValidadorLogin.validarPin(""))
    @Test fun pinConLetras_esError() = assertNotNull(ValidadorLogin.validarPin("12a4"))
    @Test fun pinCorto_esError() = assertNotNull(ValidadorLogin.validarPin("123"))
    @Test fun pinValido_noTieneError() = assertNull(ValidadorLogin.validarPin("1234"))
}

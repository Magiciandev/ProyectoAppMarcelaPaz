package com.example.mpazpro.data.security

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CifradoTest {
    @Test fun pinCorrecto_seVerifica() = assertTrue(Cifrado.verificar("1234", Cifrado.crear("1234")))
    @Test fun pinIncorrecto_noSeVerifica() = assertFalse(Cifrado.verificar("4321", Cifrado.crear("1234")))
    @Test fun elPinNoSeGuardaEnClaro() = assertFalse(Cifrado.crear("1234").contains("1234"))
    @Test fun mismaClave_generaHashesDistintosPorSal() = assertNotEquals(Cifrado.crear("1234"), Cifrado.crear("1234"))
    @Test fun formatoInvalido_noSeVerifica() = assertFalse(Cifrado.verificar("1234", "texto-sin-formato"))
}

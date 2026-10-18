package com.example.mpazpro.viewmodel.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ValidadorObservacionTest {
    @Test fun vacia_esError() = assertEquals("Escribe la observación", ValidadorObservacion.validar("  "))
    @Test fun muyCorta_esError() = assertNotNull(ValidadorObservacion.validar("ok"))
    @Test fun muyLarga_esError() = assertNotNull(ValidadorObservacion.validar("a".repeat(301)))
    @Test fun enElLimite_esValida() = assertNull(ValidadorObservacion.validar("a".repeat(300)))
    @Test fun normal_esValida() = assertNull(ValidadorObservacion.validar("Reforzar la resta con apoyo concreto"))
}

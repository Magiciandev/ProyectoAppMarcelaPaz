package com.example.mpazpro.viewmodel.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ValidadorPreguntaTest {
    @Test fun textoVacio_esError() = assertEquals("La pregunta es obligatoria", ValidadorPregunta.validarTexto(""))
    @Test fun textoCorto_esError() = assertNotNull(ValidadorPregunta.validarTexto("¿5+3?"))
    @Test fun textoLargo_esError() = assertNotNull(ValidadorPregunta.validarTexto("a".repeat(121)))
    @Test fun textoValido_noTieneError() = assertNull(ValidadorPregunta.validarTexto("¿Cuánto es 5 + 3?"))
    @Test fun opcionVacia_esError() = assertEquals("Escribe la alternativa", ValidadorPregunta.validarOpcion(" "))
    @Test fun opcionLarga_esError() = assertNotNull(ValidadorPregunta.validarOpcion("a".repeat(41)))
    @Test fun opcionValida_noTieneError() = assertNull(ValidadorPregunta.validarOpcion("8"))
    @Test fun correctaNula_esError() = assertNotNull(ValidadorPregunta.validarCorrecta(null))
    @Test fun correctaFueraDeRango_esError() = assertNotNull(ValidadorPregunta.validarCorrecta(4))
    @Test fun correctaValida_noTieneError() = assertNull(ValidadorPregunta.validarCorrecta(2))
}

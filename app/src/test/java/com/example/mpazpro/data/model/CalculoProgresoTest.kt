package com.example.mpazpro.data.model

import org.junit.Assert.assertEquals
import org.junit.Test

class CalculoProgresoTest {
    @Test fun sinEvaluaciones_todoEnCero() {
        val r = CalculoProgreso.porAsignatura(emptyList())
        assertEquals(4, r.size)
        assertEquals(0, CalculoProgreso.general(r))
    }

    @Test fun generalEsPromedioDeLas4Asignaturas() {
        // Matemática 100 % y las otras 3 sin evaluar -> 25 % general (regla de la sección 3).
        val r = CalculoProgreso.porAsignatura(listOf(PromedioAsignatura(Asignaturas.MATEMATICA, 100.0)))
        assertEquals(100, r.first { it.asignatura == Asignaturas.MATEMATICA }.porcentaje)
        assertEquals(25, CalculoProgreso.general(r))
    }

    @Test fun promedioSeRedondea() {
        val r = CalculoProgreso.porAsignatura(listOf(PromedioAsignatura(Asignaturas.LENGUAJE, 66.67)))
        assertEquals(67, r.first { it.asignatura == Asignaturas.LENGUAJE }.porcentaje)
    }

    @Test fun umbralesDeNivel() {
        assertEquals(Nivel.DOMINA, CalculoProgreso.nivel(80))
        assertEquals(Nivel.EN_VIAS, CalculoProgreso.nivel(79))
        assertEquals(Nivel.EN_VIAS, CalculoProgreso.nivel(60))
        assertEquals(Nivel.REFUERZO, CalculoProgreso.nivel(59))
    }
}

package com.example.mpazpro.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PreguntaPruebaTest {

    private val seleccion = PreguntaPrueba(
        id = 1, tipo = TipoPreguntaPrueba.SELECCION, enunciado = "¿Qué palabra forman ca-sa?", explicacion = null,
        opciones = listOf(
            OpcionPrueba("casa", true, null), OpcionPrueba("saca", false, null), OpcionPrueba("cosa", false, null)
        ),
        puntaje = 1.0
    )

    // «pa» se muestra primero pero va en la posición 2; «so» va primero.
    private val secuencia = PreguntaPrueba(
        id = 3, tipo = TipoPreguntaPrueba.SECUENCIA, enunciado = "Ordena las sílabas para formar sopa", explicacion = null,
        opciones = listOf(OpcionPrueba("pa", false, 2), OpcionPrueba("so", false, 1)),
        puntaje = 2.0
    )

    // gato–miau y perro–guau; las opciones se muestran mezcladas (índices 0 a 3).
    private val asociacion = PreguntaPrueba(
        id = 4, tipo = TipoPreguntaPrueba.ASOCIACION, enunciado = "Une cada animal con lo que dice", explicacion = null,
        opciones = listOf(
            OpcionPrueba("gato", false, null, "g"), OpcionPrueba("Dice guau", false, null, "p"),
            OpcionPrueba("perro", false, null, "p"), OpcionPrueba("Dice miau", false, null, "g")
        ),
        puntaje = 2.0
    )

    @Test fun una_seleccion_es_correcta_solo_con_la_opcion_marcada_como_correcta() {
        assertTrue(seleccion.esCorrecta(listOf(0)))
        assertFalse(seleccion.esCorrecta(listOf(1)))
        assertFalse(seleccion.esCorrecta(emptyList()))
        assertFalse(seleccion.esCorrecta(listOf(0, 1)))
    }

    @Test fun una_secuencia_es_correcta_solo_en_el_orden_exacto() {
        assertTrue(secuencia.esCorrecta(listOf(1, 0))) // so, pa
        assertFalse(secuencia.esCorrecta(listOf(0, 1))) // pa, so
        assertFalse(secuencia.esCorrecta(listOf(1))) // incompleta
    }

    @Test fun el_orden_correcto_se_muestra_como_texto() {
        assertEquals("so → pa", secuencia.ordenCorrecto())
    }

    @Test fun una_opcion_que_no_existe_no_rompe_la_correccion() {
        assertFalse(seleccion.esCorrecta(listOf(9)))
        assertFalse(secuencia.esCorrecta(listOf(1, 9)))
    }

    @Test fun la_respuesta_se_envia_como_texto_json() {
        assertEquals("""{"opcion":"saca"}""", seleccion.respuestaComoTexto(listOf(1)))
        assertEquals("""{"orden":["so","pa"]}""", secuencia.respuestaComoTexto(listOf(1, 0)))
    }

    @Test fun los_tipos_que_la_app_no_resuelve_se_reconocen() {
        assertEquals(TipoPreguntaPrueba.COMPLETACION, TipoPreguntaPrueba.desde("COMPLETACION"))
        assertEquals(TipoPreguntaPrueba.ASOCIACION, TipoPreguntaPrueba.desde("ASOCIACION"))
        assertNull(TipoPreguntaPrueba.desde("ARRASTRAR"))
    }

    @Test fun una_asociacion_es_correcta_solo_si_todos_los_pares_son_pareja() {
        assertTrue(asociacion.esCorrecta(listOf(0, 3, 2, 1))) // gato–miau, perro–guau
        assertTrue(asociacion.esCorrecta(listOf(3, 0, 1, 2))) // el orden dentro del par y entre pares da igual
        assertFalse(asociacion.esCorrecta(listOf(0, 1, 2, 3))) // gato–guau, perro–miau
        assertFalse(asociacion.esCorrecta(listOf(0, 3))) // incompleta
        assertFalse(asociacion.esCorrecta(listOf(0, 3, 2, 2))) // repite una opción
        assertFalse(asociacion.esCorrecta(listOf(0, 3, 2, 9))) // opción que no existe
    }

    @Test fun las_parejas_correctas_se_muestran_como_texto() {
        assertEquals("gato — Dice miau; Dice guau — perro", asociacion.parejasCorrectas())
    }

    @Test fun una_asociacion_se_envia_como_pares() {
        assertEquals(
            """{"pares":[["gato","Dice miau"],["perro","Dice guau"]]}""",
            asociacion.respuestaComoTexto(listOf(0, 3, 2, 1))
        )
    }

    @Test fun la_prueba_calcula_su_puntaje_y_los_intentos_que_quedan() {
        val prueba = PruebaParaRendir(1, 1, "Prueba", listOf(seleccion, secuencia), limiteIntentos = 3, intentosUsados = 1)

        assertEquals(3.0, prueba.puntajeMaximo, 0.0)
        assertEquals(2, prueba.intentosRestantes)
        assertEquals(0, prueba.copy(intentosUsados = 5).intentosRestantes)
    }
}

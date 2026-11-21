package com.example.mpazpro.data.repository

import com.example.mpazpro.data.model.TipoPreguntaPrueba
import com.example.mpazpro.data.remote.dto.ActividadDto
import com.example.mpazpro.data.remote.dto.OpcionDto
import com.example.mpazpro.data.remote.dto.PaqueteEvaluacionDto
import com.example.mpazpro.data.remote.dto.PreguntaDto
import com.example.mpazpro.data.remote.dto.PreguntaEnActividadDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MapeoActividadesTest {

    private fun opcion(texto: String, orden: Int, correcta: Boolean? = null, par: String? = null) =
        OpcionDto(texto = texto, esCorrecta = correcta, parClave = par, orden = orden)

    private fun pregunta(id: Long, tipo: String, opciones: List<OpcionDto>) =
        PreguntaDto(id = id, objetivoId = 1, tipo = tipo, enunciado = "Pregunta $id", explicacion = null, opciones = opciones)

    private fun actividad(id: Long, orden: Int, preguntas: List<PreguntaEnActividadDto>?) =
        ActividadDto(id = id, objetivoId = 1, titulo = "Actividad $id", tipo = "PRACTICA", dificultad = 2, orden = orden, estado = "PUBLICADO", preguntas = preguntas)

    private val seleccion = pregunta(1, "SELECCION", listOf(opcion("casa", 1, true), opcion("saca", 2, false)))
    private val completacion = pregunta(2, "COMPLETACION", listOf(opcion("duerme", 1, true), opcion("duermen", 2, false)))
    private val desconocida = pregunta(3, "ARRASTRAR", listOf(opcion("a", 1)))

    private fun paquete(actividades: List<ActividadDto>, preguntas: List<PreguntaDto>) =
        PaqueteEvaluacionDto(objetivoId = 1, actividades = actividades, preguntas = preguntas, prueba = null)

    @Test fun las_actividades_y_sus_preguntas_quedan_en_su_orden() {
        val r = MapeoActividades.aActividades(
            paquete(
                listOf(
                    actividad(20, 2, listOf(PreguntaEnActividadDto(1, 1))),
                    actividad(10, 1, listOf(PreguntaEnActividadDto(2, 2), PreguntaEnActividadDto(1, 1)))
                ),
                listOf(seleccion, completacion)
            )
        )

        assertEquals(listOf(10L, 20L), r.map { it.id })
        assertEquals(listOf(1L, 2L), r[0].preguntas.map { it.id }) // primero la de orden 1
        assertEquals(2, r[0].dificultad)
    }

    @Test fun cada_pregunta_de_una_actividad_vale_un_punto() {
        val r = MapeoActividades.aActividades(paquete(listOf(actividad(1, 1, listOf(PreguntaEnActividadDto(1, 1)))), listOf(seleccion)))

        assertEquals(1.0, r.single().preguntas.single().puntaje, 0.0)
        assertEquals(TipoPreguntaPrueba.SELECCION, r.single().preguntas.single().tipo)
    }

    @Test fun una_actividad_sin_preguntas_se_omite() {
        val r = MapeoActividades.aActividades(
            paquete(listOf(actividad(1, 1, emptyList()), actividad(2, 2, null), actividad(3, 3, listOf(PreguntaEnActividadDto(1, 1)))), listOf(seleccion))
        )

        assertEquals(listOf(3L), r.map { it.id })
    }

    @Test fun una_actividad_con_una_pregunta_que_la_app_no_resuelve_se_omite_completa() {
        val r = MapeoActividades.aActividades(
            paquete(
                listOf(actividad(1, 1, listOf(PreguntaEnActividadDto(1, 1), PreguntaEnActividadDto(3, 2))), actividad(2, 2, listOf(PreguntaEnActividadDto(2, 1)))),
                listOf(seleccion, completacion, desconocida)
            )
        )

        assertEquals(listOf(2L), r.map { it.id })
    }

    @Test fun una_pregunta_que_falta_en_el_paquete_omite_la_actividad() {
        val r = MapeoActividades.aActividades(paquete(listOf(actividad(1, 1, listOf(PreguntaEnActividadDto(99, 1)))), listOf(seleccion)))

        assertTrue(r.isEmpty())
    }

    @Test fun una_asociacion_mal_armada_omite_la_actividad_y_una_bien_armada_se_practica() {
        val mala = pregunta(4, "ASOCIACION", listOf(opcion("gato", 1), opcion("miau", 2)))
        val buena = pregunta(5, "ASOCIACION", listOf(opcion("gato", 1, par = "g"), opcion("miau", 2, par = "g")))

        val r = MapeoActividades.aActividades(
            paquete(listOf(actividad(1, 1, listOf(PreguntaEnActividadDto(4, 1))), actividad(2, 2, listOf(PreguntaEnActividadDto(5, 1)))), listOf(mala, buena))
        )

        assertEquals(listOf(2L), r.map { it.id })
    }
}

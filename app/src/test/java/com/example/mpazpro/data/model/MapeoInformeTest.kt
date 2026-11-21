package com.example.mpazpro.data.model

import com.example.mpazpro.data.remote.dto.InformeDto
import com.example.mpazpro.data.remote.dto.InformeObjetivoDto
import com.example.mpazpro.data.remote.dto.InformeRespuestaDto
import com.example.mpazpro.data.remote.dto.ResumenDto
import com.example.mpazpro.data.remote.dto.ResumenObjetivoDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MapeoInformeTest {
    private val nombres = mapOf(1L to "LEN-OA01", 3L to "MAT-OA01")

    private fun resumen() = ResumenDto(
        estudianteId = 3, nombre = "Ana",
        objetivos = listOf(
            ResumenObjetivoDto(1, 100, 1, 3, 75, "EN_VIAS"),
            ResumenObjetivoDto(9, 40, 0, 3, null, null)
        ),
        promedioGeneral = 75, nivelGeneral = "EN_VIAS"
    )

    @Test fun el_resumen_usa_el_nombre_del_catalogo_y_el_id_si_falta() {
        val ficha = MapeoInforme.ficha(resumen(), null, nombres)
        assertEquals("Ana", ficha.nombre)
        assertEquals("LEN-OA01", ficha.objetivos[0].nombre)
        assertEquals("Objetivo 9", ficha.objetivos[1].nombre)
        assertEquals(75, ficha.promedioGeneral)
        assertEquals(Nivel.EN_VIAS, ficha.nivelGeneral)
        assertNull(ficha.informe)
    }

    @Test fun un_objetivo_sin_prueba_rendida_no_tiene_mejor_resultado_ni_nivel() {
        val o = MapeoInforme.ficha(resumen(), null, nombres).objetivos[1]
        assertNull(o.mejorPorcentaje)
        assertNull(o.nivel)
        assertEquals(40, o.avance)
    }

    @Test fun un_nivel_desconocido_o_vacio_queda_sin_nivel() {
        assertEquals(Nivel.DOMINA, MapeoInforme.nivel("DOMINA"))
        assertEquals(Nivel.REFUERZO, MapeoInforme.nivel("REFUERZO"))
        assertNull(MapeoInforme.nivel("OTRO"))
        assertNull(MapeoInforme.nivel(null))
    }

    @Test fun el_informe_resume_las_respuestas_por_objetivo_y_las_areas_de_refuerzo() {
        val dto = InformeDto(
            id = 7, estudianteId = 3, cursoId = 1, puntajeGlobal = 66.67, nivel = "EN_VIAS",
            fechaEmision = "2026-11-21T15:00:00Z", validado = true, validadoPor = 2,
            fechaValidacion = "2026-11-21T16:00:00Z", observacionDocente = "Va bien",
            porObjetivo = listOf(InformeObjetivoDto(3, 40, "REFUERZO", 1)),
            areasRefuerzo = listOf(3),
            respuestas = listOf(
                InformeRespuestaDto(1, 10, true, 1.0), InformeRespuestaDto(1, 11, false, 0.0),
                InformeRespuestaDto(1, 12, true, 1.0), InformeRespuestaDto(3, 20, false, 0.0)
            )
        )
        val informe = MapeoInforme.ficha(resumen(), dto, nombres).informe!!
        assertEquals(7L, informe.id)
        assertEquals(67, informe.puntajeGlobal)
        assertEquals("21-11-2026 12:00", informe.emitidoEn)
        assertTrue(informe.validado)
        assertEquals("Va bien", informe.observacion)
        assertEquals(listOf("MAT-OA01"), informe.areasDeRefuerzo)
        assertEquals(RespuestasObjetivo("LEN-OA01", 2, 3), informe.respuestas[0])
        assertEquals(RespuestasObjetivo("MAT-OA01", 0, 1), informe.respuestas[1])
    }

    @Test fun un_informe_sin_observacion_ni_detalle_se_muestra_vacio() {
        val dto = InformeDto(
            id = 8, estudianteId = 3, cursoId = 1, puntajeGlobal = 50.0, nivel = "REFUERZO",
            fechaEmision = "2026-11-21T15:00:00Z", validado = false
        )
        val informe = MapeoInforme.informeFicha(dto, nombres)
        assertEquals("", informe.observacion)
        assertFalse(informe.validado)
        assertTrue(informe.areasDeRefuerzo.isEmpty())
        assertTrue(informe.respuestas.isEmpty())
    }
}

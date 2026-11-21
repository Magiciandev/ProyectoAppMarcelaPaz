package com.example.mpazpro.data.repository

import com.example.mpazpro.data.model.Asignaturas
import com.example.mpazpro.data.model.CacheAsignatura
import com.example.mpazpro.data.model.CacheContenido
import com.example.mpazpro.data.model.CacheObjetivo
import com.example.mpazpro.data.model.CacheUnidad
import com.example.mpazpro.data.model.CatalogoCache
import com.example.mpazpro.data.model.EstadoUnidad
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MapeoCatalogoTest {

    private val asignaturas = listOf(
        CacheAsignatura(1, "Lenguaje y Comunicación", "2° Básico"),
        CacheAsignatura(4, "Historia, Geografía y Ciencias Sociales", "2° Básico")
    )

    private fun contenido(id: Long, objetivoId: Long, orden: Int, explicacion: String) = CacheContenido(
        id, objetivoId, "Título", explicacion, null, null, null, null, orden, "Jefatura UTP", "Licencia"
    )

    private fun objetivo(id: Long, unidadId: Long, codigo: String, estado: String) =
        CacheObjetivo(id, unidadId, codigo, "Descripción $codigo", null, null, 1, estado)

    @Test fun cada_objetivo_pasa_a_ser_una_unidad_de_la_app() {
        val catalogo = CatalogoCache(
            asignaturas = asignaturas,
            unidades = listOf(CacheUnidad(10, 1, "Mis primeras lecturas", 1, "PUBLICADO")),
            objetivos = listOf(objetivo(100, 10, "LEN-OA01", "PUBLICADO")),
            contenidos = listOf(contenido(1000, 100, 1, "Leemos cuentos cortos."))
        )

        val u = MapeoCatalogo.aUnidades(catalogo).single()

        assertEquals("Mis primeras lecturas", u.titulo)
        assertEquals("OA01", u.objetivoAprendizaje) // sin el prefijo LEN-
        assertEquals(Asignaturas.LENGUAJE, u.asignatura)
        assertEquals(EstadoUnidad.PUBLICADO, u.estado)
        assertEquals("Leemos cuentos cortos.", u.contenido)
        assertEquals(100L, u.objetivoServidorId)
    }

    @Test fun el_estado_es_el_del_objetivo_y_se_traduce() {
        val catalogo = CatalogoCache(
            asignaturas = asignaturas,
            unidades = listOf(CacheUnidad(10, 1, "Unidad", 1, "BORRADOR")),
            objetivos = listOf(objetivo(100, 10, "LEN-OA01", "EN_REVISION")),
            contenidos = emptyList()
        )

        assertEquals(EstadoUnidad.EN_REVISION, MapeoCatalogo.aUnidades(catalogo).single().estado)
    }

    @Test fun sin_contenido_se_usa_la_descripcion_del_objetivo() {
        val catalogo = CatalogoCache(
            asignaturas = asignaturas,
            unidades = listOf(CacheUnidad(10, 4, "Mi comunidad", 1, "BORRADOR")),
            objetivos = listOf(objetivo(100, 10, "HIS-OA01", "BORRADOR")),
            contenidos = emptyList()
        )

        val u = MapeoCatalogo.aUnidades(catalogo).single()

        assertEquals("Descripción HIS-OA01", u.contenido)
        assertEquals(Asignaturas.HISTORIA, u.asignatura)
    }

    @Test fun con_varios_contenidos_se_usa_el_primero_por_orden() {
        val catalogo = CatalogoCache(
            asignaturas = asignaturas,
            unidades = listOf(CacheUnidad(10, 1, "Unidad", 1, "BORRADOR")),
            objetivos = listOf(objetivo(100, 10, "LEN-OA01", "BORRADOR")),
            contenidos = listOf(contenido(2, 100, 2, "segundo"), contenido(1, 100, 1, "primero"))
        )

        assertEquals("primero", MapeoCatalogo.aUnidades(catalogo).single().contenido)
    }

    @Test fun un_objetivo_sin_unidad_o_sin_asignatura_se_omite() {
        val catalogo = CatalogoCache(
            asignaturas = asignaturas,
            unidades = listOf(CacheUnidad(10, 99, "Sin asignatura", 1, "BORRADOR")),
            objetivos = listOf(objetivo(100, 10, "LEN-OA01", "BORRADOR"), objetivo(101, 77, "LEN-OA02", "BORRADOR")),
            contenidos = emptyList()
        )

        assertTrue(MapeoCatalogo.aUnidades(catalogo).isEmpty())
    }

    @Test fun el_nombre_del_servidor_se_traduce_al_de_la_app() {
        assertEquals(Asignaturas.LENGUAJE, MapeoUnidad.nombreLocal("Lenguaje y Comunicación"))
        assertEquals(Asignaturas.HISTORIA, MapeoUnidad.nombreLocal("Historia, Geografía y Ciencias Sociales"))
        assertEquals("Música", MapeoUnidad.nombreLocal("Música"))
    }
}

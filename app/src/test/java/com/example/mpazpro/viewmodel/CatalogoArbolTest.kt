package com.example.mpazpro.viewmodel

import com.example.mpazpro.data.model.CacheAsignatura
import com.example.mpazpro.data.model.CacheContenido
import com.example.mpazpro.data.model.CacheObjetivo
import com.example.mpazpro.data.model.CacheUnidad
import com.example.mpazpro.data.model.CatalogoCache
import com.example.mpazpro.data.repository.UsuarioRepository
import com.example.mpazpro.data.model.Roles
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CatalogoArbolTest {

    private fun objetivo(id: Long, unidadId: Long) =
        CacheObjetivo(id, unidadId, "OA-$id", "Descripción $id", null, null, 1, "PUBLICADO")

    private fun contenido(id: Long, objetivoId: Long) =
        CacheContenido(id, objetivoId, "C$id", "Texto", null, null, null, null, 1, "Autor", "Propio")

    @Test fun `agrupa unidades objetivos y cuenta los contenidos`() {
        val cache = CatalogoCache(
            asignaturas = listOf(CacheAsignatura(1, "Lenguaje", "2° Básico"), CacheAsignatura(2, "Matemática", "2° Básico")),
            unidades = listOf(CacheUnidad(10, 1, "U1", 1, "PUBLICADO"), CacheUnidad(20, 2, "U2", 1, "AUTORIZADO")),
            objetivos = listOf(objetivo(100, 10), objetivo(101, 10), objetivo(200, 20)),
            contenidos = listOf(contenido(1, 100), contenido(2, 100), contenido(3, 200))
        )

        val arbol = armarArbol(cache)

        assertEquals(listOf("Lenguaje", "Matemática"), arbol.map { it.asignatura.nombre })
        val lenguaje = arbol[0].unidades
        assertEquals(1, lenguaje.size)
        assertEquals(listOf(100L, 101L), lenguaje[0].objetivos.map { it.objetivo.id })
        assertEquals(listOf(2, 0), lenguaje[0].objetivos.map { it.cantidadContenidos })
        assertEquals(1, arbol[1].unidades[0].objetivos[0].cantidadContenidos)
    }

    @Test fun `una asignatura sin unidades queda con la lista vacia`() {
        val cache = CatalogoCache(listOf(CacheAsignatura(1, "Lenguaje", "2° Básico")), emptyList(), emptyList(), emptyList())

        val arbol = armarArbol(cache)

        assertEquals(1, arbol.size)
        assertTrue(arbol[0].unidades.isEmpty())
    }

    @Test fun `una cache vacia da un arbol vacio`() {
        assertTrue(armarArbol(CatalogoCache(emptyList(), emptyList(), emptyList(), emptyList())).isEmpty())
    }

    @Test fun `los perfiles del servidor se traducen a los de la app`() {
        assertEquals(Roles.UTP, UsuarioRepository.rolLocal("UTP"))
        assertEquals(Roles.DOCENTE, UsuarioRepository.rolLocal("DOCENTE"))
        assertEquals(Roles.ESTUDIANTE, UsuarioRepository.rolLocal("ESTUDIANTE"))
        assertEquals(Roles.ESTUDIANTE, UsuarioRepository.rolLocal("otro"))
    }
}

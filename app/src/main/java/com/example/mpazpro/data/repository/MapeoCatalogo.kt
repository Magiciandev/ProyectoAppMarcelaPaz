package com.example.mpazpro.data.repository

import com.example.mpazpro.data.model.CatalogoCache
import com.example.mpazpro.data.model.MapeoEstado
import com.example.mpazpro.data.model.Unidad

// Convierte el catálogo del servidor (asignatura > unidad > objetivo > contenido) en unidades planas de la app.
// Función pura: se prueba sin red ni base de datos.
object MapeoCatalogo {

    /** Una unidad de la app por cada objetivo de aprendizaje: el objetivo es lo que se aprueba y se publica. */
    fun aUnidades(catalogo: CatalogoCache): List<Unidad> =
        catalogo.objetivos.mapNotNull { objetivo ->
            val unidad = catalogo.unidades.firstOrNull { it.id == objetivo.unidadId } ?: return@mapNotNull null
            val asignatura = catalogo.asignaturas.firstOrNull { it.id == unidad.asignaturaId }
                ?: return@mapNotNull null
            val primerContenido = catalogo.contenidos.filter { it.objetivoId == objetivo.id }.minByOrNull { it.orden }
            Unidad(
                titulo = unidad.titulo,
                // El servidor guarda LEN-OA01; la app muestra OA01.
                objetivoAprendizaje = objetivo.codigo.substringAfter('-'),
                asignatura = MapeoUnidad.nombreLocal(asignatura.nombre),
                estado = MapeoEstado.aLocal(objetivo.estado),
                contenido = primerContenido?.explicacion ?: objetivo.descripcion,
                objetivoServidorId = objetivo.id
            )
        }
}

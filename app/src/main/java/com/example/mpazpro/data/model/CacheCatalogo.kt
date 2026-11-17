package com.example.mpazpro.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// Caché local del catálogo que entrega el backend (Room v5). Se reemplaza completa cada vez que la app
// descarga el catálogo; no se edita desde la app. Los ids son los del servidor (no se generan aquí).
// Guarda lo que el rol de quien inició sesión puede ver, con sus estados en el vocabulario del servidor
// (BORRADOR, EN_REVISION, AUTORIZADO, PUBLICADO, DESHABILITADO).

@Entity(tableName = "cache_asignatura")
data class CacheAsignatura(
    @PrimaryKey val id: Long,
    val nombre: String,
    val nivel: String
)

@Entity(tableName = "cache_unidad")
data class CacheUnidad(
    @PrimaryKey val id: Long,
    val asignaturaId: Long,
    val titulo: String,
    val orden: Int,
    val estado: String
)

@Entity(tableName = "cache_objetivo")
data class CacheObjetivo(
    @PrimaryKey val id: Long,
    val unidadId: Long,
    val codigo: String,
    val descripcion: String,
    val ejeCurricular: String?,
    val textoReferencia: String?,
    val orden: Int,
    val estado: String
)

@Entity(tableName = "cache_contenido")
data class CacheContenido(
    @PrimaryKey val id: Long,
    val objetivoId: Long,
    val titulo: String,
    val explicacion: String,
    val instrucciones: String?,
    val ejemplosGuiados: String?,
    val imagenUrl: String?,
    val archivoAudio: String?,
    val orden: Int,
    val responsableOrigen: String,
    val licencia: String
)

/** Todo el catálogo guardado, leído de una vez. */
data class CatalogoCache(
    val asignaturas: List<CacheAsignatura>,
    val unidades: List<CacheUnidad>,
    val objetivos: List<CacheObjetivo>,
    val contenidos: List<CacheContenido>
) {
    fun estaVacio() = asignaturas.isEmpty() && unidades.isEmpty()
}

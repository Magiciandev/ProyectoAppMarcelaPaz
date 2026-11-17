package com.example.mpazpro.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.mpazpro.data.model.CacheAsignatura
import com.example.mpazpro.data.model.CacheContenido
import com.example.mpazpro.data.model.CacheObjetivo
import com.example.mpazpro.data.model.CacheUnidad
import com.example.mpazpro.data.model.CatalogoCache

// Clase abstracta (no interfaz) para poder tener un método @Transaction con cuerpo, igual que AsignacionDao.
@Dao
abstract class CatalogoCacheDao {

    @Query("SELECT * FROM cache_asignatura ORDER BY id")
    abstract suspend fun asignaturas(): List<CacheAsignatura>

    @Query("SELECT * FROM cache_unidad ORDER BY asignaturaId, orden, id")
    abstract suspend fun unidades(): List<CacheUnidad>

    @Query("SELECT * FROM cache_objetivo ORDER BY unidadId, orden, id")
    abstract suspend fun objetivos(): List<CacheObjetivo>

    @Query("SELECT * FROM cache_contenido ORDER BY objetivoId, orden, id")
    abstract suspend fun contenidos(): List<CacheContenido>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertarAsignaturas(lista: List<CacheAsignatura>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertarUnidades(lista: List<CacheUnidad>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertarObjetivos(lista: List<CacheObjetivo>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertarContenidos(lista: List<CacheContenido>)

    @Query("DELETE FROM cache_asignatura")
    abstract suspend fun borrarAsignaturas()

    @Query("DELETE FROM cache_unidad")
    abstract suspend fun borrarUnidades()

    @Query("DELETE FROM cache_objetivo")
    abstract suspend fun borrarObjetivos()

    @Query("DELETE FROM cache_contenido")
    abstract suspend fun borrarContenidos()

    /** Reemplaza todo el catálogo en una sola transacción: o queda completo o no cambia nada. */
    @Transaction
    open suspend fun reemplazar(catalogo: CatalogoCache) {
        borrarContenidos()
        borrarObjetivos()
        borrarUnidades()
        borrarAsignaturas()
        insertarAsignaturas(catalogo.asignaturas)
        insertarUnidades(catalogo.unidades)
        insertarObjetivos(catalogo.objetivos)
        insertarContenidos(catalogo.contenidos)
    }

    @Transaction
    open suspend fun leer(): CatalogoCache =
        CatalogoCache(asignaturas(), unidades(), objetivos(), contenidos())

    /** Se usa al cerrar sesión: el catálogo guardado es del rol que ya salió. */
    @Transaction
    open suspend fun vaciar() {
        borrarContenidos()
        borrarObjetivos()
        borrarUnidades()
        borrarAsignaturas()
    }
}

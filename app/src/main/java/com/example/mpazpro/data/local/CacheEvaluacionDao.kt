package com.example.mpazpro.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.mpazpro.data.model.CacheAsignacionEstudiante
import com.example.mpazpro.data.model.CachePaquete

// Clase abstracta (no interfaz) para poder tener un método @Transaction con cuerpo, igual que CatalogoCacheDao.
@Dao
abstract class CacheEvaluacionDao {

    @Query("DELETE FROM cache_asignacion_estudiante WHERE usuarioId = :usuarioId")
    abstract suspend fun borrarAsignaciones(usuarioId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertarAsignaciones(lista: List<CacheAsignacionEstudiante>)

    /** Reemplaza todas las asignaciones de ese estudiante: lo que el servidor ya no entrega deja de estar disponible. */
    @Transaction
    open suspend fun reemplazarAsignaciones(usuarioId: Long, lista: List<CacheAsignacionEstudiante>) {
        borrarAsignaciones(usuarioId)
        insertarAsignaciones(lista)
    }

    @Query("SELECT * FROM cache_asignacion_estudiante WHERE usuarioId = :usuarioId ORDER BY objetivoId")
    abstract suspend fun asignaciones(usuarioId: Long): List<CacheAsignacionEstudiante>

    @Query("SELECT * FROM cache_asignacion_estudiante WHERE usuarioId = :usuarioId AND objetivoId = :objetivoId")
    abstract suspend fun asignacion(usuarioId: Long, objetivoId: Long): CacheAsignacionEstudiante?

    @Query(
        "UPDATE cache_asignacion_estudiante SET intentosUsados = intentosUsados + 1 " +
            "WHERE usuarioId = :usuarioId AND objetivoId = :objetivoId"
    )
    abstract suspend fun sumarIntentoUsado(usuarioId: Long, objetivoId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun guardarPaquete(paquete: CachePaquete)

    @Query("SELECT * FROM cache_paquete WHERE objetivoId = :objetivoId")
    abstract suspend fun paquete(objetivoId: Long): CachePaquete?
}

package com.example.mpazpro.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.mpazpro.data.model.Unidad
import kotlinx.coroutines.flow.Flow

@Dao
interface UnidadDao {

    @Insert
    suspend fun insertar(unidad: Unidad): Long

    // Devuelve un Flow: la lista se actualiza sola cada vez que cambia la tabla.
    @Query("SELECT * FROM unidades ORDER BY id DESC")
    fun observarTodas(): Flow<List<Unidad>>

    @Query("SELECT * FROM unidades WHERE id = :id")
    fun observarPorId(id: Long): Flow<Unidad?>

    @Query("UPDATE unidades SET estado = :estado WHERE id = :id")
    suspend fun actualizarEstado(id: Long, estado: String)

    @Query("SELECT COUNT(*) FROM unidades")
    suspend fun contar(): Int

    @Query("UPDATE unidades SET imagenRuta = :ruta WHERE id = :id")
    suspend fun actualizarImagen(id: Long, ruta: String)

    // Unidades que el estudiante puede ver: publicadas, asignadas a su curso, activas y dentro de fechas.
    @Query(
        "SELECT u.* FROM unidades u INNER JOIN asignaciones a ON a.unidadId = u.id " +
            "WHERE u.estado = :estado AND a.curso = :curso AND a.activa = 1 " +
            "AND :ahora BETWEEN a.desde AND a.hasta ORDER BY u.asignatura, u.id"
    )
    fun observarVisibles(estado: String, curso: String, ahora: Long): Flow<List<Unidad>>
}

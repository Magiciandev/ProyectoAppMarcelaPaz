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

    // Unidades en el estado dado que ya tienen al menos una pregunta (las que se pueden rendir).
    @Query("SELECT * FROM unidades WHERE estado = :estado AND id IN (SELECT unidadId FROM preguntas) ORDER BY asignatura, id")
    fun observarConPrueba(estado: String): Flow<List<Unidad>>
}

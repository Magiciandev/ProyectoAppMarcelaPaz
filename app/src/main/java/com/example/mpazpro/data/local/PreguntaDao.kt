package com.example.mpazpro.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.mpazpro.data.model.Pregunta
import kotlinx.coroutines.flow.Flow

@Dao
interface PreguntaDao {
    @Insert
    suspend fun insertar(pregunta: Pregunta): Long

    @Query("DELETE FROM preguntas WHERE id = :id")
    suspend fun eliminar(id: Long)

    @Query("SELECT * FROM preguntas WHERE unidadId = :unidadId ORDER BY id")
    fun observarPorUnidad(unidadId: Long): Flow<List<Pregunta>>

    @Query("SELECT * FROM preguntas WHERE unidadId = :unidadId ORDER BY id")
    suspend fun listarPorUnidad(unidadId: Long): List<Pregunta>
}

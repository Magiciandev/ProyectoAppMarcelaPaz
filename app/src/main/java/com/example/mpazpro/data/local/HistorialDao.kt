package com.example.mpazpro.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.mpazpro.data.model.HistorialUnidad
import kotlinx.coroutines.flow.Flow

@Dao
interface HistorialDao {
    @Insert
    suspend fun insertar(entrada: HistorialUnidad): Long

    @Query("SELECT * FROM historial_unidad WHERE unidadId = :unidadId ORDER BY fecha DESC, id DESC")
    fun observarPorUnidad(unidadId: Long): Flow<List<HistorialUnidad>>
}

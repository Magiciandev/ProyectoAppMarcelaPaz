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
}

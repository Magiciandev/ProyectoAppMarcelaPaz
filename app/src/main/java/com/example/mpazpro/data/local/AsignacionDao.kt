package com.example.mpazpro.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.mpazpro.data.model.Asignacion
import com.example.mpazpro.data.model.Configuracion
import kotlinx.coroutines.flow.Flow

@Dao
abstract class AsignacionDao {
    // Con el índice único (unidadId, curso), asignar de nuevo reemplaza la asignación anterior.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun guardarAsignacion(asignacion: Asignacion): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun guardarConfig(config: Configuracion)

    @Query("SELECT valor FROM configuracion WHERE clave = :clave")
    abstract suspend fun valor(clave: String): String?

    @Query("SELECT * FROM asignaciones WHERE unidadId = :unidadId ORDER BY curso")
    abstract fun observarPorUnidad(unidadId: Long): Flow<List<Asignacion>>

    // Asignación e intentos se guardan juntos: o ambos o ninguno.
    @Transaction
    open suspend fun guardar(asignacion: Asignacion, claveIntentos: String, intentos: Int): Long {
        val id = guardarAsignacion(asignacion)
        guardarConfig(Configuracion(claveIntentos, intentos.toString()))
        return id
    }
}

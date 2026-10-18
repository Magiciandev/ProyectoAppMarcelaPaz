package com.example.mpazpro.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.example.mpazpro.data.model.HistorialUnidad
import com.example.mpazpro.data.model.TransicionesUnidad
import com.example.mpazpro.data.model.Unidad
import kotlinx.coroutines.flow.Flow

// Clase abstracta (y no interfaz) porque tiene funciones con cuerpo marcadas @Transaction.
@Dao
abstract class UnidadDao {

    @Insert
    abstract suspend fun insertar(unidad: Unidad): Long

    @Insert
    abstract suspend fun insertarHistorial(entrada: HistorialUnidad): Long

    // Los Flow se actualizan solos cada vez que cambia la tabla.
    @Query("SELECT * FROM unidades ORDER BY id DESC")
    abstract fun observarTodas(): Flow<List<Unidad>>

    @Query("SELECT * FROM unidades WHERE id = :id")
    abstract fun observarPorId(id: Long): Flow<Unidad?>

    @Query("SELECT * FROM historial_unidad WHERE unidadId = :unidadId ORDER BY id DESC")
    abstract fun observarHistorial(unidadId: Long): Flow<List<HistorialUnidad>>

    @Query("UPDATE unidades SET estado = :estado WHERE id = :id")
    abstract suspend fun actualizarEstado(id: Long, estado: String)

    @Query("SELECT COUNT(*) FROM unidades")
    abstract suspend fun contar(): Int

    // Las dos escrituras ocurren juntas: o se guardan ambas o ninguna.
    @Transaction
    open suspend fun crear(unidad: Unidad, fecha: String): Long {
        val id = insertar(unidad)
        insertarHistorial(HistorialUnidad(unidadId = id, fecha = fecha, evento = TransicionesUnidad.EVENTO_CREADA))
        return id
    }

    @Transaction
    open suspend fun cambiarEstado(id: Long, estado: String, evento: String, fecha: String) {
        actualizarEstado(id, estado)
        insertarHistorial(HistorialUnidad(unidadId = id, fecha = fecha, evento = evento))
    }
}

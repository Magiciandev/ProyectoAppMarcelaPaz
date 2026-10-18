package com.example.mpazpro.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.mpazpro.data.model.Intento
import com.example.mpazpro.data.model.Resultado
import kotlinx.coroutines.flow.Flow

@Dao
abstract class EvaluacionDao {
    @Insert
    abstract suspend fun insertarResultado(resultado: Resultado): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun guardarIntento(intento: Intento)

    @Query("SELECT usados FROM intentos WHERE estudianteId = :estudianteId AND unidadId = :unidadId")
    abstract suspend fun intentosUsados(estudianteId: Long, unidadId: Long): Int?

    @Query("SELECT * FROM resultados WHERE estudianteId = :estudianteId ORDER BY id")
    abstract fun observarResultados(estudianteId: Long): Flow<List<Resultado>>

    // Guarda el resultado y suma el intento en una sola transacción: o se hacen ambos o ninguno.
    @Transaction
    open suspend fun registrar(resultado: Resultado) {
        insertarResultado(resultado)
        val usados = intentosUsados(resultado.estudianteId, resultado.unidadId) ?: 0
        guardarIntento(Intento(resultado.estudianteId, resultado.unidadId, usados + 1))
    }
}

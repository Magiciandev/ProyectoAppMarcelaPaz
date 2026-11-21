package com.example.mpazpro.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mpazpro.data.model.IntentoPendiente

@Dao
interface IntentoPendienteDao {

    // Guardar dos veces el mismo intento (mismo uuid) deja uno solo.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(intento: IntentoPendiente)

    /** Del más antiguo al más nuevo: se envían en el orden en que se rindieron. */
    @Query("SELECT * FROM intento_pendiente WHERE usuarioId = :usuarioId ORDER BY fechaHora, uuid")
    suspend fun pendientes(usuarioId: Long): List<IntentoPendiente>

    @Query("SELECT COUNT(*) FROM intento_pendiente WHERE usuarioId = :usuarioId AND objetivoId = :objetivoId")
    suspend fun contar(usuarioId: Long, objetivoId: Long): Int

    @Query("DELETE FROM intento_pendiente WHERE uuid IN (:uuids)")
    suspend fun borrar(uuids: List<String>)
}

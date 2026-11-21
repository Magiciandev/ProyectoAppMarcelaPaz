package com.example.mpazpro.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mpazpro.data.model.ItemCompletado
import com.example.mpazpro.data.model.TotalesObjetivo

@Dao
interface AvanceLocalDao {

    // REPLACE: completar de nuevo un ítem deja la fecha de la última vez.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun marcar(item: ItemCompletado)

    @Query("SELECT * FROM item_completado WHERE usuarioId = :usuarioId AND objetivoId = :objetivoId")
    suspend fun itemsDe(usuarioId: Long, objetivoId: Long): List<ItemCompletado>

    @Query("SELECT * FROM item_completado WHERE usuarioId = :usuarioId")
    suspend fun itemsDelUsuario(usuarioId: Long): List<ItemCompletado>

    @Query("SELECT DISTINCT objetivoId FROM item_completado WHERE usuarioId = :usuarioId")
    suspend fun objetivosConAvance(usuarioId: Long): List<Long>

    /** Borra lo hecho hasta el último reinicio del docente (inclusive): ya no cuenta. */
    @Query("DELETE FROM item_completado WHERE usuarioId = :usuarioId AND completadoEn <= :hastaSegundos")
    suspend fun borrarHasta(usuarioId: Long, hastaSegundos: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarTotales(totales: TotalesObjetivo)

    @Query("SELECT * FROM totales_objetivo WHERE objetivoId = :objetivoId")
    suspend fun totalesDe(objetivoId: Long): TotalesObjetivo?
}

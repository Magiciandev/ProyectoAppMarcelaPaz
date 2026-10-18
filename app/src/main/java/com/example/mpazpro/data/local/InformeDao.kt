package com.example.mpazpro.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mpazpro.data.model.Observacion
import com.example.mpazpro.data.model.PromedioAsignatura
import com.example.mpazpro.data.model.ResultadoUnidad
import com.example.mpazpro.data.model.Usuario
import kotlinx.coroutines.flow.Flow

// Consultas simples (AVG, GROUP BY, subconsulta): sin funciones de ventana, para que corran en Android 10.
@Dao
interface InformeDao {
    // El último intento de cada unidad = el resultado con id máximo por unidad.
    @Query(
        """SELECT u.asignatura AS asignatura, AVG(r.correctas * 100.0 / r.total) AS promedio
           FROM resultados r JOIN unidades u ON u.id = r.unidadId
           WHERE r.estudianteId = :estudianteId AND r.total > 0
             AND r.id IN (SELECT MAX(id) FROM resultados WHERE estudianteId = :estudianteId GROUP BY unidadId)
           GROUP BY u.asignatura"""
    )
    fun observarPromedios(estudianteId: Long): Flow<List<PromedioAsignatura>>

    @Query(
        """SELECT r.*, u.titulo AS titulo, u.asignatura AS asignatura
           FROM resultados r JOIN unidades u ON u.id = r.unidadId
           WHERE r.estudianteId = :estudianteId
             AND r.id IN (SELECT MAX(id) FROM resultados WHERE estudianteId = :estudianteId GROUP BY unidadId)
           ORDER BY u.asignatura, u.id"""
    )
    fun observarUltimos(estudianteId: Long): Flow<List<ResultadoUnidad>>

    @Query("SELECT * FROM usuarios WHERE rol = :rol ORDER BY curso, nombre")
    fun observarPorRol(rol: String): Flow<List<Usuario>>

    @Query("SELECT * FROM observaciones WHERE estudianteId = :estudianteId")
    fun observarObservacion(estudianteId: Long): Flow<Observacion?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarObservacion(observacion: Observacion)

    // asignatura = null reinicia todo; con valor, solo las unidades de esa asignatura.
    @Query(
        """DELETE FROM resultados WHERE estudianteId = :estudianteId
           AND (:asignatura IS NULL OR unidadId IN (SELECT id FROM unidades WHERE asignatura = :asignatura))"""
    )
    suspend fun reiniciarProgreso(estudianteId: Long, asignatura: String?)

    @Query(
        """DELETE FROM intentos WHERE estudianteId = :estudianteId
           AND (:asignatura IS NULL OR unidadId IN (SELECT id FROM unidades WHERE asignatura = :asignatura))"""
    )
    suspend fun reiniciarIntentos(estudianteId: Long, asignatura: String?)
}

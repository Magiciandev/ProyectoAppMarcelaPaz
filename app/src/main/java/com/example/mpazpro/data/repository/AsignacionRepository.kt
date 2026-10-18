package com.example.mpazpro.data.repository

import com.example.mpazpro.data.local.AsignacionDao
import com.example.mpazpro.data.model.Asignacion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

// Asignación de unidades a cursos y configuración de intentos (la define el docente).
class AsignacionRepository(private val dao: AsignacionDao) {

    companion object {
        const val INTENTOS_DEFECTO = 3
        fun claveIntentos(unidadId: Long) = "intentos_max_$unidadId"
    }

    fun observarPorUnidad(unidadId: Long): Flow<List<Asignacion>> = dao.observarPorUnidad(unidadId)

    suspend fun intentosMaximos(unidadId: Long): Int =
        withContext(Dispatchers.IO) {
            dao.valor(claveIntentos(unidadId))?.toIntOrNull() ?: INTENTOS_DEFECTO
        }

    suspend fun guardar(asignacion: Asignacion, intentos: Int): Result<Long> =
        withContext(Dispatchers.IO) {
            runCatching { dao.guardar(asignacion, claveIntentos(asignacion.unidadId), intentos) }
        }
}

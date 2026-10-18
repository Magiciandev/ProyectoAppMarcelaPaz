package com.example.mpazpro.data.repository

import com.example.mpazpro.data.local.UnidadDao
import com.example.mpazpro.data.model.Unidad
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

// Fuente única de la verdad para las unidades: el ViewModel nunca habla con el DAO directamente.
class UnidadRepository(private val dao: UnidadDao) {

    fun observarUnidades(): Flow<List<Unidad>> = dao.observarTodas()

    suspend fun guardar(unidad: Unidad): Result<Long> =
        withContext(Dispatchers.IO) {
            runCatching { dao.insertar(unidad) }
        }
}

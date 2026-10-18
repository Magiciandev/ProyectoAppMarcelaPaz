package com.example.mpazpro.data.repository

import com.example.mpazpro.data.local.UnidadDao
import com.example.mpazpro.data.model.HistorialUnidad
import com.example.mpazpro.data.model.Unidad
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

// Fuente única de la verdad para las unidades: el ViewModel nunca habla con el DAO directamente.
// `hoy` entrega la fecha actual como texto (se puede reemplazar en las pruebas).
class UnidadRepository(
    private val dao: UnidadDao,
    private val hoy: () -> String = { LocalDate.now().toString() }
) {

    fun observarUnidades(): Flow<List<Unidad>> = dao.observarTodas()

    fun observarUnidad(id: Long): Flow<Unidad?> = dao.observarPorId(id)

    fun observarHistorial(id: Long): Flow<List<HistorialUnidad>> = dao.observarHistorial(id)

    suspend fun guardar(unidad: Unidad): Result<Long> =
        withContext(Dispatchers.IO) {
            runCatching { dao.crear(unidad, hoy()) }
        }

    suspend fun cambiarEstado(id: Long, nuevoEstado: String, evento: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching { dao.cambiarEstado(id, nuevoEstado, evento, hoy()) }
        }
}

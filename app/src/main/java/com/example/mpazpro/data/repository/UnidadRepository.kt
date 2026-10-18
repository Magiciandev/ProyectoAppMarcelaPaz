package com.example.mpazpro.data.repository

import com.example.mpazpro.data.local.HistorialDao
import com.example.mpazpro.data.local.UnidadDao
import com.example.mpazpro.data.model.EstadoUnidad
import com.example.mpazpro.data.model.HistorialUnidad
import com.example.mpazpro.data.model.Unidad
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

// Fuente única de la verdad para las unidades: el ViewModel nunca habla con el DAO directamente.
class UnidadRepository(
    private val dao: UnidadDao,
    private val historialDao: HistorialDao
) {

    fun observarUnidades(): Flow<List<Unidad>> = dao.observarTodas()

    fun observarUnidad(id: Long): Flow<Unidad?> = dao.observarPorId(id)

    fun observarHistorial(unidadId: Long): Flow<List<HistorialUnidad>> =
        historialDao.observarPorUnidad(unidadId)

    fun observarPublicadasConPrueba(): Flow<List<Unidad>> =
        dao.observarConPrueba(EstadoUnidad.PUBLICADO)

    suspend fun guardar(unidad: Unidad): Result<Long> =
        withContext(Dispatchers.IO) {
            runCatching {
                val id = dao.insertar(unidad)
                registrar(id, "Unidad creada en borrador")
                id
            }
        }

    // Cambia el estado y deja la entrada de historial con fecha.
    suspend fun cambiarEstado(id: Long, nuevoEstado: String, evento: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                dao.actualizarEstado(id, nuevoEstado)
                registrar(id, evento)
            }
        }

    private suspend fun registrar(unidadId: Long, evento: String) {
        historialDao.insertar(HistorialUnidad(unidadId = unidadId, fecha = System.currentTimeMillis(), evento = evento))
    }
}

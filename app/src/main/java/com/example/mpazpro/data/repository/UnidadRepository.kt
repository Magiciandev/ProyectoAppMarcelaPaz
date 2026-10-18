package com.example.mpazpro.data.repository

import android.net.Uri
import com.example.mpazpro.data.local.AlmacenImagenes
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
    private val historialDao: HistorialDao,
    private val almacen: AlmacenImagenes
) {

    fun observarUnidades(): Flow<List<Unidad>> = dao.observarTodas()

    fun observarUnidad(id: Long): Flow<Unidad?> = dao.observarPorId(id)

    fun observarHistorial(unidadId: Long): Flow<List<HistorialUnidad>> =
        historialDao.observarPorUnidad(unidadId)

    // Lo que ve el estudiante: publicadas, asignadas a su curso y dentro de las fechas.
    fun observarVisibles(curso: String): Flow<List<Unidad>> =
        dao.observarVisibles(EstadoUnidad.PUBLICADO, curso, System.currentTimeMillis())

    // Copia la imagen al almacenamiento interno y guarda la ruta en la unidad.
    suspend fun guardarImagen(unidadId: Long, origen: Uri): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val ruta = almacen.copiar(origen, "unidad_${unidadId}_${System.currentTimeMillis()}.jpg")
                dao.actualizarImagen(unidadId, ruta)
                registrar(unidadId, "Imagen de apoyo actualizada")
            }
        }

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

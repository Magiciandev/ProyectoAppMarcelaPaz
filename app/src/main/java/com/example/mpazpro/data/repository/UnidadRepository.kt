package com.example.mpazpro.data.repository

import android.net.Uri
import com.example.mpazpro.data.local.AlmacenImagenes
import com.example.mpazpro.data.local.HistorialDao
import com.example.mpazpro.data.local.UnidadDao
import com.example.mpazpro.data.model.EstadoUnidad
import com.example.mpazpro.data.model.HistorialUnidad
import com.example.mpazpro.data.model.Unidad
import com.example.mpazpro.data.remote.ResultadoRemoto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

// Fuente única de la verdad para las unidades: el ViewModel nunca habla con el DAO directamente.
class UnidadRepository(
    private val dao: UnidadDao,
    private val historialDao: HistorialDao,
    private val almacen: AlmacenImagenes,
    private val servidor: UnidadServidor
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

    // Primero se crea en el servidor; solo si lo acepta se guarda la copia local (así lo demás de la app,
    // que aún lee de Room, sigue funcionando). Sin conexión no se crea: crear sin red llega en la fase 7.
    suspend fun guardar(unidad: Unidad): Result<Long> =
        withContext(Dispatchers.IO) {
            when (val remoto = servidor.crear(unidad)) {
                is ResultadoRemoto.Ok -> runCatching {
                    val id = dao.insertar(unidad.copy(objetivoServidorId = remoto.valor))
                    registrar(id, "Unidad creada en borrador")
                    id
                }
                is ResultadoRemoto.ErrorHttp ->
                    Result.failure(IllegalStateException("No se pudo crear la unidad: ${remoto.mensaje}"))
                is ResultadoRemoto.SinConexion ->
                    Result.failure(IllegalStateException("No se pudo crear la unidad: ${remoto.mensaje}"))
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

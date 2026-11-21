package com.example.mpazpro.data.repository

import android.net.Uri
import com.example.mpazpro.data.local.AlmacenImagenes
import com.example.mpazpro.data.local.HistorialDao
import com.example.mpazpro.data.local.UnidadDao
import com.example.mpazpro.data.model.AccionEstado
import com.example.mpazpro.data.model.EstadoUnidad
import com.example.mpazpro.data.model.HistorialUnidad
import com.example.mpazpro.data.model.MapeoEstado
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
    private val servidor: UnidadServidor,
    private val aprobaciones: AprobacionServidor,
    private val catalogo: CatalogoRepository
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

    // Cambia el estado y deja la entrada de historial con fecha. Si la unidad existe en el servidor, el cambio se
    // pide primero a ms-admin (que valida la transición) y solo si lo acepta se refleja en el teléfono.
    // Una unidad que solo existe en el teléfono (los datos de ejemplo) cambia de estado solo aquí.
    suspend fun cambiarEstado(unidad: Unidad, accion: AccionEstado): Result<Unit> =
        withContext(Dispatchers.IO) {
            val objetivoId = unidad.objetivoServidorId
            if (objetivoId == null) {
                runCatching { aplicarLocal(unidad.id, accion.hasta, accion.evento) }
            } else {
                when (val remoto = aprobaciones.aplicar(objetivoId, accion)) {
                    is ResultadoRemoto.Ok -> runCatching {
                        aplicarLocal(unidad.id, MapeoEstado.aLocal(remoto.valor.estadoNuevo), accion.evento)
                    }
                    is ResultadoRemoto.ErrorHttp -> Result.failure(IllegalStateException(remoto.mensaje))
                    is ResultadoRemoto.SinConexion -> Result.failure(IllegalStateException(remoto.mensaje))
                }
            }
        }

    // La copia local de una unidad del servidor (null si todavía no se ha descargado).
    suspend fun buscarPorObjetivoServidor(objetivoId: Long): Unidad? =
        withContext(Dispatchers.IO) { dao.buscarPorObjetivoServidor(objetivoId) }

    // Descarga el catálogo del servidor y deja una unidad local por cada objetivo (nueva o puesta al día).
    // Devuelve cuántas unidades cambiaron. Las unidades que solo existen en el teléfono no se tocan.
    suspend fun actualizarDesdeServidor(): ResultadoRemoto<Int> =
        withContext(Dispatchers.IO) {
            when (val descarga = catalogo.refrescar()) {
                is ResultadoRemoto.Ok -> ResultadoRemoto.Ok(guardarDelServidor(MapeoCatalogo.aUnidades(descarga.valor)))
                is ResultadoRemoto.ErrorHttp -> descarga
                is ResultadoRemoto.SinConexion -> descarga
            }
        }

    private suspend fun guardarDelServidor(delServidor: List<Unidad>): Int {
        var cambios = 0
        for (nueva in delServidor) {
            val objetivoId = nueva.objetivoServidorId ?: continue
            val local = dao.buscarPorObjetivoServidor(objetivoId)
            if (local == null) {
                val id = dao.insertar(nueva)
                registrar(id, "Unidad descargada del servidor")
                cambios++
            } else if (local.estado != nueva.estado || local.titulo != nueva.titulo || local.contenido != nueva.contenido) {
                dao.actualizarDesdeServidor(
                    local.id, nueva.titulo, nueva.objetivoAprendizaje, nueva.asignatura, nueva.estado, nueva.contenido
                )
                if (local.estado != nueva.estado) registrar(local.id, "Estado en el servidor: ${nueva.estado}")
                cambios++
            }
        }
        return cambios
    }

    private suspend fun aplicarLocal(id: Long, estado: String, evento: String) {
        dao.actualizarEstado(id, estado)
        registrar(id, evento)
    }

    private suspend fun registrar(unidadId: Long, evento: String) {
        historialDao.insertar(HistorialUnidad(unidadId = unidadId, fecha = System.currentTimeMillis(), evento = evento))
    }
}

package com.example.mpazpro.data.repository

import com.example.mpazpro.data.local.AsignacionDao
import com.example.mpazpro.data.local.UnidadDao
import com.example.mpazpro.data.model.Asignacion
import com.example.mpazpro.data.remote.ResultadoRemoto
import com.example.mpazpro.data.remote.dto.AsignacionEstudianteDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

// Asignación de unidades a cursos y configuración de intentos (la define el docente).
// Si la unidad existe en el servidor, la asignación se guarda allí; si solo existe en el teléfono (datos de
// ejemplo), se guarda en Room como siempre. Sin `unidadDao` ni `servidor` todo es local (así lo usan las pruebas).
class AsignacionRepository(
    private val dao: AsignacionDao,
    private val unidadDao: UnidadDao? = null,
    private val servidor: AsignacionServidor? = null
) {

    companion object {
        const val INTENTOS_DEFECTO = 3
        fun claveIntentos(unidadId: Long) = "intentos_max_$unidadId"
    }

    // Solo las asignaciones locales; las del servidor llegan por asignacionesDelServidor.
    fun observarPorUnidad(unidadId: Long): Flow<List<Asignacion>> = dao.observarPorUnidad(unidadId)

    suspend fun intentosMaximos(unidadId: Long): Int =
        withContext(Dispatchers.IO) {
            dao.valor(claveIntentos(unidadId))?.toIntOrNull() ?: INTENTOS_DEFECTO
        }

    // Devuelve el id de la asignación guardada. Las asignaciones del servidor no se copian a Room: así los
    // estudiantes del teléfono no ven unidades cuyas preguntas aún no llegan a la app (se migra en el paso siguiente).
    suspend fun guardar(asignacion: Asignacion, intentos: Int): Result<Long> =
        withContext(Dispatchers.IO) {
            val objetivoId = objetivoServidorDe(asignacion.unidadId)
            if (objetivoId == null || servidor == null) {
                runCatching { dao.guardar(asignacion, claveIntentos(asignacion.unidadId), intentos) }
            } else {
                when (val remoto = servidor.guardar(objetivoId, asignacion, intentos)) {
                    is ResultadoRemoto.Ok -> Result.success(remoto.valor.id)
                    is ResultadoRemoto.ErrorHttp -> Result.failure(IllegalStateException(remoto.mensaje))
                    is ResultadoRemoto.SinConexion -> Result.failure(IllegalStateException(remoto.mensaje))
                }
            }
        }

    // Las asignaciones de la unidad que están en el servidor. Si la unidad es solo local o no hay conexión,
    // la lista viene vacía (la pantalla igual muestra las locales).
    suspend fun asignacionesDelServidor(unidadId: Long): List<Asignacion> =
        withContext(Dispatchers.IO) {
            val objetivoId = objetivoServidorDe(unidadId)
            if (objetivoId == null || servidor == null) {
                emptyList()
            } else {
                when (val remoto = servidor.listar(objetivoId, unidadId)) {
                    is ResultadoRemoto.Ok -> remoto.valor
                    is ResultadoRemoto.ErrorHttp -> emptyList()
                    is ResultadoRemoto.SinConexion -> emptyList()
                }
            }
        }

    // Estudiante: lo que su docente le asignó en el servidor. Sin conexión o sin sesión del servidor, la lista viene
    // vacía y la pantalla muestra solo lo que hay en el teléfono.
    suspend fun misAsignacionesDelServidor(): List<AsignacionEstudianteDto> =
        withContext(Dispatchers.IO) {
            if (servidor == null) {
                emptyList()
            } else {
                when (val remoto = servidor.misAsignaciones()) {
                    is ResultadoRemoto.Ok -> remoto.valor
                    is ResultadoRemoto.ErrorHttp -> emptyList()
                    is ResultadoRemoto.SinConexion -> emptyList()
                }
            }
        }

    // Estudiante: deja registrada la conexión en el servidor. Si falla no pasa nada: es solo información para el docente.
    suspend fun registrarConexion() {
        withContext(Dispatchers.IO) { servidor?.registrarConexion() }
    }

    private suspend fun objetivoServidorDe(unidadId: Long): Long? = unidadDao?.buscarPorId(unidadId)?.objetivoServidorId
}

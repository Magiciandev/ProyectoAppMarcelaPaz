package com.example.mpazpro.data.repository

import com.example.mpazpro.data.local.InformeDao
import com.example.mpazpro.data.local.PreguntaDao
import com.example.mpazpro.data.local.UsuarioDao
import com.example.mpazpro.data.model.CalculoProgreso
import com.example.mpazpro.data.model.DesglosePregunta
import com.example.mpazpro.data.model.DetalleUnidad
import com.example.mpazpro.data.model.InformeEstudiante
import com.example.mpazpro.data.model.Observacion
import com.example.mpazpro.data.model.Roles
import com.example.mpazpro.data.model.Usuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

// Informes reservados al docente: cada operación comprueba el rol de quien la pide.
class InformeRepository(
    private val dao: InformeDao,
    private val usuarioDao: UsuarioDao,
    private val preguntaDao: PreguntaDao
) {
    private suspend fun exigirDocente(docenteId: Long) {
        val actor = usuarioDao.buscarPorId(docenteId)
        if (actor?.rol != Roles.DOCENTE) throw SecurityException("Los informes son solo para el docente")
    }

    fun observarEstudiantes(docenteId: Long): Flow<List<Usuario>> = flow {
        exigirDocente(docenteId)
        emitAll(dao.observarPorRol(Roles.ESTUDIANTE))
    }.flowOn(Dispatchers.IO)

    fun observarInforme(docenteId: Long, estudianteId: Long): Flow<InformeEstudiante> = flow {
        exigirDocente(docenteId)
        val estudiante = usuarioDao.buscarPorId(estudianteId)
            ?: throw IllegalStateException("Estudiante no encontrado")
        emitAll(
            combine(
                dao.observarPromedios(estudianteId),
                dao.observarUltimos(estudianteId),
                dao.observarObservacion(estudianteId)
            ) { promedios, ultimos, observacion ->
                val porAsignatura = CalculoProgreso.porAsignatura(promedios)
                val detalle = ultimos.map { u ->
                    val preguntas = preguntaDao.listarPorUnidad(u.resultado.unidadId)
                    val elegidas = u.resultado.respuestas.split(",").map { it.toIntOrNull() }
                    DetalleUnidad(
                        titulo = u.titulo, asignatura = u.asignatura, porcentaje = u.resultado.porcentaje,
                        desglose = preguntas.mapIndexed { i, p ->
                            val e = elegidas.getOrNull(i)
                            DesglosePregunta(p.texto, e?.let { p.opciones().getOrNull(it) },
                                p.opciones()[p.indiceCorrecta], e == p.indiceCorrecta)
                        }
                    )
                }
                InformeEstudiante(estudiante, porAsignatura, CalculoProgreso.general(porAsignatura),
                    detalle, observacion?.texto.orEmpty())
            }
        )
    }.flowOn(Dispatchers.IO)

    suspend fun guardarObservacion(docenteId: Long, estudianteId: Long, texto: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                exigirDocente(docenteId)
                dao.guardarObservacion(Observacion(estudianteId, texto.trim(), System.currentTimeMillis()))
            }
        }

    // Progreso e intentos se reinician por separado; asignatura = null es "todo".
    suspend fun reiniciarProgreso(docenteId: Long, estudianteId: Long, asignatura: String?): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching { exigirDocente(docenteId); dao.reiniciarProgreso(estudianteId, asignatura) }
        }

    suspend fun reiniciarIntentos(docenteId: Long, estudianteId: Long, asignatura: String?): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching { exigirDocente(docenteId); dao.reiniciarIntentos(estudianteId, asignatura) }
        }
}

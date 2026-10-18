package com.example.mpazpro.data.repository

import com.example.mpazpro.data.local.EvaluacionDao
import com.example.mpazpro.data.local.PreguntaDao
import com.example.mpazpro.data.model.Pregunta
import com.example.mpazpro.data.model.Resultado
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class EvaluacionRepository(
    private val preguntaDao: PreguntaDao,
    private val evaluacionDao: EvaluacionDao
) {
    companion object {
        // En la fase 5 el docente configura este valor (1 a 5); por ahora es fijo.
        const val INTENTOS_MAXIMOS = 3
    }

    fun observarPreguntas(unidadId: Long): Flow<List<Pregunta>> = preguntaDao.observarPorUnidad(unidadId)

    fun observarResultados(estudianteId: Long): Flow<List<Resultado>> =
        evaluacionDao.observarResultados(estudianteId)

    suspend fun agregarPregunta(pregunta: Pregunta): Result<Long> =
        withContext(Dispatchers.IO) { runCatching { preguntaDao.insertar(pregunta) } }

    suspend fun eliminarPregunta(id: Long): Result<Unit> =
        withContext(Dispatchers.IO) { runCatching { preguntaDao.eliminar(id) } }

    suspend fun obtenerPreguntas(unidadId: Long): List<Pregunta> =
        withContext(Dispatchers.IO) { preguntaDao.listarPorUnidad(unidadId) }

    suspend fun intentosUsados(estudianteId: Long, unidadId: Long): Int =
        withContext(Dispatchers.IO) { evaluacionDao.intentosUsados(estudianteId, unidadId) ?: 0 }

    suspend fun registrarResultado(
        estudianteId: Long, unidadId: Long, correctas: Int, total: Int, respuestas: List<Int>
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            evaluacionDao.registrar(
                Resultado(
                    estudianteId = estudianteId, unidadId = unidadId, correctas = correctas,
                    total = total, respuestas = respuestas.joinToString(","),
                    fecha = System.currentTimeMillis()
                )
            )
        }
    }
}

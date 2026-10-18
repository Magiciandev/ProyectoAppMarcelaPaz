package com.example.mpazpro.viewmodel.fakes

import com.example.mpazpro.data.local.AsignacionDao
import com.example.mpazpro.data.local.EvaluacionDao
import com.example.mpazpro.data.local.PreguntaDao
import com.example.mpazpro.data.model.Asignacion
import com.example.mpazpro.data.model.Configuracion
import com.example.mpazpro.data.model.Intento
import com.example.mpazpro.data.model.Pregunta
import com.example.mpazpro.data.model.Resultado
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

// DAO falsos en memoria: los repositorios reales trabajan sobre ellos y los ViewModels
// se prueban sin Room ni Android. Los métodos con @Transaction (guardar, registrar) se heredan.

class FakeAsignacionDao(val config: MutableMap<String, String> = mutableMapOf()) : AsignacionDao() {
    val guardadas = mutableListOf<Asignacion>()
    private val flujo = MutableStateFlow<List<Asignacion>>(emptyList())

    override suspend fun guardarAsignacion(asignacion: Asignacion): Long {
        guardadas.add(asignacion)
        flujo.value = guardadas.toList()
        return guardadas.size.toLong()
    }

    override suspend fun guardarConfig(config: Configuracion) { this.config[config.clave] = config.valor }
    override suspend fun valor(clave: String): String? = config[clave]
    override fun observarPorUnidad(unidadId: Long): Flow<List<Asignacion>> = flujo
}

class FakePreguntaDao(private val preguntas: List<Pregunta> = emptyList()) : PreguntaDao {
    override suspend fun insertar(pregunta: Pregunta): Long = 0
    override suspend fun eliminar(id: Long) = Unit
    override fun observarPorUnidad(unidadId: Long): Flow<List<Pregunta>> = MutableStateFlow(preguntas)
    override suspend fun listarPorUnidad(unidadId: Long): List<Pregunta> = preguntas
}

class FakeEvaluacionDao(var usados: Int = 0) : EvaluacionDao() {
    val resultados = mutableListOf<Resultado>()

    override suspend fun insertarResultado(resultado: Resultado): Long {
        resultados.add(resultado)
        return resultados.size.toLong()
    }

    override suspend fun guardarIntento(intento: Intento) { usados = intento.usados }
    override suspend fun intentosUsados(estudianteId: Long, unidadId: Long): Int? = usados
    override fun observarResultados(estudianteId: Long): Flow<List<Resultado>> = MutableStateFlow(resultados.toList())
}

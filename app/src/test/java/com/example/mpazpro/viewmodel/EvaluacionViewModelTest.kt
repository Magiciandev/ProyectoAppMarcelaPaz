package com.example.mpazpro.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.example.mpazpro.data.model.Pregunta
import com.example.mpazpro.data.repository.AsignacionRepository
import com.example.mpazpro.data.repository.EvaluacionRepository
import com.example.mpazpro.viewmodel.fakes.FakeAsignacionDao
import com.example.mpazpro.viewmodel.fakes.FakeEvaluacionDao
import com.example.mpazpro.viewmodel.fakes.FakePreguntaDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EvaluacionViewModelTest {

    @Before fun antes() { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    @After fun despues() { Dispatchers.resetMain() }

    // Tres preguntas; la alternativa correcta es siempre la B (índice 1).
    private val preguntas = (1..3).map {
        Pregunta(unidadId = 1, texto = "P$it", opcionA = "a", opcionB = "b", opcionC = "c", opcionD = "d", indiceCorrecta = 1)
    }

    private fun crear(evaluacionDao: FakeEvaluacionDao, lista: List<Pregunta> = preguntas) =
        EvaluacionViewModel(
            SavedStateHandle(mapOf("unidadId" to 1L, "estudianteId" to 9L)),
            EvaluacionRepository(FakePreguntaDao(lista), evaluacionDao),
            AsignacionRepository(FakeAsignacionDao()) // intentos máximos por defecto: 3
        )

    private fun esperar(vm: EvaluacionViewModel, cond: (EvaluacionUiState) -> Boolean) = runBlocking {
        withTimeout(5_000) { vm.uiState.first(cond) }
    }

    @Test fun `con preguntas y sin intentos usados parte en la primera pregunta`() {
        val vm = crear(FakeEvaluacionDao())
        val estado = esperar(vm) { it !is EvaluacionUiState.Cargando }
        assertTrue(estado is EvaluacionUiState.EnCurso && estado.indice == 0)
    }

    @Test fun `sin preguntas muestra el estado SinPreguntas`() {
        val vm = crear(FakeEvaluacionDao(), emptyList())
        assertTrue(esperar(vm) { it !is EvaluacionUiState.Cargando } is EvaluacionUiState.SinPreguntas)
    }

    @Test fun `con los intentos agotados la prueba queda bloqueada`() {
        val vm = crear(FakeEvaluacionDao(usados = 3))
        val estado = esperar(vm) { it !is EvaluacionUiState.Cargando }
        assertEquals(EvaluacionUiState.Bloqueada(3, 3), estado)
    }

    @Test fun `solo se acepta una respuesta por pregunta`() {
        val vm = crear(FakeEvaluacionDao())
        esperar(vm) { it is EvaluacionUiState.EnCurso }
        vm.responder(1)
        vm.responder(2) // se ignora
        assertEquals(1, (vm.uiState.value as EvaluacionUiState.EnCurso).seleccionada)
    }

    @Test fun `todas correctas guarda el resultado y suma un intento`() {
        val dao = FakeEvaluacionDao()
        val vm = crear(dao)
        esperar(vm) { it is EvaluacionUiState.EnCurso }
        repeat(3) { vm.responder(1); vm.siguiente() }
        val estado = esperar(vm) { it is EvaluacionUiState.Finalizada || it is EvaluacionUiState.Error }
        assertEquals(EvaluacionUiState.Finalizada(correctas = 3, total = 3, intentosRestantes = 2), estado)
        assertEquals(1, dao.usados)
        assertEquals(100, dao.resultados.single().porcentaje)
    }
}

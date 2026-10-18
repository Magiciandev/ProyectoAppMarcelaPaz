package com.example.mpazpro.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.example.mpazpro.data.model.Cursos
import com.example.mpazpro.data.repository.AsignacionRepository
import com.example.mpazpro.viewmodel.fakes.FakeAsignacionDao
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

// Se usa runBlocking con tiempo real porque el repositorio cambia a Dispatchers.IO.
@OptIn(ExperimentalCoroutinesApi::class)
class AsignarViewModelTest {

    @Before fun antes() { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    @After fun despues() { Dispatchers.resetMain() }

    private fun crear(dao: FakeAsignacionDao) =
        AsignarViewModel(SavedStateHandle(mapOf("unidadId" to 7L)), AsignacionRepository(dao))

    // Espera a que el init cargue los intentos configurados (evita carreras con el IO).
    private fun esperarIntentos(vm: AsignarViewModel, esperados: Int) = runBlocking {
        withTimeout(5_000) { vm.formulario.first { it.intentos == esperados } }
    }

    @Test fun `parte con los intentos configurados para la unidad`() {
        val vm = crear(FakeAsignacionDao(mutableMapOf("intentos_max_7" to "5")))
        esperarIntentos(vm, 5)
    }

    @Test fun `guardar sin llenar muestra errores y no guarda`() {
        val dao = FakeAsignacionDao()
        val vm = crear(dao)
        esperarIntentos(vm, AsignacionRepository.INTENTOS_DEFECTO)
        vm.guardar()
        val e = vm.errores.value
        assertNotNull(e.curso); assertNotNull(e.desde); assertNotNull(e.hasta); assertNotNull(e.confirmado)
        assertNull(e.intentos)
        assertTrue(dao.guardadas.isEmpty())
        assertTrue(vm.guardado.value is GuardadoUiState.Idle)
    }

    @Test fun `guardar un formulario valido persiste la asignacion y los intentos`() {
        val dao = FakeAsignacionDao()
        val vm = crear(dao)
        esperarIntentos(vm, AsignacionRepository.INTENTOS_DEFECTO)
        vm.onCurso(Cursos.todos[0]); vm.onDesde(1_000L); vm.onHasta(2_000L)
        vm.onIntentos(4); vm.onConfirmado(true)
        vm.guardar()
        runBlocking { withTimeout(5_000) { vm.guardado.first { it is GuardadoUiState.Success } } }
        val guardada = dao.guardadas.single()
        assertEquals(7L, guardada.unidadId)
        assertEquals(2_000L + 86_399_999L, guardada.hasta) // «hasta» incluye todo ese día
        assertEquals("4", dao.config["intentos_max_7"])
    }
}

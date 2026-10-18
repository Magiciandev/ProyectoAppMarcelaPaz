package com.example.mpazpro.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mpazpro.data.model.InformeEstudiante
import com.example.mpazpro.data.repository.InformeRepository
import com.example.mpazpro.viewmodel.validation.ValidadorObservacion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface InformeUiState {
    object Cargando : InformeUiState
    data class Listo(val informe: InformeEstudiante) : InformeUiState
    data class Error(val mensaje: String) : InformeUiState
}

// Resultado de una acción del docente; la pantalla lo muestra y, si hay permiso, lo notifica.
sealed interface AccionUiState {
    object Idle : AccionUiState
    data class Success(val mensaje: String) : AccionUiState
    data class Error(val mensaje: String) : AccionUiState
}

enum class TipoReinicio { PROGRESO, INTENTOS }

// Reinicio esperando confirmación. asignatura = null significa "todo".
data class ReinicioPendiente(val tipo: TipoReinicio, val asignatura: String?)

class InformeViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: InformeRepository
) : ViewModel() {

    private val docenteId: Long = savedStateHandle.get<Long>("docenteId") ?: -1L
    private val estudianteId: Long = savedStateHandle.get<Long>("estudianteId") ?: -1L

    val uiState: StateFlow<InformeUiState> = repository.observarInforme(docenteId, estudianteId)
        .map<InformeEstudiante, InformeUiState> { InformeUiState.Listo(it) }
        .catch { emit(InformeUiState.Error(it.localizedMessage ?: "No se pudo cargar el informe")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), InformeUiState.Cargando)

    // null = el docente aún no editó; la pantalla muestra la observación guardada.
    private val _borrador = MutableStateFlow<String?>(null)
    val borrador: StateFlow<String?> = _borrador.asStateFlow()

    private val _errorObservacion = MutableStateFlow<String?>(null)
    val errorObservacion: StateFlow<String?> = _errorObservacion.asStateFlow()

    private val _pendiente = MutableStateFlow<ReinicioPendiente?>(null)
    val pendiente: StateFlow<ReinicioPendiente?> = _pendiente.asStateFlow()

    private val _accion = MutableStateFlow<AccionUiState>(AccionUiState.Idle)
    val accion: StateFlow<AccionUiState> = _accion.asStateFlow()

    fun onObservacion(texto: String) {
        _borrador.value = texto
        _errorObservacion.value = null
    }

    fun guardarObservacion() {
        val texto = _borrador.value.orEmpty()
        val error = ValidadorObservacion.validar(texto)
        _errorObservacion.value = error
        if (error != null) return
        viewModelScope.launch {
            repository.guardarObservacion(docenteId, estudianteId, texto)
                .onSuccess { _borrador.value = null; _accion.value = AccionUiState.Success("Observación guardada") }
                .onFailure { _accion.value = AccionUiState.Error(it.localizedMessage ?: "No se pudo guardar") }
        }
    }

    fun pedirReinicio(tipo: TipoReinicio, asignatura: String?) { _pendiente.value = ReinicioPendiente(tipo, asignatura) }
    fun cancelarReinicio() { _pendiente.value = null }

    fun confirmarReinicio() {
        val p = _pendiente.value ?: return
        _pendiente.value = null
        viewModelScope.launch {
            val resultado = when (p.tipo) {
                TipoReinicio.PROGRESO -> repository.reiniciarProgreso(docenteId, estudianteId, p.asignatura)
                TipoReinicio.INTENTOS -> repository.reiniciarIntentos(docenteId, estudianteId, p.asignatura)
            }
            val que = if (p.tipo == TipoReinicio.PROGRESO) "Progreso" else "Intentos"
            val donde = p.asignatura ?: "todas las asignaturas"
            resultado
                .onSuccess { _accion.value = AccionUiState.Success("$que reiniciado: $donde") }
                .onFailure { _accion.value = AccionUiState.Error(it.localizedMessage ?: "No se pudo reiniciar") }
        }
    }

    fun consumirAccion() { _accion.value = AccionUiState.Idle }
}

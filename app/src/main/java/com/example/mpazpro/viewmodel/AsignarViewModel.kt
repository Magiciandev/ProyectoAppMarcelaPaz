package com.example.mpazpro.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mpazpro.data.model.Asignacion
import com.example.mpazpro.data.repository.AsignacionRepository
import com.example.mpazpro.viewmodel.validation.ValidadorAsignacion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AsignarFormulario(
    val curso: String = "",
    val desde: Long? = null,
    val hasta: Long? = null,
    val intentos: Int = AsignacionRepository.INTENTOS_DEFECTO,
    val activa: Boolean = true,
    val confirmado: Boolean = false
)

data class AsignarErrores(
    val curso: String? = null,
    val desde: String? = null,
    val hasta: String? = null,
    val intentos: String? = null,
    val confirmado: String? = null
) {
    fun hayErrores() = curso != null || desde != null || hasta != null || intentos != null || confirmado != null
}

class AsignarViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: AsignacionRepository
) : ViewModel() {

    private companion object { const val FIN_DE_DIA = 86_399_999L }

    private val unidadId: Long = savedStateHandle.get<Long>("unidadId") ?: -1L

    private val _formulario = MutableStateFlow(AsignarFormulario())
    val formulario: StateFlow<AsignarFormulario> = _formulario.asStateFlow()

    private val _errores = MutableStateFlow(AsignarErrores())
    val errores: StateFlow<AsignarErrores> = _errores.asStateFlow()

    private val _guardado = MutableStateFlow<GuardadoUiState>(GuardadoUiState.Idle)
    val guardado: StateFlow<GuardadoUiState> = _guardado.asStateFlow()

    val asignaciones: StateFlow<List<Asignacion>> = repository.observarPorUnidad(unidadId)
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        // Parte con los intentos ya configurados para esta unidad.
        viewModelScope.launch {
            val actual = repository.intentosMaximos(unidadId)
            _formulario.update { it.copy(intentos = actual) }
        }
    }

    fun onCurso(v: String) { _formulario.update { it.copy(curso = v) }; _errores.update { it.copy(curso = null) } }
    fun onDesde(v: Long) { _formulario.update { it.copy(desde = v) }; _errores.update { it.copy(desde = null) } }
    fun onHasta(v: Long) { _formulario.update { it.copy(hasta = v) }; _errores.update { it.copy(hasta = null) } }
    fun onIntentos(v: Int) { _formulario.update { it.copy(intentos = v) }; _errores.update { it.copy(intentos = null) } }
    fun onActiva(v: Boolean) = _formulario.update { it.copy(activa = v) }
    fun onConfirmado(v: Boolean) { _formulario.update { it.copy(confirmado = v) }; _errores.update { it.copy(confirmado = null) } }

    fun guardar() {
        val f = _formulario.value
        val e = AsignarErrores(
            curso = ValidadorAsignacion.validarCurso(f.curso),
            desde = ValidadorAsignacion.validarDesde(f.desde),
            hasta = ValidadorAsignacion.validarHasta(f.desde, f.hasta),
            intentos = ValidadorAsignacion.validarIntentos(f.intentos),
            confirmado = ValidadorAsignacion.validarConfirmacion(f.confirmado)
        )
        _errores.value = e
        if (e.hayErrores()) return
        viewModelScope.launch {
            _guardado.value = GuardadoUiState.Loading
            val asignacion = Asignacion(
                unidadId = unidadId, curso = f.curso, desde = f.desde!!,
                hasta = f.hasta!! + FIN_DE_DIA, activa = f.activa
            )
            repository.guardar(asignacion, f.intentos)
                .onSuccess {
                    _formulario.value = AsignarFormulario(intentos = f.intentos)
                    _guardado.value = GuardadoUiState.Success(it)
                }
                .onFailure { _guardado.value = GuardadoUiState.Error(it.localizedMessage ?: "No se pudo guardar") }
        }
    }

    fun consumirGuardado() { _guardado.value = GuardadoUiState.Idle }
}

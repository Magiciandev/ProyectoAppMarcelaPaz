package com.example.mpazpro.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mpazpro.data.model.FichaEstudiante
import com.example.mpazpro.data.remote.ResultadoRemoto
import com.example.mpazpro.data.repository.InformeServidor
import com.example.mpazpro.viewmodel.validation.ValidadorObservacion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface InformeUiState {
    object Cargando : InformeUiState
    data class Listo(val ficha: FichaEstudiante) : InformeUiState
    data class Error(val mensaje: String) : InformeUiState
}

// Resultado de una acción del docente; la pantalla lo muestra y, si hay permiso, lo notifica.
sealed interface AccionUiState {
    object Idle : AccionUiState
    data class Success(val mensaje: String) : AccionUiState
    data class Error(val mensaje: String) : AccionUiState
}

// Informe de un estudiante con datos del servidor. El id del estudiante es el del servidor (viene en la ruta).
class InformeViewModel(
    savedStateHandle: SavedStateHandle,
    private val servidor: InformeServidor
) : ViewModel() {

    private val estudianteId: Long = savedStateHandle.get<Long>("estudianteId") ?: -1L

    private val _uiState = MutableStateFlow<InformeUiState>(InformeUiState.Cargando)
    val uiState: StateFlow<InformeUiState> = _uiState.asStateFlow()

    // null = el docente aún no editó; la pantalla muestra la observación del informe.
    private val _borrador = MutableStateFlow<String?>(null)
    val borrador: StateFlow<String?> = _borrador.asStateFlow()

    private val _errorObservacion = MutableStateFlow<String?>(null)
    val errorObservacion: StateFlow<String?> = _errorObservacion.asStateFlow()

    private val _reinicioPendiente = MutableStateFlow(false)
    val reinicioPendiente: StateFlow<Boolean> = _reinicioPendiente.asStateFlow()

    private val _accion = MutableStateFlow<AccionUiState>(AccionUiState.Idle)
    val accion: StateFlow<AccionUiState> = _accion.asStateFlow()

    init { cargar() }

    /** Carga inicial o reintento: si falla, la pantalla muestra el error. */
    fun cargar() {
        _uiState.value = InformeUiState.Cargando
        viewModelScope.launch {
            _uiState.value = when (val r = servidor.cargar(estudianteId)) {
                is ResultadoRemoto.Ok -> InformeUiState.Listo(r.valor)
                is ResultadoRemoto.ErrorHttp -> InformeUiState.Error(r.mensaje)
                is ResultadoRemoto.SinConexion -> InformeUiState.Error(r.mensaje)
            }
        }
    }

    // Tras una acción se vuelve a pedir la ficha sin tapar la pantalla; si esa lectura falla, queda lo que había.
    private suspend fun recargar() {
        val r = servidor.cargar(estudianteId)
        if (r is ResultadoRemoto.Ok) _uiState.value = InformeUiState.Listo(r.valor)
    }

    private fun ejecutar(exito: String, alExito: () -> Unit = {}, llamada: suspend () -> ResultadoRemoto<*>) {
        viewModelScope.launch {
            when (val r = llamada()) {
                is ResultadoRemoto.Ok -> {
                    alExito()
                    _accion.value = AccionUiState.Success(exito)
                    recargar()
                }
                is ResultadoRemoto.ErrorHttp -> _accion.value = AccionUiState.Error(r.mensaje)
                is ResultadoRemoto.SinConexion -> _accion.value = AccionUiState.Error(r.mensaje)
            }
        }
    }

    private fun informeId(): Long? = (_uiState.value as? InformeUiState.Listo)?.ficha?.informe?.id

    fun onObservacion(texto: String) {
        _borrador.value = texto
        _errorObservacion.value = null
    }

    fun generarInforme() {
        ejecutar("Informe generado") { servidor.generar(estudianteId) }
    }

    fun guardarObservacion() {
        val id = informeId()
        if (id == null) {
            _accion.value = AccionUiState.Error(SIN_INFORME)
            return
        }
        val texto = _borrador.value.orEmpty()
        val error = ValidadorObservacion.validar(texto)
        _errorObservacion.value = error
        if (error != null) return
        ejecutar("Observación guardada", alExito = { _borrador.value = null }) { servidor.guardarObservacion(id, texto) }
    }

    fun validarInforme() {
        val ficha = (_uiState.value as? InformeUiState.Listo)?.ficha
        val informe = ficha?.informe
        if (informe == null) {
            _accion.value = AccionUiState.Error(SIN_INFORME)
            return
        }
        // Si el docente escribió una observación sin guardar, se valida el informe con ese texto (si es válido).
        val borrador = _borrador.value
        val texto = if (borrador != null) {
            val error = ValidadorObservacion.validar(borrador)
            _errorObservacion.value = error
            if (error != null) return
            borrador
        } else null
        ejecutar("Informe validado", alExito = { _borrador.value = null }) { servidor.validar(informe.id, texto) }
    }

    fun pedirReinicio() { _reinicioPendiente.value = true }
    fun cancelarReinicio() { _reinicioPendiente.value = false }

    fun confirmarReinicio() {
        _reinicioPendiente.value = false
        ejecutar("Progreso e intentos reiniciados") { servidor.reiniciar(estudianteId) }
    }

    fun consumirAccion() { _accion.value = AccionUiState.Idle }

    private companion object {
        const val SIN_INFORME = "Primero genera el informe"
    }
}

package com.example.mpazpro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mpazpro.data.model.EstadoUnidad
import com.example.mpazpro.data.model.HistorialUnidad
import com.example.mpazpro.data.model.TransicionesUnidad
import com.example.mpazpro.data.model.Unidad
import com.example.mpazpro.data.repository.UnidadRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface DetalleUiState {
    object Cargando : DetalleUiState
    object NoEncontrada : DetalleUiState
    data class Error(val mensaje: String) : DetalleUiState

    data class Listo(
        val unidad: Unidad,
        val historial: List<HistorialUnidad>
    ) : DetalleUiState {
        // Qué botones se pueden usar según el estado actual (la pantalla solo los dibuja).
        val puedeEnviar: Boolean get() = TransicionesUnidad.puedeEnviarARevision(unidad.estado)
        val puedeAprobar: Boolean get() = TransicionesUnidad.puedeAprobar(unidad.estado)
        val puedeDevolver: Boolean get() = TransicionesUnidad.puedeDevolver(unidad.estado)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class UnidadDetalleViewModel(
    private val repository: UnidadRepository
) : ViewModel() {

    private val unidadId = MutableStateFlow<Long?>(null)

    // Cada vez que cambia el id, se observan de nuevo la unidad y su historial.
    val uiState: StateFlow<DetalleUiState> = unidadId
        .filterNotNull()
        .flatMapLatest { id ->
            combine<Unidad?, List<HistorialUnidad>, DetalleUiState>(
                repository.observarUnidad(id),
                repository.observarHistorial(id)
            ) { unidad, historial ->
                if (unidad == null) DetalleUiState.NoEncontrada else DetalleUiState.Listo(unidad, historial)
            }
        }
        .catch { emit(DetalleUiState.Error(it.localizedMessage ?: "No se pudo cargar la unidad")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DetalleUiState.Cargando
        )

    private val _mensajeError = MutableStateFlow<String?>(null)
    val mensajeError: StateFlow<String?> = _mensajeError.asStateFlow()

    fun cargar(id: Long) {
        unidadId.value = id
    }

    fun enviarARevision() = cambiarEstado(
        permitido = { TransicionesUnidad.puedeEnviarARevision(it) },
        nuevoEstado = EstadoUnidad.EN_REVISION,
        evento = TransicionesUnidad.EVENTO_REVISION
    )

    fun aprobar() = cambiarEstado(
        permitido = { TransicionesUnidad.puedeAprobar(it) },
        nuevoEstado = EstadoUnidad.PUBLICADO,
        evento = TransicionesUnidad.EVENTO_APROBADA
    )

    fun devolver() = cambiarEstado(
        permitido = { TransicionesUnidad.puedeDevolver(it) },
        nuevoEstado = EstadoUnidad.BORRADOR,
        evento = TransicionesUnidad.EVENTO_DEVUELTA
    )

    private fun cambiarEstado(permitido: (String) -> Boolean, nuevoEstado: String, evento: String) {
        val actual = (uiState.value as? DetalleUiState.Listo)?.unidad ?: return
        if (!permitido(actual.estado)) return
        _mensajeError.value = null
        viewModelScope.launch {
            repository.cambiarEstado(actual.id, nuevoEstado, evento)
                .onFailure { _mensajeError.value = it.localizedMessage ?: "No se pudo cambiar el estado" }
        }
    }
}

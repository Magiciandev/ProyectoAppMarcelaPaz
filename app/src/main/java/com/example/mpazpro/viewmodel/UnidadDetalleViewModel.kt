package com.example.mpazpro.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mpazpro.data.model.EstadoUnidad
import com.example.mpazpro.data.model.HistorialUnidad
import com.example.mpazpro.data.model.Unidad
import com.example.mpazpro.data.repository.UnidadRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface UnidadDetalleUiState {
    object Cargando : UnidadDetalleUiState
    object NoEncontrada : UnidadDetalleUiState
    data class Listo(val unidad: Unidad, val historial: List<HistorialUnidad>) : UnidadDetalleUiState
    data class Error(val mensaje: String) : UnidadDetalleUiState
}

class UnidadDetalleViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: UnidadRepository
) : ViewModel() {

    private val unidadId: Long = savedStateHandle.get<Long>("unidadId") ?: -1L

    val uiState: StateFlow<UnidadDetalleUiState> =
        combine(repository.observarUnidad(unidadId), repository.observarHistorial(unidadId)) { unidad, historial ->
            if (unidad == null) UnidadDetalleUiState.NoEncontrada else UnidadDetalleUiState.Listo(unidad, historial)
        }
            .catch { emit(UnidadDetalleUiState.Error(it.localizedMessage ?: "No se pudo cargar la unidad")) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UnidadDetalleUiState.Cargando)

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Flujo de estados: Borrador -> En revisión -> Publicado. UTP aprueba o rechaza.
    fun enviarARevision() = cambiar(EstadoUnidad.EN_REVISION, "Enviada a revisión")
    fun aprobar() = cambiar(EstadoUnidad.PUBLICADO, "Aprobada y publicada")
    fun rechazar() = cambiar(EstadoUnidad.BORRADOR, "Rechazada: vuelve a borrador")
    fun deshabilitar() = cambiar(EstadoUnidad.BORRADOR, "Deshabilitada: vuelve a borrador")

    private fun cambiar(estado: String, evento: String) {
        viewModelScope.launch {
            _error.value = null
            repository.cambiarEstado(unidadId, estado, evento)
                .onFailure { _error.value = it.localizedMessage ?: "No se pudo cambiar el estado" }
        }
    }
}

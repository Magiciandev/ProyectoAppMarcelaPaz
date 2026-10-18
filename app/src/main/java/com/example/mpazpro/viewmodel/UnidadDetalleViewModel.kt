package com.example.mpazpro.viewmodel

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mpazpro.data.model.EstadoUnidad
import com.example.mpazpro.data.model.HistorialUnidad
import com.example.mpazpro.data.model.TransicionesUnidad
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
    // Cada acción solo se ejecuta si TransicionesUnidad la permite desde el estado actual.
    fun enviarARevision() = cambiar(TransicionesUnidad::puedeEnviarARevision, EstadoUnidad.EN_REVISION, TransicionesUnidad.EVENTO_REVISION)
    fun aprobar() = cambiar(TransicionesUnidad::puedeAprobar, EstadoUnidad.PUBLICADO, TransicionesUnidad.EVENTO_APROBADA)
    fun rechazar() = cambiar(TransicionesUnidad::puedeRechazar, EstadoUnidad.BORRADOR, TransicionesUnidad.EVENTO_RECHAZADA)
    fun deshabilitar() = cambiar(TransicionesUnidad::puedeDeshabilitar, EstadoUnidad.BORRADOR, TransicionesUnidad.EVENTO_DESHABILITADA)

    // Recurso nativo: la imagen elegida con el selector de fotos se copia y se guarda en la unidad.
    fun elegirImagen(uri: Uri) {
        viewModelScope.launch {
            _error.value = null
            repository.guardarImagen(unidadId, uri)
                .onFailure { _error.value = it.localizedMessage ?: "No se pudo guardar la imagen" }
        }
    }

    private fun cambiar(permitido: (String) -> Boolean, estado: String, evento: String) {
        val actual = (uiState.value as? UnidadDetalleUiState.Listo)?.unidad ?: return
        if (!permitido(actual.estado)) return
        viewModelScope.launch {
            _error.value = null
            repository.cambiarEstado(unidadId, estado, evento)
                .onFailure { _error.value = it.localizedMessage ?: "No se pudo cambiar el estado" }
        }
    }
}

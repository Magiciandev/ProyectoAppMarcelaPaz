package com.example.mpazpro.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mpazpro.data.model.Unidad
import com.example.mpazpro.data.repository.AvanceServidor
import com.example.mpazpro.data.repository.UnidadRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface ContenidoUiState {
    object Cargando : ContenidoUiState
    object NoEncontrada : ContenidoUiState
    data class Listo(val unidad: Unidad) : ContenidoUiState
    data class Error(val mensaje: String) : ContenidoUiState
}

// Contenido de una unidad para el estudiante (sin historial ni datos de gestión).
class ContenidoViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: UnidadRepository,
    private val avance: AvanceServidor
) : ViewModel() {
    private val unidadId: Long = savedStateHandle.get<Long>("unidadId") ?: -1L
    private val estudianteId: Long = savedStateHandle.get<Long>("estudianteId") ?: -1L

    // Abrir el contenido cuenta como completar los contenidos del objetivo; se anota y se envía al servidor
    // (si no hay conexión queda anotado y se envía después). Las unidades que solo existen en el teléfono no envían nada.
    init {
        viewModelScope.launch {
            val objetivoId = repository.objetivoServidorDe(unidadId) ?: return@launch
            if (estudianteId > 0) avance.contenidoVisto(estudianteId, objetivoId)
        }
    }

    val uiState: StateFlow<ContenidoUiState> = repository.observarUnidad(unidadId)
        .map { if (it == null) ContenidoUiState.NoEncontrada else ContenidoUiState.Listo(it) as ContenidoUiState }
        .catch { emit(ContenidoUiState.Error(it.localizedMessage ?: "No se pudo cargar el contenido")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ContenidoUiState.Cargando)
}

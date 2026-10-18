package com.example.mpazpro.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mpazpro.data.model.Usuario
import com.example.mpazpro.data.repository.InformeRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

sealed interface InformesListaUiState {
    object Cargando : InformesListaUiState
    data class Listo(val estudiantes: List<Usuario>) : InformesListaUiState
    data class Error(val mensaje: String) : InformesListaUiState
}

class InformesListaViewModel(
    savedStateHandle: SavedStateHandle,
    repository: InformeRepository
) : ViewModel() {
    private val docenteId: Long = savedStateHandle.get<Long>("docenteId") ?: -1L

    val uiState: StateFlow<InformesListaUiState> = repository.observarEstudiantes(docenteId)
        .map<List<Usuario>, InformesListaUiState> { InformesListaUiState.Listo(it) }
        .catch { emit(InformesListaUiState.Error(it.localizedMessage ?: "No se pudo cargar")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), InformesListaUiState.Cargando)
}

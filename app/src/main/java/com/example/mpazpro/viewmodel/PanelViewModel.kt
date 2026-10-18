package com.example.mpazpro.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mpazpro.data.model.Usuario
import com.example.mpazpro.data.repository.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface PanelUiState {
    object Cargando : PanelUiState
    data class Listo(val usuario: Usuario) : PanelUiState
    data class Error(val mensaje: String) : PanelUiState
}

class PanelViewModel(
    savedStateHandle: SavedStateHandle,
    repository: UsuarioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<PanelUiState>(PanelUiState.Cargando)
    val uiState: StateFlow<PanelUiState> = _uiState.asStateFlow()

    init {
        val id = savedStateHandle.get<Long>("usuarioId") ?: -1L
        viewModelScope.launch {
            repository.obtener(id)
                .onSuccess { _uiState.value = PanelUiState.Listo(it) }
                .onFailure { _uiState.value = PanelUiState.Error(it.localizedMessage ?: "No se pudo cargar el perfil") }
        }
    }
}

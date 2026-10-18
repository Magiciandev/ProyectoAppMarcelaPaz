package com.example.mpazpro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mpazpro.data.model.Unidad
import com.example.mpazpro.data.repository.UnidadRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

sealed interface UnidadesUiState {
    object Cargando : UnidadesUiState
    data class Listo(val unidades: List<Unidad>) : UnidadesUiState
    data class Error(val mensaje: String) : UnidadesUiState
}

class UnidadesViewModel(
    repository: UnidadRepository
) : ViewModel() {

    // La lista viene de Room como Flow; stateIn la convierte en un StateFlow para la pantalla.
    val uiState: StateFlow<UnidadesUiState> = repository.observarUnidades()
        .map<List<Unidad>, UnidadesUiState> { UnidadesUiState.Listo(it) }
        .catch { emit(UnidadesUiState.Error(it.localizedMessage ?: "No se pudieron cargar las unidades")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UnidadesUiState.Cargando
        )
}

package com.example.mpazpro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mpazpro.data.model.Unidad
import com.example.mpazpro.data.remote.ResultadoRemoto
import com.example.mpazpro.data.repository.UnidadRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface UnidadesUiState {
    object Cargando : UnidadesUiState
    data class Listo(val unidades: List<Unidad>) : UnidadesUiState
    data class Error(val mensaje: String) : UnidadesUiState
}

class UnidadesViewModel(
    private val repository: UnidadRepository
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

    // Aviso breve para el usuario (la pantalla lo muestra y llama a mensajeMostrado).
    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    // Trae las unidades del servidor. Al abrir la pantalla es silencioso (avisa solo si hubo cambios);
    // cuando el usuario toca «Actualizar» (manual = true) siempre cuenta qué pasó.
    fun actualizar(manual: Boolean = false) {
        viewModelScope.launch {
            val aviso: String? = when (val resultado = repository.actualizarDesdeServidor()) {
                is ResultadoRemoto.Ok -> when {
                    resultado.valor > 0 -> "Se actualizaron ${resultado.valor} unidades desde el servidor"
                    manual -> "Las unidades ya estaban al día"
                    else -> null
                }
                is ResultadoRemoto.ErrorHttp -> if (manual) resultado.mensaje else null
                is ResultadoRemoto.SinConexion ->
                    if (manual) "${resultado.mensaje}: se muestra lo guardado en el teléfono" else null
            }
            if (aviso != null) _mensaje.value = aviso
        }
    }

    fun mensajeMostrado() {
        _mensaje.value = null
    }
}

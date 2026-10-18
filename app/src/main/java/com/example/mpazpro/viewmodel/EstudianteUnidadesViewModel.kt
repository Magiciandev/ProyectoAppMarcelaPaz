package com.example.mpazpro.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mpazpro.data.model.Unidad
import com.example.mpazpro.data.repository.EvaluacionRepository
import com.example.mpazpro.data.repository.UnidadRepository
import com.example.mpazpro.data.repository.UsuarioRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

// Una unidad rendible y el porcentaje del último intento del estudiante (null si no ha rendido).
data class UnidadEstudiante(val unidad: Unidad, val ultimoPorcentaje: Int?)

sealed interface EstudianteUnidadesUiState {
    object Cargando : EstudianteUnidadesUiState
    data class Listo(val items: List<UnidadEstudiante>) : EstudianteUnidadesUiState
    data class Error(val mensaje: String) : EstudianteUnidadesUiState
}

class EstudianteUnidadesViewModel(
    savedStateHandle: SavedStateHandle,
    unidadRepository: UnidadRepository,
    evaluacionRepository: EvaluacionRepository,
    usuarioRepository: UsuarioRepository
) : ViewModel() {

    private val estudianteId: Long = savedStateHandle.get<Long>("estudianteId") ?: -1L

    // El curso del estudiante decide qué unidades se le muestran (publicadas, asignadas y en fecha).
    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<EstudianteUnidadesUiState> = flow {
        emit(usuarioRepository.obtener(estudianteId).getOrNull()?.curso.orEmpty())
    }
        .flatMapLatest { curso ->
            combine(
                unidadRepository.observarVisibles(curso),
                evaluacionRepository.observarResultados(estudianteId)
            ) { unidades, resultados ->
                val items = unidades.map { u ->
                    UnidadEstudiante(u, resultados.lastOrNull { it.unidadId == u.id }?.porcentaje)
                }
                EstudianteUnidadesUiState.Listo(items) as EstudianteUnidadesUiState
            }
        }
        .catch { emit(EstudianteUnidadesUiState.Error(it.localizedMessage ?: "No se pudieron cargar las evaluaciones")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EstudianteUnidadesUiState.Cargando)
}

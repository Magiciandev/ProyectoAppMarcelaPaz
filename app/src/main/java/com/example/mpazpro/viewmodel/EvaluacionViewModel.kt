package com.example.mpazpro.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mpazpro.data.model.Pregunta
import com.example.mpazpro.data.repository.EvaluacionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface EvaluacionUiState {
    object Cargando : EvaluacionUiState
    data class Bloqueada(val usados: Int, val maximo: Int) : EvaluacionUiState
    object SinPreguntas : EvaluacionUiState
    data class EnCurso(
        val indice: Int,
        val total: Int,
        val pregunta: Pregunta,
        val seleccionada: Int?
    ) : EvaluacionUiState {
        val esUltima: Boolean get() = indice == total - 1
    }
    data class Finalizada(val correctas: Int, val total: Int, val intentosRestantes: Int) : EvaluacionUiState
    data class Error(val mensaje: String) : EvaluacionUiState
}

class EvaluacionViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: EvaluacionRepository
) : ViewModel() {

    private val unidadId: Long = savedStateHandle.get<Long>("unidadId") ?: -1L
    private val estudianteId: Long = savedStateHandle.get<Long>("estudianteId") ?: -1L

    private val _uiState = MutableStateFlow<EvaluacionUiState>(EvaluacionUiState.Cargando)
    val uiState: StateFlow<EvaluacionUiState> = _uiState.asStateFlow()

    private var preguntas: List<Pregunta> = emptyList()
    private val respuestas = mutableListOf<Int>()
    private var usados = 0

    init { cargar() }

    private fun cargar() {
        viewModelScope.launch {
            runCatching {
                usados = repository.intentosUsados(estudianteId, unidadId)
                preguntas = repository.obtenerPreguntas(unidadId)
            }.onFailure {
                _uiState.value = EvaluacionUiState.Error(it.localizedMessage ?: "No se pudo cargar la prueba")
                return@launch
            }
            _uiState.value = when {
                usados >= EvaluacionRepository.INTENTOS_MAXIMOS ->
                    EvaluacionUiState.Bloqueada(usados, EvaluacionRepository.INTENTOS_MAXIMOS)
                preguntas.isEmpty() -> EvaluacionUiState.SinPreguntas
                else -> EvaluacionUiState.EnCurso(0, preguntas.size, preguntas[0], null)
            }
        }
    }

    // El estudiante elige una alternativa; solo se acepta una respuesta por pregunta.
    fun responder(opcion: Int) {
        val actual = _uiState.value as? EvaluacionUiState.EnCurso ?: return
        if (actual.seleccionada != null) return
        _uiState.value = actual.copy(seleccionada = opcion)
    }

    fun siguiente() {
        val actual = _uiState.value as? EvaluacionUiState.EnCurso ?: return
        val elegida = actual.seleccionada ?: return
        respuestas.add(elegida)
        if (actual.esUltima) {
            finalizar()
        } else {
            val sig = actual.indice + 1
            _uiState.value = EvaluacionUiState.EnCurso(sig, actual.total, preguntas[sig], null)
        }
    }

    private fun finalizar() {
        val correctas = preguntas.indices.count { respuestas[it] == preguntas[it].indiceCorrecta }
        viewModelScope.launch {
            repository.registrarResultado(estudianteId, unidadId, correctas, preguntas.size, respuestas.toList())
                .onSuccess {
                    _uiState.value = EvaluacionUiState.Finalizada(
                        correctas, preguntas.size,
                        (EvaluacionRepository.INTENTOS_MAXIMOS - (usados + 1)).coerceAtLeast(0)
                    )
                }
                .onFailure { _uiState.value = EvaluacionUiState.Error(it.localizedMessage ?: "No se pudo guardar el resultado") }
        }
    }
}

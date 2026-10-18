package com.example.mpazpro.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mpazpro.data.model.Pregunta
import com.example.mpazpro.data.repository.EvaluacionRepository
import com.example.mpazpro.viewmodel.validation.ValidadorPregunta
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PreguntaFormulario(
    val texto: String = "",
    val opciones: List<String> = List(ValidadorPregunta.CANTIDAD_OPCIONES) { "" },
    val correcta: Int? = null
)

data class PreguntaErrores(
    val texto: String? = null,
    val opciones: List<String?> = List(ValidadorPregunta.CANTIDAD_OPCIONES) { null },
    val correcta: String? = null
) {
    fun hayErrores() = texto != null || opciones.any { it != null } || correcta != null
}

sealed interface PreguntasUiState {
    object Cargando : PreguntasUiState
    data class Listo(val preguntas: List<Pregunta>) : PreguntasUiState
    data class Error(val mensaje: String) : PreguntasUiState
}

class PreguntasEditorViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: EvaluacionRepository
) : ViewModel() {

    private val unidadId: Long = savedStateHandle.get<Long>("unidadId") ?: -1L

    val preguntas: StateFlow<PreguntasUiState> = repository.observarPreguntas(unidadId)
        .map<List<Pregunta>, PreguntasUiState> { PreguntasUiState.Listo(it) }
        .catch { emit(PreguntasUiState.Error(it.localizedMessage ?: "No se pudieron cargar las preguntas")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PreguntasUiState.Cargando)

    private val _formulario = MutableStateFlow(PreguntaFormulario())
    val formulario: StateFlow<PreguntaFormulario> = _formulario.asStateFlow()

    private val _errores = MutableStateFlow(PreguntaErrores())
    val errores: StateFlow<PreguntaErrores> = _errores.asStateFlow()

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    private var intentoGuardar = false

    fun actualizarTexto(valor: String) {
        _formulario.update { it.copy(texto = valor) }
        if (intentoGuardar) _errores.update { it.copy(texto = ValidadorPregunta.validarTexto(valor)) }
    }

    fun actualizarOpcion(indice: Int, valor: String) {
        _formulario.update { f -> f.copy(opciones = f.opciones.toMutableList().also { it[indice] = valor }) }
        if (intentoGuardar) {
            _errores.update { e ->
                e.copy(opciones = e.opciones.toMutableList().also { it[indice] = ValidadorPregunta.validarOpcion(valor) })
            }
        }
    }

    fun elegirCorrecta(indice: Int) {
        _formulario.update { it.copy(correcta = indice) }
        if (intentoGuardar) _errores.update { it.copy(correcta = ValidadorPregunta.validarCorrecta(indice)) }
    }

    fun agregar() {
        intentoGuardar = true
        val f = _formulario.value
        val errores = PreguntaErrores(
            texto = ValidadorPregunta.validarTexto(f.texto),
            opciones = f.opciones.map { ValidadorPregunta.validarOpcion(it) },
            correcta = ValidadorPregunta.validarCorrecta(f.correcta)
        )
        _errores.value = errores
        if (errores.hayErrores()) return

        viewModelScope.launch {
            val o = f.opciones.map { it.trim() }
            repository.agregarPregunta(
                Pregunta(
                    unidadId = unidadId, texto = f.texto.trim(),
                    opcionA = o[0], opcionB = o[1], opcionC = o[2], opcionD = o[3],
                    indiceCorrecta = f.correcta ?: 0
                )
            ).onSuccess {
                _formulario.value = PreguntaFormulario()
                _errores.value = PreguntaErrores()
                intentoGuardar = false
                _mensaje.value = "Pregunta agregada"
            }.onFailure { _mensaje.value = it.localizedMessage ?: "No se pudo guardar la pregunta" }
        }
    }

    fun eliminar(id: Long) {
        viewModelScope.launch {
            repository.eliminarPregunta(id)
                .onSuccess { _mensaje.value = "Pregunta eliminada" }
                .onFailure { _mensaje.value = it.localizedMessage ?: "No se pudo eliminar" }
        }
    }

    fun mensajeMostrado() { _mensaje.value = null }
}

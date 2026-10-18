package com.example.mpazpro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mpazpro.data.model.Asignaturas
import com.example.mpazpro.data.model.Unidad
import com.example.mpazpro.data.repository.UnidadRepository
import com.example.mpazpro.viewmodel.validation.ValidadorUnidad
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Lo que el usuario escribe en el formulario.
data class UnidadFormulario(
    val titulo: String = "",
    val objetivoAprendizaje: String = "",
    val asignatura: String = "",
    val contenido: String = ""
)

// Un mensaje de error por campo; null significa que el campo es válido.
data class UnidadErrores(
    val titulo: String? = null,
    val objetivoAprendizaje: String? = null,
    val asignatura: String? = null,
    val contenido: String? = null
) {
    fun hayErrores(): Boolean =
        titulo != null || objetivoAprendizaje != null || asignatura != null || contenido != null
}

// Estado del proceso de guardado.
sealed interface GuardadoUiState {
    object Idle : GuardadoUiState
    object Loading : GuardadoUiState
    data class Success(val id: Long) : GuardadoUiState
    data class Error(val mensaje: String) : GuardadoUiState
}

class UnidadFormViewModel(
    private val repository: UnidadRepository
) : ViewModel() {

    private val _formulario = MutableStateFlow(UnidadFormulario())
    val formulario: StateFlow<UnidadFormulario> = _formulario.asStateFlow()

    private val _errores = MutableStateFlow(UnidadErrores())
    val errores: StateFlow<UnidadErrores> = _errores.asStateFlow()

    private val _guardado = MutableStateFlow<GuardadoUiState>(GuardadoUiState.Idle)
    val guardado: StateFlow<GuardadoUiState> = _guardado.asStateFlow()

    // Los errores se muestran desde el primer intento de guardar; después se actualizan al escribir.
    private var intentoGuardar = false

    fun actualizarTitulo(valor: String) {
        _formulario.update { it.copy(titulo = valor) }
        if (intentoGuardar) _errores.update { it.copy(titulo = ValidadorUnidad.validarTitulo(valor)) }
    }

    fun actualizarObjetivo(valor: String) {
        _formulario.update { it.copy(objetivoAprendizaje = valor) }
        if (intentoGuardar) {
            _errores.update { it.copy(objetivoAprendizaje = ValidadorUnidad.validarObjetivo(valor)) }
        }
    }

    fun actualizarAsignatura(valor: String) {
        _formulario.update { it.copy(asignatura = valor) }
        if (intentoGuardar) {
            _errores.update {
                it.copy(asignatura = ValidadorUnidad.validarAsignatura(valor, Asignaturas.todas))
            }
        }
    }

    fun actualizarContenido(valor: String) {
        _formulario.update { it.copy(contenido = valor) }
        if (intentoGuardar) _errores.update { it.copy(contenido = ValidadorUnidad.validarContenido(valor)) }
    }

    fun guardar() {
        if (_guardado.value is GuardadoUiState.Loading) return
        intentoGuardar = true

        val actual = _formulario.value
        val errores = UnidadErrores(
            titulo = ValidadorUnidad.validarTitulo(actual.titulo),
            objetivoAprendizaje = ValidadorUnidad.validarObjetivo(actual.objetivoAprendizaje),
            asignatura = ValidadorUnidad.validarAsignatura(actual.asignatura, Asignaturas.todas),
            contenido = ValidadorUnidad.validarContenido(actual.contenido)
        )
        _errores.value = errores
        if (errores.hayErrores()) return

        viewModelScope.launch {
            _guardado.value = GuardadoUiState.Loading
            val unidad = Unidad(
                titulo = actual.titulo.trim(),
                objetivoAprendizaje = actual.objetivoAprendizaje.trim().uppercase(),
                asignatura = actual.asignatura,
                contenido = actual.contenido.trim()
            )
            repository.guardar(unidad)
                .onSuccess { id -> _guardado.value = GuardadoUiState.Success(id) }
                .onFailure { error ->
                    _guardado.value = GuardadoUiState.Error(
                        error.localizedMessage ?: "No se pudo guardar la unidad"
                    )
                }
        }
    }

    // Deja el formulario limpio para la próxima vez que se abra.
    fun reiniciar() {
        _formulario.value = UnidadFormulario()
        _errores.value = UnidadErrores()
        _guardado.value = GuardadoUiState.Idle
        intentoGuardar = false
    }
}

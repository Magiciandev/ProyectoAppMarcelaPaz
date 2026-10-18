package com.example.mpazpro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mpazpro.data.repository.UsuarioRepository
import com.example.mpazpro.viewmodel.validation.ValidadorLogin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginFormulario(val usuario: String = "", val pin: String = "")

data class LoginErrores(val usuario: String? = null, val pin: String? = null) {
    fun hayErrores() = usuario != null || pin != null
}

sealed interface LoginUiState {
    object Idle : LoginUiState
    object Loading : LoginUiState
    data class Success(val usuarioId: Long) : LoginUiState
    data class Error(val mensaje: String) : LoginUiState
}

class LoginViewModel(private val repository: UsuarioRepository) : ViewModel() {

    private val _formulario = MutableStateFlow(LoginFormulario())
    val formulario: StateFlow<LoginFormulario> = _formulario.asStateFlow()

    private val _errores = MutableStateFlow(LoginErrores())
    val errores: StateFlow<LoginErrores> = _errores.asStateFlow()

    private val _estado = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val estado: StateFlow<LoginUiState> = _estado.asStateFlow()

    private var intentoIngresar = false

    fun actualizarUsuario(valor: String) {
        _formulario.update { it.copy(usuario = valor) }
        if (intentoIngresar) _errores.update { it.copy(usuario = ValidadorLogin.validarUsuario(valor)) }
    }

    fun actualizarPin(valor: String) {
        _formulario.update { it.copy(pin = valor) }
        if (intentoIngresar) _errores.update { it.copy(pin = ValidadorLogin.validarPin(valor)) }
    }

    fun ingresar() {
        if (_estado.value is LoginUiState.Loading) return
        intentoIngresar = true
        val actual = _formulario.value
        val errores = LoginErrores(
            usuario = ValidadorLogin.validarUsuario(actual.usuario),
            pin = ValidadorLogin.validarPin(actual.pin)
        )
        _errores.value = errores
        if (errores.hayErrores()) return

        viewModelScope.launch {
            _estado.value = LoginUiState.Loading
            repository.autenticar(actual.usuario.trim().lowercase(), actual.pin)
                .onSuccess { _estado.value = LoginUiState.Success(it.id) }
                .onFailure { _estado.value = LoginUiState.Error(it.localizedMessage ?: "No se pudo ingresar") }
        }
    }

    // Se llama al salir de la pantalla para que el login quede limpio.
    fun reiniciar() {
        _formulario.value = LoginFormulario()
        _errores.value = LoginErrores()
        _estado.value = LoginUiState.Idle
        intentoIngresar = false
    }
}

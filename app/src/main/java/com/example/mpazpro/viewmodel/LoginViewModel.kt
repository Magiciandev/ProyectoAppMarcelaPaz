package com.example.mpazpro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mpazpro.data.model.Usuario
import com.example.mpazpro.data.repository.UsuarioRepository
import com.example.mpazpro.viewmodel.validation.ValidadorLogin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginFormulario(
    val usuario: String = "",
    val pin: String = ""
)

data class LoginErrores(
    val usuario: String? = null,
    val pin: String? = null
) {
    fun hayErrores(): Boolean = usuario != null || pin != null
}

sealed interface LoginUiState {
    object Idle : LoginUiState
    object Loading : LoginUiState
    data class Success(val usuario: Usuario) : LoginUiState
    data class Error(val mensaje: String) : LoginUiState
}

class LoginViewModel(
    private val repository: UsuarioRepository
) : ViewModel() {

    private val _formulario = MutableStateFlow(LoginFormulario())
    val formulario: StateFlow<LoginFormulario> = _formulario.asStateFlow()

    private val _errores = MutableStateFlow(LoginErrores())
    val errores: StateFlow<LoginErrores> = _errores.asStateFlow()

    private val _estado = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val estado: StateFlow<LoginUiState> = _estado.asStateFlow()

    private var intentoEnviar = false

    fun actualizarUsuario(valor: String) {
        _formulario.update { it.copy(usuario = valor) }
        if (intentoEnviar) _errores.update { it.copy(usuario = ValidadorLogin.validarUsuario(valor)) }
        limpiarErrorDeAcceso()
    }

    fun actualizarPin(valor: String) {
        _formulario.update { it.copy(pin = valor) }
        if (intentoEnviar) _errores.update { it.copy(pin = ValidadorLogin.validarPin(valor)) }
        limpiarErrorDeAcceso()
    }

    fun iniciarSesion() {
        if (_estado.value is LoginUiState.Loading) return
        intentoEnviar = true

        val actual = _formulario.value
        val errores = LoginErrores(
            usuario = ValidadorLogin.validarUsuario(actual.usuario),
            pin = ValidadorLogin.validarPin(actual.pin)
        )
        _errores.value = errores
        if (errores.hayErrores()) return

        viewModelScope.launch {
            _estado.value = LoginUiState.Loading
            repository.autenticar(actual.usuario, actual.pin)
                .onSuccess { usuario -> _estado.value = LoginUiState.Success(usuario) }
                .onFailure { error ->
                    _estado.value = LoginUiState.Error(error.localizedMessage ?: "No se pudo iniciar sesión")
                }
        }
    }

    // Se llama al terminar el ingreso: borra el PIN de la memoria y deja el formulario limpio.
    fun reiniciar() {
        _formulario.value = LoginFormulario()
        _errores.value = LoginErrores()
        _estado.value = LoginUiState.Idle
        intentoEnviar = false
    }

    private fun limpiarErrorDeAcceso() {
        if (_estado.value is LoginUiState.Error) _estado.value = LoginUiState.Idle
    }
}

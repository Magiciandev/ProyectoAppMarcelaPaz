package com.example.mpazpro.viewmodel

import androidx.lifecycle.ViewModel
import com.example.mpazpro.data.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// Quién ha iniciado sesión. Vive a nivel de toda la app y sobrevive al giro de pantalla.
class SesionViewModel : ViewModel() {

    private val _usuario = MutableStateFlow<Usuario?>(null)
    val usuario: StateFlow<Usuario?> = _usuario.asStateFlow()

    fun iniciar(usuario: Usuario) {
        _usuario.value = usuario
    }

    fun cerrar() {
        _usuario.value = null
    }
}

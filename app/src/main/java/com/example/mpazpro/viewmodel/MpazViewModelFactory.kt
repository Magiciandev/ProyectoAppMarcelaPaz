package com.example.mpazpro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mpazpro.data.repository.UnidadRepository
import com.example.mpazpro.data.repository.UsuarioRepository

// Fábrica manual: entrega a cada ViewModel sus repositorios (sin Context).
// Un ViewModel nuevo = una línea nueva en el `when`.
class MpazViewModelFactory(
    private val unidadRepository: UnidadRepository,
    private val usuarioRepository: UsuarioRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(UnidadFormViewModel::class.java) ->
            UnidadFormViewModel(unidadRepository) as T
        modelClass.isAssignableFrom(UnidadesViewModel::class.java) ->
            UnidadesViewModel(unidadRepository) as T
        modelClass.isAssignableFrom(UnidadDetalleViewModel::class.java) ->
            UnidadDetalleViewModel(unidadRepository) as T
        modelClass.isAssignableFrom(LoginViewModel::class.java) ->
            LoginViewModel(usuarioRepository) as T
        modelClass.isAssignableFrom(SesionViewModel::class.java) ->
            SesionViewModel() as T
        else -> throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    }
}

package com.example.mpazpro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mpazpro.data.repository.UnidadRepository

// Fábrica manual: entrega a cada ViewModel su repositorio (sin Context).
// Un ViewModel nuevo = una línea nueva en el `when`.
class MpazViewModelFactory(
    private val unidadRepository: UnidadRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(UnidadFormViewModel::class.java) ->
            UnidadFormViewModel(unidadRepository) as T
        modelClass.isAssignableFrom(UnidadesViewModel::class.java) ->
            UnidadesViewModel(unidadRepository) as T
        else -> throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    }
}

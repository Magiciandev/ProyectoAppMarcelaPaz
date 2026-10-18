package com.example.mpazpro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.mpazpro.data.repository.AsignacionRepository
import com.example.mpazpro.data.repository.EvaluacionRepository
import com.example.mpazpro.data.repository.UnidadRepository
import com.example.mpazpro.data.repository.UsuarioRepository

// Fábrica manual: entrega a cada ViewModel su repositorio (sin Context).
// Los argumentos de navegación (ids) llegan por SavedStateHandle.
// Un ViewModel nuevo = una línea nueva en el `when`.
class MpazViewModelFactory(
    private val unidadRepository: UnidadRepository,
    private val usuarioRepository: UsuarioRepository,
    private val evaluacionRepository: EvaluacionRepository,
    private val asignacionRepository: AsignacionRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val handle by lazy { extras.createSavedStateHandle() }
        return when {
            modelClass.isAssignableFrom(UnidadFormViewModel::class.java) ->
                UnidadFormViewModel(unidadRepository) as T
            modelClass.isAssignableFrom(UnidadesViewModel::class.java) ->
                UnidadesViewModel(unidadRepository) as T
            modelClass.isAssignableFrom(LoginViewModel::class.java) ->
                LoginViewModel(usuarioRepository) as T
            modelClass.isAssignableFrom(PanelViewModel::class.java) ->
                PanelViewModel(handle, usuarioRepository) as T
            modelClass.isAssignableFrom(UnidadDetalleViewModel::class.java) ->
                UnidadDetalleViewModel(handle, unidadRepository) as T
            modelClass.isAssignableFrom(PreguntasEditorViewModel::class.java) ->
                PreguntasEditorViewModel(handle, evaluacionRepository) as T
            modelClass.isAssignableFrom(EstudianteUnidadesViewModel::class.java) ->
                EstudianteUnidadesViewModel(handle, unidadRepository, evaluacionRepository, usuarioRepository) as T
            modelClass.isAssignableFrom(EvaluacionViewModel::class.java) ->
                EvaluacionViewModel(handle, evaluacionRepository, asignacionRepository) as T
            modelClass.isAssignableFrom(AsignarViewModel::class.java) ->
                AsignarViewModel(handle, asignacionRepository) as T
            modelClass.isAssignableFrom(ContenidoViewModel::class.java) ->
                ContenidoViewModel(handle, unidadRepository) as T
            else -> throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
        }
    }
}

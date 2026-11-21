package com.example.mpazpro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.mpazpro.data.remote.SesionStore
import com.example.mpazpro.data.repository.ActividadesServidor
import com.example.mpazpro.data.repository.AsignacionRepository
import com.example.mpazpro.data.repository.AvanceServidor
import com.example.mpazpro.data.repository.BandejaIntentos
import com.example.mpazpro.data.repository.CatalogoRepository
import com.example.mpazpro.data.repository.EvaluacionRepository
import com.example.mpazpro.data.repository.InformeRepository
import com.example.mpazpro.data.repository.InformeServidor
import com.example.mpazpro.data.repository.MonitoreoServidor
import com.example.mpazpro.data.repository.PruebaServidor
import com.example.mpazpro.data.repository.UnidadRepository
import com.example.mpazpro.data.repository.UsuarioRepository

// Fábrica manual: entrega a cada ViewModel su repositorio (sin Context).
// Los argumentos de navegación (ids) llegan por SavedStateHandle.
// Un ViewModel nuevo = una línea nueva en el `when`.
class MpazViewModelFactory(
    private val unidadRepository: UnidadRepository,
    private val usuarioRepository: UsuarioRepository,
    private val evaluacionRepository: EvaluacionRepository,
    private val asignacionRepository: AsignacionRepository,
    private val informeRepository: InformeRepository,
    private val catalogoRepository: CatalogoRepository,
    private val sesionStore: SesionStore,
    private val pruebaServidor: PruebaServidor,
    private val actividadesServidor: ActividadesServidor,
    private val monitoreoServidor: MonitoreoServidor,
    private val informeServidor: InformeServidor,
    private val avanceServidor: AvanceServidor,
    private val bandejaIntentos: BandejaIntentos
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
                EstudianteUnidadesViewModel(handle, unidadRepository, evaluacionRepository, usuarioRepository, asignacionRepository, avanceServidor) as T
            modelClass.isAssignableFrom(EvaluacionViewModel::class.java) ->
                EvaluacionViewModel(handle, evaluacionRepository, asignacionRepository) as T
            modelClass.isAssignableFrom(PruebaServidorViewModel::class.java) ->
                PruebaServidorViewModel(handle, unidadRepository, pruebaServidor, bandejaIntentos) as T
            modelClass.isAssignableFrom(ActividadesViewModel::class.java) ->
                ActividadesViewModel(handle, unidadRepository, actividadesServidor, avanceServidor) as T
            modelClass.isAssignableFrom(AsignarViewModel::class.java) ->
                AsignarViewModel(handle, asignacionRepository) as T
            modelClass.isAssignableFrom(ContenidoViewModel::class.java) ->
                ContenidoViewModel(handle, unidadRepository, avanceServidor) as T
            modelClass.isAssignableFrom(InformesListaViewModel::class.java) ->
                InformesListaViewModel(monitoreoServidor) as T
            modelClass.isAssignableFrom(InformeViewModel::class.java) ->
                InformeViewModel(handle, informeServidor) as T
            modelClass.isAssignableFrom(CatalogoViewModel::class.java) ->
                CatalogoViewModel(catalogoRepository, sesionStore) as T
            else -> throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
        }
    }
}

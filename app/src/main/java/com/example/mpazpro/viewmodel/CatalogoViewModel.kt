package com.example.mpazpro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mpazpro.data.model.CacheObjetivo
import com.example.mpazpro.data.model.CacheAsignatura
import com.example.mpazpro.data.model.CacheUnidad
import com.example.mpazpro.data.model.CatalogoCache
import com.example.mpazpro.data.remote.ResultadoRemoto
import com.example.mpazpro.data.remote.SesionStore
import com.example.mpazpro.data.repository.CatalogoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// El catálogo agrupado como lo muestra la pantalla: asignatura -> unidad -> objetivo.
data class ObjetivoCatalogo(val objetivo: CacheObjetivo, val cantidadContenidos: Int)
data class UnidadCatalogo(val unidad: CacheUnidad, val objetivos: List<ObjetivoCatalogo>)
data class AsignaturaCatalogo(val asignatura: CacheAsignatura, val unidades: List<UnidadCatalogo>)

/** Agrupa las listas planas de la caché en un árbol (función pura, fácil de probar). */
fun armarArbol(cache: CatalogoCache): List<AsignaturaCatalogo> {
    val contenidosPorObjetivo = cache.contenidos.groupingBy { it.objetivoId }.eachCount()
    val objetivosPorUnidad = cache.objetivos.groupBy { it.unidadId }
    val unidadesPorAsignatura = cache.unidades.groupBy { it.asignaturaId }
    return cache.asignaturas.map { asignatura ->
        AsignaturaCatalogo(
            asignatura = asignatura,
            unidades = unidadesPorAsignatura[asignatura.id].orEmpty().map { unidad ->
                UnidadCatalogo(
                    unidad = unidad,
                    objetivos = objetivosPorUnidad[unidad.id].orEmpty().map { objetivo ->
                        ObjetivoCatalogo(objetivo, contenidosPorObjetivo[objetivo.id] ?: 0)
                    }
                )
            }
        )
    }
}

sealed interface CatalogoUiState {
    object Cargando : CatalogoUiState

    data class Listo(
        val asignaturas: List<AsignaturaCatalogo>,
        /** True mientras se descarga del servidor (lo guardado se sigue mostrando). */
        val actualizando: Boolean,
        /** Mensaje para el usuario (sin conexión, sesión vencida...), o nulo si todo salió bien. */
        val aviso: String?,
        /** Con quién se inició sesión en el servidor, o nulo si no hay sesión. */
        val sesion: String?
    ) : CatalogoUiState
}

/** Muestra el catálogo guardado en Room y lo actualiza desde el backend. */
class CatalogoViewModel(
    private val repository: CatalogoRepository,
    private val sesion: SesionStore
) : ViewModel() {

    private val _uiState = MutableStateFlow<CatalogoUiState>(CatalogoUiState.Cargando)
    val uiState: StateFlow<CatalogoUiState> = _uiState.asStateFlow()

    private var arbol: List<AsignaturaCatalogo> = emptyList()

    init {
        viewModelScope.launch {
            // Si lo guardado lo descargó otro usuario, no se muestra: cada perfil ve lo suyo.
            val duenoDeLaCache = sesion.catalogoDe()
            val usuarioActual = sesion.actual()?.usuario
            val cacheEsDeOtro = duenoDeLaCache != null && usuarioActual != null && duenoDeLaCache != usuarioActual
            arbol = if (cacheEsDeOtro) emptyList() else armarArbol(repository.leerCache())
            if (sesion.hayToken()) {
                publicar(actualizando = true, aviso = null)
                refrescar()
            } else {
                publicar(actualizando = false, aviso = AVISO_SIN_SESION)
            }
        }
    }

    fun actualizar() {
        if (_uiState.value is CatalogoUiState.Listo && (_uiState.value as CatalogoUiState.Listo).actualizando) return
        viewModelScope.launch {
            if (!sesion.hayToken()) {
                publicar(actualizando = false, aviso = AVISO_SIN_SESION)
                return@launch
            }
            publicar(actualizando = true, aviso = null)
            refrescar()
        }
    }

    private suspend fun refrescar() {
        when (val resultado = repository.refrescar()) {
            is ResultadoRemoto.Ok -> {
                sesion.marcarCatalogo(sesion.actual()?.usuario)
                arbol = armarArbol(resultado.valor)
                publicar(actualizando = false, aviso = null)
            }
            is ResultadoRemoto.ErrorHttp -> publicar(actualizando = false, aviso = resultado.mensaje)
            is ResultadoRemoto.SinConexion -> publicar(actualizando = false, aviso = AVISO_SIN_CONEXION)
        }
    }

    private fun publicar(actualizando: Boolean, aviso: String?) {
        val datos = sesion.actual()
        _uiState.value = CatalogoUiState.Listo(
            asignaturas = arbol,
            actualizando = actualizando,
            aviso = aviso,
            sesion = datos?.let { "${it.nombre} (${it.rol})" }
        )
    }

    companion object {
        const val AVISO_SIN_SESION = "Todavía no hay sesión con el servidor. Ingresa con conexión para descargar el catálogo."
        const val AVISO_SIN_CONEXION = "Sin conexión con el servidor: se muestra lo último que se descargó."
    }
}

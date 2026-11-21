package com.example.mpazpro.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mpazpro.data.model.Unidad
import com.example.mpazpro.data.remote.dto.AsignacionEstudianteDto
import com.example.mpazpro.data.repository.AsignacionRepository
import com.example.mpazpro.data.repository.AvanceServidor
import com.example.mpazpro.data.repository.EvaluacionRepository
import com.example.mpazpro.data.repository.UnidadRepository
import com.example.mpazpro.data.repository.UsuarioRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Una unidad rendible y el porcentaje del último intento del estudiante (null si no ha rendido).
// Si viene del servidor, `asignacion` trae la ventana de fechas y los intentos usados y permitidos.
data class UnidadEstudiante(
    val unidad: Unidad,
    val ultimoPorcentaje: Int?,
    val asignacion: AsignacionEstudianteDto? = null
)

sealed interface EstudianteUnidadesUiState {
    object Cargando : EstudianteUnidadesUiState
    data class Listo(val items: List<UnidadEstudiante>) : EstudianteUnidadesUiState
    data class Error(val mensaje: String) : EstudianteUnidadesUiState
}

class EstudianteUnidadesViewModel(
    savedStateHandle: SavedStateHandle,
    private val unidadRepository: UnidadRepository,
    evaluacionRepository: EvaluacionRepository,
    usuarioRepository: UsuarioRepository,
    private val asignacionRepository: AsignacionRepository,
    private val avance: AvanceServidor
) : ViewModel() {

    private val estudianteId: Long = savedStateHandle.get<Long>("estudianteId") ?: -1L

    // Lo que el docente le asignó en el servidor (vacío mientras carga o si no hay conexión).
    private val _delServidor = MutableStateFlow<List<UnidadEstudiante>>(emptyList())

    // La pantalla la llama cada vez que se abre o se vuelve a ella (por ejemplo, después de rendir una prueba):
    // avisa la conexión, trae las unidades publicadas y pide las asignaciones vigentes con sus intentos usados.
    // Cada asignación se muestra con la copia local de su unidad (si aún no se descargó, se omite).
    fun recargar() {
        viewModelScope.launch {
            asignacionRepository.registrarConexion()
            // Envía el avance que haya quedado pendiente (por ejemplo, hecho sin conexión).
            if (estudianteId > 0) avance.sincronizar(estudianteId)
            unidadRepository.actualizarDesdeServidor()
            _delServidor.value = asignacionRepository.misAsignacionesDelServidor()
                .mapNotNull { asignacion ->
                    unidadRepository.buscarPorObjetivoServidor(asignacion.objetivoId)
                        ?.let { UnidadEstudiante(it, null, asignacion) }
                }
                .sortedBy { it.unidad.asignatura }
        }
    }

    // El curso del estudiante decide qué unidades locales se le muestran (publicadas, asignadas y en fecha).
    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<EstudianteUnidadesUiState> = flow {
        emit(usuarioRepository.obtener(estudianteId).getOrNull()?.curso.orEmpty())
    }
        .flatMapLatest { curso ->
            combine(
                unidadRepository.observarVisibles(curso),
                evaluacionRepository.observarResultados(estudianteId),
                _delServidor
            ) { unidades, resultados, delServidor ->
                val locales = unidades.map { u ->
                    UnidadEstudiante(u, resultados.lastOrNull { it.unidadId == u.id }?.porcentaje)
                }
                EstudianteUnidadesUiState.Listo(locales + delServidor) as EstudianteUnidadesUiState
            }
        }
        .catch { emit(EstudianteUnidadesUiState.Error(it.localizedMessage ?: "No se pudieron cargar las evaluaciones")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EstudianteUnidadesUiState.Cargando)
}

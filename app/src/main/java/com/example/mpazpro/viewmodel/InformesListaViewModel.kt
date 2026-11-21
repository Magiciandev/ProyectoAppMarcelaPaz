package com.example.mpazpro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mpazpro.data.model.MensajeInactividad
import com.example.mpazpro.data.remote.ResultadoRemoto
import com.example.mpazpro.data.repository.EstudianteMonitoreo
import com.example.mpazpro.data.repository.MonitoreoServidor
import java.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Un estudiante de la lista, con el mensaje que ayuda a interpretar su última conexión y sincronización. */
data class FilaMonitoreo(val estudiante: EstudianteMonitoreo, val mensaje: String)

sealed interface InformesListaUiState {
    object Cargando : InformesListaUiState
    data class Listo(val filas: List<FilaMonitoreo>) : InformesListaUiState
    data class Error(val mensaje: String) : InformesListaUiState
}

// Docente: lista de sus estudiantes con datos del servidor (avance, última conexión y última sincronización).
class InformesListaViewModel(
    private val monitoreo: MonitoreoServidor,
    private val ahora: () -> Instant = { Instant.now() }
) : ViewModel() {

    private val _uiState = MutableStateFlow<InformesListaUiState>(InformesListaUiState.Cargando)
    val uiState: StateFlow<InformesListaUiState> = _uiState.asStateFlow()

    init { cargar() }

    fun cargar() {
        _uiState.value = InformesListaUiState.Cargando
        viewModelScope.launch {
            _uiState.value = when (val r = monitoreo.estudiantes()) {
                is ResultadoRemoto.Ok -> {
                    val ahoraMismo = ahora()
                    InformesListaUiState.Listo(r.valor.map {
                        FilaMonitoreo(it, MensajeInactividad.para(it.ultimaConexion, it.ultimaSincronizacion, ahoraMismo))
                    })
                }
                is ResultadoRemoto.ErrorHttp -> InformesListaUiState.Error(r.mensaje)
                is ResultadoRemoto.SinConexion -> InformesListaUiState.Error(r.mensaje)
            }
        }
    }
}

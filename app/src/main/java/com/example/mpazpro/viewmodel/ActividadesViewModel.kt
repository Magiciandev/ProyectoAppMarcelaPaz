package com.example.mpazpro.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mpazpro.data.model.MensajesActividades
import com.example.mpazpro.data.model.MensajesPrueba
import com.example.mpazpro.data.model.SesionPrueba
import com.example.mpazpro.data.remote.ResultadoRemoto
import com.example.mpazpro.data.repository.ActividadPracticable
import com.example.mpazpro.data.repository.ActividadesServidor
import com.example.mpazpro.data.repository.UnidadRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ActividadesUiState {
    object Cargando : ActividadesUiState
    data class Mensaje(val texto: String, val esError: Boolean = false) : ActividadesUiState
    data class Lista(val actividades: List<ActividadPracticable>) : ActividadesUiState
    data class Practicando(val titulo: String, val enCurso: PruebaServidorUiState.EnCurso) : ActividadesUiState
    data class Resultado(val titulo: String, val aciertos: Int, val total: Int) : ActividadesUiState
}

// Practicar las actividades de una unidad del servidor: elegir una, resolverla pregunta a pregunta y ver cuántas acertó.
// Es práctica libre: no usa los intentos de la prueba ni se envía al servidor.
class ActividadesViewModel(
    savedStateHandle: SavedStateHandle,
    private val unidades: UnidadRepository,
    private val servidor: ActividadesServidor
) : ViewModel() {

    private val unidadId: Long = savedStateHandle.get<Long>("unidadId") ?: -1L

    private val _uiState = MutableStateFlow<ActividadesUiState>(ActividadesUiState.Cargando)
    val uiState: StateFlow<ActividadesUiState> = _uiState.asStateFlow()

    private var actividades: List<ActividadPracticable> = emptyList()
    private var actual: ActividadPracticable? = null
    private var sesion: SesionPrueba? = null

    init { cargar() }

    private fun cargar() {
        viewModelScope.launch {
            val objetivoId = unidades.objetivoServidorDe(unidadId)
            if (objetivoId == null) {
                _uiState.value = ActividadesUiState.Mensaje(MensajesPrueba.SIN_UNIDAD, esError = true)
                return@launch
            }
            _uiState.value = when (val carga = servidor.cargar(objetivoId)) {
                is ResultadoRemoto.Ok -> {
                    actividades = carga.valor
                    if (actividades.isEmpty()) ActividadesUiState.Mensaje(MensajesActividades.SIN_ACTIVIDADES)
                    else ActividadesUiState.Lista(actividades)
                }
                is ResultadoRemoto.ErrorHttp -> ActividadesUiState.Mensaje(carga.mensaje, esError = true)
                is ResultadoRemoto.SinConexion -> ActividadesUiState.Mensaje(carga.mensaje, esError = true)
            }
        }
    }

    fun abrir(actividadId: Long) {
        val elegida = actividades.firstOrNull { it.id == actividadId } ?: return
        val nueva = SesionPrueba(elegida.preguntas)
        actual = elegida
        sesion = nueva
        _uiState.value = practicando(elegida, nueva)
    }

    fun elegir(opcion: Int) = cambiar { it.elegir(opcion) }
    fun deshacer() = cambiar { it.deshacer() }
    fun comprobar() = cambiar { it.comprobar() }

    fun siguiente() {
        val a = actual ?: return
        val s = sesion ?: return
        if (_uiState.value !is ActividadesUiState.Practicando) return
        _uiState.value =
            if (s.siguiente()) ActividadesUiState.Resultado(a.titulo, s.puntajeObtenido().toInt(), s.preguntas.size)
            else practicando(a, s)
    }

    fun volverALista() {
        actual = null
        sesion = null
        _uiState.value = ActividadesUiState.Lista(actividades)
    }

    // Aplica un cambio a la sesión y muestra cómo quedó (solo mientras se está practicando).
    private fun cambiar(accion: (SesionPrueba) -> Unit) {
        val a = actual ?: return
        val s = sesion ?: return
        if (_uiState.value !is ActividadesUiState.Practicando) return
        accion(s)
        _uiState.value = practicando(a, s)
    }

    private fun practicando(a: ActividadPracticable, s: SesionPrueba) = ActividadesUiState.Practicando(
        titulo = a.titulo,
        enCurso = PruebaServidorUiState.EnCurso(
            indice = s.indice, total = s.preguntas.size, pregunta = s.actual,
            seleccion = s.seleccion, corregida = s.corregida, esUltima = s.esUltima
        )
    )
}

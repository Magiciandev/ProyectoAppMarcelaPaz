package com.example.mpazpro.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mpazpro.data.model.FormatoPuntaje
import com.example.mpazpro.data.model.MensajesPrueba
import com.example.mpazpro.data.model.PreguntaPrueba
import com.example.mpazpro.data.model.PruebaParaRendir
import com.example.mpazpro.data.model.SesionPrueba
import com.example.mpazpro.data.remote.ResultadoRemoto
import com.example.mpazpro.data.remote.dto.SyncRequestDto
import com.example.mpazpro.data.repository.CargaPrueba
import com.example.mpazpro.data.repository.ConstructorIntento
import com.example.mpazpro.data.repository.PruebaServidor
import com.example.mpazpro.data.repository.UnidadRepository
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface PruebaServidorUiState {
    object Cargando : PruebaServidorUiState
    data class Mensaje(val texto: String, val esError: Boolean = false) : PruebaServidorUiState
    data class EnCurso(
        val indice: Int,
        val total: Int,
        val pregunta: PreguntaPrueba,
        val seleccion: List<Int>,
        val corregida: Boolean,
        val esUltima: Boolean
    ) : PruebaServidorUiState
    object Enviando : PruebaServidorUiState
    data class ErrorAlEnviar(val mensaje: String) : PruebaServidorUiState
    data class Finalizada(
        val obtenido: Double,
        val maximo: Double,
        val porcentaje: Int,
        val registrado: Boolean,
        val mensaje: String?,
        val intentosRestantes: Int
    ) : PruebaServidorUiState
}

// Rinde la prueba de una unidad del servidor: carga las preguntas, lleva la sesión y, al terminar, envía el intento.
class PruebaServidorViewModel(
    savedStateHandle: SavedStateHandle,
    private val unidades: UnidadRepository,
    private val servidor: PruebaServidor
) : ViewModel() {

    private val unidadId: Long = savedStateHandle.get<Long>("unidadId") ?: -1L

    private val _uiState = MutableStateFlow<PruebaServidorUiState>(PruebaServidorUiState.Cargando)
    val uiState: StateFlow<PruebaServidorUiState> = _uiState.asStateFlow()

    private var prueba: PruebaParaRendir? = null
    private var sesion: SesionPrueba? = null

    // Cuándo empezó el estudiante. Es la fecha del intento: si empieza el último día a las 23:58 y termina a las 00:03,
    // el servidor lo acepta, porque mira el día en que se rindió y no el día en que se envió.
    private var inicio: Instant? = null

    // Se arma una sola vez al terminar. Si el envío falla, «Reintentar» manda el mismo lote (mismos uuid).
    private var solicitud: SyncRequestDto? = null

    init { cargar() }

    private fun cargar() {
        viewModelScope.launch {
            val objetivoId = unidades.objetivoServidorDe(unidadId)
            if (objetivoId == null) {
                _uiState.value = PruebaServidorUiState.Mensaje(MensajesPrueba.SIN_UNIDAD, esError = true)
                return@launch
            }
            _uiState.value = when (val carga = servidor.cargar(objetivoId)) {
                is ResultadoRemoto.Ok -> alCargar(carga.valor)
                is ResultadoRemoto.ErrorHttp -> PruebaServidorUiState.Mensaje(carga.mensaje, esError = true)
                is ResultadoRemoto.SinConexion -> PruebaServidorUiState.Mensaje(carga.mensaje, esError = true)
            }
        }
    }

    private fun alCargar(carga: CargaPrueba): PruebaServidorUiState = when (carga) {
        is CargaPrueba.SinAsignacion -> PruebaServidorUiState.Mensaje(MensajesPrueba.SIN_ASIGNACION)
        is CargaPrueba.AunNoDisponible -> PruebaServidorUiState.Mensaje(MensajesPrueba.aunNoDisponible(carga.desde))
        is CargaPrueba.YaCerrada -> PruebaServidorUiState.Mensaje(MensajesPrueba.yaCerrada(carga.hasta))
        is CargaPrueba.SinPrueba -> PruebaServidorUiState.Mensaje(MensajesPrueba.SIN_PRUEBA)
        is CargaPrueba.NoSoportada -> PruebaServidorUiState.Mensaje(MensajesPrueba.NO_SOPORTADA)
        is CargaPrueba.Lista -> {
            prueba = carga.prueba
            if (carga.prueba.intentosRestantes == 0) {
                PruebaServidorUiState.Mensaje(MensajesPrueba.bloqueada(carga.prueba.limiteIntentos))
            } else {
                val nueva = SesionPrueba(carga.prueba.preguntas)
                sesion = nueva
                inicio = Instant.now()
                enCurso(nueva)
            }
        }
    }

    private fun enCurso(s: SesionPrueba) = PruebaServidorUiState.EnCurso(
        indice = s.indice, total = s.preguntas.size, pregunta = s.actual,
        seleccion = s.seleccion, corregida = s.corregida, esUltima = s.esUltima
    )

    fun elegir(opcion: Int) = cambiar { it.elegir(opcion) }
    fun deshacer() = cambiar { it.deshacer() }
    fun comprobar() = cambiar { it.comprobar() }

    // Aplica un cambio a la sesión y muestra cómo quedó (solo mientras se está rindiendo).
    private fun cambiar(accion: (SesionPrueba) -> Unit) {
        val s = sesion ?: return
        if (_uiState.value !is PruebaServidorUiState.EnCurso) return
        accion(s)
        _uiState.value = enCurso(s)
    }

    fun siguiente() {
        val s = sesion ?: return
        if (_uiState.value !is PruebaServidorUiState.EnCurso) return
        if (s.siguiente()) enviar() else _uiState.value = enCurso(s)
    }

    fun reintentar() {
        if (_uiState.value is PruebaServidorUiState.ErrorAlEnviar) enviar()
    }

    private fun enviar() {
        val p = prueba ?: return
        val s = sesion ?: return
        val lote = solicitud ?: ConstructorIntento.armar(
            prueba = p, numeroIntento = p.intentosUsados + 1, respuestas = s.respuestasDadas(),
            fechaHora = inicio ?: Instant.now(), nuevoUuid = { UUID.randomUUID().toString() }
        ).also { solicitud = it }
        _uiState.value = PruebaServidorUiState.Enviando
        viewModelScope.launch {
            _uiState.value = when (val envio = servidor.enviar(lote)) {
                is ResultadoRemoto.Ok -> finalizada(p, s, envio.valor.intentos.firstOrNull()?.resultado, envio.valor.intentos.firstOrNull()?.motivo)
                is ResultadoRemoto.ErrorHttp -> PruebaServidorUiState.ErrorAlEnviar(envio.mensaje)
                is ResultadoRemoto.SinConexion ->
                    PruebaServidorUiState.ErrorAlEnviar("${envio.mensaje}. Tu resultado no se perdió: vuelve a intentarlo.")
            }
        }
    }

    // DUPLICADO significa que el servidor ya tenía este intento (por ejemplo, la respuesta anterior se perdió).
    private fun finalizada(p: PruebaParaRendir, s: SesionPrueba, resultado: String?, motivo: String?): PruebaServidorUiState {
        val registrado = resultado == "ACEPTADO" || resultado == "DUPLICADO"
        val obtenido = s.puntajeObtenido()
        val maximo = s.puntajeMaximo()
        return PruebaServidorUiState.Finalizada(
            obtenido = obtenido,
            maximo = maximo,
            porcentaje = FormatoPuntaje.porcentaje(obtenido, maximo),
            registrado = registrado,
            mensaje = if (registrado) null else MensajesPrueba.motivoRechazo(motivo),
            intentosRestantes = if (registrado) (p.limiteIntentos - (p.intentosUsados + 1)).coerceAtLeast(0) else p.intentosRestantes
        )
    }
}

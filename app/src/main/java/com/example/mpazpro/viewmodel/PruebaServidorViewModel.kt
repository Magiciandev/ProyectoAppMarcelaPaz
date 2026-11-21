package com.example.mpazpro.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mpazpro.data.model.FormatoPuntaje
import com.example.mpazpro.data.model.MensajesPrueba
import com.example.mpazpro.data.model.PoliticaIntentos
import com.example.mpazpro.data.model.PreguntaPrueba
import com.example.mpazpro.data.model.PruebaParaRendir
import com.example.mpazpro.data.model.SesionPrueba
import com.example.mpazpro.data.remote.ResultadoRemoto
import com.example.mpazpro.data.remote.dto.SyncRequestDto
import com.example.mpazpro.data.repository.BandejaIntentos
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
        val intentosRestantes: Int,
        // El intento quedó guardado en el teléfono porque no había conexión; se enviará después.
        val pendiente: Boolean = false
    ) : PruebaServidorUiState
}

// Rinde la prueba de una unidad del servidor: carga las preguntas, lleva la sesión y, al terminar, guarda el intento en
// el teléfono y lo envía. Sin conexión queda guardado y se envía después; los intentos guardados se descuentan de los que quedan.
class PruebaServidorViewModel(
    savedStateHandle: SavedStateHandle,
    private val unidades: UnidadRepository,
    private val servidor: PruebaServidor,
    private val bandeja: BandejaIntentos
) : ViewModel() {

    private val unidadId: Long = savedStateHandle.get<Long>("unidadId") ?: -1L
    private val estudianteId: Long = savedStateHandle.get<Long>("estudianteId") ?: -1L

    private val _uiState = MutableStateFlow<PruebaServidorUiState>(PruebaServidorUiState.Cargando)
    val uiState: StateFlow<PruebaServidorUiState> = _uiState.asStateFlow()

    private var prueba: PruebaParaRendir? = null
    private var sesion: SesionPrueba? = null

    // Cuándo empezó el estudiante. Es la fecha del intento: si empieza el último día a las 23:58 y termina a las 00:03,
    // el servidor lo acepta, porque mira el día en que se rindió y no el día en que se envió.
    private var inicio: Instant? = null

    // Se arma una sola vez al terminar. Si el envío falla, «Reintentar» manda el mismo lote (mismos uuid).
    private var solicitud: SyncRequestDto? = null
    private var guardado = false

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

    private suspend fun alCargar(carga: CargaPrueba): PruebaServidorUiState = when (carga) {
        is CargaPrueba.SinAsignacion -> PruebaServidorUiState.Mensaje(MensajesPrueba.SIN_ASIGNACION)
        is CargaPrueba.AunNoDisponible -> PruebaServidorUiState.Mensaje(MensajesPrueba.aunNoDisponible(carga.desde))
        is CargaPrueba.YaCerrada -> PruebaServidorUiState.Mensaje(MensajesPrueba.yaCerrada(carga.hasta))
        is CargaPrueba.SinPrueba -> PruebaServidorUiState.Mensaje(MensajesPrueba.SIN_PRUEBA)
        is CargaPrueba.NoSoportada -> PruebaServidorUiState.Mensaje(MensajesPrueba.NO_SOPORTADA)
        is CargaPrueba.Lista -> {
            // Los intentos guardados sin enviar cuentan como usados (regla D7: valen solo los primeros N).
            val pendientes = bandeja.pendientesDe(estudianteId, carga.prueba.objetivoId)
            val rendible = carga.prueba.copy(intentosUsados = PoliticaIntentos.usados(carga.prueba.intentosUsados, pendientes))
            prueba = rendible
            if (rendible.intentosRestantes == 0) {
                PruebaServidorUiState.Mensaje(MensajesPrueba.bloqueada(rendible.limiteIntentos))
            } else {
                val nueva = SesionPrueba(rendible.preguntas)
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
        val intento = lote.intentos.single()
        _uiState.value = PruebaServidorUiState.Enviando
        viewModelScope.launch {
            // Primero se guarda en el teléfono: si falla el envío o se cierra la app, el resultado no se pierde.
            if (!guardado) {
                bandeja.guardar(estudianteId, intento)
                guardado = true
            }
            _uiState.value = when (val envio = bandeja.enviar(estudianteId)) {
                is ResultadoRemoto.Ok -> {
                    val respuesta = envio.valor[intento.uuid]
                    if (respuesta == null) finalizada(p, s, null, null, pendiente = true)
                    else finalizada(p, s, respuesta.resultado, respuesta.motivo)
                }
                is ResultadoRemoto.ErrorHttp -> PruebaServidorUiState.ErrorAlEnviar(envio.mensaje)
                is ResultadoRemoto.SinConexion -> finalizada(p, s, null, null, pendiente = true)
            }
        }
    }

    // DUPLICADO significa que el servidor ya tenía este intento (por ejemplo, la respuesta anterior se perdió).
    private fun finalizada(
        p: PruebaParaRendir, s: SesionPrueba, resultado: String?, motivo: String?, pendiente: Boolean = false
    ): PruebaServidorUiState {
        val registrado = resultado == "ACEPTADO" || resultado == "DUPLICADO"
        val obtenido = s.puntajeObtenido()
        val maximo = s.puntajeMaximo()
        return PruebaServidorUiState.Finalizada(
            obtenido = obtenido,
            maximo = maximo,
            porcentaje = FormatoPuntaje.porcentaje(obtenido, maximo),
            registrado = registrado,
            mensaje = if (registrado) null else MensajesPrueba.motivoRechazo(motivo),
            // Un intento guardado sin enviar ya cuenta como usado; uno rechazado no.
            intentosRestantes = if (registrado || pendiente) (p.limiteIntentos - (p.intentosUsados + 1)).coerceAtLeast(0) else p.intentosRestantes,
            pendiente = pendiente
        )
    }
}

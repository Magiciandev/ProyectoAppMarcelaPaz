package com.example.mpazpro.data.repository

import com.example.mpazpro.data.model.DisponibilidadPrueba
import com.example.mpazpro.data.model.OpcionPrueba
import com.example.mpazpro.data.model.PreguntaPrueba
import com.example.mpazpro.data.model.PruebaParaRendir
import com.example.mpazpro.data.model.TipoPreguntaPrueba
import com.example.mpazpro.data.model.VentanaPrueba
import com.example.mpazpro.data.model.ZonaColegio
import com.example.mpazpro.data.remote.ResultadoRemoto
import com.example.mpazpro.data.remote.api.EvaluacionesApi
import com.example.mpazpro.data.remote.api.ProgresoApi
import com.example.mpazpro.data.remote.dto.AsignacionEstudianteDto
import com.example.mpazpro.data.remote.dto.IntentoSyncDto
import com.example.mpazpro.data.remote.dto.PaqueteEvaluacionDto
import com.example.mpazpro.data.remote.dto.PreguntaDto
import com.example.mpazpro.data.remote.dto.RespuestaSyncDto
import com.example.mpazpro.data.remote.dto.SyncRequestDto
import com.example.mpazpro.data.remote.dto.SyncResponseDto
import com.example.mpazpro.data.remote.llamarApi
import java.time.Instant
import java.time.LocalDate

// Qué se encontró al pedir la prueba de un objetivo.
sealed interface CargaPrueba {
    data class Lista(val prueba: PruebaParaRendir) : CargaPrueba
    object SinAsignacion : CargaPrueba
    // La asignación existe, pero hoy (en la zona del colegio) cae fuera de la ventana en que se puede rendir.
    data class AunNoDisponible(val desde: LocalDate) : CargaPrueba
    data class YaCerrada(val hasta: LocalDate) : CargaPrueba
    object SinPrueba : CargaPrueba
    object NoSoportada : CargaPrueba
}

// Convierte el paquete de evaluación del servidor en la prueba que se rinde. Función pura.
object MapeoPrueba {
    /**
     * Las preguntas de la prueba, en su orden y con su puntaje (1 si el servidor no lo dice).
     * Devuelve null si alguna pregunta es de un tipo que la app no resuelve, es una asociación mal armada o no viene en el paquete:
     * es mejor no dejar rendir una prueba incompleta que dar un puntaje distinto al que el docente espera.
     */
    fun aPreguntas(paquete: PaqueteEvaluacionDto): List<PreguntaPrueba>? {
        val prueba = paquete.prueba ?: return emptyList()
        val porId = paquete.preguntas.associateBy { it.id }
        val resultado = mutableListOf<PreguntaPrueba>()
        for (enPrueba in prueba.preguntas.sortedBy { it.ordenEnPrueba ?: Int.MAX_VALUE }) {
            val dto = porId[enPrueba.preguntaId] ?: return null
            resultado.add(aPregunta(dto, enPrueba.puntaje ?: 1.0) ?: return null)
        }
        return resultado
    }

    /** Una pregunta del servidor lista para resolver; null si es de un tipo que la app no resuelve o está mal armada. */
    fun aPregunta(dto: PreguntaDto, puntaje: Double): PreguntaPrueba? {
        val tipo = TipoPreguntaPrueba.desde(dto.tipo) ?: return null
        val opciones = dto.opciones
            .sortedBy { it.orden ?: Int.MAX_VALUE }
            .map { OpcionPrueba(it.texto, it.esCorrecta == true, it.posicionCorrecta, it.parClave) }
        if (tipo == TipoPreguntaPrueba.ASOCIACION && !asociacionBienArmada(opciones)) return null
        return PreguntaPrueba(dto.id, tipo, dto.enunciado, dto.explicacion, opciones, puntaje)
    }

    // Una asociación se puede resolver solo si todas sus opciones forman parejas: cada clave aparece exactamente dos veces.
    private fun asociacionBienArmada(opciones: List<OpcionPrueba>): Boolean =
        opciones.isNotEmpty() && opciones.all { it.parClave != null } &&
            opciones.groupBy { it.parClave }.values.all { it.size == 2 }
}

// Arma lo que se envía a /progreso/sync cuando el estudiante termina la prueba: un intento con todas sus respuestas.
// Cada intento y cada respuesta lleva su uuid: si el envío se repite, el servidor responde DUPLICADO y no cuenta dos veces.
object ConstructorIntento {
    fun armar(
        prueba: PruebaParaRendir,
        numeroIntento: Int,
        respuestas: List<List<Int>>,
        fechaHora: Instant,
        nuevoUuid: () -> String
    ): SyncRequestDto {
        val momento = fechaHora.toString()
        val respuestasSync = prueba.preguntas.mapIndexed { i, pregunta ->
            val correcta = pregunta.esCorrecta(respuestas[i])
            RespuestaSyncDto(
                uuid = nuevoUuid(),
                preguntaId = pregunta.id,
                respuesta = pregunta.respuestaComoTexto(respuestas[i]),
                esCorrecta = correcta,
                puntajeObtenido = if (correcta) pregunta.puntaje else 0.0,
                marcaTiempo = momento
            )
        }
        val intento = IntentoSyncDto(
            uuid = nuevoUuid(),
            pruebaId = prueba.pruebaId,
            objetivoId = prueba.objetivoId,
            numeroIntento = numeroIntento,
            puntajeObtenido = respuestasSync.sumOf { it.puntajeObtenido },
            puntajeMaximo = prueba.puntajeMaximo,
            fechaHora = momento,
            respuestas = respuestasSync
        )
        return SyncRequestDto(enviadoEn = momento, intentos = listOf(intento))
    }
}

/**
 * Rendir la prueba de un objetivo contra el servidor: pide la prueba (ms-actividades-evaluaciones) y la asignación
 * del estudiante (ms-progreso) y, al terminar, envía el intento a /progreso/sync, que lo acepta o lo rechaza con un motivo.
 */
class PruebaServidor(
    private val evaluaciones: EvaluacionesApi,
    private val progreso: ProgresoApi,
    // Lo último que se cargó con conexión: permite rendir sin red con la prueba ya guardada.
    private val cache: CacheEvaluacion,
    // Qué día es hoy en el colegio; las pruebas lo reemplazan por una fecha fija.
    private val hoy: () -> LocalDate = { ZonaColegio.hoy() }
) {

    /**
     * Con conexión baja la asignación y la prueba y las guarda en el teléfono. Sin conexión usa lo guardado la última
     * vez (decisión del proyecto: SÍ se puede rendir sin red); si nunca se cargó con red, informa que no hay conexión.
     * `estudianteId` es el id local del estudiante: la caché de asignaciones es de cada persona.
     */
    suspend fun cargar(estudianteId: Long, objetivoId: Long): ResultadoRemoto<CargaPrueba> {
        val enLinea = llamarApi { cargarEnServidor(estudianteId, objetivoId) }
        if (enLinea !is ResultadoRemoto.SinConexion) return enLinea
        return cargarDeCache(estudianteId, objetivoId) ?: enLinea
    }

    /** El mismo lote se puede reenviar sin riesgo: los uuid evitan que cuente dos veces. */
    suspend fun enviar(solicitud: SyncRequestDto): ResultadoRemoto<SyncResponseDto> =
        llamarApi { progreso.sincronizar(solicitud) }

    private suspend fun cargarEnServidor(estudianteId: Long, objetivoId: Long): CargaPrueba {
        val todas = progreso.misAsignaciones()
        // Guardar la caché nunca debe impedir rendir: si falla, se sigue con lo que bajó el servidor.
        runCatching { cache.guardarAsignaciones(estudianteId, todas) }
        val asignacion = todas.firstOrNull { it.objetivoId == objetivoId } ?: return CargaPrueba.SinAsignacion
        // Se avisa antes de bajar la prueba, no al final: el servidor rechazaría el intento con FUERA_DE_FECHA.
        fueraDeVentana(asignacion)?.let { return it }
        val paquete = evaluaciones.paquete(objetivoId)
        runCatching { cache.guardarPaquete(paquete) }
        return aCarga(asignacion, paquete)
    }

    // Sin conexión: la asignación y el paquete guardados. La ventana de fechas se valida con la fecha del teléfono;
    // el servidor tiene la última palabra cuando el intento se sincroniza. Devuelve null si no hay nada guardado.
    private suspend fun cargarDeCache(estudianteId: Long, objetivoId: Long): ResultadoRemoto<CargaPrueba>? {
        val asignacion = cache.asignacion(estudianteId, objetivoId) ?: return null
        fueraDeVentana(asignacion)?.let { return ResultadoRemoto.Ok(it) }
        val paquete = cache.paquete(objetivoId) ?: return null
        return ResultadoRemoto.Ok(aCarga(asignacion, paquete))
    }

    // Si hoy no se puede rendir, el aviso que corresponde; null si está en fecha.
    private fun fueraDeVentana(asignacion: AsignacionEstudianteDto): CargaPrueba? =
        when (val ventana = disponibilidad(asignacion, hoy())) {
            is DisponibilidadPrueba.AunNoDisponible -> CargaPrueba.AunNoDisponible(ventana.desde)
            is DisponibilidadPrueba.YaCerrada -> CargaPrueba.YaCerrada(ventana.hasta)
            DisponibilidadPrueba.EnFecha -> null
        }

    private fun aCarga(asignacion: AsignacionEstudianteDto, paquete: PaqueteEvaluacionDto): CargaPrueba {
        val objetivoId = asignacion.objetivoId
        val prueba = paquete.prueba ?: return CargaPrueba.SinPrueba
        val preguntas = MapeoPrueba.aPreguntas(paquete) ?: return CargaPrueba.NoSoportada
        if (preguntas.isEmpty()) return CargaPrueba.SinPrueba
        return CargaPrueba.Lista(
            PruebaParaRendir(
                pruebaId = prueba.id,
                objetivoId = objetivoId,
                titulo = prueba.titulo,
                preguntas = preguntas,
                limiteIntentos = asignacion.limiteIntentos,
                intentosUsados = asignacion.intentosUsados.toInt()
            )
        )
    }

    // Una fecha que no se pueda leer no bloquea: en ese caso decide el servidor, que es quien tiene la última palabra.
    private fun disponibilidad(a: AsignacionEstudianteDto, hoy: LocalDate): DisponibilidadPrueba {
        val desde = fecha(a.fechaDesde)
        val hasta = fecha(a.fechaHasta)
        if (desde == null || hasta == null) return DisponibilidadPrueba.EnFecha
        return VentanaPrueba.evaluar(desde, hasta, fecha(a.fechaHabilitacion), fecha(a.fechaRendicion), hoy)
    }

    private fun fecha(texto: String?): LocalDate? =
        texto?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
}

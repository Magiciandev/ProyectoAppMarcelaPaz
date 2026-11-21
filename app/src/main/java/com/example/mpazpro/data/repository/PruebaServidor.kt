package com.example.mpazpro.data.repository

import com.example.mpazpro.data.model.OpcionPrueba
import com.example.mpazpro.data.model.PreguntaPrueba
import com.example.mpazpro.data.model.PruebaParaRendir
import com.example.mpazpro.data.model.TipoPreguntaPrueba
import com.example.mpazpro.data.remote.ResultadoRemoto
import com.example.mpazpro.data.remote.api.EvaluacionesApi
import com.example.mpazpro.data.remote.api.ProgresoApi
import com.example.mpazpro.data.remote.dto.IntentoSyncDto
import com.example.mpazpro.data.remote.dto.PaqueteEvaluacionDto
import com.example.mpazpro.data.remote.dto.RespuestaSyncDto
import com.example.mpazpro.data.remote.dto.SyncRequestDto
import com.example.mpazpro.data.remote.dto.SyncResponseDto
import com.example.mpazpro.data.remote.llamarApi
import java.time.Instant

// Qué se encontró al pedir la prueba de un objetivo.
sealed interface CargaPrueba {
    data class Lista(val prueba: PruebaParaRendir) : CargaPrueba
    object SinAsignacion : CargaPrueba
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
            val tipo = TipoPreguntaPrueba.desde(dto.tipo) ?: return null
            val opciones = dto.opciones
                .sortedBy { it.orden ?: Int.MAX_VALUE }
                .map { OpcionPrueba(it.texto, it.esCorrecta == true, it.posicionCorrecta, it.parClave) }
            if (tipo == TipoPreguntaPrueba.ASOCIACION && !asociacionBienArmada(opciones)) return null
            resultado.add(PreguntaPrueba(dto.id, tipo, dto.enunciado, dto.explicacion, opciones, enPrueba.puntaje ?: 1.0))
        }
        return resultado
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
class PruebaServidor(private val evaluaciones: EvaluacionesApi, private val progreso: ProgresoApi) {

    suspend fun cargar(objetivoId: Long): ResultadoRemoto<CargaPrueba> =
        llamarApi { cargarEnServidor(objetivoId) }

    /** El mismo lote se puede reenviar sin riesgo: los uuid evitan que cuente dos veces. */
    suspend fun enviar(solicitud: SyncRequestDto): ResultadoRemoto<SyncResponseDto> =
        llamarApi { progreso.sincronizar(solicitud) }

    private suspend fun cargarEnServidor(objetivoId: Long): CargaPrueba {
        val asignacion = progreso.misAsignaciones().firstOrNull { it.objetivoId == objetivoId }
            ?: return CargaPrueba.SinAsignacion
        val paquete = evaluaciones.paquete(objetivoId)
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
}

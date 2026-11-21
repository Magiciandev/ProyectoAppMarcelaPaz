package com.example.mpazpro.data.repository

import com.example.mpazpro.data.model.PreguntaPrueba
import com.example.mpazpro.data.remote.ResultadoRemoto
import com.example.mpazpro.data.remote.api.EvaluacionesApi
import com.example.mpazpro.data.remote.dto.ActividadDto
import com.example.mpazpro.data.remote.dto.PaqueteEvaluacionDto
import com.example.mpazpro.data.remote.llamarApi

// Una actividad lista para practicar: sus preguntas ya traducidas, en su orden. Cada pregunta vale 1 punto.
data class ActividadPracticable(val id: Long, val titulo: String, val dificultad: Int, val preguntas: List<PreguntaPrueba>)

// Convierte las actividades del paquete de evaluación del servidor en actividades practicables. Función pura.
object MapeoActividades {
    /**
     * Las actividades en su orden. Una actividad se omite si no tiene preguntas o si alguna es de un tipo que la app
     * no resuelve (o no viene en el paquete): es mejor no mostrarla que mostrarla incompleta.
     */
    fun aActividades(paquete: PaqueteEvaluacionDto): List<ActividadPracticable> {
        val porId = paquete.preguntas.associateBy { it.id }
        return paquete.actividades.sortedWith(compareBy<ActividadDto>({ it.orden }, { it.id })).mapNotNull { actividad ->
            val preguntas = (actividad.preguntas ?: emptyList())
                .sortedBy { it.orden ?: Int.MAX_VALUE }
                .map { enActividad ->
                    val dto = porId[enActividad.preguntaId] ?: return@mapNotNull null
                    MapeoPrueba.aPregunta(dto, 1.0) ?: return@mapNotNull null
                }
            if (preguntas.isEmpty()) null
            else ActividadPracticable(actividad.id, actividad.titulo, actividad.dificultad, preguntas)
        }
    }
}

/** Pide al servidor las actividades de un objetivo (ms-actividades-evaluaciones, paquete de evaluación). */
class ActividadesServidor(private val evaluaciones: EvaluacionesApi) {
    suspend fun cargar(objetivoId: Long): ResultadoRemoto<List<ActividadPracticable>> =
        llamarApi { MapeoActividades.aActividades(evaluaciones.paquete(objetivoId)) }
}

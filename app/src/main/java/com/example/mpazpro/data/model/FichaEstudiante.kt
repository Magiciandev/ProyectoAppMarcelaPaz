package com.example.mpazpro.data.model

import com.example.mpazpro.data.remote.dto.InformeDto
import com.example.mpazpro.data.remote.dto.ResumenDto
import com.example.mpazpro.data.repository.MapeoMonitoreo

/** Lo que el servidor sabe de un objetivo para este estudiante (la prueba cuenta aparte del avance). */
data class ObjetivoFicha(
    val objetivoId: Long,
    val nombre: String,
    val avance: Int,
    val intentosAceptados: Long,
    val limiteIntentos: Int,
    /** Mejor porcentaje entre los intentos aceptados; nulo si aún no rinde la prueba. */
    val mejorPorcentaje: Int?,
    val nivel: Nivel?
)

data class RespuestasObjetivo(val nombre: String, val correctas: Int, val total: Int)

/** Un informe pedagógico ya emitido. La observación y la validación son del docente. */
data class InformeFicha(
    val id: Long,
    val emitidoEn: String,
    val puntajeGlobal: Int,
    val nivel: Nivel?,
    val validado: Boolean,
    val observacion: String,
    val areasDeRefuerzo: List<String>,
    val respuestas: List<RespuestasObjetivo>
)

/** Todo lo que el docente ve de un estudiante en su pantalla de informe. */
data class FichaEstudiante(
    val nombre: String,
    val objetivos: List<ObjetivoFicha>,
    val promedioGeneral: Int?,
    val nivelGeneral: Nivel?,
    /** El más reciente; nulo si el docente aún no genera ninguno. */
    val informe: InformeFicha?
)

object MapeoInforme {
    /** El servidor manda el nivel como DOMINA, EN_VIAS o REFUERZO; cualquier otra cosa (o nada) es «sin nivel». */
    fun nivel(texto: String?): Nivel? = Nivel.values().firstOrNull { it.name == texto }

    /** Los servicios devuelven ids; el nombre sale del catálogo guardado en el teléfono, y si falta se usa el id. */
    fun nombreObjetivo(id: Long, nombres: Map<Long, String>): String = nombres[id] ?: "Objetivo $id"

    fun ficha(resumen: ResumenDto, informe: InformeDto?, nombres: Map<Long, String>) = FichaEstudiante(
        nombre = resumen.nombre,
        objetivos = resumen.objetivos.map {
            ObjetivoFicha(
                objetivoId = it.objetivoId,
                nombre = nombreObjetivo(it.objetivoId, nombres),
                avance = it.porcentajeAvance.coerceIn(0, 100),
                intentosAceptados = it.intentosAceptados,
                limiteIntentos = it.limiteIntentos,
                mejorPorcentaje = it.mejorPorcentaje,
                nivel = nivel(it.nivel)
            )
        },
        promedioGeneral = resumen.promedioGeneral,
        nivelGeneral = nivel(resumen.nivelGeneral),
        informe = informe?.let { informeFicha(it, nombres) }
    )

    fun informeFicha(dto: InformeDto, nombres: Map<Long, String>) = InformeFicha(
        id = dto.id,
        emitidoEn = MapeoMonitoreo.instante(dto.fechaEmision)?.let { FechaHoraColegio.formatear(it) } ?: dto.fechaEmision,
        puntajeGlobal = Math.round(dto.puntajeGlobal).toInt(),
        nivel = nivel(dto.nivel),
        validado = dto.validado,
        observacion = dto.observacionDocente.orEmpty(),
        areasDeRefuerzo = dto.areasRefuerzo.orEmpty().map { nombreObjetivo(it, nombres) },
        respuestas = dto.respuestas.orEmpty().groupBy { it.objetivoId }.map { (objetivoId, lista) ->
            RespuestasObjetivo(nombreObjetivo(objetivoId, nombres), lista.count { it.esCorrecta }, lista.size)
        }
    )
}

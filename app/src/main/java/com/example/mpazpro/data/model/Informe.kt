package com.example.mpazpro.data.model

import androidx.room.Embedded

// Resultado de consultas (no son tablas).
data class PromedioAsignatura(val asignatura: String, val promedio: Double)

data class ResultadoUnidad(
    @Embedded val resultado: Resultado,
    val titulo: String,
    val asignatura: String
)

// Modelos que ve la pantalla de informe.
data class DesglosePregunta(val texto: String, val elegida: String?, val correcta: String, val acierto: Boolean)

data class DetalleUnidad(
    val titulo: String,
    val asignatura: String,
    val porcentaje: Int,
    val desglose: List<DesglosePregunta>
)

data class ResumenAsignatura(val asignatura: String, val porcentaje: Int)

data class InformeEstudiante(
    val estudiante: Usuario,
    val porAsignatura: List<ResumenAsignatura>,
    val general: Int,
    val detalle: List<DetalleUnidad>,
    val observacion: String
)

// Umbrales provisorios del equipo (por confirmar con el cliente). Es una regla fija, no IA:
// la decisión pedagógica es siempre del docente.
enum class Nivel(val etiqueta: String) {
    DOMINA("Domina el objetivo"),
    EN_VIAS("En vías de logro"),
    REFUERZO("Requiere refuerzo")
}

object CalculoProgreso {
    const val UMBRAL_DOMINA = 80
    const val UMBRAL_EN_VIAS = 60

    fun nivel(porcentaje: Int): Nivel = when {
        porcentaje >= UMBRAL_DOMINA -> Nivel.DOMINA
        porcentaje >= UMBRAL_EN_VIAS -> Nivel.EN_VIAS
        else -> Nivel.REFUERZO
    }

    // Promedio por asignatura; las 4 asignaturas siempre existen y sin evaluaciones valen 0.
    fun porAsignatura(promedios: List<PromedioAsignatura>): List<ResumenAsignatura> =
        Asignaturas.todas.map { nombre ->
            val p = promedios.firstOrNull { it.asignatura == nombre }?.promedio ?: 0.0
            ResumenAsignatura(nombre, Math.round(p).toInt())
        }

    // General = promedio de las 4 asignaturas.
    fun general(porAsignatura: List<ResumenAsignatura>): Int =
        if (porAsignatura.isEmpty()) 0 else Math.round(porAsignatura.map { it.porcentaje }.average()).toInt()
}

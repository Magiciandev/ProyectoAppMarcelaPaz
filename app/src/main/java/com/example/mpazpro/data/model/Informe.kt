package com.example.mpazpro.data.model

// Resultado de consulta (no es tabla).
data class PromedioAsignatura(val asignatura: String, val promedio: Double)

// Modelos del resumen por asignatura.
data class ResumenAsignatura(val asignatura: String, val porcentaje: Int)

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

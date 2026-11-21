package com.example.mpazpro.data.model

import com.example.mpazpro.data.remote.dto.ProgresoSyncDto
import java.time.Instant

/**
 * Decisión del equipo: un desafío es una actividad de dificultad 3 (el servidor no distingue actividad de
 * desafío; solo guarda `dificultad` de 1 a 3). Si el criterio cambia, se cambia solo aquí.
 */
object CriterioDesafio {
    const val DIFICULTAD_MINIMA = 3

    fun esDesafio(dificultad: Int): Boolean = dificultad >= DIFICULTAD_MINIMA

    fun tipoDe(dificultad: Int): String = if (esDesafio(dificultad)) TipoItem.DESAFIO else TipoItem.ACTIVIDAD

    /** Cuenta actividades y desafíos de una lista de dificultades, para guardar los totales del objetivo. */
    fun totalesDe(objetivoId: Long, contenidos: Int, dificultades: List<Int>): TotalesObjetivo {
        val desafios = dificultades.count { esDesafio(it) }
        return TotalesObjetivo(objetivoId, contenidos, dificultades.size - desafios, desafios)
    }
}

/** El avance de un objetivo listo para enviar: cuántos ítems de cada tipo, el porcentaje y la marca de tiempo. */
data class AvanceCalculado(
    val objetivoId: Long,
    val contenidos: Int,
    val actividades: Int,
    val desafios: Int,
    val porcentaje: Int,
    /** Segundos desde 1970 (UTC) del último ítem completado que cuenta. */
    val marcaTiempo: Long
) {
    fun aDto() = ProgresoSyncDto(
        objetivoId = objetivoId,
        contenidosCompletados = contenidos,
        actividadesCompletadas = actividades,
        desafiosCompletados = desafios,
        porcentajeAvance = porcentaje,
        marcaTiempo = Instant.ofEpochSecond(marcaTiempo).toString()
    )
}

// Cálculo del avance del estudiante. Funciones puras, sin Android.
// Porcentaje = (contenidos + actividades + desafíos completados) / total de esos tres, sin contar la prueba.
object CalculoAvance {

    /**
     * Los ítems que siguen valiendo después del último reinicio del docente: solo los hechos estrictamente
     * después. Así un reinicio no se deshace al reenviar lo anterior. Sin reinicio, valen todos.
     */
    fun vigentes(items: List<ItemCompletado>, ultimoReinicio: Instant?): List<ItemCompletado> {
        if (ultimoReinicio == null) return items
        val limite = ultimoReinicio.epochSecond
        return items.filter { it.completadoEn > limite }
    }

    /** Porcentaje entero de 0 a 100 (redondeo hacia arriba desde la mitad). Sin ítems en el objetivo, 0. */
    fun porcentaje(completados: Int, total: Int): Int {
        if (total <= 0 || completados <= 0) return 0
        val c = completados.coerceAtMost(total)
        return ((c * 100 + total / 2) / total).coerceIn(0, 100)
    }

    /**
     * Avance de un objetivo, o null si no hay nada vigente que enviar (el servidor ya tiene 0 tras un reinicio,
     * y enviar un avance vacío no aporta). Los conteos no superan el total del objetivo.
     */
    fun calcular(
        totales: TotalesObjetivo,
        items: List<ItemCompletado>,
        ultimoReinicio: Instant?
    ): AvanceCalculado? {
        val validos = vigentes(items.filter { it.objetivoId == totales.objetivoId }, ultimoReinicio)
        if (validos.isEmpty()) return null
        val contenidos = validos.count { it.tipo == TipoItem.CONTENIDO }.coerceAtMost(totales.contenidos)
        val actividades = validos.count { it.tipo == TipoItem.ACTIVIDAD }.coerceAtMost(totales.actividades)
        val desafios = validos.count { it.tipo == TipoItem.DESAFIO }.coerceAtMost(totales.desafios)
        return AvanceCalculado(
            objetivoId = totales.objetivoId,
            contenidos = contenidos,
            actividades = actividades,
            desafios = desafios,
            porcentaje = porcentaje(contenidos + actividades + desafios, totales.total),
            marcaTiempo = validos.maxOf { it.completadoEn }
        )
    }
}

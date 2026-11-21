package com.example.mpazpro.data.repository

import com.example.mpazpro.data.local.AvanceLocalDao
import com.example.mpazpro.data.model.CalculoAvance
import com.example.mpazpro.data.model.CriterioDesafio
import com.example.mpazpro.data.model.ItemCompletado
import com.example.mpazpro.data.model.TipoItem
import com.example.mpazpro.data.model.TotalesObjetivo
import com.example.mpazpro.data.remote.ResultadoRemoto
import com.example.mpazpro.data.remote.api.ProgresoApi
import com.example.mpazpro.data.remote.dto.SyncRequestDto
import com.example.mpazpro.data.remote.llamarApi
import java.time.Instant

/** Cuántos avances aceptó, ya tenía o rechazó el servidor en un envío. */
data class ResumenEnvioAvance(val aceptados: Int = 0, val duplicados: Int = 0, val rechazados: Int = 0)

/**
 * El avance del estudiante: lo que completa se anota en Room (ítem y fecha) y se envía a `POST /progreso/sync`.
 * El servidor no calcula el avance, solo guarda lo que manda la app (gana el más reciente por marca de tiempo).
 *
 * Antes de cada envío pregunta el último reinicio del docente (`miAvance()`) y descarta lo hecho hasta esa fecha,
 * para que un reinicio no se deshaga al reenviar lo viejo. Si no hay conexión, lo anotado queda en Room y se
 * envía en el siguiente intento (al abrir las unidades o al completar otro ítem).
 *
 * @param idsContenidos ids de los contenidos del objetivo, del catálogo guardado en el teléfono.
 * @param dificultadesActividades dificultad de cada actividad del objetivo (la lista que ve el estudiante).
 * Los dos se reciben como función para poder probar sin Room ni red.
 */
class AvanceServidor(
    private val api: ProgresoApi,
    private val dao: AvanceLocalDao,
    private val idsContenidos: suspend (Long) -> List<Long>,
    private val dificultadesActividades: suspend (Long) -> ResultadoRemoto<List<Int>>,
    private val ahora: () -> Instant = { Instant.now() },
    // Se llama cuando un envío falla y el avance sigue pendiente (para pedir un reintento con conexión).
    private val alQuedarPendiente: () -> Unit = {}
) {

    /**
     * El estudiante abrió el contenido de la unidad: cuentan como completados todos los contenidos del objetivo
     * (ve un solo texto con todos). Guarda y envía.
     */
    suspend fun contenidoVisto(usuarioId: Long, objetivoId: Long): ResultadoRemoto<ResumenEnvioAvance> {
        val momento = ahora().epochSecond
        idsContenidos(objetivoId).forEach {
            dao.marcar(ItemCompletado(usuarioId, objetivoId, TipoItem.CONTENIDO, it, momento))
        }
        asegurarTotales(objetivoId)
        return sincronizar(usuarioId)
    }

    /**
     * El estudiante terminó una actividad (o desafío, según su dificultad). `dificultades` son las de todas las
     * actividades del objetivo: de ahí salen los totales contra los que se calcula el porcentaje.
     */
    suspend fun actividadCompletada(
        usuarioId: Long,
        objetivoId: Long,
        actividadId: Long,
        dificultad: Int,
        dificultades: List<Int>
    ): ResultadoRemoto<ResumenEnvioAvance> {
        guardarTotales(objetivoId, dificultades)
        dao.marcar(ItemCompletado(usuarioId, objetivoId, CriterioDesafio.tipoDe(dificultad), actividadId, ahora().epochSecond))
        return sincronizar(usuarioId)
    }

    /** Envía lo anotado que siga valiendo. Sin nada que enviar responde Ok con ceros, sin llamar a /sync. */
    suspend fun sincronizar(usuarioId: Long): ResultadoRemoto<ResumenEnvioAvance> {
        val resultado = llamarApi { enviar(usuarioId) }
        if (resultado !is ResultadoRemoto.Ok) alQuedarPendiente()
        return resultado
    }

    private suspend fun enviar(usuarioId: Long): ResumenEnvioAvance {
        val reinicio = instanteDe(api.miAvance().ultimoReinicio)
        descartarHastaElReinicio(usuarioId, reinicio)

        val items = dao.itemsDelUsuario(usuarioId)
        val avances = items.map { it.objetivoId }.distinct().mapNotNull { objetivoId ->
            val totales = dao.totalesDe(objetivoId) ?: asegurarTotales(objetivoId)
            totales?.let { CalculoAvance.calcular(it, items, reinicio) }
        }
        if (avances.isEmpty()) return ResumenEnvioAvance()

        val respuesta = api.sincronizar(SyncRequestDto(enviadoEn = ahora().toString(), progreso = avances.map { it.aDto() }))
        descartarHastaElReinicio(usuarioId, instanteDe(respuesta.ultimoReinicio))
        return ResumenEnvioAvance(
            aceptados = respuesta.progreso.count { it.resultado == ACEPTADO },
            duplicados = respuesta.progreso.count { it.resultado == DUPLICADO },
            rechazados = respuesta.progreso.count { it.resultado == RECHAZADO }
        )
    }

    private suspend fun descartarHastaElReinicio(usuarioId: Long, reinicio: Instant?) {
        if (reinicio != null) dao.borrarHasta(usuarioId, reinicio.epochSecond)
    }

    // Los totales guardados; si faltan, se piden con las actividades del objetivo (null si no hay conexión).
    private suspend fun asegurarTotales(objetivoId: Long): TotalesObjetivo? {
        dao.totalesDe(objetivoId)?.let { return it }
        return when (val actividades = dificultadesActividades(objetivoId)) {
            is ResultadoRemoto.Ok -> guardarTotales(objetivoId, actividades.valor)
            else -> null
        }
    }

    private suspend fun guardarTotales(objetivoId: Long, dificultades: List<Int>): TotalesObjetivo {
        val totales = CriterioDesafio.totalesDe(objetivoId, idsContenidos(objetivoId).size, dificultades)
        dao.guardarTotales(totales)
        return totales
    }

    private fun instanteDe(texto: String?): Instant? = texto?.let { runCatching { Instant.parse(it) }.getOrNull() }

    private companion object {
        const val ACEPTADO = "ACEPTADO"
        const val DUPLICADO = "DUPLICADO"
        const val RECHAZADO = "RECHAZADO"
    }
}

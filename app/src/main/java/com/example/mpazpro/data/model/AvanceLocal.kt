package com.example.mpazpro.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Tipos de ítem que cuentan para el avance del estudiante en un objetivo (la prueba final no cuenta). */
object TipoItem {
    const val CONTENIDO = "CONTENIDO"
    const val ACTIVIDAD = "ACTIVIDAD"
    const val DESAFIO = "DESAFIO"
}

/**
 * Un ítem que el estudiante completó en este dispositivo. Un ítem repetido reemplaza al anterior, así queda
 * la fecha de la última vez (si el docente reinicia y el estudiante lo vuelve a hacer, vuelve a contar).
 * `usuarioId` es el id local de Room, no el del servidor. `completadoEn` va en segundos desde 1970 (UTC),
 * porque el servidor compara la marca de tiempo del avance en segundos.
 */
@Entity(tableName = "item_completado", primaryKeys = ["usuarioId", "objetivoId", "tipo", "itemId"])
data class ItemCompletado(
    val usuarioId: Long,
    val objetivoId: Long,
    val tipo: String,
    val itemId: Long,
    val completadoEn: Long
)

/**
 * Cuántos ítems tiene un objetivo (el total contra el que se calcula el porcentaje). Se guarda al conocer las
 * actividades del servidor, para poder calcular el avance también sin conexión.
 */
@Entity(tableName = "totales_objetivo")
data class TotalesObjetivo(
    @PrimaryKey val objetivoId: Long,
    val contenidos: Int,
    val actividades: Int,
    val desafios: Int
) {
    val total: Int get() = contenidos + actividades + desafios
}

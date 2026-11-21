package com.example.mpazpro.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// Caché para usar la app sin conexión (Room v9). Se llena cada vez que el estudiante carga algo con red.

/** Una asignación del estudiante tal como la entregó el servidor. El id es el LOCAL del usuario (Room), no el del servidor. */
@Entity(tableName = "cache_asignacion_estudiante", primaryKeys = ["usuarioId", "objetivoId"])
data class CacheAsignacionEstudiante(
    val usuarioId: Long,
    val objetivoId: Long,
    val asignacionId: Long,
    val fechaDesde: String,
    val fechaHasta: String,
    val limiteIntentos: Int,
    val fechaHabilitacion: String?,
    val fechaRendicion: String?,
    val intentosUsados: Long
)

/** El paquete de evaluación de un objetivo (actividades, preguntas y prueba), guardado como texto JSON. */
@Entity(tableName = "cache_paquete")
data class CachePaquete(
    @PrimaryKey val objetivoId: Long,
    val json: String
)

package com.example.mpazpro.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// Una fila por cada acción sobre una unidad (trazabilidad pedida por UTP).
@Entity(tableName = "historial_unidad")
data class HistorialUnidad(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val unidadId: Long,
    val fecha: Long,
    val evento: String
)

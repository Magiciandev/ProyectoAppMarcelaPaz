package com.example.mpazpro.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// Cada cambio importante de una unidad deja una entrada (trazabilidad que pide el caso).
@Entity(
    tableName = "historial_unidad",
    foreignKeys = [
        ForeignKey(
            entity = Unidad::class,
            parentColumns = ["id"],
            childColumns = ["unidadId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["unidadId"])]
)
data class HistorialUnidad(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val unidadId: Long,
    val fecha: String,
    val evento: String
)

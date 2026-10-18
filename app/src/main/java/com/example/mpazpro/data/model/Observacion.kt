package com.example.mpazpro.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// Una observación docente por estudiante; guardar de nuevo reemplaza la anterior.
@Entity(tableName = "observaciones")
data class Observacion(
    @PrimaryKey val estudianteId: Long,
    val texto: String,
    val fecha: Long
)

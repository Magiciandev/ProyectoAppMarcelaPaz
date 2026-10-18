package com.example.mpazpro.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// Cada pasada completa de una prueba es un resultado. respuestas = índices elegidos, "1,0,2".
@Entity(tableName = "resultados")
data class Resultado(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val estudianteId: Long,
    val unidadId: Long,
    val correctas: Int,
    val total: Int,
    val respuestas: String,
    val fecha: Long
) {
    val porcentaje: Int get() = if (total == 0) 0 else Math.round(correctas * 100f / total)
}

// Intentos usados, separados de los resultados para poder reiniciarlos sin borrar el historial.
@Entity(tableName = "intentos", primaryKeys = ["estudianteId", "unidadId"])
data class Intento(
    val estudianteId: Long,
    val unidadId: Long,
    val usados: Int
)

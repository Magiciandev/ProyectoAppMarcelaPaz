package com.example.mpazpro.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// Entidad de Room: cada fila de la tabla "unidades" es una unidad de estudio.
@Entity(tableName = "unidades")
data class Unidad(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val titulo: String,
    val objetivoAprendizaje: String,
    val asignatura: String,
    val estado: String = EstadoUnidad.BORRADOR,
    val contenido: String,
    val imagenRuta: String? = null
)

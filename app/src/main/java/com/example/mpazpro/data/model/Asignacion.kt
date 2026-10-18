package com.example.mpazpro.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// Una unidad asignada a un curso. desde/hasta en milisegundos (hasta = fin de ese día).
@Entity(tableName = "asignaciones", indices = [Index(value = ["unidadId", "curso"], unique = true)])
data class Asignacion(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val unidadId: Long,
    val curso: String,
    val desde: Long,
    val hasta: Long,
    val activa: Boolean = true
)

// Pares clave/valor. Hoy guarda "intentos_max_<unidadId>" (1 a 5).
@Entity(tableName = "configuracion")
data class Configuracion(
    @PrimaryKey val clave: String,
    val valor: String
)

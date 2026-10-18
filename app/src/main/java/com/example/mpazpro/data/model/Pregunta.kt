package com.example.mpazpro.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "preguntas")
data class Pregunta(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val unidadId: Long,
    val texto: String,
    val opcionA: String,
    val opcionB: String,
    val opcionC: String,
    val opcionD: String,
    val indiceCorrecta: Int
) {
    fun opciones(): List<String> = listOf(opcionA, opcionB, opcionC, opcionD)
}

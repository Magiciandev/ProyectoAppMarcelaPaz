package com.example.mpazpro.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// Usuario ficticio de la app. El PIN se guarda en texto plano solo porque todos los datos son
// de prueba (el caso prohíbe credenciales reales); en un sistema real se guardaría un hash.
@Entity(tableName = "usuarios", indices = [Index(value = ["usuario"], unique = true)])
data class Usuario(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val usuario: String,
    val pin: String,
    val nombre: String,
    val rol: String,
    val curso: String? = null
)

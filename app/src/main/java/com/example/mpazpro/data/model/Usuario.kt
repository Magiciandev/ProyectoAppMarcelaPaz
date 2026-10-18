package com.example.mpazpro.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// Usuario ficticio. El PIN nunca se guarda en claro: pinHash = "sal:hash" (PBKDF2).
@Entity(tableName = "usuarios", indices = [Index(value = ["usuario"], unique = true)])
data class Usuario(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val usuario: String,
    val pinHash: String,
    val rol: String,
    val curso: String? = null
)

package com.example.mpazpro.viewmodel.validation

// Validación de la observación docente (texto libre, breve).
object ValidadorObservacion {
    const val MAXIMO = 300

    fun validar(texto: String): String? = when {
        texto.isBlank() -> "Escribe la observación"
        texto.trim().length < 5 -> "Escribe al menos 5 caracteres"
        texto.trim().length > MAXIMO -> "Máximo $MAXIMO caracteres"
        else -> null
    }
}

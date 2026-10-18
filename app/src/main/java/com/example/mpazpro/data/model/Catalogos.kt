package com.example.mpazpro.data.model

// Valores fijos del dominio. Se usan como texto para no necesitar conversores en Room.
object Asignaturas {
    const val LENGUAJE = "Lenguaje"
    const val MATEMATICA = "Matemática"
    const val CIENCIAS = "Ciencias Naturales"
    const val HISTORIA = "Historia"

    val todas = listOf(LENGUAJE, MATEMATICA, CIENCIAS, HISTORIA)
}

object EstadoUnidad {
    const val BORRADOR = "Borrador"
    const val EN_REVISION = "En revisión"
    const val PUBLICADO = "Publicado"
}

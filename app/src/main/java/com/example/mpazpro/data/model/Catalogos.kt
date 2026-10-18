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

object Roles {
    const val UTP = "UTP"
    const val DOCENTE = "Docente"
    const val ESTUDIANTE = "Estudiante"
}

object Cursos {
    const val A = "2° A"
    const val B = "2° B"

    val todos = listOf(A, B)
}

package com.example.mpazpro.data.model

// Valores fijos del dominio. Se usan como texto para no necesitar conversores en Room.
object Asignaturas {
    const val LENGUAJE = "Lenguaje"
    const val MATEMATICA = "Matemática"
    const val CIENCIAS = "Ciencias Naturales"
    const val HISTORIA = "Historia"
    const val INGLES  = "Ingles"

    val todas = listOf(LENGUAJE, MATEMATICA, CIENCIAS, HISTORIA, INGLES)
}

object EstadoUnidad {
    const val BORRADOR = "Borrador"
    const val EN_REVISION = "En revisión"
    const val PUBLICADO = "Publicado"
}

object Roles {
    const val UTP = "Jefatura UTP"
    const val DOCENTE = "Docente"
    const val ESTUDIANTE = "Estudiante"
}

object Cursos {
    val todos = listOf("2° A", "2° B")
}

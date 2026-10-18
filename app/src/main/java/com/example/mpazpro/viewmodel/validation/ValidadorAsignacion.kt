package com.example.mpazpro.viewmodel.validation

import com.example.mpazpro.data.model.Cursos

// Validaciones del formulario de asignación (docente): curso, fechas, intentos y confirmación.
object ValidadorAsignacion {
    const val INTENTOS_MIN = 1
    const val INTENTOS_MAX = 5

    fun validarCurso(curso: String): String? = when {
        curso.isBlank() -> "Elige un curso"
        curso !in Cursos.todos -> "Curso no válido"
        else -> null
    }

    fun validarDesde(desde: Long?): String? =
        if (desde == null) "Elige la fecha de inicio" else null

    fun validarHasta(desde: Long?, hasta: Long?): String? = when {
        hasta == null -> "Elige la fecha de término"
        desde != null && hasta < desde -> "El término no puede ser anterior al inicio"
        else -> null
    }

    fun validarIntentos(intentos: Int?): String? =
        if (intentos == null || intentos !in INTENTOS_MIN..INTENTOS_MAX)
            "Los intentos deben ser de $INTENTOS_MIN a $INTENTOS_MAX" else null

    fun validarConfirmacion(confirmado: Boolean): String? =
        if (!confirmado) "Confirma que revisaste la unidad" else null
}

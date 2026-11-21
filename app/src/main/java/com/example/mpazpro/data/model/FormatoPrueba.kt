package com.example.mpazpro.data.model

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

// Formatos y mensajes de la prueba del servidor, como funciones puras para poder probarlos.
object FormatoPuntaje {
    /** 3.0 se muestra «3» y 2.5 se muestra «2,5». */
    fun texto(valor: Double): String =
        if (valor % 1.0 == 0.0) valor.toLong().toString() else String.format(Locale("es", "CL"), "%.1f", valor)

    fun porcentaje(obtenido: Double, maximo: Double): Int =
        if (maximo <= 0.0) 0 else Math.round(obtenido * 100.0 / maximo).toInt()
}

object MensajesPrueba {
    const val SIN_ASIGNACION = "Esta unidad no está asignada a ti."
    const val SIN_PRUEBA = "Esta unidad todavía no tiene prueba."
    const val NO_SOPORTADA = "Esta prueba tiene preguntas que todavía no se pueden resolver en la app."
    const val SIN_UNIDAD = "No se encontró la unidad en el servidor."

    private val FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    fun aunNoDisponible(desde: LocalDate) = "La prueba se abre el ${desde.format(FORMATO_FECHA)}. Vuelve ese día."

    fun yaCerrada(hasta: LocalDate) = "El plazo para rendir esta prueba terminó el ${hasta.format(FORMATO_FECHA)}. Habla con tu profesora o profesor."

    fun bloqueada(limite: Int) = "Ya usaste tus $limite intentos. Habla con tu profesora o profesor."

    /** Por qué el servidor no registró el intento (los motivos de rechazo de /progreso/sync). */
    fun motivoRechazo(motivo: String?): String = when (motivo) {
        "LIMITE_INTENTOS" -> "Ya usaste todos tus intentos."
        "FUERA_DE_FECHA" -> "La prueba no estaba disponible en la fecha en que la rendiste."
        "SIN_ASIGNACION" -> "Esta unidad ya no está asignada a tu curso."
        "ANTERIOR_AL_REINICIO" -> "Tu docente reinició tu avance en esta unidad."
        else -> "El servidor no pudo registrar el intento."
    }
}

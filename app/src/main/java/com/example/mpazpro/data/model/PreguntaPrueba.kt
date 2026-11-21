package com.example.mpazpro.data.model

import com.google.gson.Gson

// Los tipos de pregunta que el estudiante puede resolver en la app.
enum class TipoPreguntaPrueba {
    SELECCION, COMPLETACION, SECUENCIA, ASOCIACION;

    companion object {
        /** null si el servidor manda un tipo que la app todavía no sabe resolver. */
        fun desde(texto: String): TipoPreguntaPrueba? = entries.firstOrNull { it.name == texto }
    }
}

/**
 * posicionCorrecta solo se usa en SECUENCIA (1, 2, 3...). esCorrecta, en SELECCION y COMPLETACION.
 * parClave, en ASOCIACION: dos opciones con la misma clave son pareja.
 */
data class OpcionPrueba(
    val texto: String,
    val esCorrecta: Boolean,
    val posicionCorrecta: Int?,
    val parClave: String? = null
)

// Una pregunta de la prueba final, ya con el puntaje que vale. Las respuestas del estudiante son índices de `opciones`:
// una sola en SELECCION y COMPLETACION; todas, en el orden elegido, en SECUENCIA; y en ASOCIACION todas también,
// donde cada dos índices seguidos son un par unido por el estudiante (0 con 1, 2 con 3...).
data class PreguntaPrueba(
    val id: Long,
    val tipo: TipoPreguntaPrueba,
    val enunciado: String,
    val explicacion: String?,
    val opciones: List<OpcionPrueba>,
    val puntaje: Double
) {
    fun esCorrecta(seleccion: List<Int>): Boolean = when (tipo) {
        TipoPreguntaPrueba.SELECCION, TipoPreguntaPrueba.COMPLETACION ->
            seleccion.size == 1 && opciones.getOrNull(seleccion[0])?.esCorrecta == true
        TipoPreguntaPrueba.SECUENCIA ->
            seleccion.size == opciones.size &&
                seleccion.withIndex().all { (posicion, indice) -> opciones.getOrNull(indice)?.posicionCorrecta == posicion + 1 }
        TipoPreguntaPrueba.ASOCIACION ->
            seleccion.size == opciones.size && seleccion.size % 2 == 0 && seleccion.distinct().size == seleccion.size &&
                seleccion.chunked(2).all { (a, b) ->
                    val clave = opciones.getOrNull(a)?.parClave
                    clave != null && clave == opciones.getOrNull(b)?.parClave
                }
    }

    /** El orden correcto de una secuencia, como texto («so → pa»), para mostrarlo cuando el estudiante se equivoca. */
    fun ordenCorrecto(): String =
        opciones.filter { it.posicionCorrecta != null }.sortedBy { it.posicionCorrecta }.joinToString(" → ") { it.texto }

    /** Las parejas correctas de una asociación, como texto («gato — Dice miau; perro — Dice guau»). */
    fun parejasCorrectas(): String =
        opciones.filter { it.parClave != null }.groupBy { it.parClave }.values
            .joinToString("; ") { grupo -> grupo.joinToString(" — ") { it.texto } }

    /** Lo que se envía al servidor como respuesta: texto JSON con la opción elegida o el orden elegido. */
    fun respuestaComoTexto(seleccion: List<Int>): String {
        val textos = seleccion.mapNotNull { opciones.getOrNull(it)?.texto }
        return when (tipo) {
            TipoPreguntaPrueba.SELECCION, TipoPreguntaPrueba.COMPLETACION ->
                Gson().toJson(mapOf("opcion" to textos.firstOrNull().orEmpty()))
            TipoPreguntaPrueba.SECUENCIA -> Gson().toJson(mapOf("orden" to textos))
            TipoPreguntaPrueba.ASOCIACION -> Gson().toJson(mapOf("pares" to textos.chunked(2)))
        }
    }
}

// La prueba lista para rendir: sus preguntas y los intentos que el docente permite y el estudiante ya usó.
data class PruebaParaRendir(
    val pruebaId: Long,
    val objetivoId: Long,
    val titulo: String,
    val preguntas: List<PreguntaPrueba>,
    val limiteIntentos: Int,
    val intentosUsados: Int
) {
    val puntajeMaximo: Double get() = preguntas.sumOf { it.puntaje }
    val intentosRestantes: Int get() = (limiteIntentos - intentosUsados).coerceAtLeast(0)
}

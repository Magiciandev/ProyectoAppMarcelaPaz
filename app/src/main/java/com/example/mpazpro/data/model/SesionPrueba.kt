package com.example.mpazpro.data.model

// Lo que va pasando mientras el estudiante rinde la prueba: pregunta actual, lo que lleva elegido y sus respuestas.
// Es Kotlin puro (sin Android): el ViewModel solo la maneja y muestra su estado, y así se puede probar sola.
class SesionPrueba(val preguntas: List<PreguntaPrueba>) {

    var indice: Int = 0
        private set
    var seleccion: List<Int> = emptyList()
        private set
    var corregida: Boolean = false
        private set
    var terminada: Boolean = false
        private set

    private val respuestas = mutableListOf<List<Int>>()

    val actual: PreguntaPrueba get() = preguntas[indice]
    val esUltima: Boolean get() = indice == preguntas.size - 1

    /** Alternativa elegida. En selección y completación corrige al tiro; en secuencia va sumando el orden. */
    fun elegir(opcion: Int) {
        if (corregida || terminada || opcion !in actual.opciones.indices) return
        when (actual.tipo) {
            TipoPreguntaPrueba.SELECCION, TipoPreguntaPrueba.COMPLETACION -> {
                seleccion = listOf(opcion)
                corregida = true
            }
            TipoPreguntaPrueba.SECUENCIA -> if (opcion !in seleccion) seleccion = seleccion + opcion
        }
    }

    /** Secuencia: saca la última opción elegida. */
    fun deshacer() {
        if (corregida || terminada || actual.tipo != TipoPreguntaPrueba.SECUENCIA || seleccion.isEmpty()) return
        seleccion = seleccion.dropLast(1)
    }

    /** Secuencia: corrige, pero solo cuando ya se ordenaron todas las opciones. */
    fun comprobar() {
        if (corregida || terminada || actual.tipo != TipoPreguntaPrueba.SECUENCIA) return
        if (seleccion.size == actual.opciones.size) corregida = true
    }

    /** Pasa a la pregunta siguiente. Devuelve true cuando ya no quedan más (la prueba terminó). */
    fun siguiente(): Boolean {
        if (terminada) return true
        if (!corregida) return false
        respuestas.add(seleccion)
        if (esUltima) {
            terminada = true
            return true
        }
        indice++
        seleccion = emptyList()
        corregida = false
        return false
    }

    /** Las respuestas dadas, en el orden de las preguntas (completas solo cuando la prueba terminó). */
    fun respuestasDadas(): List<List<Int>> = respuestas.toList()

    fun puntajeObtenido(): Double =
        respuestas.withIndex().sumOf { (i, r) -> if (preguntas[i].esCorrecta(r)) preguntas[i].puntaje else 0.0 }

    fun puntajeMaximo(): Double = preguntas.sumOf { it.puntaje }
}

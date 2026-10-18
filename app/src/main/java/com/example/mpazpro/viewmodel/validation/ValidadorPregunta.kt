package com.example.mpazpro.viewmodel.validation

// Validaciones del editor de preguntas (UTP): texto obligatorio, 4 alternativas y una correcta.
object ValidadorPregunta {
    const val TEXTO_MIN = 8
    const val TEXTO_MAX = 120
    const val OPCION_MAX = 40
    const val CANTIDAD_OPCIONES = 4

    fun validarTexto(valor: String): String? {
        val texto = valor.trim()
        return when {
            texto.isEmpty() -> "La pregunta es obligatoria"
            texto.length < TEXTO_MIN -> "Debe tener al menos $TEXTO_MIN caracteres"
            texto.length > TEXTO_MAX -> "No puede superar los $TEXTO_MAX caracteres"
            else -> null
        }
    }

    fun validarOpcion(valor: String): String? {
        val texto = valor.trim()
        return when {
            texto.isEmpty() -> "Escribe la alternativa"
            texto.length > OPCION_MAX -> "No puede superar los $OPCION_MAX caracteres"
            else -> null
        }
    }

    fun validarCorrecta(indice: Int?): String? =
        if (indice == null || indice !in 0 until CANTIDAD_OPCIONES) "Marca cuál es la alternativa correcta" else null
}

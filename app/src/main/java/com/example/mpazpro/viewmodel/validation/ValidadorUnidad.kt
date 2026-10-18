package com.example.mpazpro.viewmodel.validation

// Validaciones del formulario de unidad como funciones puras:
// reciben un texto y devuelven el mensaje de error, o null si el valor es válido.
// No dependen de Android, por eso se prueban con pruebas unitarias normales.
object ValidadorUnidad {
    const val TITULO_MIN = 5
    const val TITULO_MAX = 60
    const val CONTENIDO_MIN = 20
    const val CONTENIDO_MAX = 500

    private val formatoOA = Regex("^OA\\d{1,2}$", RegexOption.IGNORE_CASE)

    fun validarTitulo(valor: String): String? {
        val texto = valor.trim()
        return when {
            texto.isEmpty() -> "El título es obligatorio"
            texto.length < TITULO_MIN -> "Debe tener al menos $TITULO_MIN caracteres"
            texto.length > TITULO_MAX -> "No puede superar los $TITULO_MAX caracteres"
            else -> null
        }
    }

    fun validarObjetivo(valor: String): String? {
        val texto = valor.trim()
        return when {
            texto.isEmpty() -> "El objetivo de aprendizaje es obligatorio"
            !formatoOA.matches(texto) -> "Usa el formato OA y un número, por ejemplo OA05"
            else -> null
        }
    }

    fun validarAsignatura(valor: String, permitidas: List<String>): String? =
        when {
            valor.isBlank() -> "Selecciona una asignatura"
            valor !in permitidas -> "La asignatura no es válida"
            else -> null
        }

    fun validarContenido(valor: String): String? {
        val texto = valor.trim()
        return when {
            texto.isEmpty() -> "El contenido es obligatorio"
            texto.length < CONTENIDO_MIN -> "Debe tener al menos $CONTENIDO_MIN caracteres"
            texto.length > CONTENIDO_MAX -> "No puede superar los $CONTENIDO_MAX caracteres"
            else -> null
        }
    }
}

package com.example.mpazpro.viewmodel.validation

// Validaciones del login como funciones puras: texto -> mensaje de error, o null si es válido.
object ValidadorLogin {
    const val USUARIO_MIN = 3
    const val USUARIO_MAX = 20
    const val PIN_LARGO = 4

    private val formatoUsuario = Regex("^[A-Za-z0-9._]+$")
    private val formatoPin = Regex("^\\d{$PIN_LARGO}$")

    fun validarUsuario(valor: String): String? {
        val texto = valor.trim()
        return when {
            texto.isEmpty() -> "Escribe tu usuario"
            texto.length < USUARIO_MIN -> "Debe tener al menos $USUARIO_MIN caracteres"
            texto.length > USUARIO_MAX -> "No puede superar los $USUARIO_MAX caracteres"
            !formatoUsuario.matches(texto) -> "Usa solo letras y números"
            else -> null
        }
    }

    fun validarPin(valor: String): String? =
        when {
            valor.isEmpty() -> "Escribe tu PIN"
            !formatoPin.matches(valor) -> "El PIN tiene $PIN_LARGO números"
            else -> null
        }
}

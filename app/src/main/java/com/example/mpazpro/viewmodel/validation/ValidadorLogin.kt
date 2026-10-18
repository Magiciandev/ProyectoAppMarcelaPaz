package com.example.mpazpro.viewmodel.validation

// Validaciones del formulario de ingreso, como funciones puras (texto -> mensaje de error o null).
object ValidadorLogin {
    const val USUARIO_MIN = 3
    const val USUARIO_MAX = 20
    const val LARGO_PIN = 4

    private val soloAlfanumerico = Regex("^[A-Za-z0-9]+$")
    private val soloDigitos = Regex("^[0-9]+$")

    fun validarUsuario(valor: String): String? {
        val texto = valor.trim()
        return when {
            texto.isEmpty() -> "El usuario es obligatorio"
            texto.length < USUARIO_MIN -> "Debe tener al menos $USUARIO_MIN caracteres"
            texto.length > USUARIO_MAX -> "No puede superar los $USUARIO_MAX caracteres"
            !soloAlfanumerico.matches(texto) -> "Usa solo letras y números"
            else -> null
        }
    }

    fun validarPin(valor: String): String? =
        when {
            valor.isEmpty() -> "El PIN es obligatorio"
            !soloDigitos.matches(valor) -> "El PIN solo puede tener números"
            valor.length != LARGO_PIN -> "El PIN debe tener $LARGO_PIN dígitos"
            else -> null
        }
}

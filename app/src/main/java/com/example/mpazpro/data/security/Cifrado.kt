package com.example.mpazpro.data.security

import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

// Cifrado de la clave (PIN) con PBKDF2 + sal aleatoria. Se guarda "sal:hash"; no es reversible.
object Cifrado {
    private const val ITERACIONES = 10_000
    private const val BITS = 256

    fun crear(pin: String): String {
        val sal = ByteArray(16).also { SecureRandom().nextBytes(it) }
        return "${aHex(sal)}:${aHex(derivar(pin, sal))}"
    }

    fun verificar(pin: String, almacenado: String): Boolean {
        val partes = almacenado.split(":")
        if (partes.size != 2) return false
        return aHex(derivar(pin, deHex(partes[0]))) == partes[1]
    }

    private fun derivar(pin: String, sal: ByteArray): ByteArray =
        SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            .generateSecret(PBEKeySpec(pin.toCharArray(), sal, ITERACIONES, BITS)).encoded

    private fun aHex(bytes: ByteArray) = bytes.joinToString("") { "%02x".format(it) }
    private fun deHex(hex: String) = hex.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
}

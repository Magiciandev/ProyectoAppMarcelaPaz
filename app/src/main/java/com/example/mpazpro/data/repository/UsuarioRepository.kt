package com.example.mpazpro.data.repository

import com.example.mpazpro.data.local.UsuarioDao
import com.example.mpazpro.data.model.Usuario
import com.example.mpazpro.data.security.Cifrado
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UsuarioRepository(
    private val dao: UsuarioDao,
    private val semilla: SemillaDatos
) {
    // Mismo mensaje para usuario inexistente y PIN incorrecto: no revela cuál falló.
    suspend fun autenticar(usuario: String, pin: String): Result<Usuario> =
        withContext(Dispatchers.IO) {
            runCatching {
                semilla.asegurar()
                val encontrado = dao.buscarPorUsuario(usuario)
                if (encontrado == null || !Cifrado.verificar(pin, encontrado.pinHash)) {
                    throw IllegalArgumentException("Usuario o PIN incorrectos")
                }
                encontrado
            }
        }

    suspend fun obtener(id: Long): Result<Usuario> =
        withContext(Dispatchers.IO) {
            runCatching { dao.buscarPorId(id) ?: throw IllegalStateException("Usuario no encontrado") }
        }
}

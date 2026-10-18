package com.example.mpazpro.data.repository

import com.example.mpazpro.data.local.UsuarioDao
import com.example.mpazpro.data.model.Usuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CredencialesInvalidasException : Exception("Usuario o PIN incorrectos")

class UsuarioRepository(private val dao: UsuarioDao) {

    // No se indica cuál de los dos datos falló, para no dar pistas.
    suspend fun autenticar(usuario: String, pin: String): Result<Usuario> =
        withContext(Dispatchers.IO) {
            runCatching {
                val encontrado = dao.buscarPorUsuario(usuario.trim().lowercase())
                if (encontrado == null || encontrado.pin != pin) throw CredencialesInvalidasException()
                encontrado
            }
        }
}

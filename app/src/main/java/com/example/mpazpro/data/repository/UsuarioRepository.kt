package com.example.mpazpro.data.repository

import com.example.mpazpro.data.local.UsuarioDao
import com.example.mpazpro.data.model.Roles
import com.example.mpazpro.data.model.Usuario
import com.example.mpazpro.data.remote.AutenticacionRemota
import com.example.mpazpro.data.remote.ResultadoRemoto
import com.example.mpazpro.data.remote.dto.LoginResponseDto
import com.example.mpazpro.data.security.Cifrado
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Inicio de sesión. Si hay conexión con el backend, valida allá (y deja guardado el token). Si no hay conexión
 * (o el servidor falla), valida contra la copia local: así un estudiante sin red puede entrar con el último
 * PIN que usó con conexión. El usuario local sigue siendo el que usan las demás pantallas (fase 6 lo unificará).
 */
class UsuarioRepository(
    private val dao: UsuarioDao,
    private val semilla: SemillaDatos,
    private val remota: AutenticacionRemota? = null
) {
    // Mismo mensaje para usuario inexistente y PIN incorrecto: no revela cuál falló.
    suspend fun autenticar(usuario: String, pin: String): Result<Usuario> =
        withContext(Dispatchers.IO) {
            runCatching {
                semilla.asegurar()
                when (val remoto = remota?.iniciarSesion(usuario, pin)) {
                    is ResultadoRemoto.Ok -> sincronizarLocal(usuario, pin, remoto.valor)
                    is ResultadoRemoto.ErrorHttp -> when {
                        remoto.codigo == 401 -> throw IllegalArgumentException(MENSAJE_CREDENCIALES)
                        remoto.codigo >= 500 -> autenticarLocal(usuario, pin) // el servidor tiene un problema
                        else -> throw IllegalStateException(remoto.mensaje)
                    }
                    is ResultadoRemoto.SinConexion, null -> autenticarLocal(usuario, pin)
                }
            }
        }

    suspend fun obtener(id: Long): Result<Usuario> =
        withContext(Dispatchers.IO) {
            runCatching { dao.buscarPorId(id) ?: throw IllegalStateException("Usuario no encontrado") }
        }

    /** Olvida el token del servidor. */
    fun cerrarSesion() {
        remota?.cerrarSesion()
    }

    private suspend fun autenticarLocal(usuario: String, pin: String): Usuario {
        val encontrado = dao.buscarPorUsuario(usuario)
        if (encontrado == null || !Cifrado.verificar(pin, encontrado.pinHash)) {
            throw IllegalArgumentException(MENSAJE_CREDENCIALES)
        }
        return encontrado
    }

    /** Deja la copia local al día con lo que dijo el servidor (nombre, perfil y PIN para el modo sin conexión). */
    private suspend fun sincronizarLocal(usuario: String, pin: String, respuesta: LoginResponseDto): Usuario {
        val hash = Cifrado.crear(pin)
        val rol = rolLocal(respuesta.rol)
        val existente = dao.buscarPorUsuario(usuario)
        return if (existente == null) {
            dao.insertar(Usuario(nombre = respuesta.nombre, usuario = usuario, pinHash = hash, rol = rol))
            dao.buscarPorUsuario(usuario) ?: throw IllegalStateException("No se pudo guardar el usuario")
        } else {
            existente.copy(nombre = respuesta.nombre, pinHash = hash, rol = rol).also { dao.actualizar(it) }
        }
    }

    companion object {
        const val MENSAJE_CREDENCIALES = "Usuario o PIN incorrectos"

        /** El servidor usa UTP, DOCENTE y ESTUDIANTE; la app guarda estos perfiles con otros textos. */
        fun rolLocal(rolRemoto: String): String = when (rolRemoto) {
            "UTP" -> Roles.UTP
            "DOCENTE" -> Roles.DOCENTE
            else -> Roles.ESTUDIANTE
        }
    }
}

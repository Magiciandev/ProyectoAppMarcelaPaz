package com.example.mpazpro.data.remote

import com.example.mpazpro.data.remote.dto.LoginResponseDto
import java.time.Instant

/** Datos de la sesión con el servidor (distinta del usuario local de la base Room). */
data class SesionRemota(
    val token: String,
    val expiraEnMillis: Long,
    val usuarioIdRemoto: Long,
    val usuario: String,
    val nombre: String,
    val rol: String
)

/**
 * Guarda el token que entregó el backend al iniciar sesión. Se conserva aunque se cierre la app: así un estudiante
 * sin red puede seguir usando lo ya descargado y sincronizar después. Limitación del MVP: el token queda en
 * SharedPreferences sin cifrar (los datos del prototipo son ficticios).
 */
class SesionStore(
    private val almacen: AlmacenClaves,
    private val ahora: () -> Long = { System.currentTimeMillis() }
) {
    fun guardar(respuesta: LoginResponseDto, usuario: String) {
        val expira = runCatching { Instant.parse(respuesta.expiraEn).toEpochMilli() }
            .getOrDefault(ahora() + UNA_HORA)
        almacen.escribir(TOKEN, respuesta.token)
        almacen.escribir(EXPIRA, expira.toString())
        almacen.escribir(ID_REMOTO, respuesta.usuarioId.toString())
        almacen.escribir(USUARIO, usuario)
        almacen.escribir(NOMBRE, respuesta.nombre)
        almacen.escribir(ROL, respuesta.rol)
    }

    fun actual(): SesionRemota? {
        val token = almacen.leer(TOKEN) ?: return null
        return SesionRemota(
            token = token,
            expiraEnMillis = almacen.leer(EXPIRA)?.toLongOrNull() ?: 0L,
            usuarioIdRemoto = almacen.leer(ID_REMOTO)?.toLongOrNull() ?: -1L,
            usuario = almacen.leer(USUARIO).orEmpty(),
            nombre = almacen.leer(NOMBRE).orEmpty(),
            rol = almacen.leer(ROL).orEmpty()
        )
    }

    /** El token se envía tal cual esté: si venció, el servidor responde 401 y la app pide ingresar de nuevo. */
    fun token(): String? = actual()?.token

    fun hayToken(): Boolean = almacen.leer(TOKEN) != null

    fun vigente(): Boolean {
        val sesion = actual() ?: return false
        return sesion.expiraEnMillis > ahora()
    }

    /** Quién descargó el catálogo que hay en la caché (para no mostrarle a un perfil lo de otro). */
    fun catalogoDe(): String? = almacen.leer(CATALOGO_DE)

    fun marcarCatalogo(usuario: String?) = almacen.escribir(CATALOGO_DE, usuario)

    fun cerrar() {
        listOf(TOKEN, EXPIRA, ID_REMOTO, USUARIO, NOMBRE, ROL).forEach { almacen.escribir(it, null) }
    }

    private companion object {
        const val TOKEN = "token"
        const val EXPIRA = "expira_en"
        const val ID_REMOTO = "usuario_id_remoto"
        const val USUARIO = "usuario"
        const val NOMBRE = "nombre"
        const val ROL = "rol"
        const val CATALOGO_DE = "catalogo_de"
        const val UNA_HORA = 3_600_000L
    }
}

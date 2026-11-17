package com.example.mpazpro.data.remote

import com.example.mpazpro.data.remote.dto.LoginResponseDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SesionStoreTest {

    /** Almacén en memoria: reemplaza a SharedPreferences en las pruebas. */
    private class AlmacenEnMemoria : AlmacenClaves {
        val datos = mutableMapOf<String, String>()
        override fun leer(clave: String) = datos[clave]
        override fun escribir(clave: String, valor: String?) {
            if (valor == null) datos.remove(clave) else datos[clave] = valor
        }
    }

    private val respuesta = LoginResponseDto(
        token = "abc.def.ghi", tipo = "Bearer", expiraEn = "2026-11-17T12:00:00Z",
        usuarioId = 3, nombre = "Ana", rol = "ESTUDIANTE"
    )
    private val ahora = java.time.Instant.parse("2026-11-16T12:00:00Z").toEpochMilli()

    @Test fun `sin sesion no hay token`() {
        val store = SesionStore(AlmacenEnMemoria()) { ahora }
        assertNull(store.actual())
        assertNull(store.token())
        assertFalse(store.hayToken())
        assertFalse(store.vigente())
    }

    @Test fun `guarda y devuelve la sesion`() {
        val store = SesionStore(AlmacenEnMemoria()) { ahora }
        store.guardar(respuesta, "ana")

        val sesion = store.actual()!!
        assertEquals("abc.def.ghi", sesion.token)
        assertEquals(3L, sesion.usuarioIdRemoto)
        assertEquals("ana", sesion.usuario)
        assertEquals("Ana", sesion.nombre)
        assertEquals("ESTUDIANTE", sesion.rol)
        assertTrue(store.hayToken())
        assertEquals("abc.def.ghi", store.token())
    }

    @Test fun `la sesion esta vigente hasta que vence`() {
        val antes = SesionStore(AlmacenEnMemoria()) { ahora }
        antes.guardar(respuesta, "ana")
        assertTrue(antes.vigente())

        val vencida = SesionStore(AlmacenEnMemoria()) { java.time.Instant.parse("2026-11-18T00:00:00Z").toEpochMilli() }
        vencida.guardar(respuesta, "ana")
        assertFalse(vencida.vigente())
        // Aunque venza, el token se sigue entregando: es el servidor quien responde 401.
        assertEquals("abc.def.ghi", vencida.token())
    }

    @Test fun `una fecha de vencimiento ilegible no rompe el guardado`() {
        val store = SesionStore(AlmacenEnMemoria()) { ahora }
        store.guardar(respuesta.copy(expiraEn = "no es una fecha"), "ana")
        assertTrue(store.vigente())
    }

    @Test fun `cerrar borra la sesion pero conserva de quien es el catalogo`() {
        val store = SesionStore(AlmacenEnMemoria()) { ahora }
        store.guardar(respuesta, "ana")
        store.marcarCatalogo("ana")

        store.cerrar()

        assertNull(store.actual())
        assertFalse(store.hayToken())
        assertEquals("ana", store.catalogoDe())
    }
}

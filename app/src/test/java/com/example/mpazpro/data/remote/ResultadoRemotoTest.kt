package com.example.mpazpro.data.remote

import java.io.IOException
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class ResultadoRemotoTest {

    private fun errorHttp(codigo: Int, cuerpo: String): HttpException =
        HttpException(Response.error<Any>(codigo, cuerpo.toResponseBody("application/json".toMediaType())))

    @Test fun `una llamada correcta devuelve Ok`() = runBlocking {
        val r = llamarApi { 42 }
        assertEquals(ResultadoRemoto.Ok(42), r)
    }

    @Test fun `un error del servidor usa el mensaje que mando el backend`() = runBlocking {
        val cuerpo = """{"status":409,"error":"Conflict","message":"Solo se puede editar en BORRADOR","path":"/x"}"""
        val r = llamarApi<Int> { throw errorHttp(409, cuerpo) }

        r as ResultadoRemoto.ErrorHttp
        assertEquals(409, r.codigo)
        assertEquals("Solo se puede editar en BORRADOR", r.mensaje)
    }

    @Test fun `sin cuerpo se usa un mensaje segun el codigo`() = runBlocking {
        val r = llamarApi<Int> { throw errorHttp(403, "") }

        r as ResultadoRemoto.ErrorHttp
        assertEquals(403, r.codigo)
        assertEquals(ApiErrores.mensajePorCodigo(403), r.mensaje)
    }

    @Test fun `un cuerpo que no es JSON tampoco rompe`() = runBlocking {
        val r = llamarApi<Int> { throw errorHttp(500, "<html>fallo</html>") }

        r as ResultadoRemoto.ErrorHttp
        assertEquals(ApiErrores.mensajePorCodigo(500), r.mensaje)
    }

    @Test fun `un fallo de red es SinConexion`() = runBlocking {
        val r = llamarApi<Int> { throw IOException("sin red") }
        assertTrue(r is ResultadoRemoto.SinConexion)
    }

    @Test fun `una respuesta con JSON roto no es un cierre de la app`() = runBlocking {
        val r = llamarApi<Int> { throw com.google.gson.JsonSyntaxException("roto") }
        assertTrue(r is ResultadoRemoto.ErrorHttp)
    }

    @Test fun `hay un mensaje para los codigos comunes`() {
        listOf(400, 401, 403, 404, 409, 500, 502).forEach {
            assertTrue(ApiErrores.mensajePorCodigo(it).isNotBlank())
        }
    }
}

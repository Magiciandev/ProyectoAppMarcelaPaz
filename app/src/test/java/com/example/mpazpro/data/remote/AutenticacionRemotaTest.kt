package com.example.mpazpro.data.remote

import com.example.mpazpro.data.remote.api.AuthApi
import com.example.mpazpro.data.remote.dto.LoginRequestDto
import com.example.mpazpro.data.remote.dto.LoginResponseDto
import com.example.mpazpro.data.remote.dto.UsuarioMeDto
import java.io.IOException
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class AutenticacionRemotaTest {

    private class AlmacenEnMemoria : AlmacenClaves {
        val datos = mutableMapOf<String, String>()
        override fun leer(clave: String) = datos[clave]
        override fun escribir(clave: String, valor: String?) {
            if (valor == null) datos.remove(clave) else datos[clave] = valor
        }
    }

    private class ApiFalsa(private val comportamiento: () -> LoginResponseDto) : AuthApi {
        var ultimaPeticion: LoginRequestDto? = null
        override suspend fun login(cuerpo: LoginRequestDto): LoginResponseDto {
            ultimaPeticion = cuerpo
            return comportamiento()
        }
        override suspend fun me(): UsuarioMeDto = throw NotImplementedError()
    }

    private val ok = LoginResponseDto("tok", "Bearer", "2026-11-17T12:00:00Z", 3, "Ana", "ESTUDIANTE")

    @Test fun `un login correcto deja la sesion guardada`() = runBlocking {
        val sesion = SesionStore(AlmacenEnMemoria())
        val api = ApiFalsa { ok }
        val remota = AutenticacionRemota(api, sesion)

        val r = remota.iniciarSesion("ana", "1234")

        assertTrue(r is ResultadoRemoto.Ok)
        assertEquals("ana", api.ultimaPeticion!!.usuario)
        assertEquals("1234", api.ultimaPeticion!!.pin)
        assertEquals("tok", sesion.token())
    }

    @Test fun `un 401 no guarda nada`() = runBlocking {
        val sesion = SesionStore(AlmacenEnMemoria())
        val remota = AutenticacionRemota(ApiFalsa {
            throw HttpException(Response.error<Any>(401, "".toResponseBody("application/json".toMediaType())))
        }, sesion)

        val r = remota.iniciarSesion("ana", "0000")

        r as ResultadoRemoto.ErrorHttp
        assertEquals(401, r.codigo)
        assertFalse(sesion.hayToken())
    }

    @Test fun `sin conexion no guarda nada`() = runBlocking {
        val sesion = SesionStore(AlmacenEnMemoria())
        val remota = AutenticacionRemota(ApiFalsa { throw IOException("sin red") }, sesion)

        assertTrue(remota.iniciarSesion("ana", "1234") is ResultadoRemoto.SinConexion)
        assertFalse(sesion.hayToken())
    }

    @Test fun `cerrar sesion borra el token`() = runBlocking {
        val sesion = SesionStore(AlmacenEnMemoria())
        val remota = AutenticacionRemota(ApiFalsa { ok }, sesion)
        remota.iniciarSesion("ana", "1234")

        remota.cerrarSesion()

        assertFalse(sesion.hayToken())
    }
}

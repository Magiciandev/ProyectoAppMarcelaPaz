package com.example.mpazpro.data.repository

import com.example.mpazpro.data.model.AccionEstado
import com.example.mpazpro.data.remote.ResultadoRemoto
import com.example.mpazpro.data.remote.api.AdminApi
import com.example.mpazpro.data.remote.dto.AprobacionRequestDto
import com.example.mpazpro.data.remote.dto.AprobacionResponseDto
import com.example.mpazpro.data.remote.dto.HistorialDto
import java.io.IOException
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class AprobacionServidorTest {

    /** Admin falso: anota lo que recibe o falla con el error que se le pida. */
    private class AdminFalso(private val error: Exception? = null) : AdminApi {
        var recibido: AprobacionRequestDto? = null

        override suspend fun aprobar(cuerpo: AprobacionRequestDto): AprobacionResponseDto {
            if (error != null) throw error
            recibido = cuerpo
            return AprobacionResponseDto("OBJETIVO", cuerpo.entidadId, "BORRADOR", "EN_REVISION", "2026-11-19T10:00:00Z")
        }

        override suspend fun historial(entidadId: Long, entidad: String): List<HistorialDto> = throw NotImplementedError()
    }

    @Test fun pide_la_accion_con_el_nombre_del_servidor_y_el_id_del_objetivo() = runBlocking {
        val admin = AdminFalso()

        val r = AprobacionServidor(admin).aplicar(100, AccionEstado.ENVIAR_REVISION)

        assertTrue(r is ResultadoRemoto.Ok)
        assertEquals("OBJETIVO", admin.recibido?.entidad)
        assertEquals(100L, admin.recibido?.entidadId)
        assertEquals("ENVIAR_REVISION", admin.recibido?.accion)
    }

    @Test fun un_rechazo_del_servidor_llega_con_su_motivo() = runBlocking {
        val cuerpo = """{"message":"Para enviar a revisión se necesita una prueba final con al menos una pregunta"}"""
        val error409 = HttpException(Response.error<Any>(409, cuerpo.toResponseBody("application/json".toMediaType())))

        val r = AprobacionServidor(AdminFalso(error409)).aplicar(100, AccionEstado.ENVIAR_REVISION)

        assertTrue(r is ResultadoRemoto.ErrorHttp)
        assertEquals(409, (r as ResultadoRemoto.ErrorHttp).codigo)
        assertEquals("Para enviar a revisión se necesita una prueba final con al menos una pregunta", r.mensaje)
    }

    @Test fun un_docente_recibe_403() = runBlocking {
        val error403 = HttpException(Response.error<Any>(403, "".toResponseBody("application/json".toMediaType())))

        val r = AprobacionServidor(AdminFalso(error403)).aplicar(100, AccionEstado.PUBLICAR)

        assertEquals(403, (r as ResultadoRemoto.ErrorHttp).codigo)
    }

    @Test fun sin_red_se_informa_sin_conexion() = runBlocking {
        val r = AprobacionServidor(AdminFalso(IOException("sin red"))).aplicar(100, AccionEstado.AUTORIZAR)

        assertTrue(r is ResultadoRemoto.SinConexion)
    }
}

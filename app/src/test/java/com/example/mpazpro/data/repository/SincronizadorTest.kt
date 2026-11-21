package com.example.mpazpro.data.repository

import com.example.mpazpro.data.remote.ResultadoRemoto
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class SincronizadorTest {

    private val ok = ResultadoRemoto.Ok(Unit)
    private val sinRed = ResultadoRemoto.SinConexion("sin red")

    private fun sincronizador(
        orden: MutableList<String>,
        intentos: ResultadoRemoto<*> = ok,
        avance: ResultadoRemoto<*> = ok
    ) = Sincronizador(
        enviarIntentos = { orden.add("intentos"); intentos },
        enviarAvance = { orden.add("avance"); avance }
    )

    @Test fun primero_los_intentos_y_despues_el_avance() = runBlocking {
        val orden = mutableListOf<String>()

        val r = sincronizador(orden).ejecutar()

        assertEquals(listOf("intentos", "avance"), orden)
        assertEquals(ResultadoSincronizacion.COMPLETA, r)
    }

    @Test fun sin_red_en_los_intentos_no_intenta_el_avance_y_reintenta() = runBlocking {
        val orden = mutableListOf<String>()

        val r = sincronizador(orden, intentos = sinRed).ejecutar()

        assertEquals(listOf("intentos"), orden)
        assertEquals(ResultadoSincronizacion.REINTENTAR, r)
    }

    @Test fun sin_red_en_el_avance_reintenta() = runBlocking {
        val r = sincronizador(mutableListOf(), avance = sinRed).ejecutar()

        assertEquals(ResultadoSincronizacion.REINTENTAR, r)
    }

    @Test fun un_error_del_servidor_reintenta() = runBlocking {
        val r = sincronizador(mutableListOf(), avance = ResultadoRemoto.ErrorHttp(503, "caído")).ejecutar()

        assertEquals(ResultadoSincronizacion.REINTENTAR, r)
    }

    @Test fun un_error_4xx_no_reintenta() = runBlocking {
        val r = sincronizador(mutableListOf(), intentos = ResultadoRemoto.ErrorHttp(403, "sin permiso")).ejecutar()

        assertEquals(ResultadoSincronizacion.COMPLETA, r)
    }

    @Test fun si_los_intentos_fallan_con_error_http_igual_envia_el_avance() = runBlocking {
        val orden = mutableListOf<String>()

        sincronizador(orden, intentos = ResultadoRemoto.ErrorHttp(500, "falla")).ejecutar()

        assertEquals(listOf("intentos", "avance"), orden)
    }
}

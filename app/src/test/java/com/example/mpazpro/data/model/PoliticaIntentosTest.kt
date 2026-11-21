package com.example.mpazpro.data.model

import org.junit.Assert.assertEquals
import org.junit.Test

class PoliticaIntentosTest {

    @Test fun losPendientesSeSumanALosDelServidor() = assertEquals(3, PoliticaIntentos.usados(delServidor = 1, pendientes = 2))

    @Test fun losPendientesNegativosNoRestan() = assertEquals(1, PoliticaIntentos.usados(delServidor = 1, pendientes = -4))

    @Test fun quedanLosQueFaltanParaElLimite() = assertEquals(1, PoliticaIntentos.restantes(limite = 3, delServidor = 1, pendientes = 1))

    @Test fun restantesNuncaBajaDeCero() = assertEquals(0, PoliticaIntentos.restantes(limite = 3, delServidor = 2, pendientes = 5))

    @Test fun sinPendientesQuedanLosDelServidor() = assertEquals(2, PoliticaIntentos.restantes(limite = 3, delServidor = 1, pendientes = 0))

    // D7: con 5 intentos hechos sin conexión y límite 3, valen solo los primeros 3.
    @Test fun d7_deLosPendientesSoloCabenLosPrimerosN() = assertEquals(3, PoliticaIntentos.queCabenEnElLimite(limite = 3, delServidor = 0, pendientes = 5))

    @Test fun d7_siElServidorYaTieneAlgunosCabenMenos() = assertEquals(1, PoliticaIntentos.queCabenEnElLimite(limite = 3, delServidor = 2, pendientes = 4))

    @Test fun d7_siElLimiteYaSeAgotoNoCabeNinguno() = assertEquals(0, PoliticaIntentos.queCabenEnElLimite(limite = 3, delServidor = 3, pendientes = 2))

    @Test fun d7_sinPendientesNoHayNada() = assertEquals(0, PoliticaIntentos.queCabenEnElLimite(limite = 3, delServidor = 1, pendientes = 0))
}

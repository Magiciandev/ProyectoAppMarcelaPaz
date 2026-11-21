package com.example.mpazpro.data.model

import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculoAvanceTest {
    private val totales = TotalesObjetivo(objetivoId = 1, contenidos = 1, actividades = 3, desafios = 1) // 5 en total

    private fun item(tipo: String, id: Long, en: Long, objetivo: Long = 1) =
        ItemCompletado(usuarioId = 10, objetivoId = objetivo, tipo = tipo, itemId = id, completadoEn = en)

    // ---- criterio de desafío

    @Test fun dificultad3EsDesafio() {
        assertTrue(CriterioDesafio.esDesafio(3))
        assertEquals(TipoItem.DESAFIO, CriterioDesafio.tipoDe(3))
    }

    @Test fun dificultad1y2SonActividad() {
        assertFalse(CriterioDesafio.esDesafio(1))
        assertFalse(CriterioDesafio.esDesafio(2))
        assertEquals(TipoItem.ACTIVIDAD, CriterioDesafio.tipoDe(2))
    }

    @Test fun totalesSeparanActividadesDeDesafios() {
        val t = CriterioDesafio.totalesDe(objetivoId = 7, contenidos = 2, dificultades = listOf(1, 2, 3, 3, 1))
        assertEquals(TotalesObjetivo(7, 2, 3, 2), t)
        assertEquals(7, t.total)
    }

    // ---- porcentaje

    @Test fun porcentajeSinItemsEnElObjetivoEsCero() = assertEquals(0, CalculoAvance.porcentaje(0, 0))

    @Test fun porcentajeSinCompletadosEsCero() = assertEquals(0, CalculoAvance.porcentaje(0, 5))

    @Test fun porcentajeRedondeaDesdeLaMitad() {
        assertEquals(33, CalculoAvance.porcentaje(1, 3))
        assertEquals(67, CalculoAvance.porcentaje(2, 3))
        assertEquals(20, CalculoAvance.porcentaje(1, 5))
    }

    @Test fun porcentajeNoPasaDe100() = assertEquals(100, CalculoAvance.porcentaje(9, 5))

    // ---- reinicio

    @Test fun sinReinicioValenTodos() {
        val items = listOf(item(TipoItem.CONTENIDO, 1, 100), item(TipoItem.ACTIVIDAD, 2, 200))
        assertEquals(items, CalculoAvance.vigentes(items, null))
    }

    @Test fun elReinicioDescartaLoAnteriorYLoIgual() {
        val reinicio = Instant.ofEpochSecond(200)
        val items = listOf(
            item(TipoItem.CONTENIDO, 1, 100),
            item(TipoItem.ACTIVIDAD, 2, 200),
            item(TipoItem.ACTIVIDAD, 3, 201)
        )
        assertEquals(listOf(items[2]), CalculoAvance.vigentes(items, reinicio))
    }

    // ---- avance de un objetivo

    @Test fun sinItemsNoHayNadaQueEnviar() = assertNull(CalculoAvance.calcular(totales, emptyList(), null))

    @Test fun todoAnteriorAlReinicioNoSeEnvia() {
        val items = listOf(item(TipoItem.CONTENIDO, 1, 100), item(TipoItem.ACTIVIDAD, 2, 150))
        assertNull(CalculoAvance.calcular(totales, items, Instant.ofEpochSecond(500)))
    }

    @Test fun cuentaCadaTipoYCalculaElPorcentaje() {
        val items = listOf(
            item(TipoItem.CONTENIDO, 1, 100),
            item(TipoItem.ACTIVIDAD, 2, 120),
            item(TipoItem.DESAFIO, 3, 150)
        )
        val a = CalculoAvance.calcular(totales, items, null)!!
        assertEquals(1, a.contenidos)
        assertEquals(1, a.actividades)
        assertEquals(1, a.desafios)
        assertEquals(60, a.porcentaje) // 3 de 5
    }

    @Test fun laMarcaDeTiempoEsLaDelUltimoItemVigente() {
        val items = listOf(item(TipoItem.CONTENIDO, 1, 100), item(TipoItem.ACTIVIDAD, 2, 900), item(TipoItem.ACTIVIDAD, 3, 300))
        assertEquals(900L, CalculoAvance.calcular(totales, items, null)!!.marcaTiempo)
    }

    @Test fun despuesDeUnReinicioSoloCuentaLoNuevo() {
        val items = listOf(
            item(TipoItem.CONTENIDO, 1, 100),
            item(TipoItem.ACTIVIDAD, 2, 150),
            item(TipoItem.ACTIVIDAD, 3, 600)
        )
        val a = CalculoAvance.calcular(totales, items, Instant.ofEpochSecond(500))!!
        assertEquals(0, a.contenidos)
        assertEquals(1, a.actividades)
        assertEquals(20, a.porcentaje)
        assertEquals(600L, a.marcaTiempo)
    }

    @Test fun losConteosNoSuperanElTotalDelObjetivo() {
        val items = (1L..6L).map { item(TipoItem.ACTIVIDAD, it, 100 + it) }
        val a = CalculoAvance.calcular(totales, items, null)!!
        assertEquals(3, a.actividades)
        assertEquals(60, a.porcentaje)
    }

    @Test fun ignoraItemsDeOtrosObjetivos() {
        val items = listOf(item(TipoItem.CONTENIDO, 1, 100, objetivo = 2), item(TipoItem.ACTIVIDAD, 2, 110, objetivo = 1))
        val a = CalculoAvance.calcular(totales, items, null)!!
        assertEquals(0, a.contenidos)
        assertEquals(1, a.actividades)
    }

    @Test fun objetivoSinItemsTotalesDaCeroPorciento() {
        val vacio = TotalesObjetivo(objetivoId = 1, contenidos = 0, actividades = 0, desafios = 0)
        val a = CalculoAvance.calcular(vacio, listOf(item(TipoItem.ACTIVIDAD, 1, 100)), null)!!
        assertEquals(0, a.porcentaje)
    }

    // ---- paso al formato del servidor

    @Test fun aDtoUsaInstantIsoConZ() {
        val a = AvanceCalculado(objetivoId = 1, contenidos = 1, actividades = 2, desafios = 0, porcentaje = 60, marcaTiempo = 1_700_000_000L)
        val dto = a.aDto()
        assertEquals("2023-11-14T22:13:20Z", dto.marcaTiempo)
        assertEquals(1L, dto.objetivoId)
        assertEquals(2, dto.actividadesCompletadas)
        assertEquals(60, dto.porcentajeAvance)
    }
}

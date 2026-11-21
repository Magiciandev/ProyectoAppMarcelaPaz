package com.example.mpazpro.data.model

import org.junit.Assert.assertEquals
import org.junit.Test

class MapeoEstadoTest {

    @Test fun los_cinco_estados_del_servidor_se_muestran_en_espanol() {
        assertEquals(EstadoUnidad.BORRADOR, MapeoEstado.aLocal("BORRADOR"))
        assertEquals(EstadoUnidad.EN_REVISION, MapeoEstado.aLocal("EN_REVISION"))
        assertEquals(EstadoUnidad.AUTORIZADO, MapeoEstado.aLocal("AUTORIZADO"))
        assertEquals(EstadoUnidad.PUBLICADO, MapeoEstado.aLocal("PUBLICADO"))
        assertEquals(EstadoUnidad.DESHABILITADO, MapeoEstado.aLocal("DESHABILITADO"))
    }

    @Test fun un_estado_desconocido_se_deja_tal_cual() {
        assertEquals("OTRO", MapeoEstado.aLocal("OTRO"))
    }

    @Test fun el_estado_al_que_lleva_cada_accion_coincide_con_el_del_servidor() {
        // Lo que el servidor responde como «estadoNuevo» tras cada acción, traducido, debe ser el `hasta` de la acción.
        assertEquals(AccionEstado.AUTORIZAR.hasta, MapeoEstado.aLocal("AUTORIZADO"))
        assertEquals(AccionEstado.PUBLICAR.hasta, MapeoEstado.aLocal("PUBLICADO"))
        assertEquals(AccionEstado.DESHABILITAR.hasta, MapeoEstado.aLocal("DESHABILITADO"))
        assertEquals(AccionEstado.REABRIR.hasta, MapeoEstado.aLocal("BORRADOR"))
    }
}

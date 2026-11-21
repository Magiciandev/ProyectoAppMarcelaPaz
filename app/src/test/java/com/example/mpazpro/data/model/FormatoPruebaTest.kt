package com.example.mpazpro.data.model

import org.junit.Assert.assertEquals
import org.junit.Test

class FormatoPruebaTest {

    @Test fun los_puntos_enteros_se_muestran_sin_decimales() {
        assertEquals("3", FormatoPuntaje.texto(3.0))
        assertEquals("0", FormatoPuntaje.texto(0.0))
    }

    @Test fun los_puntos_con_decimales_usan_coma() {
        assertEquals("2,5", FormatoPuntaje.texto(2.5))
    }

    @Test fun el_porcentaje_se_redondea() {
        assertEquals(75, FormatoPuntaje.porcentaje(3.0, 4.0))
        assertEquals(67, FormatoPuntaje.porcentaje(2.0, 3.0))
        assertEquals(100, FormatoPuntaje.porcentaje(4.0, 4.0))
        assertEquals(0, FormatoPuntaje.porcentaje(1.0, 0.0))
    }

    @Test fun cada_motivo_de_rechazo_tiene_su_mensaje() {
        assertEquals("Ya usaste todos tus intentos.", MensajesPrueba.motivoRechazo("LIMITE_INTENTOS"))
        assertEquals("Tu docente reinició tu avance en esta unidad.", MensajesPrueba.motivoRechazo("ANTERIOR_AL_REINICIO"))
        assertEquals("El servidor no pudo registrar el intento.", MensajesPrueba.motivoRechazo(null))
    }
}

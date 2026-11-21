package com.example.mpazpro.data.repository

import com.example.mpazpro.data.local.AvanceLocalDao
import com.example.mpazpro.data.model.ItemCompletado
import com.example.mpazpro.data.model.TipoItem
import com.example.mpazpro.data.model.TotalesObjetivo
import com.example.mpazpro.data.remote.ResultadoRemoto
import com.example.mpazpro.data.remote.api.ProgresoApi
import com.example.mpazpro.data.remote.dto.AsignacionDto
import com.example.mpazpro.data.remote.dto.AsignacionEstudianteDto
import com.example.mpazpro.data.remote.dto.AsignacionRequestDto
import com.example.mpazpro.data.remote.dto.AvanceDto
import com.example.mpazpro.data.remote.dto.CursoDto
import com.example.mpazpro.data.remote.dto.EstudianteCursoDto
import com.example.mpazpro.data.remote.dto.EstudianteMeDto
import com.example.mpazpro.data.remote.dto.InformeDto
import com.example.mpazpro.data.remote.dto.InformeRequestDto
import com.example.mpazpro.data.remote.dto.IntentoDto
import com.example.mpazpro.data.remote.dto.ReinicioDto
import com.example.mpazpro.data.remote.dto.ReinicioRequestDto
import com.example.mpazpro.data.remote.dto.ResultadoProgresoDto
import com.example.mpazpro.data.remote.dto.ResumenDto
import com.example.mpazpro.data.remote.dto.SyncRequestDto
import com.example.mpazpro.data.remote.dto.SyncResponseDto
import com.example.mpazpro.data.remote.dto.ValidacionRequestDto
import java.io.IOException
import java.time.Instant
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class AvanceServidorTest {

    private val usuario = 10L
    private val objetivo = 1L
    private var reloj = Instant.parse("2026-11-21T15:00:00Z")

    /** DAO en memoria con las mismas reglas que Room (marcar reemplaza al ítem repetido). */
    private class DaoFalso : AvanceLocalDao {
        val items = mutableListOf<ItemCompletado>()
        val totales = mutableMapOf<Long, TotalesObjetivo>()

        override suspend fun marcar(item: ItemCompletado) {
            items.removeAll { it.usuarioId == item.usuarioId && it.objetivoId == item.objetivoId && it.tipo == item.tipo && it.itemId == item.itemId }
            items.add(item)
        }

        override suspend fun itemsDe(usuarioId: Long, objetivoId: Long) =
            items.filter { it.usuarioId == usuarioId && it.objetivoId == objetivoId }

        override suspend fun itemsDelUsuario(usuarioId: Long) = items.filter { it.usuarioId == usuarioId }
        override suspend fun objetivosConAvance(usuarioId: Long) = itemsDelUsuario(usuarioId).map { it.objetivoId }.distinct()
        override suspend fun borrarHasta(usuarioId: Long, hastaSegundos: Long) {
            items.removeAll { it.usuarioId == usuarioId && it.completadoEn <= hastaSegundos }
        }

        override suspend fun guardarTotales(totales: TotalesObjetivo) { this.totales[totales.objetivoId] = totales }
        override suspend fun totalesDe(objetivoId: Long) = totales[objetivoId]
    }

    /** API falsa: el avance que ve el estudiante, la respuesta al sync y un registro de los lotes recibidos. */
    private class ProgresoFalso(
        private val ultimoReinicio: String? = null,
        private val errorDeRed: Exception? = null,
        private val respuesta: SyncResponseDto = SyncResponseDto(progreso = listOf(ResultadoProgresoDto(1, "ACEPTADO")))
    ) : ProgresoApi {
        val lotes = mutableListOf<SyncRequestDto>()

        override suspend fun miAvance(): AvanceDto {
            if (errorDeRed != null) throw errorDeRed
            return AvanceDto(ultimoReinicio = ultimoReinicio)
        }

        override suspend fun sincronizar(lote: SyncRequestDto): SyncResponseDto {
            if (errorDeRed != null) throw errorDeRed
            lotes.add(lote)
            return respuesta
        }

        override suspend fun yo(): EstudianteMeDto = throw NotImplementedError()
        override suspend fun misAsignaciones(): List<AsignacionEstudianteDto> = throw NotImplementedError()
        override suspend fun conexion(): Response<Unit> = throw NotImplementedError()
        override suspend fun cursos(): List<CursoDto> = throw NotImplementedError()
        override suspend fun estudiantesDelCurso(cursoId: Long): List<EstudianteCursoDto> = throw NotImplementedError()
        override suspend fun crearAsignacion(cuerpo: AsignacionRequestDto): AsignacionDto = throw NotImplementedError()
        override suspend fun asignaciones(cursoId: Long): List<AsignacionDto> = throw NotImplementedError()
        override suspend fun actualizarAsignacion(id: Long, cuerpo: AsignacionRequestDto): AsignacionDto = throw NotImplementedError()
        override suspend fun resumen(estudianteId: Long): ResumenDto = throw NotImplementedError()
        override suspend fun intentos(estudianteId: Long, objetivoId: Long?): List<IntentoDto> = throw NotImplementedError()
        override suspend fun reiniciar(estudianteId: Long, cuerpo: ReinicioRequestDto): ReinicioDto = throw NotImplementedError()
        override suspend fun generarInforme(cuerpo: InformeRequestDto): InformeDto = throw NotImplementedError()
        override suspend fun informes(cursoId: Long?, estudianteId: Long?): List<InformeDto> = throw NotImplementedError()
        override suspend fun informe(id: Long): InformeDto = throw NotImplementedError()
        override suspend fun validarInforme(id: Long, cuerpo: ValidacionRequestDto): InformeDto = throw NotImplementedError()
    }

    // El objetivo 1 tiene 1 contenido y 3 actividades (dificultades 1, 2 y 3: dos actividades y un desafío): 4 ítems.
    private fun servicio(
        api: ProgresoApi,
        dao: DaoFalso,
        dificultades: ResultadoRemoto<List<Int>> = ResultadoRemoto.Ok(listOf(1, 2, 3))
    ) = AvanceServidor(
        api = api,
        dao = dao,
        idsContenidos = { listOf(50L) },
        dificultadesActividades = { dificultades },
        ahora = { reloj }
    )

    private fun item(tipo: String, id: Long, en: Long) = ItemCompletado(usuario, objetivo, tipo, id, en)

    // ---- contenido

    @Test fun abrir_el_contenido_cuenta_el_contenido_y_envia_el_avance() = runBlocking {
        val api = ProgresoFalso()
        val dao = DaoFalso()

        val r = servicio(api, dao).contenidoVisto(usuario, objetivo)

        assertEquals(ResultadoRemoto.Ok(ResumenEnvioAvance(aceptados = 1)), r)
        val p = api.lotes.single().progreso.single()
        assertEquals(1, p.contenidosCompletados)
        assertEquals(0, p.actividadesCompletadas)
        assertEquals(25, p.porcentajeAvance) // 1 de 4
        assertEquals(TotalesObjetivo(objetivo, 1, 2, 1), dao.totales[objetivo])
    }

    @Test fun el_lote_lleva_la_hora_del_envio_y_la_marca_en_iso_con_z() = runBlocking {
        val api = ProgresoFalso()

        servicio(api, DaoFalso()).contenidoVisto(usuario, objetivo)

        assertEquals("2026-11-21T15:00:00Z", api.lotes.single().enviadoEn)
        assertEquals("2026-11-21T15:00:00Z", api.lotes.single().progreso.single().marcaTiempo)
    }

    // ---- actividades y desafíos

    @Test fun una_actividad_de_dificultad_baja_cuenta_como_actividad() = runBlocking {
        val api = ProgresoFalso()

        servicio(api, DaoFalso()).actividadCompletada(usuario, objetivo, actividadId = 7, dificultad = 2, dificultades = listOf(1, 2, 3))

        val p = api.lotes.single().progreso.single()
        assertEquals(1, p.actividadesCompletadas)
        assertEquals(0, p.desafiosCompletados)
    }

    @Test fun una_actividad_de_dificultad_3_cuenta_como_desafio() = runBlocking {
        val api = ProgresoFalso()

        servicio(api, DaoFalso()).actividadCompletada(usuario, objetivo, actividadId = 8, dificultad = 3, dificultades = listOf(1, 2, 3))

        val p = api.lotes.single().progreso.single()
        assertEquals(0, p.actividadesCompletadas)
        assertEquals(1, p.desafiosCompletados)
    }

    @Test fun el_avance_se_acumula_con_lo_ya_completado() = runBlocking {
        val api = ProgresoFalso()
        val dao = DaoFalso()
        val s = servicio(api, dao)

        s.contenidoVisto(usuario, objetivo)
        reloj = reloj.plusSeconds(60)
        s.actividadCompletada(usuario, objetivo, 7, 1, listOf(1, 2, 3))
        reloj = reloj.plusSeconds(60)
        s.actividadCompletada(usuario, objetivo, 9, 3, listOf(1, 2, 3))

        val ultimo = api.lotes.last().progreso.single()
        assertEquals(75, ultimo.porcentajeAvance) // contenido + actividad + desafío = 3 de 4
        assertEquals("2026-11-21T15:02:00Z", ultimo.marcaTiempo)
    }

    @Test fun repetir_una_actividad_no_la_cuenta_dos_veces() = runBlocking {
        val api = ProgresoFalso()
        val dao = DaoFalso()
        val s = servicio(api, dao)

        s.actividadCompletada(usuario, objetivo, 7, 1, listOf(1, 2, 3))
        reloj = reloj.plusSeconds(30)
        s.actividadCompletada(usuario, objetivo, 7, 1, listOf(1, 2, 3))

        assertEquals(1, dao.items.size)
        assertEquals(1, api.lotes.last().progreso.single().actividadesCompletadas)
    }

    // ---- reinicio del docente

    @Test fun lo_hecho_antes_del_reinicio_se_descarta_y_no_se_envia() = runBlocking {
        val reinicio = reloj.plusSeconds(600)
        val api = ProgresoFalso(ultimoReinicio = reinicio.toString())
        val dao = DaoFalso()
        dao.guardarTotales(TotalesObjetivo(objetivo, 1, 2, 1))
        dao.marcar(item(TipoItem.CONTENIDO, 50, reloj.epochSecond))
        dao.marcar(item(TipoItem.ACTIVIDAD, 7, reloj.epochSecond + 100))

        val r = servicio(api, dao).sincronizar(usuario)

        assertEquals(ResultadoRemoto.Ok(ResumenEnvioAvance()), r)
        assertTrue(api.lotes.isEmpty())
        assertTrue(dao.items.isEmpty())
    }

    @Test fun despues_de_un_reinicio_solo_se_envia_lo_nuevo() = runBlocking {
        val reinicio = reloj.plusSeconds(600)
        val api = ProgresoFalso(ultimoReinicio = reinicio.toString())
        val dao = DaoFalso()
        dao.guardarTotales(TotalesObjetivo(objetivo, 1, 2, 1))
        dao.marcar(item(TipoItem.CONTENIDO, 50, reloj.epochSecond))
        dao.marcar(item(TipoItem.ACTIVIDAD, 7, reinicio.epochSecond + 30))

        servicio(api, dao).sincronizar(usuario)

        val p = api.lotes.single().progreso.single()
        assertEquals(0, p.contenidosCompletados)
        assertEquals(1, p.actividadesCompletadas)
        assertEquals(25, p.porcentajeAvance)
        assertEquals(1, dao.items.size)
    }

    @Test fun si_el_servidor_responde_un_reinicio_mas_nuevo_se_descarta_lo_anterior() = runBlocking {
        val reinicio = reloj.plusSeconds(300)
        val respuesta = SyncResponseDto(
            progreso = listOf(ResultadoProgresoDto(1, "RECHAZADO", "ANTERIOR_AL_REINICIO")),
            ultimoReinicio = reinicio.toString()
        )
        val api = ProgresoFalso(ultimoReinicio = null, respuesta = respuesta)
        val dao = DaoFalso()
        dao.guardarTotales(TotalesObjetivo(objetivo, 1, 2, 1))
        dao.marcar(item(TipoItem.ACTIVIDAD, 7, reloj.epochSecond))

        val r = servicio(api, dao).sincronizar(usuario)

        assertEquals(ResultadoRemoto.Ok(ResumenEnvioAvance(rechazados = 1)), r)
        assertTrue(dao.items.isEmpty())
    }

    // ---- sin conexión y resultados

    @Test fun sin_conexion_lo_completado_queda_anotado_para_despues() = runBlocking {
        val api = ProgresoFalso(errorDeRed = IOException("sin red"))
        val dao = DaoFalso()

        val r = servicio(api, dao).actividadCompletada(usuario, objetivo, 7, 1, listOf(1, 2, 3))

        assertTrue(r is ResultadoRemoto.SinConexion)
        assertEquals(1, dao.items.size)
        assertTrue(api.lotes.isEmpty())
    }

    @Test fun lo_pendiente_se_envia_en_el_siguiente_intento() = runBlocking {
        val dao = DaoFalso()
        servicio(ProgresoFalso(errorDeRed = IOException("sin red")), dao).actividadCompletada(usuario, objetivo, 7, 1, listOf(1, 2, 3))
        val conRed = ProgresoFalso()

        val r = servicio(conRed, dao).sincronizar(usuario)

        assertEquals(ResultadoRemoto.Ok(ResumenEnvioAvance(aceptados = 1)), r)
        assertEquals(1, conRed.lotes.single().progreso.single().actividadesCompletadas)
    }

    @Test fun sin_totales_ni_conexion_para_pedirlos_no_se_envia_nada() = runBlocking {
        val api = ProgresoFalso()
        val dao = DaoFalso()
        dao.marcar(item(TipoItem.ACTIVIDAD, 7, reloj.epochSecond))

        val r = servicio(api, dao, dificultades = ResultadoRemoto.SinConexion("sin red")).sincronizar(usuario)

        assertEquals(ResultadoRemoto.Ok(ResumenEnvioAvance()), r)
        assertTrue(api.lotes.isEmpty())
        assertEquals(1, dao.items.size)
        assertNull(dao.totales[objetivo])
    }

    @Test fun cuenta_aceptados_duplicados_y_rechazados() = runBlocking {
        val respuesta = SyncResponseDto(
            progreso = listOf(
                ResultadoProgresoDto(1, "ACEPTADO"),
                ResultadoProgresoDto(2, "DUPLICADO"),
                ResultadoProgresoDto(3, "RECHAZADO", "SIN_ASIGNACION")
            )
        )
        val api = ProgresoFalso(respuesta = respuesta)
        val dao = DaoFalso()
        dao.guardarTotales(TotalesObjetivo(objetivo, 1, 2, 1))
        dao.marcar(item(TipoItem.ACTIVIDAD, 7, reloj.epochSecond))

        val r = servicio(api, dao).sincronizar(usuario)

        assertEquals(ResultadoRemoto.Ok(ResumenEnvioAvance(aceptados = 1, duplicados = 1, rechazados = 1)), r)
    }

    @Test fun un_envio_sin_nada_anotado_no_llama_a_sync() = runBlocking {
        val api = ProgresoFalso()

        val r = servicio(api, DaoFalso()).sincronizar(usuario)

        assertEquals(ResultadoRemoto.Ok(ResumenEnvioAvance()), r)
        assertTrue(api.lotes.isEmpty())
    }

    @Test fun cada_objetivo_va_con_su_propio_avance() = runBlocking {
        val api = ProgresoFalso()
        val dao = DaoFalso()
        dao.guardarTotales(TotalesObjetivo(1, 1, 2, 1))
        dao.guardarTotales(TotalesObjetivo(3, 0, 1, 0))
        dao.marcar(ItemCompletado(usuario, 1, TipoItem.ACTIVIDAD, 7, reloj.epochSecond))
        dao.marcar(ItemCompletado(usuario, 3, TipoItem.ACTIVIDAD, 20, reloj.epochSecond))

        servicio(api, dao).sincronizar(usuario)

        val porObjetivo = api.lotes.single().progreso.associateBy { it.objetivoId }
        assertEquals(25, porObjetivo[1]!!.porcentajeAvance)
        assertEquals(100, porObjetivo[3]!!.porcentajeAvance)
    }

    @Test fun avisa_cuando_el_envio_falla_y_el_avance_queda_pendiente() = runBlocking {
        var avisos = 0
        val s = AvanceServidor(
            api = ProgresoFalso(errorDeRed = IOException("sin red")), dao = DaoFalso(),
            idsContenidos = { listOf(50L) }, dificultadesActividades = { ResultadoRemoto.Ok(listOf(1, 2, 3)) },
            ahora = { reloj }, alQuedarPendiente = { avisos++ }
        )

        s.actividadCompletada(usuario, objetivo, 7, 1, listOf(1, 2, 3))

        assertEquals(1, avisos)
    }

    @Test fun no_avisa_cuando_el_avance_se_envia_bien() = runBlocking {
        var avisos = 0
        val s = AvanceServidor(
            api = ProgresoFalso(), dao = DaoFalso(),
            idsContenidos = { listOf(50L) }, dificultadesActividades = { ResultadoRemoto.Ok(listOf(1, 2, 3)) },
            ahora = { reloj }, alQuedarPendiente = { avisos++ }
        )

        s.actividadCompletada(usuario, objetivo, 7, 1, listOf(1, 2, 3))

        assertEquals(0, avisos)
    }
}

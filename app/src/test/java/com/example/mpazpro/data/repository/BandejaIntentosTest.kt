package com.example.mpazpro.data.repository

import com.example.mpazpro.data.local.IntentoPendienteDao
import com.example.mpazpro.data.model.IntentoPendiente
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
import com.example.mpazpro.data.remote.dto.IntentoSyncDto
import com.example.mpazpro.data.remote.dto.ReinicioDto
import com.example.mpazpro.data.remote.dto.ReinicioRequestDto
import com.example.mpazpro.data.remote.dto.RespuestaSyncDto
import com.example.mpazpro.data.remote.dto.ResultadoIntentoDto
import com.example.mpazpro.data.remote.dto.ResumenDto
import com.example.mpazpro.data.remote.dto.SyncRequestDto
import com.example.mpazpro.data.remote.dto.SyncResponseDto
import com.example.mpazpro.data.remote.dto.ValidacionRequestDto
import java.io.IOException
import java.time.Instant
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class BandejaIntentosTest {

    private val usuario = 10L

    /** DAO en memoria con las reglas de Room: mismo uuid reemplaza; pendientes por fecha. */
    private class DaoFalso : IntentoPendienteDao {
        val filas = mutableMapOf<String, IntentoPendiente>()
        override suspend fun guardar(intento: IntentoPendiente) { filas[intento.uuid] = intento }
        override suspend fun pendientes(usuarioId: Long) =
            filas.values.filter { it.usuarioId == usuarioId }.sortedWith(compareBy({ it.fechaHora }, { it.uuid }))
        override suspend fun contar(usuarioId: Long, objetivoId: Long) =
            filas.values.count { it.usuarioId == usuarioId && it.objetivoId == objetivoId }
        override suspend fun borrar(uuids: List<String>) { uuids.forEach { filas.remove(it) } }
    }

    /** API falsa: responde a cada intento según `resultados` (ACEPTADO por defecto) y registra los lotes. */
    private class ProgresoFalso(
        private val error: Exception? = null,
        private val resultados: Map<String, ResultadoIntentoDto> = emptyMap(),
        private val omitir: Set<String> = emptySet()
    ) : ProgresoApi {
        val lotes = mutableListOf<SyncRequestDto>()

        override suspend fun sincronizar(lote: SyncRequestDto): SyncResponseDto {
            if (error != null) throw error
            lotes.add(lote)
            return SyncResponseDto(
                intentos = lote.intentos.filter { it.uuid !in omitir }
                    .map { resultados[it.uuid] ?: ResultadoIntentoDto(it.uuid, "ACEPTADO") }
            )
        }

        override suspend fun miAvance(): AvanceDto = throw NotImplementedError()
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

    private fun intento(uuid: String, objetivo: Long = 1, numero: Int = 1, fecha: String = "2026-11-21T15:00:00Z") = IntentoSyncDto(
        uuid = uuid, pruebaId = 1, objetivoId = objetivo, numeroIntento = numero,
        puntajeObtenido = 3.0, puntajeMaximo = 4.0, fechaHora = fecha,
        respuestas = listOf(RespuestaSyncDto("r-$uuid", 7, "[1]", true, 1.0, fecha))
    )

    private fun bandeja(api: ProgresoApi, dao: DaoFalso) =
        BandejaIntentos(api, dao, ahora = { Instant.parse("2026-11-21T16:00:00Z") })

    @Test fun guardar_deja_el_intento_pendiente_del_objetivo() = runBlocking {
        val dao = DaoFalso()
        val b = bandeja(ProgresoFalso(), dao)

        b.guardar(usuario, intento("a", objetivo = 1))
        b.guardar(usuario, intento("b", objetivo = 3))

        assertEquals(1, b.pendientesDe(usuario, 1))
        assertEquals(1, b.pendientesDe(usuario, 3))
        assertEquals(0, b.pendientesDe(usuario, 9))
    }

    @Test fun guardar_dos_veces_el_mismo_intento_no_lo_duplica() = runBlocking {
        val dao = DaoFalso()
        val b = bandeja(ProgresoFalso(), dao)

        b.guardar(usuario, intento("a"))
        b.guardar(usuario, intento("a"))

        assertEquals(1, b.pendientesDe(usuario, 1))
    }

    @Test fun los_pendientes_de_otro_usuario_no_se_mezclan() = runBlocking {
        val dao = DaoFalso()
        val b = bandeja(ProgresoFalso(), dao)

        b.guardar(usuario, intento("a"))
        b.guardar(99, intento("b"))

        assertEquals(1, b.pendientesDe(usuario, 1))
    }

    @Test fun sin_pendientes_no_llama_al_servidor() = runBlocking {
        val api = ProgresoFalso()

        val r = bandeja(api, DaoFalso()).enviar(usuario)

        assertEquals(ResultadoRemoto.Ok(emptyMap<String, ResultadoIntentoDto>()), r)
        assertTrue(api.lotes.isEmpty())
    }

    @Test fun un_intento_aceptado_se_envia_completo_y_se_borra() = runBlocking {
        val api = ProgresoFalso()
        val dao = DaoFalso()
        val b = bandeja(api, dao)
        b.guardar(usuario, intento("a"))

        val r = b.enviar(usuario) as ResultadoRemoto.Ok

        assertEquals("ACEPTADO", r.valor["a"]!!.resultado)
        assertTrue(dao.filas.isEmpty())
        val enviado = api.lotes.single().intentos.single()
        assertEquals("a", enviado.uuid)
        assertEquals("r-a", enviado.respuestas.single().uuid)
        assertEquals("2026-11-21T16:00:00Z", api.lotes.single().enviadoEn)
    }

    @Test fun se_envian_del_mas_antiguo_al_mas_nuevo() = runBlocking {
        val api = ProgresoFalso()
        val b = bandeja(api, DaoFalso())
        b.guardar(usuario, intento("tercero", numero = 3, fecha = "2026-11-21T15:30:00Z"))
        b.guardar(usuario, intento("primero", numero = 1, fecha = "2026-11-21T15:00:00Z"))
        b.guardar(usuario, intento("segundo", numero = 2, fecha = "2026-11-21T15:10:00Z"))

        b.enviar(usuario)

        assertEquals(listOf("primero", "segundo", "tercero"), api.lotes.single().intentos.map { it.uuid })
    }

    @Test fun un_duplicado_tambien_se_borra() = runBlocking {
        val api = ProgresoFalso(resultados = mapOf("a" to ResultadoIntentoDto("a", "DUPLICADO")))
        val dao = DaoFalso()
        val b = bandeja(api, dao)
        b.guardar(usuario, intento("a"))

        b.enviar(usuario)

        assertTrue(dao.filas.isEmpty())
    }

    // D7: el servidor rechaza los que pasan del límite con un motivo; es definitivo y no se reintenta.
    @Test fun un_rechazo_por_limite_se_borra_y_entrega_el_motivo() = runBlocking {
        val api = ProgresoFalso(resultados = mapOf("c" to ResultadoIntentoDto("c", "RECHAZADO", "LIMITE_INTENTOS")))
        val dao = DaoFalso()
        val b = bandeja(api, dao)
        b.guardar(usuario, intento("c", numero = 4))

        val r = b.enviar(usuario) as ResultadoRemoto.Ok

        assertEquals("LIMITE_INTENTOS", r.valor["c"]!!.motivo)
        assertTrue(dao.filas.isEmpty())
    }

    @Test fun sin_conexion_los_intentos_quedan_guardados() = runBlocking {
        val dao = DaoFalso()
        val b = bandeja(ProgresoFalso(error = IOException("sin red")), dao)
        b.guardar(usuario, intento("a"))

        val r = b.enviar(usuario)

        assertTrue(r is ResultadoRemoto.SinConexion)
        assertEquals(1, dao.filas.size)
    }

    @Test fun lo_que_el_servidor_no_responde_queda_pendiente() = runBlocking {
        val api = ProgresoFalso(omitir = setOf("b"))
        val dao = DaoFalso()
        val b = bandeja(api, dao)
        b.guardar(usuario, intento("a", fecha = "2026-11-21T15:00:00Z"))
        b.guardar(usuario, intento("b", fecha = "2026-11-21T15:05:00Z"))

        b.enviar(usuario)

        assertEquals(setOf("b"), dao.filas.keys)
    }

    @Test fun lo_pendiente_se_envia_en_el_siguiente_intento_con_el_mismo_uuid() = runBlocking {
        val dao = DaoFalso()
        bandeja(ProgresoFalso(error = IOException("sin red")), dao).also { it.guardar(usuario, intento("a")) }.enviar(usuario)
        val conRed = ProgresoFalso()

        bandeja(conRed, dao).enviar(usuario)

        assertEquals("a", conRed.lotes.single().intentos.single().uuid)
        assertTrue(dao.filas.isEmpty())
    }
}

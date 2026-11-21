package com.example.mpazpro.data.repository

import com.example.mpazpro.data.model.Asignacion
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
import com.example.mpazpro.data.remote.dto.ResumenDto
import com.example.mpazpro.data.remote.dto.SyncRequestDto
import com.example.mpazpro.data.remote.dto.SyncResponseDto
import com.example.mpazpro.data.remote.dto.ValidacionRequestDto
import java.time.LocalDate
import java.time.ZoneOffset
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class AsignacionServidorTest {

    /** API falsa de progreso: dos cursos, las asignaciones que se le den y un registro de lo que recibe. */
    private class ProgresoFalso(
        private val existentes: Map<Long, List<AsignacionDto>> = emptyMap(),
        private val errorAlCrear: Exception? = null
    ) : ProgresoApi {
        var creada: AsignacionRequestDto? = null
        var actualizada: Pair<Long, AsignacionRequestDto>? = null

        override suspend fun cursos(): List<CursoDto> = listOf(CursoDto(1, "2° A", 2), CursoDto(2, "2° B", 2))

        override suspend fun asignaciones(cursoId: Long): List<AsignacionDto> = existentes[cursoId] ?: emptyList()

        override suspend fun crearAsignacion(cuerpo: AsignacionRequestDto): AsignacionDto {
            if (errorAlCrear != null) throw errorAlCrear
            creada = cuerpo
            return dto(id = 50, objetivoId = cuerpo.objetivoId, cursoId = cuerpo.cursoId ?: 0)
        }

        override suspend fun actualizarAsignacion(id: Long, cuerpo: AsignacionRequestDto): AsignacionDto {
            actualizada = id to cuerpo
            return dto(id = id, objetivoId = cuerpo.objetivoId, cursoId = cuerpo.cursoId ?: 0)
        }

        // Lo que estas pruebas no usan falla si alguien lo llama.
        override suspend fun yo(): EstudianteMeDto = throw NotImplementedError()
        override suspend fun misAsignaciones(): List<AsignacionEstudianteDto> = throw NotImplementedError()
        override suspend fun miAvance(): AvanceDto = throw NotImplementedError()
        override suspend fun conexion(): Response<Unit> = throw NotImplementedError()
        override suspend fun sincronizar(lote: SyncRequestDto): SyncResponseDto = throw NotImplementedError()
        override suspend fun estudiantesDelCurso(cursoId: Long): List<EstudianteCursoDto> = throw NotImplementedError()
        override suspend fun resumen(estudianteId: Long): ResumenDto = throw NotImplementedError()
        override suspend fun intentos(estudianteId: Long, objetivoId: Long?): List<IntentoDto> = throw NotImplementedError()
        override suspend fun reiniciar(estudianteId: Long, cuerpo: ReinicioRequestDto): ReinicioDto = throw NotImplementedError()
        override suspend fun generarInforme(cuerpo: InformeRequestDto): InformeDto = throw NotImplementedError()
        override suspend fun informes(cursoId: Long?, estudianteId: Long?): List<InformeDto> = throw NotImplementedError()
        override suspend fun informe(id: Long): InformeDto = throw NotImplementedError()
        override suspend fun validarInforme(id: Long, cuerpo: ValidacionRequestDto): InformeDto = throw NotImplementedError()
    }

    companion object {
        fun dto(id: Long, objetivoId: Long, cursoId: Long) = AsignacionDto(
            id = id, objetivoId = objetivoId, cursoId = cursoId, docenteId = 2, disponible = true,
            fechaDesde = "2026-11-19", fechaHasta = "2026-12-19", limiteIntentos = 3
        )

        // El selector de fecha entrega la medianoche UTC; «hasta» se guarda como el fin de ese día.
        private fun medianocheUtc(anio: Int, mes: Int, dia: Int): Long =
            LocalDate.of(anio, mes, dia).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

        fun asignacion(curso: String = "2° A") = Asignacion(
            unidadId = 9, curso = curso,
            desde = medianocheUtc(2026, 11, 19),
            hasta = medianocheUtc(2026, 12, 19) + 86_399_999L,
            activa = true
        )
    }

    @Test fun crea_la_asignacion_si_el_objetivo_no_estaba_asignado_al_curso() = runBlocking {
        val api = ProgresoFalso()

        val r = AsignacionServidor(api).guardar(objetivoId = 100, asignacion = asignacion(), intentos = 4)

        assertTrue(r is ResultadoRemoto.Ok)
        assertEquals(50L, (r as ResultadoRemoto.Ok<AsignacionDto>).valor.id)
        val cuerpo = api.creada
        assertEquals(100L, cuerpo?.objetivoId)
        assertEquals(1L, cuerpo?.cursoId) // «2° A» es el curso 1 en el servidor
        assertNull(cuerpo?.estudianteId)
        assertEquals("2026-11-19", cuerpo?.fechaDesde)
        assertEquals("2026-12-19", cuerpo?.fechaHasta)
        assertEquals(4, cuerpo?.limiteIntentos)
        assertEquals(true, cuerpo?.disponible)
        assertNull(api.actualizada)
    }

    @Test fun edita_la_asignacion_si_ya_existia_para_ese_curso() = runBlocking {
        val api = ProgresoFalso(existentes = mapOf(1L to listOf(dto(id = 7, objetivoId = 100, cursoId = 1))))

        val r = AsignacionServidor(api).guardar(objetivoId = 100, asignacion = asignacion(), intentos = 5)

        assertTrue(r is ResultadoRemoto.Ok)
        assertEquals(7L, api.actualizada?.first)
        assertEquals(5, api.actualizada?.second?.limiteIntentos)
        assertNull(api.creada) // no intenta crear otra: el servidor respondería 409
    }

    @Test fun una_asignacion_de_otro_objetivo_no_se_confunde_con_la_de_este() = runBlocking {
        val api = ProgresoFalso(existentes = mapOf(1L to listOf(dto(id = 7, objetivoId = 200, cursoId = 1))))

        AsignacionServidor(api).guardar(objetivoId = 100, asignacion = asignacion(), intentos = 3)

        assertEquals(100L, api.creada?.objetivoId)
        assertNull(api.actualizada)
    }

    @Test fun un_curso_que_el_servidor_no_tiene_no_crea_nada() = runBlocking {
        val api = ProgresoFalso()

        val r = AsignacionServidor(api).guardar(objetivoId = 100, asignacion = asignacion(curso = "3° C"), intentos = 3)

        assertTrue(r is ResultadoRemoto.ErrorHttp)
        assertEquals(404, (r as ResultadoRemoto.ErrorHttp).codigo)
        assertNull(api.creada)
    }

    @Test fun el_motivo_del_servidor_llega_a_la_pantalla() = runBlocking {
        val cuerpo = """{"message":"Objetivo no encontrado o todavía no autorizado por UTP"}"""
        val error404 = HttpException(Response.error<Any>(404, cuerpo.toResponseBody("application/json".toMediaType())))

        val r = AsignacionServidor(ProgresoFalso(errorAlCrear = error404))
            .guardar(objetivoId = 100, asignacion = asignacion(), intentos = 3)

        assertTrue(r is ResultadoRemoto.ErrorHttp)
        assertEquals("Objetivo no encontrado o todavía no autorizado por UTP", (r as ResultadoRemoto.ErrorHttp).mensaje)
    }

    @Test fun lista_solo_las_asignaciones_del_objetivo_con_el_nombre_del_curso() = runBlocking {
        val api = ProgresoFalso(
            existentes = mapOf(
                1L to listOf(dto(id = 7, objetivoId = 100, cursoId = 1), dto(id = 8, objetivoId = 200, cursoId = 1)),
                2L to listOf(dto(id = 9, objetivoId = 100, cursoId = 2))
            )
        )

        val r = AsignacionServidor(api).listar(objetivoId = 100, unidadId = 9)

        assertTrue(r is ResultadoRemoto.Ok)
        val lista = (r as ResultadoRemoto.Ok<List<Asignacion>>).valor
        assertEquals(listOf("2° A", "2° B"), lista.map { it.curso })
        assertEquals(listOf(7L, 9L), lista.map { it.id })
        assertTrue(lista.all { it.unidadId == 9L })
    }
}

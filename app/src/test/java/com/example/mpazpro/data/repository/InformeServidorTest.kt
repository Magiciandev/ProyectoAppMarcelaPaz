package com.example.mpazpro.data.repository

import com.example.mpazpro.data.model.Nivel
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
import com.example.mpazpro.data.remote.dto.ResumenObjetivoDto
import java.io.IOException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class InformeServidorTest {

    private fun informe(id: Long, validado: Boolean = false) = InformeDto(
        id = id, estudianteId = 3, cursoId = 1, puntajeGlobal = 75.0, nivel = "EN_VIAS",
        fechaEmision = "2026-11-21T15:00:00Z", validado = validado
    )

    private class ProgresoFalso(
        private val informes: List<InformeDto> = emptyList(),
        private val errorDeRed: Exception? = null
    ) : ProgresoApi {
        private fun nuevo(id: Long) = InformeDto(
            id = id, estudianteId = 3, cursoId = 1, puntajeGlobal = 75.0, nivel = "EN_VIAS",
            fechaEmision = "2026-11-21T15:00:00Z", validado = false
        )

        val detallesPedidos = mutableListOf<Long>()
        var informeSolicitado: InformeRequestDto? = null
        var validacionEnviada: Pair<Long, ValidacionRequestDto>? = null
        var reinicioEnviado: Pair<Long, ReinicioRequestDto>? = null

        override suspend fun resumen(estudianteId: Long): ResumenDto {
            if (errorDeRed != null) throw errorDeRed
            return ResumenDto(
                estudianteId, "Ana",
                listOf(ResumenObjetivoDto(1, 100, 1, 3, 75, "EN_VIAS")), 75, "EN_VIAS"
            )
        }
        override suspend fun informes(cursoId: Long?, estudianteId: Long?): List<InformeDto> = informes
        override suspend fun informe(id: Long): InformeDto {
            detallesPedidos += id
            return informes.first { it.id == id }
        }
        override suspend fun generarInforme(cuerpo: InformeRequestDto): InformeDto {
            informeSolicitado = cuerpo
            return nuevo(99)
        }
        override suspend fun validarInforme(id: Long, cuerpo: ValidacionRequestDto): InformeDto {
            validacionEnviada = id to cuerpo
            return nuevo(id)
        }
        override suspend fun reiniciar(estudianteId: Long, cuerpo: ReinicioRequestDto): ReinicioDto {
            reinicioEnviado = estudianteId to cuerpo
            return ReinicioDto("2026-11-21T15:00:00Z")
        }

        override suspend fun misAsignaciones(): List<AsignacionEstudianteDto> = throw NotImplementedError()
        override suspend fun sincronizar(lote: SyncRequestDto): SyncResponseDto = throw NotImplementedError()
        override suspend fun yo(): EstudianteMeDto = throw NotImplementedError()
        override suspend fun miAvance(): AvanceDto = throw NotImplementedError()
        override suspend fun conexion(): Response<Unit> = throw NotImplementedError()
        override suspend fun cursos(): List<CursoDto> = throw NotImplementedError()
        override suspend fun estudiantesDelCurso(cursoId: Long): List<EstudianteCursoDto> = throw NotImplementedError()
        override suspend fun crearAsignacion(cuerpo: AsignacionRequestDto): AsignacionDto = throw NotImplementedError()
        override suspend fun asignaciones(cursoId: Long): List<AsignacionDto> = throw NotImplementedError()
        override suspend fun actualizarAsignacion(id: Long, cuerpo: AsignacionRequestDto): AsignacionDto = throw NotImplementedError()
        override suspend fun intentos(estudianteId: Long, objetivoId: Long?): List<IntentoDto> = throw NotImplementedError()
    }

    @Test fun carga_el_resumen_y_el_detalle_del_informe_mas_reciente() = runBlocking {
        val api = ProgresoFalso(informes = listOf(informe(8), informe(5)))
        val r = InformeServidor(api) { mapOf(1L to "LEN-OA01") }.cargar(3)
        val ficha = (r as ResultadoRemoto.Ok).valor
        assertEquals("Ana", ficha.nombre)
        assertEquals("LEN-OA01", ficha.objetivos[0].nombre)
        assertEquals(Nivel.EN_VIAS, ficha.nivelGeneral)
        assertEquals(8L, ficha.informe?.id)
        assertEquals(listOf(8L), api.detallesPedidos)
    }

    @Test fun sin_informes_la_ficha_no_trae_informe() = runBlocking {
        val api = ProgresoFalso()
        val ficha = (InformeServidor(api).cargar(3) as ResultadoRemoto.Ok).valor
        assertNull(ficha.informe)
        assertTrue(api.detallesPedidos.isEmpty())
    }

    @Test fun sin_red_se_avisa_sin_excepcion() = runBlocking {
        val r = InformeServidor(ProgresoFalso(errorDeRed = IOException("sin red"))).cargar(3)
        assertTrue(r is ResultadoRemoto.SinConexion)
    }

    @Test fun generar_pide_el_informe_de_ese_estudiante_sin_filtrar_objetivo() = runBlocking {
        val api = ProgresoFalso()
        InformeServidor(api).generar(3)
        assertEquals(3L, api.informeSolicitado?.estudianteId)
        assertNull(api.informeSolicitado?.objetivoId)
    }

    @Test fun guardar_la_observacion_no_valida_el_informe() = runBlocking {
        val api = ProgresoFalso()
        InformeServidor(api).guardarObservacion(8, "  Necesita refuerzo  ")
        val (id, cuerpo) = api.validacionEnviada!!
        assertEquals(8L, id)
        assertEquals("Necesita refuerzo", cuerpo.observacion)
        assertNull(cuerpo.validar)
    }

    @Test fun validar_marca_el_informe_como_validado() = runBlocking {
        val api = ProgresoFalso()
        InformeServidor(api).validar(8, null)
        val (_, cuerpo) = api.validacionEnviada!!
        assertEquals(true, cuerpo.validar)
        assertNull(cuerpo.observacion)
    }

    @Test fun reiniciar_afecta_a_todos_los_objetivos_del_estudiante() = runBlocking {
        val api = ProgresoFalso()
        InformeServidor(api).reiniciar(3, "Nuevo semestre")
        val (id, cuerpo) = api.reinicioEnviado!!
        assertEquals(3L, id)
        assertNull(cuerpo.objetivoId)
        assertEquals("Nuevo semestre", cuerpo.motivo)
    }
}

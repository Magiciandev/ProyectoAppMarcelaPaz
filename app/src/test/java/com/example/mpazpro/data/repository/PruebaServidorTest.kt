package com.example.mpazpro.data.repository

import com.example.mpazpro.data.model.OpcionPrueba
import com.example.mpazpro.data.model.PreguntaPrueba
import com.example.mpazpro.data.model.PruebaParaRendir
import com.example.mpazpro.data.model.TipoPreguntaPrueba
import com.example.mpazpro.data.remote.ResultadoRemoto
import com.example.mpazpro.data.remote.api.EvaluacionesApi
import com.example.mpazpro.data.remote.api.ProgresoApi
import com.example.mpazpro.data.remote.dto.ActividadDto
import com.example.mpazpro.data.remote.dto.ActividadRequestDto
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
import com.example.mpazpro.data.remote.dto.OpcionDto
import com.example.mpazpro.data.remote.dto.PaqueteEvaluacionDto
import com.example.mpazpro.data.remote.dto.PreguntaDto
import com.example.mpazpro.data.remote.dto.PreguntaEnActividadDto
import com.example.mpazpro.data.remote.dto.PreguntaEnPruebaDto
import com.example.mpazpro.data.remote.dto.PreguntaRequestDto
import com.example.mpazpro.data.remote.dto.PruebaDto
import com.example.mpazpro.data.remote.dto.PruebaRequestDto
import com.example.mpazpro.data.remote.dto.ReinicioDto
import com.example.mpazpro.data.remote.dto.ReinicioRequestDto
import com.example.mpazpro.data.remote.dto.ResultadoIntentoDto
import com.example.mpazpro.data.remote.dto.ResumenDto
import com.example.mpazpro.data.remote.dto.SyncRequestDto
import com.example.mpazpro.data.remote.dto.SyncResponseDto
import com.example.mpazpro.data.remote.dto.ValidacionRequestDto
import java.io.IOException
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class PruebaServidorTest {

    // ---- ayudas para armar paquetes como los entrega el servidor
    private fun opcion(texto: String, orden: Int, correcta: Boolean? = null, posicion: Int? = null, par: String? = null) =
        OpcionDto(texto = texto, esCorrecta = correcta, posicionCorrecta = posicion, parClave = par, orden = orden)

    private fun pregunta(id: Long, tipo: String, opciones: List<OpcionDto>) =
        PreguntaDto(id = id, objetivoId = 1, tipo = tipo, enunciado = "Pregunta $id", explicacion = "Explica $id", opciones = opciones)

    private val p1 = pregunta(1, "SELECCION", listOf(opcion("saca", 2, false), opcion("casa", 1, true), opcion("cosa", 3, false)))
    private val p2 = pregunta(2, "COMPLETACION", listOf(opcion("duerme", 1, true), opcion("duermen", 2, false)))
    private val p3 = pregunta(3, "SECUENCIA", listOf(opcion("pa", 1, posicion = 2), opcion("so", 2, posicion = 1)))
    // p4 es una asociación mal armada (sin parClave); p5 está bien armada.
    private val p4 = pregunta(4, "ASOCIACION", listOf(opcion("gato", 1), opcion("Dice miau", 2)))
    private val p5 = pregunta(5, "ASOCIACION", listOf(opcion("gato", 1, par = "g"), opcion("Dice guau", 2, par = "p"), opcion("perro", 3, par = "p"), opcion("Dice miau", 4, par = "g")))

    private fun paquete(preguntas: List<PreguntaDto>, enPrueba: List<PreguntaEnPruebaDto>) = PaqueteEvaluacionDto(
        objetivoId = 1,
        preguntas = preguntas,
        prueba = PruebaDto(10, 1, "Prueba final", null, 3, "PUBLICADO", enPrueba)
    )

    private val enPruebaNormal = listOf(
        PreguntaEnPruebaDto(3, 3, 2.0), PreguntaEnPruebaDto(1, 1, 1.0), PreguntaEnPruebaDto(2, 2, null)
    )

    // ---- mapeo del paquete
    @Test fun las_preguntas_quedan_en_el_orden_de_la_prueba_con_su_puntaje() {
        val preguntas = MapeoPrueba.aPreguntas(paquete(listOf(p1, p2, p3), enPruebaNormal))!!

        assertEquals(listOf(1L, 2L, 3L), preguntas.map { it.id })
        assertEquals(listOf(1.0, 1.0, 2.0), preguntas.map { it.puntaje }) // sin puntaje del servidor vale 1
        assertEquals(TipoPreguntaPrueba.SECUENCIA, preguntas[2].tipo)
    }

    @Test fun las_opciones_se_muestran_en_el_orden_que_dice_el_servidor() {
        val seleccion = MapeoPrueba.aPreguntas(paquete(listOf(p1), listOf(PreguntaEnPruebaDto(1, 1, 1.0))))!!.single()

        assertEquals(listOf("casa", "saca", "cosa"), seleccion.opciones.map { it.texto })
        assertTrue(seleccion.opciones[0].esCorrecta)
    }

    @Test fun una_asociacion_bien_armada_se_puede_rendir_con_sus_pares() {
        val r = MapeoPrueba.aPreguntas(paquete(listOf(p1, p5), listOf(PreguntaEnPruebaDto(1, 1, 1.0), PreguntaEnPruebaDto(5, 2, 2.0))))!!

        assertEquals(TipoPreguntaPrueba.ASOCIACION, r[1].tipo)
        assertEquals(listOf("g", "p", "p", "g"), r[1].opciones.map { it.parClave })
        assertTrue(r[1].esCorrecta(listOf(0, 3, 1, 2)))
    }

    @Test fun una_asociacion_con_una_opcion_sin_pareja_no_se_puede_rendir() {
        val r = MapeoPrueba.aPreguntas(paquete(listOf(p1, p4), listOf(PreguntaEnPruebaDto(1, 1, 1.0), PreguntaEnPruebaDto(4, 2, 1.0))))

        assertNull(r)
    }

    @Test fun una_pregunta_que_falta_en_el_paquete_tampoco_se_puede_rendir() {
        assertNull(MapeoPrueba.aPreguntas(paquete(listOf(p1), listOf(PreguntaEnPruebaDto(1, 1, 1.0), PreguntaEnPruebaDto(99, 2, 1.0)))))
    }

    // ---- armado del intento
    private val prueba = PruebaParaRendir(
        pruebaId = 10, objetivoId = 1, titulo = "Prueba final", limiteIntentos = 3, intentosUsados = 1,
        preguntas = MapeoPrueba.aPreguntas(paquete(listOf(p1, p2, p3), enPruebaNormal))!!
    )

    @Test fun el_intento_lleva_los_puntajes_y_las_respuestas() {
        var n = 0
        // Acierta la selección (casa = opción 0), se equivoca en la completación y ordena bien la secuencia (so, pa).
        val lote = ConstructorIntento.armar(
            prueba, numeroIntento = 2, respuestas = listOf(listOf(0), listOf(1), listOf(1, 0)),
            fechaHora = Instant.parse("2026-11-20T15:30:00Z"), nuevoUuid = { "uuid-${n++}" }
        )

        val intento = lote.intentos.single()
        assertEquals(10L, intento.pruebaId)
        assertEquals(1L, intento.objetivoId)
        assertEquals(2, intento.numeroIntento)
        assertEquals(3.0, intento.puntajeObtenido, 0.0) // 1 + 0 + 2
        assertEquals(4.0, intento.puntajeMaximo, 0.0)
        assertEquals("2026-11-20T15:30:00Z", intento.fechaHora)
        assertEquals(listOf(true, false, true), intento.respuestas.map { it.esCorrecta })
        assertEquals(listOf(1.0, 0.0, 2.0), intento.respuestas.map { it.puntajeObtenido })
        assertEquals("""{"opcion":"casa"}""", intento.respuestas[0].respuesta)
        assertEquals("""{"orden":["so","pa"]}""", intento.respuestas[2].respuesta)
        assertEquals(listOf(1L, 2L, 3L), intento.respuestas.map { it.preguntaId })
    }

    @Test fun cada_intento_y_respuesta_lleva_un_uuid_distinto_que_cabe_en_el_servidor() {
        val lote = ConstructorIntento.armar(
            prueba, 2, listOf(listOf(0), listOf(0), listOf(1, 0)), Instant.now(),
            nuevoUuid = { java.util.UUID.randomUUID().toString() }
        )

        val intento = lote.intentos.single()
        val uuids = listOf(intento.uuid) + intento.respuestas.map { it.uuid }
        assertEquals(uuids.size, uuids.toSet().size)
        assertTrue(uuids.all { it.length <= 36 }) // el servidor rechaza uuid de más de 36 caracteres
    }

    // ---- llamadas al servidor
    private class EvaluacionesFalso(private val paquete: PaqueteEvaluacionDto) : EvaluacionesApi {
        override suspend fun paquete(objetivoId: Long): PaqueteEvaluacionDto = paquete
        override suspend fun preguntas(objetivoId: Long): List<PreguntaDto> = throw NotImplementedError()
        override suspend fun crearPregunta(cuerpo: PreguntaRequestDto): PreguntaDto = throw NotImplementedError()
        override suspend fun actualizarPregunta(id: Long, cuerpo: PreguntaRequestDto): PreguntaDto = throw NotImplementedError()
        override suspend fun eliminarPregunta(id: Long): Response<Unit> = throw NotImplementedError()
        override suspend fun actividades(objetivoId: Long): List<ActividadDto> = throw NotImplementedError()
        override suspend fun crearActividad(cuerpo: ActividadRequestDto): ActividadDto = throw NotImplementedError()
        override suspend fun actualizarActividad(id: Long, cuerpo: ActividadRequestDto): ActividadDto = throw NotImplementedError()
        override suspend fun preguntasDeActividad(id: Long, preguntas: List<PreguntaEnActividadDto>): List<PreguntaEnActividadDto> =
            throw NotImplementedError()
        override suspend fun prueba(objetivoId: Long): PruebaDto = throw NotImplementedError()
        override suspend fun crearPrueba(cuerpo: PruebaRequestDto): PruebaDto = throw NotImplementedError()
        override suspend fun actualizarPrueba(id: Long, cuerpo: PruebaRequestDto): PruebaDto = throw NotImplementedError()
        override suspend fun preguntasDePrueba(id: Long, preguntas: List<PreguntaEnPruebaDto>): List<PreguntaEnPruebaDto> =
            throw NotImplementedError()
    }

    private class ProgresoFalso(
        private val propias: List<AsignacionEstudianteDto>,
        private val respuestaSync: SyncResponseDto = SyncResponseDto(intentos = listOf(ResultadoIntentoDto("u", "ACEPTADO"))),
        private val errorDeRed: Exception? = null
    ) : ProgresoApi {
        var enviado: SyncRequestDto? = null

        override suspend fun misAsignaciones(): List<AsignacionEstudianteDto> = propias
        override suspend fun sincronizar(lote: SyncRequestDto): SyncResponseDto {
            if (errorDeRed != null) throw errorDeRed
            enviado = lote
            return respuestaSync
        }

        override suspend fun yo(): EstudianteMeDto = throw NotImplementedError()
        override suspend fun miAvance(): AvanceDto = throw NotImplementedError()
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

    private fun asignada(objetivoId: Long = 1, limite: Int = 3, usados: Long = 1) = AsignacionEstudianteDto(
        asignacionId = 5, objetivoId = objetivoId, fechaDesde = "2026-11-01", fechaHasta = "2026-12-31",
        limiteIntentos = limite, intentosUsados = usados
    )

    // Hoy fijo (20-11-2026) para que las pruebas no dependan del reloj real.
    private val HOY = LocalDate.of(2026, 11, 20)

    private fun servidorCon(paquete: PaqueteEvaluacionDto, asignaciones: List<AsignacionEstudianteDto>, hoy: LocalDate = HOY) =
        PruebaServidor(EvaluacionesFalso(paquete), ProgresoFalso(asignaciones), hoy = { hoy })

    @Test fun carga_la_prueba_con_el_limite_y_los_intentos_del_servidor() = runBlocking {
        val servidor = servidorCon(paquete(listOf(p1, p2, p3), enPruebaNormal), listOf(asignada()))

        val r = servidor.cargar(1)

        assertTrue(r is ResultadoRemoto.Ok)
        val carga = (r as ResultadoRemoto.Ok<CargaPrueba>).valor
        assertTrue(carga is CargaPrueba.Lista)
        val cargada = (carga as CargaPrueba.Lista).prueba
        assertEquals(10L, cargada.pruebaId)
        assertEquals(3, cargada.limiteIntentos)
        assertEquals(1, cargada.intentosUsados)
        assertEquals(3, cargada.preguntas.size)
    }

    @Test fun sin_asignacion_no_se_puede_rendir() = runBlocking {
        val servidor = servidorCon(paquete(listOf(p1), enPruebaNormal), listOf(asignada(objetivoId = 99)))

        val r = servidor.cargar(1)

        assertTrue((r as ResultadoRemoto.Ok<CargaPrueba>).valor is CargaPrueba.SinAsignacion)
    }

    @Test fun sin_prueba_o_con_asociacion_mal_armada_se_avisa() = runBlocking {
        val sinPrueba = PaqueteEvaluacionDto(objetivoId = 1, preguntas = listOf(p1), prueba = null)
        val conAsociacion = paquete(listOf(p1, p4), listOf(PreguntaEnPruebaDto(1, 1, 1.0), PreguntaEnPruebaDto(4, 2, 1.0)))

        val a = servidorCon(sinPrueba, listOf(asignada())).cargar(1)
        val b = servidorCon(conAsociacion, listOf(asignada())).cargar(1)

        assertTrue((a as ResultadoRemoto.Ok<CargaPrueba>).valor is CargaPrueba.SinPrueba)
        assertTrue((b as ResultadoRemoto.Ok<CargaPrueba>).valor is CargaPrueba.NoSoportada)
    }

    @Test fun el_intento_se_envia_a_sync_y_vuelve_su_resultado() = runBlocking {
        val progreso = ProgresoFalso(listOf(asignada()))
        val lote = ConstructorIntento.armar(prueba, 2, listOf(listOf(0), listOf(0), listOf(1, 0)), Instant.now()) { java.util.UUID.randomUUID().toString() }

        val r = PruebaServidor(EvaluacionesFalso(paquete(listOf(p1), enPruebaNormal)), progreso).enviar(lote)

        assertTrue(r is ResultadoRemoto.Ok)
        assertEquals("ACEPTADO", (r as ResultadoRemoto.Ok<SyncResponseDto>).valor.intentos.single().resultado)
        assertNotNull(progreso.enviado)
        assertEquals(lote, progreso.enviado)
    }

    // ---- fuera de fecha: se avisa al empezar, no al final
    private fun asignadaConFechas(desde: String, hasta: String, habilitacion: String? = null, rendicion: String? = null) =
        AsignacionEstudianteDto(
            asignacionId = 5, objetivoId = 1, fechaDesde = desde, fechaHasta = hasta, limiteIntentos = 3,
            fechaHabilitacion = habilitacion, fechaRendicion = rendicion, intentosUsados = 0
        )

    private suspend fun cargaCon(asignacion: AsignacionEstudianteDto): CargaPrueba =
        (servidorCon(paquete(listOf(p1), enPruebaNormal), listOf(asignacion)).cargar(1) as ResultadoRemoto.Ok<CargaPrueba>).valor

    @Test fun antes_de_la_habilitacion_de_la_prueba_se_avisa_cuando_se_abre() = runBlocking {
        // La asignación ya empezó, pero la docente habilitó la prueba para el 25-11.
        val r = cargaCon(asignadaConFechas("2026-11-01", "2026-12-31", habilitacion = "2026-11-25"))

        assertEquals(CargaPrueba.AunNoDisponible(LocalDate.of(2026, 11, 25)), r)
    }

    @Test fun despues_de_la_rendicion_de_la_prueba_se_avisa_que_ya_cerro() = runBlocking {
        // La asignación sigue vigente hasta fin de año, pero la prueba se podía rendir hasta el 15-11.
        val r = cargaCon(asignadaConFechas("2026-11-01", "2026-12-31", rendicion = "2026-11-15"))

        assertEquals(CargaPrueba.YaCerrada(LocalDate.of(2026, 11, 15)), r)
    }

    @Test fun el_ultimo_dia_de_la_ventana_todavia_se_puede_rendir() = runBlocking {
        val r = cargaCon(asignadaConFechas("2026-11-01", "2026-12-31", rendicion = "2026-11-20"))

        assertTrue(r is CargaPrueba.Lista)
    }

    @Test fun una_fecha_ilegible_no_bloquea_y_decide_el_servidor() = runBlocking {
        val r = cargaCon(asignadaConFechas("no-es-fecha", "2026-12-31"))

        assertTrue(r is CargaPrueba.Lista)
    }

    @Test fun sin_red_el_envio_informa_sin_conexion_y_el_lote_se_puede_reintentar() = runBlocking {
        val lote = ConstructorIntento.armar(prueba, 2, listOf(listOf(0), listOf(0), listOf(1, 0)), Instant.now()) { java.util.UUID.randomUUID().toString() }

        val r = PruebaServidor(
            EvaluacionesFalso(paquete(listOf(p1), enPruebaNormal)),
            ProgresoFalso(listOf(asignada()), errorDeRed = IOException("sin red"))
        ).enviar(lote)

        assertTrue(r is ResultadoRemoto.SinConexion)
    }
}

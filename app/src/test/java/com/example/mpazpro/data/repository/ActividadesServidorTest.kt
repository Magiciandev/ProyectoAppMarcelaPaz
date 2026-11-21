package com.example.mpazpro.data.repository

import com.example.mpazpro.data.remote.ResultadoRemoto
import com.example.mpazpro.data.remote.api.EvaluacionesApi
import com.example.mpazpro.data.remote.dto.ActividadDto
import com.example.mpazpro.data.remote.dto.ActividadRequestDto
import com.example.mpazpro.data.remote.dto.OpcionDto
import com.example.mpazpro.data.remote.dto.PaqueteEvaluacionDto
import com.example.mpazpro.data.remote.dto.PreguntaDto
import com.example.mpazpro.data.remote.dto.PreguntaEnActividadDto
import com.example.mpazpro.data.remote.dto.PreguntaEnPruebaDto
import com.example.mpazpro.data.remote.dto.PreguntaRequestDto
import com.example.mpazpro.data.remote.dto.PruebaDto
import com.example.mpazpro.data.remote.dto.PruebaRequestDto
import java.io.IOException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

// Las actividades se practican con la caché: con red se guarda el paquete y sin red se usa el guardado.
class ActividadesServidorTest {

    private class EvaluacionesFalso(private val paquete: PaqueteEvaluacionDto, private val sinRed: Boolean = false) : EvaluacionesApi {
        override suspend fun paquete(objetivoId: Long): PaqueteEvaluacionDto {
            if (sinRed) throw IOException("sin red")
            return paquete
        }
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

    // Un paquete con una actividad de dificultad 2 que tiene una pregunta de selección.
    private val paquete = PaqueteEvaluacionDto(
        objetivoId = 1,
        actividades = listOf(
            ActividadDto(7, 1, "Practica 1", "SELECCION", 2, 1, "PUBLICADO", listOf(PreguntaEnActividadDto(1, 1)))
        ),
        preguntas = listOf(
            PreguntaDto(
                id = 1, objetivoId = 1, tipo = "SELECCION", enunciado = "¿Cuál?", explicacion = "Porque sí",
                opciones = listOf(
                    OpcionDto(texto = "casa", esCorrecta = true, orden = 1),
                    OpcionDto(texto = "cosa", esCorrecta = false, orden = 2)
                )
            )
        )
    )

    @Test fun con_conexion_devuelve_las_actividades_y_guarda_el_paquete() = runBlocking {
        val cache = CacheEvaluacionFalsa()

        val r = ActividadesServidor(EvaluacionesFalso(paquete), cache).cargar(1)

        val actividades = (r as ResultadoRemoto.Ok<List<ActividadPracticable>>).valor
        assertEquals(listOf(7L), actividades.map { it.id })
        assertEquals(paquete, cache.paquetes[1L])
    }

    @Test fun sin_conexion_usa_el_paquete_guardado() = runBlocking {
        val cache = CacheEvaluacionFalsa()
        cache.guardarPaquete(paquete)

        val r = ActividadesServidor(EvaluacionesFalso(paquete, sinRed = true), cache).cargar(1)

        val actividades = (r as ResultadoRemoto.Ok<List<ActividadPracticable>>).valor
        assertEquals(listOf(7L), actividades.map { it.id })
        assertEquals(2, actividades.single().dificultad)
    }

    @Test fun sin_conexion_y_sin_paquete_guardado_informa_sin_conexion() = runBlocking {
        val r = ActividadesServidor(EvaluacionesFalso(paquete, sinRed = true), CacheEvaluacionFalsa()).cargar(1)

        assertTrue(r is ResultadoRemoto.SinConexion)
    }
}

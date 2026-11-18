package com.example.mpazpro.data.repository

import com.example.mpazpro.data.model.Asignaturas
import com.example.mpazpro.data.model.Unidad
import com.example.mpazpro.data.remote.ResultadoRemoto
import com.example.mpazpro.data.remote.api.ContenidosApi
import com.example.mpazpro.data.remote.dto.AsignaturaDto
import com.example.mpazpro.data.remote.dto.ContenidoDto
import com.example.mpazpro.data.remote.dto.ContenidoRequestDto
import com.example.mpazpro.data.remote.dto.ObjetivoDto
import com.example.mpazpro.data.remote.dto.ObjetivoRequestDto
import com.example.mpazpro.data.remote.dto.PaqueteContenidosDto
import com.example.mpazpro.data.remote.dto.UnidadDto
import com.example.mpazpro.data.remote.dto.UnidadRequestDto
import java.io.IOException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class UnidadServidorTest {

    /** API que anota lo que recibe. Si se pide, falla con falta de red en el paso indicado. */
    private class ApiFalsa(private val fallaEn: String? = null) : ContenidosApi {
        val pasos = mutableListOf<String>()
        var unidadRecibida: UnidadRequestDto? = null
        var objetivoRecibido: ObjetivoRequestDto? = null
        var contenidoRecibido: ContenidoRequestDto? = null

        private fun paso(nombre: String) {
            pasos.add(nombre)
            if (nombre == fallaEn) throw IOException("sin red")
        }

        override suspend fun asignaturas(): List<AsignaturaDto> {
            paso("asignaturas")
            return listOf(
                AsignaturaDto(1, "Lenguaje y Comunicación", "2° Básico"),
                AsignaturaDto(2, "Matemática", "2° Básico")
            )
        }

        override suspend fun crearUnidad(cuerpo: UnidadRequestDto): UnidadDto {
            paso("unidad")
            unidadRecibida = cuerpo
            return UnidadDto(10, cuerpo.asignaturaId ?: 0, cuerpo.titulo, 1, "BORRADOR")
        }

        override suspend fun crearObjetivo(unidadId: Long, cuerpo: ObjetivoRequestDto): ObjetivoDto {
            paso("objetivo")
            objetivoRecibido = cuerpo
            return ObjetivoDto(100, unidadId, cuerpo.codigo, cuerpo.descripcion, null, null, 1, "BORRADOR")
        }

        override suspend fun crearContenido(objetivoId: Long, cuerpo: ContenidoRequestDto): ContenidoDto {
            paso("contenido")
            contenidoRecibido = cuerpo
            return ContenidoDto(
                1000, objetivoId, cuerpo.titulo, cuerpo.explicacion, null, null, null, null, 1,
                cuerpo.responsableOrigen, cuerpo.licencia
            )
        }

        // Lo que estas pruebas no usan falla si alguien lo llama.
        override suspend fun unidades(asignaturaId: Long?): List<UnidadDto> = throw NotImplementedError()
        override suspend fun actualizarUnidad(id: Long, cuerpo: UnidadRequestDto): UnidadDto = throw NotImplementedError()
        override suspend fun objetivos(unidadId: Long?, ids: String?): List<ObjetivoDto> = throw NotImplementedError()
        override suspend fun objetivo(id: Long): ObjetivoDto = throw NotImplementedError()
        override suspend fun paquete(id: Long): PaqueteContenidosDto = throw NotImplementedError()
        override suspend fun actualizarObjetivo(id: Long, cuerpo: ObjetivoRequestDto): ObjetivoDto = throw NotImplementedError()
        override suspend fun actualizarContenido(id: Long, cuerpo: ContenidoRequestDto): ContenidoDto = throw NotImplementedError()
        override suspend fun eliminarContenido(id: Long): Response<Unit> = throw NotImplementedError()
    }

    private fun unidad(asignatura: String = Asignaturas.LENGUAJE) = Unidad(
        titulo = "Unidad 4 · Poemas",
        objetivoAprendizaje = "OA05",
        asignatura = asignatura,
        contenido = "Un poema usa versos y rimas para contar algo."
    )

    @Test fun `crea unidad objetivo y contenido en orden y devuelve el id del objetivo`() = runBlocking {
        val api = ApiFalsa()

        val r = UnidadServidor(api).crear(unidad())

        assertTrue(r is ResultadoRemoto.Ok)
        assertEquals(100L, (r as ResultadoRemoto.Ok<Long>).valor)
        assertEquals(listOf("asignaturas", "unidad", "objetivo", "contenido"), api.pasos)
    }

    @Test fun `manda al servidor los datos traducidos`() = runBlocking {
        val api = ApiFalsa()

        UnidadServidor(api).crear(unidad())

        assertEquals(1L, api.unidadRecibida?.asignaturaId) // «Lenguaje» es «Lenguaje y Comunicación» en el servidor
        assertEquals("Unidad 4 · Poemas", api.unidadRecibida?.titulo)
        assertEquals("LEN-OA05", api.objetivoRecibido?.codigo)
        assertEquals("Unidad 4 · Poemas", api.objetivoRecibido?.descripcion)
        assertEquals("Un poema usa versos y rimas para contar algo.", api.contenidoRecibido?.explicacion)
        assertEquals(MapeoUnidad.RESPONSABLE, api.contenidoRecibido?.responsableOrigen)
        assertEquals(MapeoUnidad.LICENCIA, api.contenidoRecibido?.licencia)
    }

    @Test fun `si falla al crear el objetivo no crea el contenido`() = runBlocking {
        val api = ApiFalsa(fallaEn = "objetivo")

        val r = UnidadServidor(api).crear(unidad())

        assertTrue(r is ResultadoRemoto.SinConexion)
        assertEquals(listOf("asignaturas", "unidad", "objetivo"), api.pasos)
    }

    @Test fun `una asignatura que el servidor no tiene no crea nada`() = runBlocking {
        val api = ApiFalsa()

        val r = UnidadServidor(api).crear(unidad(asignatura = Asignaturas.CIENCIAS))

        assertTrue(r is ResultadoRemoto.ErrorHttp)
        assertEquals(404, (r as ResultadoRemoto.ErrorHttp).codigo)
        assertEquals(listOf("asignaturas"), api.pasos)
    }

    @Test fun `sin conexion desde el primer paso se informa y no se crea nada`() = runBlocking {
        val api = ApiFalsa(fallaEn = "asignaturas")

        val r = UnidadServidor(api).crear(unidad())

        assertTrue(r is ResultadoRemoto.SinConexion)
        assertNull(api.unidadRecibida)
    }

    @Test fun `las cuatro asignaturas de la app se reconocen por el inicio del nombre del servidor`() {
        val remotas = listOf(
            AsignaturaDto(1, "Lenguaje y Comunicación", "2° Básico"),
            AsignaturaDto(2, "Matemática", "2° Básico"),
            AsignaturaDto(3, "Ciencias Naturales", "2° Básico"),
            AsignaturaDto(4, "Historia, Geografía y Ciencias Sociales", "2° Básico")
        )

        assertEquals(1L, MapeoUnidad.idAsignatura(Asignaturas.LENGUAJE, remotas))
        assertEquals(2L, MapeoUnidad.idAsignatura(Asignaturas.MATEMATICA, remotas))
        assertEquals(3L, MapeoUnidad.idAsignatura(Asignaturas.CIENCIAS, remotas))
        assertEquals(4L, MapeoUnidad.idAsignatura(Asignaturas.HISTORIA, remotas))
        assertNull(MapeoUnidad.idAsignatura("Música", remotas))
    }

    @Test fun `el codigo del objetivo lleva el prefijo de la asignatura`() {
        assertEquals("LEN-OA05", MapeoUnidad.codigoObjetivo(Asignaturas.LENGUAJE, "oa05"))
        assertEquals("MAT-OA01", MapeoUnidad.codigoObjetivo(Asignaturas.MATEMATICA, "OA01"))
        assertEquals("CIE-OA04", MapeoUnidad.codigoObjetivo(Asignaturas.CIENCIAS, "OA04"))
        assertEquals("HIS-OA12", MapeoUnidad.codigoObjetivo(Asignaturas.HISTORIA, "OA12"))
        assertEquals("OA03", MapeoUnidad.codigoObjetivo("Otra", "OA03"))
    }
}

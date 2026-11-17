package com.example.mpazpro.data.repository

import com.example.mpazpro.data.local.CatalogoCacheDao
import com.example.mpazpro.data.model.CacheAsignatura
import com.example.mpazpro.data.model.CacheContenido
import com.example.mpazpro.data.model.CacheObjetivo
import com.example.mpazpro.data.model.CacheUnidad
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
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class CatalogoRepositoryTest {

    /** DAO en memoria. Los métodos con @Transaction (reemplazar, leer, vaciar) se heredan tal cual. */
    private class DaoEnMemoria : CatalogoCacheDao() {
        var asignaturas = listOf<CacheAsignatura>()
        var unidades = listOf<CacheUnidad>()
        var objetivos = listOf<CacheObjetivo>()
        var contenidos = listOf<CacheContenido>()

        override suspend fun asignaturas() = asignaturas
        override suspend fun unidades() = unidades
        override suspend fun objetivos() = objetivos
        override suspend fun contenidos() = contenidos
        override suspend fun insertarAsignaturas(lista: List<CacheAsignatura>) { asignaturas = lista }
        override suspend fun insertarUnidades(lista: List<CacheUnidad>) { unidades = lista }
        override suspend fun insertarObjetivos(lista: List<CacheObjetivo>) { objetivos = lista }
        override suspend fun insertarContenidos(lista: List<CacheContenido>) { contenidos = lista }
        override suspend fun borrarAsignaturas() { asignaturas = emptyList() }
        override suspend fun borrarUnidades() { unidades = emptyList() }
        override suspend fun borrarObjetivos() { objetivos = emptyList() }
        override suspend fun borrarContenidos() { contenidos = emptyList() }
    }

    /** API de contenidos con datos fijos; lo que no se usa en estas pruebas falla si se llama. */
    private class ApiFalsa(private val fallo: Throwable? = null) : ContenidosApi {
        var llamadas = 0

        private val unidad = UnidadDto(10, 1, "Mis primeras lecturas", 1, "PUBLICADO")
        private val objetivo = ObjetivoDto(100, 10, "LEN-OA01", "Leer palabras", "Lectura", null, 1, "PUBLICADO")
        private val contenido = ContenidoDto(1000, 100, "Sílabas", "Texto", null, null, null, null, 1, "Equipo", "Propio")

        private fun contar() {
            llamadas++
            fallo?.let { throw it }
        }

        override suspend fun asignaturas(): List<AsignaturaDto> { contar(); return listOf(AsignaturaDto(1, "Lenguaje", "2° Básico")) }
        override suspend fun unidades(asignaturaId: Long?): List<UnidadDto> { contar(); return listOf(unidad) }
        override suspend fun objetivos(unidadId: Long?, ids: String?): List<ObjetivoDto> {
            contar(); return if (unidadId == 10L) listOf(objetivo) else emptyList()
        }
        override suspend fun paquete(id: Long): PaqueteContenidosDto { contar(); return PaqueteContenidosDto(objetivo, listOf(contenido)) }

        override suspend fun crearUnidad(cuerpo: UnidadRequestDto): UnidadDto = throw NotImplementedError()
        override suspend fun actualizarUnidad(id: Long, cuerpo: UnidadRequestDto): UnidadDto = throw NotImplementedError()
        override suspend fun objetivo(id: Long): ObjetivoDto = throw NotImplementedError()
        override suspend fun crearObjetivo(unidadId: Long, cuerpo: ObjetivoRequestDto): ObjetivoDto = throw NotImplementedError()
        override suspend fun actualizarObjetivo(id: Long, cuerpo: ObjetivoRequestDto): ObjetivoDto = throw NotImplementedError()
        override suspend fun crearContenido(objetivoId: Long, cuerpo: ContenidoRequestDto): ContenidoDto = throw NotImplementedError()
        override suspend fun actualizarContenido(id: Long, cuerpo: ContenidoRequestDto): ContenidoDto = throw NotImplementedError()
        override suspend fun eliminarContenido(id: Long): Response<Unit> = throw NotImplementedError()
    }

    @Test fun `refrescar descarga todo y lo deja en la cache`() = runBlocking {
        val dao = DaoEnMemoria()
        val repo = CatalogoRepository(ApiFalsa(), dao)

        val r = repo.refrescar()

        assertTrue(r is ResultadoRemoto.Ok)
        val cache = repo.leerCache()
        assertEquals(listOf("Lenguaje"), cache.asignaturas.map { it.nombre })
        assertEquals(listOf("Mis primeras lecturas"), cache.unidades.map { it.titulo })
        assertEquals(listOf("LEN-OA01"), cache.objetivos.map { it.codigo })
        assertEquals(listOf("Sílabas"), cache.contenidos.map { it.titulo })
        assertEquals(100L, cache.contenidos[0].objetivoId)
    }

    @Test fun `si no hay conexion la cache anterior queda intacta`() = runBlocking {
        val dao = DaoEnMemoria()
        CatalogoRepository(ApiFalsa(), dao).refrescar()

        val r = CatalogoRepository(ApiFalsa(IOException("sin red")), dao).refrescar()

        assertTrue(r is ResultadoRemoto.SinConexion)
        assertEquals(1, dao.leer().objetivos.size)
        assertEquals(1, dao.leer().contenidos.size)
    }

    @Test fun `un error del servidor tampoco borra la cache`() = runBlocking {
        val dao = DaoEnMemoria()
        CatalogoRepository(ApiFalsa(), dao).refrescar()
        val error500 = retrofit2.HttpException(Response.error<Any>(500, "".toResponseBody("application/json".toMediaType())))

        val r = CatalogoRepository(ApiFalsa(error500), dao).refrescar()

        assertTrue(r is ResultadoRemoto.ErrorHttp)
        assertEquals(1, dao.leer().unidades.size)
    }

    @Test fun `vaciar borra todo el catalogo guardado`() = runBlocking {
        val dao = DaoEnMemoria()
        val repo = CatalogoRepository(ApiFalsa(), dao)
        repo.refrescar()

        repo.vaciarCache()

        assertTrue(repo.leerCache().estaVacio())
        assertTrue(dao.leer().contenidos.isEmpty())
    }
}

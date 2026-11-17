package com.example.mpazpro.data.repository

import com.example.mpazpro.data.local.CatalogoCacheDao
import com.example.mpazpro.data.model.CacheAsignatura
import com.example.mpazpro.data.model.CacheContenido
import com.example.mpazpro.data.model.CacheObjetivo
import com.example.mpazpro.data.model.CacheUnidad
import com.example.mpazpro.data.model.CatalogoCache
import com.example.mpazpro.data.remote.ResultadoRemoto
import com.example.mpazpro.data.remote.api.ContenidosApi
import com.example.mpazpro.data.remote.llamarApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Catálogo curricular: lo descarga del backend y lo guarda en Room. Las pantallas leen siempre de la caché, así
 * siguen funcionando sin conexión. Lo que llega ya viene filtrado por el rol de quien inició sesión.
 */
class CatalogoRepository(
    private val api: ContenidosApi,
    private val dao: CatalogoCacheDao
) {
    suspend fun leerCache(): CatalogoCache = withContext(Dispatchers.IO) { dao.leer() }

    /** Descarga el catálogo y reemplaza la caché. Si falla, la caché queda como estaba. */
    suspend fun refrescar(): ResultadoRemoto<CatalogoCache> = withContext(Dispatchers.IO) {
        when (val descarga = llamarApi { descargar() }) {
            is ResultadoRemoto.Ok -> {
                dao.reemplazar(descarga.valor)
                descarga
            }
            is ResultadoRemoto.ErrorHttp -> descarga
            is ResultadoRemoto.SinConexion -> descarga
        }
    }

    suspend fun vaciarCache() = withContext(Dispatchers.IO) { dao.vaciar() }

    // Una llamada por asignaturas, una por unidades, una por cada unidad (sus objetivos) y una por cada objetivo
    // (su paquete de contenidos). Es poco para el MVP; si creciera, el backend podría ofrecer un catálogo completo.
    private suspend fun descargar(): CatalogoCache {
        val asignaturas = api.asignaturas()
        val unidades = api.unidades()
        val objetivos = unidades.flatMap { api.objetivos(unidadId = it.id) }
        val paquetes = objetivos.map { api.paquete(it.id) }
        return CatalogoCache(
            asignaturas = asignaturas.map { CacheAsignatura(it.id, it.nombre, it.nivel) },
            unidades = unidades.map { CacheUnidad(it.id, it.asignaturaId, it.titulo, it.orden, it.estado) },
            objetivos = objetivos.map {
                CacheObjetivo(it.id, it.unidadId, it.codigo, it.descripcion, it.ejeCurricular, it.textoReferencia,
                    it.orden, it.estado)
            },
            contenidos = paquetes.flatMap { it.contenidos }.map {
                CacheContenido(it.id, it.objetivoId, it.titulo, it.explicacion, it.instrucciones, it.ejemplosGuiados,
                    it.imagenUrl, it.archivoAudio, it.orden, it.responsableOrigen, it.licencia)
            }
        )
    }
}

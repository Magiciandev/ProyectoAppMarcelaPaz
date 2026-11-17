package com.example.mpazpro.data.remote.api

import com.example.mpazpro.data.remote.dto.AsignaturaDto
import com.example.mpazpro.data.remote.dto.ContenidoDto
import com.example.mpazpro.data.remote.dto.ContenidoRequestDto
import com.example.mpazpro.data.remote.dto.ObjetivoDto
import com.example.mpazpro.data.remote.dto.ObjetivoRequestDto
import com.example.mpazpro.data.remote.dto.PaqueteContenidosDto
import com.example.mpazpro.data.remote.dto.UnidadDto
import com.example.mpazpro.data.remote.dto.UnidadRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

// Qué devuelve cada lectura depende del rol y del estado (UTP ve todo; docente, lo autorizado y publicado;
// estudiante, lo publicado). Escribir (POST, PUT, DELETE) es solo de UTP.
interface ContenidosApi {
    @GET("contenidos/asignaturas")
    suspend fun asignaturas(): List<AsignaturaDto>

    @GET("contenidos/unidades")
    suspend fun unidades(@Query("asignaturaId") asignaturaId: Long? = null): List<UnidadDto>

    @POST("contenidos/unidades")
    suspend fun crearUnidad(@Body cuerpo: UnidadRequestDto): UnidadDto

    @PUT("contenidos/unidades/{id}")
    suspend fun actualizarUnidad(@Path("id") id: Long, @Body cuerpo: UnidadRequestDto): UnidadDto

    /** Por unidad, o por una lista de ids separados por coma (por ejemplo "1,3,4"). */
    @GET("contenidos/objetivos")
    suspend fun objetivos(
        @Query("unidadId") unidadId: Long? = null,
        @Query("ids") ids: String? = null
    ): List<ObjetivoDto>

    @GET("contenidos/objetivos/{id}")
    suspend fun objetivo(@Path("id") id: Long): ObjetivoDto

    @GET("contenidos/objetivos/{id}/paquete")
    suspend fun paquete(@Path("id") id: Long): PaqueteContenidosDto

    @POST("contenidos/unidades/{unidadId}/objetivos")
    suspend fun crearObjetivo(@Path("unidadId") unidadId: Long, @Body cuerpo: ObjetivoRequestDto): ObjetivoDto

    @PUT("contenidos/objetivos/{id}")
    suspend fun actualizarObjetivo(@Path("id") id: Long, @Body cuerpo: ObjetivoRequestDto): ObjetivoDto

    @POST("contenidos/objetivos/{id}/contenidos")
    suspend fun crearContenido(@Path("id") objetivoId: Long, @Body cuerpo: ContenidoRequestDto): ContenidoDto

    @PUT("contenidos/contenidos/{id}")
    suspend fun actualizarContenido(@Path("id") id: Long, @Body cuerpo: ContenidoRequestDto): ContenidoDto

    @DELETE("contenidos/contenidos/{id}")
    suspend fun eliminarContenido(@Path("id") id: Long): Response<Unit>
}

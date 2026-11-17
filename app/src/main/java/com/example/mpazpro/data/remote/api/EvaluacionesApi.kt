package com.example.mpazpro.data.remote.api

import com.example.mpazpro.data.remote.dto.ActividadDto
import com.example.mpazpro.data.remote.dto.ActividadRequestDto
import com.example.mpazpro.data.remote.dto.PaqueteEvaluacionDto
import com.example.mpazpro.data.remote.dto.PreguntaDto
import com.example.mpazpro.data.remote.dto.PreguntaEnActividadDto
import com.example.mpazpro.data.remote.dto.PreguntaEnPruebaDto
import com.example.mpazpro.data.remote.dto.PreguntaRequestDto
import com.example.mpazpro.data.remote.dto.PruebaDto
import com.example.mpazpro.data.remote.dto.PruebaRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface EvaluacionesApi {
    // Banco de preguntas: solo UTP (trae las respuestas correctas).
    @GET("evaluaciones/preguntas")
    suspend fun preguntas(@Query("objetivoId") objetivoId: Long): List<PreguntaDto>

    @POST("evaluaciones/preguntas")
    suspend fun crearPregunta(@Body cuerpo: PreguntaRequestDto): PreguntaDto

    @PUT("evaluaciones/preguntas/{id}")
    suspend fun actualizarPregunta(@Path("id") id: Long, @Body cuerpo: PreguntaRequestDto): PreguntaDto

    @DELETE("evaluaciones/preguntas/{id}")
    suspend fun eliminarPregunta(@Path("id") id: Long): Response<Unit>

    @GET("evaluaciones/actividades")
    suspend fun actividades(@Query("objetivoId") objetivoId: Long): List<ActividadDto>

    @POST("evaluaciones/actividades")
    suspend fun crearActividad(@Body cuerpo: ActividadRequestDto): ActividadDto

    @PUT("evaluaciones/actividades/{id}")
    suspend fun actualizarActividad(@Path("id") id: Long, @Body cuerpo: ActividadRequestDto): ActividadDto

    @PUT("evaluaciones/actividades/{id}/preguntas")
    suspend fun preguntasDeActividad(
        @Path("id") id: Long,
        @Body preguntas: List<PreguntaEnActividadDto>
    ): List<PreguntaEnActividadDto>

    /** La prueba final del objetivo (404 si no existe o no es visible para el rol). */
    @GET("evaluaciones/pruebas")
    suspend fun prueba(@Query("objetivoId") objetivoId: Long): PruebaDto

    @POST("evaluaciones/pruebas")
    suspend fun crearPrueba(@Body cuerpo: PruebaRequestDto): PruebaDto

    @PUT("evaluaciones/pruebas/{id}")
    suspend fun actualizarPrueba(@Path("id") id: Long, @Body cuerpo: PruebaRequestDto): PruebaDto

    @PUT("evaluaciones/pruebas/{id}/preguntas")
    suspend fun preguntasDePrueba(
        @Path("id") id: Long,
        @Body preguntas: List<PreguntaEnPruebaDto>
    ): List<PreguntaEnPruebaDto>

    @GET("evaluaciones/objetivos/{id}/paquete")
    suspend fun paquete(@Path("id") objetivoId: Long): PaqueteEvaluacionDto
}

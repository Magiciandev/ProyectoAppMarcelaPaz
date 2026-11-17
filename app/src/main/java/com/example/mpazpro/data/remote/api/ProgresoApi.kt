package com.example.mpazpro.data.remote.api

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
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ProgresoApi {
    // ---- lo propio del estudiante (solo rol Estudiante)
    @GET("progreso/estudiantes/me")
    suspend fun yo(): EstudianteMeDto

    @GET("progreso/estudiantes/me/asignaciones")
    suspend fun misAsignaciones(): List<AsignacionEstudianteDto>

    @GET("progreso/estudiantes/me/avance")
    suspend fun miAvance(): AvanceDto

    /** Registra la última conexión (responde 204 sin cuerpo). */
    @POST("progreso/conexion")
    suspend fun conexion(): Response<Unit>

    @POST("progreso/sync")
    suspend fun sincronizar(@Body lote: SyncRequestDto): SyncResponseDto

    // ---- docente y UTP
    @GET("progreso/cursos")
    suspend fun cursos(): List<CursoDto>

    @GET("progreso/cursos/{cursoId}/estudiantes")
    suspend fun estudiantesDelCurso(@Path("cursoId") cursoId: Long): List<EstudianteCursoDto>

    // ---- asignaciones (solo el docente escribe)
    @POST("progreso/asignaciones")
    suspend fun crearAsignacion(@Body cuerpo: AsignacionRequestDto): AsignacionDto

    @GET("progreso/asignaciones")
    suspend fun asignaciones(@Query("cursoId") cursoId: Long): List<AsignacionDto>

    @PUT("progreso/asignaciones/{id}")
    suspend fun actualizarAsignacion(@Path("id") id: Long, @Body cuerpo: AsignacionRequestDto): AsignacionDto

    // ---- monitoreo de un estudiante
    @GET("progreso/estudiantes/{id}/resumen")
    suspend fun resumen(@Path("id") estudianteId: Long): ResumenDto

    @GET("progreso/estudiantes/{id}/intentos")
    suspend fun intentos(
        @Path("id") estudianteId: Long,
        @Query("objetivoId") objetivoId: Long? = null
    ): List<IntentoDto>

    /** El cuerpo es obligatorio (puede ir vacío: ReinicioRequestDto()). */
    @POST("progreso/estudiantes/{id}/reinicio")
    suspend fun reiniciar(@Path("id") estudianteId: Long, @Body cuerpo: ReinicioRequestDto): ReinicioDto

    // ---- informes pedagógicos
    @POST("progreso/informes")
    suspend fun generarInforme(@Body cuerpo: InformeRequestDto): InformeDto

    @GET("progreso/informes")
    suspend fun informes(
        @Query("cursoId") cursoId: Long? = null,
        @Query("estudianteId") estudianteId: Long? = null
    ): List<InformeDto>

    @GET("progreso/informes/{id}")
    suspend fun informe(@Path("id") id: Long): InformeDto

    @PUT("progreso/informes/{id}/validacion")
    suspend fun validarInforme(@Path("id") id: Long, @Body cuerpo: ValidacionRequestDto): InformeDto
}

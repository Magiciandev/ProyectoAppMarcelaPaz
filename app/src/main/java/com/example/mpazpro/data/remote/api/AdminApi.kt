package com.example.mpazpro.data.remote.api

import com.example.mpazpro.data.remote.dto.AprobacionRequestDto
import com.example.mpazpro.data.remote.dto.AprobacionResponseDto
import com.example.mpazpro.data.remote.dto.HistorialDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

// Solo UTP. Es el único lugar donde se cambia el estado de un objetivo de aprendizaje.
interface AdminApi {
    @POST("admin/aprobaciones")
    suspend fun aprobar(@Body cuerpo: AprobacionRequestDto): AprobacionResponseDto

    @GET("admin/historial")
    suspend fun historial(
        @Query("entidadId") entidadId: Long,
        @Query("entidad") entidad: String = "OBJETIVO"
    ): List<HistorialDto>
}

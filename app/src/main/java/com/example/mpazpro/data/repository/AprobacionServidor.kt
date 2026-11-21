package com.example.mpazpro.data.repository

import com.example.mpazpro.data.model.AccionEstado
import com.example.mpazpro.data.remote.ResultadoRemoto
import com.example.mpazpro.data.remote.api.AdminApi
import com.example.mpazpro.data.remote.dto.AprobacionRequestDto
import com.example.mpazpro.data.remote.dto.AprobacionResponseDto
import com.example.mpazpro.data.remote.llamarApi

/**
 * Pide al servidor (ms-admin) que cambie el estado de un objetivo de aprendizaje. Es el único lugar que valida las
 * transiciones: si no corresponde (o falta algo, como la prueba final), responde 409 con el motivo.
 * Solo la UTP puede hacerlo.
 */
class AprobacionServidor(private val admin: AdminApi) {

    suspend fun aplicar(objetivoId: Long, accion: AccionEstado): ResultadoRemoto<AprobacionResponseDto> =
        llamarApi {
            // El nombre de la acción es el mismo que usa el servidor (ENVIAR_REVISION, AUTORIZAR, ...).
            admin.aprobar(AprobacionRequestDto(entidadId = objetivoId, accion = accion.name))
        }
}

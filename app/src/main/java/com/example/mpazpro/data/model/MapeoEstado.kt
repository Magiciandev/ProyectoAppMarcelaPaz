package com.example.mpazpro.data.model

// El servidor escribe los estados como BORRADOR, EN_REVISION...; la app los muestra como «Borrador», «En revisión».
object MapeoEstado {
    fun aLocal(estadoServidor: String): String = when (estadoServidor) {
        "BORRADOR" -> EstadoUnidad.BORRADOR
        "EN_REVISION" -> EstadoUnidad.EN_REVISION
        "AUTORIZADO" -> EstadoUnidad.AUTORIZADO
        "PUBLICADO" -> EstadoUnidad.PUBLICADO
        "DESHABILITADO" -> EstadoUnidad.DESHABILITADO
        else -> estadoServidor
    }
}

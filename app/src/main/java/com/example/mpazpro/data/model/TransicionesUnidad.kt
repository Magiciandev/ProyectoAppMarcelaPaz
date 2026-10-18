package com.example.mpazpro.data.model

// Reglas del flujo de estados de una unidad (Borrador -> En revisión -> Publicado),
// como funciones puras: se prueban sin Android y el ViewModel las usa como guarda.
object TransicionesUnidad {
    const val EVENTO_REVISION = "Enviada a revisión"
    const val EVENTO_APROBADA = "Aprobada y publicada"
    const val EVENTO_RECHAZADA = "Rechazada: vuelve a borrador"
    const val EVENTO_DESHABILITADA = "Deshabilitada: vuelve a borrador"

    fun puedeEnviarARevision(estado: String) = estado == EstadoUnidad.BORRADOR
    fun puedeAprobar(estado: String) = estado == EstadoUnidad.EN_REVISION
    fun puedeRechazar(estado: String) = estado == EstadoUnidad.EN_REVISION
    fun puedeDeshabilitar(estado: String) = estado == EstadoUnidad.PUBLICADO
}

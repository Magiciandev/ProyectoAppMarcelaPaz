package com.example.mpazpro.data.model

// Reglas de cambio de estado de una unidad, como funciones puras (se prueban sin Android).
object TransicionesUnidad {
    const val EVENTO_REVISION = "Enviada a revisión por UTP"
    const val EVENTO_APROBADA = "Aprobada y publicada por UTP"
    const val EVENTO_DEVUELTA = "Devuelta a borrador por UTP"
    const val EVENTO_CREADA = "Creada por UTP"

    fun puedeEnviarARevision(estado: String): Boolean = estado == EstadoUnidad.BORRADOR
    fun puedeAprobar(estado: String): Boolean = estado != EstadoUnidad.PUBLICADO
    fun puedeDevolver(estado: String): Boolean = estado != EstadoUnidad.BORRADOR
}

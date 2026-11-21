package com.example.mpazpro.data.model

// Las seis acciones del ciclo de estados de una unidad, con los mismos nombres que usa el servidor
// (ms-admin). Cada una dice desde qué estado se puede hacer y a cuál lleva.
enum class AccionEstado(
    val desde: String,
    val hasta: String,
    val evento: String, // lo que queda escrito en el historial
    val principal: Boolean // true: botón destacado; false: botón de contorno
) {
    ENVIAR_REVISION(EstadoUnidad.BORRADOR, EstadoUnidad.EN_REVISION, "Enviada a revisión", true),
    AUTORIZAR(EstadoUnidad.EN_REVISION, EstadoUnidad.AUTORIZADO, "Autorizada", true),
    RECHAZAR(EstadoUnidad.EN_REVISION, EstadoUnidad.BORRADOR, "Rechazada: vuelve a borrador", false),
    PUBLICAR(EstadoUnidad.AUTORIZADO, EstadoUnidad.PUBLICADO, "Publicada", true),
    DESHABILITAR(EstadoUnidad.PUBLICADO, EstadoUnidad.DESHABILITADO, "Deshabilitada", false),
    REABRIR(EstadoUnidad.DESHABILITADO, EstadoUnidad.BORRADOR, "Reabierta: vuelve a borrador", false)
}

// Reglas del flujo Borrador -> En revisión -> Autorizado -> Publicado -> Deshabilitado, como funciones puras:
// se prueban sin Android y el ViewModel las usa como guarda.
object TransicionesUnidad {
    /** Las acciones que se pueden hacer desde un estado (para dibujar los botones). */
    fun accionesDesde(estado: String): List<AccionEstado> = AccionEstado.entries.filter { it.desde == estado }

    fun permite(estado: String, accion: AccionEstado): Boolean = accion.desde == estado
}

package com.example.mpazpro.data.repository

import com.example.mpazpro.data.model.Asignaturas
import com.example.mpazpro.data.model.Roles
import com.example.mpazpro.data.model.Unidad
import com.example.mpazpro.data.remote.ResultadoRemoto
import com.example.mpazpro.data.remote.api.ContenidosApi
import com.example.mpazpro.data.remote.dto.AsignaturaDto
import com.example.mpazpro.data.remote.dto.ContenidoRequestDto
import com.example.mpazpro.data.remote.dto.ObjetivoRequestDto
import com.example.mpazpro.data.remote.dto.UnidadRequestDto
import com.example.mpazpro.data.remote.llamarApi

/**
 * Traduce la unidad plana de la app (título, OA, asignatura y contenido en una sola fila) al modelo del servidor,
 * que la reparte en tres cosas: unidad, objetivo de aprendizaje y contenido didáctico.
 * Son funciones puras para poder probarlas sin red.
 */
object MapeoUnidad {
    // El servidor exige estos dos datos y el formulario todavía no los pide: van fijos.
    const val RESPONSABLE = Roles.UTP
    const val LICENCIA = "Material propio de la escuela (datos ficticios)"

    /** El servidor llama «Lenguaje y Comunicación» a lo que la app llama «Lenguaje»: se compara por el inicio. */
    fun idAsignatura(nombreLocal: String, remotas: List<AsignaturaDto>): Long? =
        remotas.firstOrNull { it.nombre.startsWith(nombreLocal, ignoreCase = true) }?.id

    /** Igual que los datos del servidor: LEN-OA05, MAT-OA01, CIE-OA04, HIS-OA01 (máximo 12 caracteres). */
    fun codigoObjetivo(asignaturaLocal: String, oa: String): String {
        val prefijo = when (asignaturaLocal) {
            Asignaturas.LENGUAJE -> "LEN"
            Asignaturas.MATEMATICA -> "MAT"
            Asignaturas.CIENCIAS -> "CIE"
            Asignaturas.HISTORIA -> "HIS"
            else -> null
        }
        val codigo = oa.trim().uppercase()
        return if (prefijo == null) codigo else "$prefijo-$codigo"
    }
}

/**
 * Crea una unidad en el backend (ms-contenidos). Solo la UTP puede escribir contenido: si el usuario es otro,
 * el servidor responde 403 y el mensaje llega listo para mostrar.
 */
class UnidadServidor(private val api: ContenidosApi) {

    /** Crea unidad, objetivo y contenido, en ese orden. Devuelve el id del objetivo creado en el servidor. */
    suspend fun crear(unidad: Unidad): ResultadoRemoto<Long> =
        when (val resultado = llamarApi { crearEnServidor(unidad) }) {
            is ResultadoRemoto.Ok -> {
                val objetivoId = resultado.valor
                if (objetivoId == null) {
                    ResultadoRemoto.ErrorHttp(404, "La asignatura «${unidad.asignatura}» no existe en el servidor")
                } else {
                    ResultadoRemoto.Ok(objetivoId)
                }
            }
            is ResultadoRemoto.ErrorHttp -> resultado
            is ResultadoRemoto.SinConexion -> resultado
        }

    // Devuelve null si el servidor no tiene la asignatura. El servidor deja la unidad y el objetivo en BORRADOR.
    private suspend fun crearEnServidor(unidad: Unidad): Long? {
        val asignaturaId = MapeoUnidad.idAsignatura(unidad.asignatura, api.asignaturas()) ?: return null

        val unidadCreada = api.crearUnidad(
            UnidadRequestDto(asignaturaId = asignaturaId, titulo = unidad.titulo)
        )
        // El formulario no pide una descripción del objetivo: se usa el título de la unidad.
        val objetivo = api.crearObjetivo(
            unidadCreada.id,
            ObjetivoRequestDto(
                codigo = MapeoUnidad.codigoObjetivo(unidad.asignatura, unidad.objetivoAprendizaje),
                descripcion = unidad.titulo
            )
        )
        api.crearContenido(
            objetivo.id,
            ContenidoRequestDto(
                titulo = unidad.titulo,
                explicacion = unidad.contenido,
                responsableOrigen = MapeoUnidad.RESPONSABLE,
                licencia = MapeoUnidad.LICENCIA
            )
        )
        return objetivo.id
    }
}

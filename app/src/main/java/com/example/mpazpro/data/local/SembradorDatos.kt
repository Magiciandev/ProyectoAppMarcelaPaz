package com.example.mpazpro.data.local

import com.example.mpazpro.data.model.Asignaturas
import com.example.mpazpro.data.model.Cursos
import com.example.mpazpro.data.model.EstadoUnidad
import com.example.mpazpro.data.model.HistorialUnidad
import com.example.mpazpro.data.model.Roles
import com.example.mpazpro.data.model.TransicionesUnidad
import com.example.mpazpro.data.model.Unidad
import com.example.mpazpro.data.model.Usuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Carga datos ficticios la primera vez que se abre la app (si las tablas están vacías).
class SembradorDatos(
    private val usuarioDao: UsuarioDao,
    private val unidadDao: UnidadDao
) {
    suspend fun sembrarSiVacio() = withContext(Dispatchers.IO) {
        if (usuarioDao.contar() == 0) {
            usuarioDao.insertarTodos(USUARIOS)
        }
        if (unidadDao.contar() == 0) {
            UNIDADES.forEach { (unidad, historial) ->
                val id = unidadDao.insertar(unidad)
                historial.forEach { (fecha, evento) ->
                    unidadDao.insertarHistorial(HistorialUnidad(unidadId = id, fecha = fecha, evento = evento))
                }
            }
        }
    }

    companion object {
        const val PIN_DE_PRUEBA = "1234"

        val USUARIOS = listOf(
            Usuario(usuario = "utp", pin = PIN_DE_PRUEBA, nombre = "Jefatura UTP", rol = Roles.UTP),
            Usuario(usuario = "docente", pin = PIN_DE_PRUEBA, nombre = "Docente de 2°", rol = Roles.DOCENTE),
            Usuario(usuario = "est001", pin = PIN_DE_PRUEBA, nombre = "Estudiante 001", rol = Roles.ESTUDIANTE, curso = Cursos.A),
            Usuario(usuario = "est002", pin = PIN_DE_PRUEBA, nombre = "Estudiante 002", rol = Roles.ESTUDIANTE, curso = Cursos.A),
            Usuario(usuario = "est003", pin = PIN_DE_PRUEBA, nombre = "Estudiante 003", rol = Roles.ESTUDIANTE, curso = Cursos.A),
            Usuario(usuario = "est004", pin = PIN_DE_PRUEBA, nombre = "Estudiante 004", rol = Roles.ESTUDIANTE, curso = Cursos.B),
            Usuario(usuario = "est005", pin = PIN_DE_PRUEBA, nombre = "Estudiante 005", rol = Roles.ESTUDIANTE, curso = Cursos.B),
            Usuario(usuario = "est006", pin = PIN_DE_PRUEBA, nombre = "Estudiante 006", rol = Roles.ESTUDIANTE, curso = Cursos.B)
        )

        // Cada unidad con su historial inicial (fecha, evento).
        val UNIDADES: List<Pair<Unidad, List<Pair<String, String>>>> = listOf(
            Unidad(
                titulo = "Números hasta 100", objetivoAprendizaje = "OA03", asignatura = Asignaturas.MATEMATICA,
                estado = EstadoUnidad.PUBLICADO,
                contenido = "Aprenderemos a contar, leer y escribir números hasta el 100, con material concreto y ejercicios guiados."
            ) to listOf(
                "2026-09-02" to TransicionesUnidad.EVENTO_CREADA,
                "2026-09-10" to TransicionesUnidad.EVENTO_APROBADA
            ),
            Unidad(
                titulo = "Suma y resta", objetivoAprendizaje = "OA05", asignatura = Asignaturas.MATEMATICA,
                estado = EstadoUnidad.EN_REVISION,
                contenido = "Sumar es juntar cantidades. Restar es quitar una cantidad de otra. Practicaremos con objetos y dibujos."
            ) to listOf(
                "2026-09-12" to TransicionesUnidad.EVENTO_CREADA,
                "2026-09-18" to TransicionesUnidad.EVENTO_REVISION
            ),
            Unidad(
                titulo = "Figuras 2D", objetivoAprendizaje = "OA12", asignatura = Asignaturas.MATEMATICA,
                estado = EstadoUnidad.BORRADOR,
                contenido = "Reconoceremos triángulos, cuadrados, rectángulos y círculos en objetos de la sala de clases."
            ) to listOf("2026-09-20" to TransicionesUnidad.EVENTO_CREADA),
            Unidad(
                titulo = "Cuentos breves", objetivoAprendizaje = "OA02", asignatura = Asignaturas.LENGUAJE,
                estado = EstadoUnidad.PUBLICADO,
                contenido = "Leeremos cuentos breves y conversaremos sobre los personajes, el lugar y lo que pasó en la historia."
            ) to listOf(
                "2026-09-01" to TransicionesUnidad.EVENTO_CREADA,
                "2026-09-05" to TransicionesUnidad.EVENTO_APROBADA
            ),
            Unidad(
                titulo = "Los sentidos", objetivoAprendizaje = "OA04", asignatura = Asignaturas.CIENCIAS,
                estado = EstadoUnidad.EN_REVISION,
                contenido = "Exploraremos los cinco sentidos y cómo nos ayudan a conocer el mundo que nos rodea."
            ) to listOf(
                "2026-09-19" to TransicionesUnidad.EVENTO_CREADA,
                "2026-09-22" to TransicionesUnidad.EVENTO_REVISION
            ),
            Unidad(
                titulo = "Mi familia y comunidad", objetivoAprendizaje = "OA01", asignatura = Asignaturas.HISTORIA,
                estado = EstadoUnidad.EN_REVISION,
                contenido = "Conversaremos sobre nuestra familia, nuestra casa y las personas de nuestra comunidad."
            ) to listOf(
                "2026-09-15" to TransicionesUnidad.EVENTO_CREADA,
                "2026-09-20" to TransicionesUnidad.EVENTO_REVISION
            )
        )
    }
}

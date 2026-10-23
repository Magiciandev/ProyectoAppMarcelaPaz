package com.example.mpazpro.data.repository

import com.example.mpazpro.data.local.AsignacionDao
import com.example.mpazpro.data.local.HistorialDao
import com.example.mpazpro.data.local.PreguntaDao
import com.example.mpazpro.data.local.UnidadDao
import com.example.mpazpro.data.local.UsuarioDao
import com.example.mpazpro.data.model.Asignacion
import com.example.mpazpro.data.model.Asignaturas
import com.example.mpazpro.data.model.Cursos
import com.example.mpazpro.data.model.EstadoUnidad
import com.example.mpazpro.data.model.HistorialUnidad
import com.example.mpazpro.data.model.Pregunta
import com.example.mpazpro.data.model.Roles
import com.example.mpazpro.data.model.Unidad
import com.example.mpazpro.data.model.Usuario
import com.example.mpazpro.data.security.Cifrado
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

// Datos 100 % ficticios que se cargan la primera vez que la base está vacía.
class SemillaDatos(
    private val usuarioDao: UsuarioDao,
    private val unidadDao: UnidadDao,
    private val historialDao: HistorialDao,
    private val preguntaDao: PreguntaDao,
    private val asignacionDao: AsignacionDao
) {
    private companion object { const val DIA = 86_400_000L }

    private val mutex = Mutex()

    suspend fun asegurar() = mutex.withLock {
        withContext(Dispatchers.IO) {
            sembrarUsuarios()
            if (unidadDao.contar() == 0) sembrarUnidades()
        }
    }

    private suspend fun sembrarUsuarios() {
        fun u(nombre: String, usuario: String, rol: String, curso: String? = null) =
            Usuario(nombre = nombre, usuario = usuario, pinHash = Cifrado.crear("1234"), rol = rol, curso = curso)
        
        val usuarios = listOf(
            u("Jefatura UTP", "utp", Roles.UTP),
            u("Docente de prueba", "docente", Roles.DOCENTE),
            u("Ana", "ana", Roles.ESTUDIANTE, "2° A"),
            u("Beto", "beto", Roles.ESTUDIANTE, "2° A"),
            u("Carla", "carla", Roles.ESTUDIANTE, "2° A"),
            u("Ronald Villalobos", "villalobos", Roles.ESTUDIANTE, "2° A"),
            u("Diego", "diego", Roles.ESTUDIANTE, "2° B"),
            u("Eva", "eva", Roles.ESTUDIANTE, "2° B"),
            u("Juan Abarzua", "abarzua", Roles.ESTUDIANTE, "2° B"),
            u("Felipe", "felipe", Roles.ESTUDIANTE, "2° B")
        )
        usuarioDao.insertarTodos(usuarios)
    }

    private suspend fun sembrarUnidades() {
        fun q(t: String, a: String, b: String, c: String, d: String, ok: Int) =
            Pregunta(unidadId = 0, texto = t, opcionA = a, opcionB = b, opcionC = c, opcionD = d, indiceCorrecta = ok)

        data class Semilla(val u: Unidad, val preguntas: List<Pregunta>)

        val semillas = listOf(
            Semilla(
                Unidad(titulo = "Unidad 2 · Suma y resta", objetivoAprendizaje = "OA05", asignatura = Asignaturas.MATEMATICA,
                    estado = EstadoUnidad.PUBLICADO, contenido = "Sumar es juntar cantidades. Restar es quitar una cantidad de otra."),
                listOf(
                    q("¿Cuánto es 5 + 3?", "6", "8", "9", "12", 1),
                    q("¿Cuánto es 9 − 4?", "3", "5", "6", "7", 1),
                    q("¿Cuánto es 6 + 2?", "9", "8", "7", "10", 1)
                )
            ),
            Semilla(
                Unidad(titulo = "Unidad 1 · Cuentos breves", objetivoAprendizaje = "OA02", asignatura = Asignaturas.LENGUAJE,
                    estado = EstadoUnidad.PUBLICADO, contenido = "Un cuento tiene personajes, un lugar donde ocurre y un final."),
                listOf(
                    q("El personaje principal es...", "el lugar", "quien vive la historia", "el título", "el autor", 1),
                    q("¿Dónde ocurre la historia?", "personaje", "escenario", "final", "autor", 1),
                    q("La parte final del cuento es el...", "inicio", "desarrollo", "desenlace", "portada", 2)
                )
            ),
            Semilla(
                Unidad(titulo = "Unidad 1 · Los sentidos", objetivoAprendizaje = "OA04", asignatura = Asignaturas.CIENCIAS,
                    estado = EstadoUnidad.PUBLICADO, contenido = "Usamos los cinco sentidos para conocer el mundo que nos rodea."),
                listOf(
                    q("¿Con qué sentido escuchamos?", "vista", "oído", "olfato", "tacto", 1),
                    q("¿Con qué sentido vemos los colores?", "gusto", "tacto", "vista", "oído", 2),
                    q("¿Con qué parte del cuerpo olemos?", "ojos", "nariz", "oídos", "manos", 1)
                )
            ),
            Semilla(
                Unidad(titulo = "Unidad 1 · Mi familia y comunidad", objetivoAprendizaje = "OA01", asignatura = Asignaturas.HISTORIA,
                    estado = EstadoUnidad.PUBLICADO, contenido = "La familia y la comunidad nos cuidan y nos ayudan a vivir mejor."),
                listOf(
                    q("Una familia es...", "solo mamá", "personas que viven y se cuidan juntas", "solo vecinos", "solo abuelos", 1),
                    q("¿Quién nos ayuda con la seguridad?", "el carabinero", "panadero", "doctor", "profesor", 0),
                    q("El hogar es...", "escuela", "hospital", "nuestra casa", "parque", 2)
                )
            ),
            Semilla(
                Unidad(titulo = "Unidad 1 · Números hasta 100", objetivoAprendizaje = "OA03", asignatura = Asignaturas.MATEMATICA,
                    estado = EstadoUnidad.EN_REVISION, contenido = "Contamos, leemos y escribimos números hasta el 100."),
                emptyList()
            ),
            Semilla(
                Unidad(titulo = "Unidad 3 · Figuras 2D", objetivoAprendizaje = "OA12", asignatura = Asignaturas.MATEMATICA,
                    estado = EstadoUnidad.BORRADOR, contenido = "Reconocemos círculos, cuadrados, triángulos y rectángulos."),
                emptyList()
            )
        )
        val ahora = System.currentTimeMillis()
        semillas.forEach { s ->
            val id = unidadDao.insertar(s.u)
            historialDao.insertar(HistorialUnidad(unidadId = id, fecha = ahora, evento = "Unidad de ejemplo cargada (${s.u.estado})"))
            s.preguntas.forEach { preguntaDao.insertar(it.copy(unidadId = id)) }
            // Historia queda sin asignar a propósito: sirve para demostrar que el estudiante no la ve.
            if (s.u.estado == EstadoUnidad.PUBLICADO && s.u.asignatura != Asignaturas.HISTORIA) {
                Cursos.todos.forEach { curso ->
                    asignacionDao.guardarAsignacion(
                        Asignacion(unidadId = id, curso = curso, desde = ahora - DIA, hasta = ahora + 365 * DIA)
                    )
                }
            }
        }
    }
}

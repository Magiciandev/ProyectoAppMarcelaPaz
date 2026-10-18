package com.example.mpazpro.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.mpazpro.ui.screens.BienvenidaScreen
import com.example.mpazpro.ui.screens.EstudianteUnidadesScreen
import com.example.mpazpro.ui.screens.EvaluacionScreen
import com.example.mpazpro.ui.screens.LoginScreen
import com.example.mpazpro.ui.screens.PanelScreen
import com.example.mpazpro.ui.screens.PreguntasEditorScreen
import com.example.mpazpro.ui.screens.UnidadDetalleScreen
import com.example.mpazpro.ui.screens.UnidadFormScreen
import com.example.mpazpro.ui.screens.UnidadesScreen

// Nombres de las rutas en un solo lugar.
object Rutas {
    const val BIENVENIDA = "bienvenida"
    const val LOGIN = "login"
    const val PANEL = "panel/{usuarioId}"
    const val UNIDADES = "unidades/{editable}"
    const val UNIDAD_NUEVA = "unidad_nueva"
    const val UNIDAD_DETALLE = "unidad/{unidadId}/{editable}"
    const val PREGUNTAS = "preguntas/{unidadId}"
    const val ESTUDIANTE_UNIDADES = "estudiante/{estudianteId}"
    const val EVALUACION = "evaluacion/{unidadId}/{estudianteId}"

    fun panel(usuarioId: Long) = "panel/$usuarioId"
    fun unidades(editable: Boolean) = "unidades/$editable"
    fun unidadDetalle(unidadId: Long, editable: Boolean) = "unidad/$unidadId/$editable"
    fun preguntas(unidadId: Long) = "preguntas/$unidadId"
    fun estudianteUnidades(estudianteId: Long) = "estudiante/$estudianteId"
    fun evaluacion(unidadId: Long, estudianteId: Long) = "evaluacion/$unidadId/$estudianteId"
}

private val argLong = { nombre: String -> navArgument(nombre) { type = NavType.LongType } }
private val argBool = { nombre: String -> navArgument(nombre) { type = NavType.BoolType } }

// Grafo de navegación. Cada pantalla recibe su ViewModel creado con la fábrica manual;
// los ids de las rutas llegan al ViewModel por SavedStateHandle.
@Composable
fun AppNavHost(
    factory: ViewModelProvider.Factory,
    navController: NavHostController = rememberNavController()
) {
    NavHost(navController = navController, startDestination = Rutas.BIENVENIDA) {
        composable(Rutas.BIENVENIDA) {
            BienvenidaScreen(onEntrar = { navController.navigate(Rutas.LOGIN) })
        }
        composable(Rutas.LOGIN) {
            LoginScreen(
                viewModel = viewModel(factory = factory),
                onIngreso = { id ->
                    navController.navigate(Rutas.panel(id)) { popUpTo(Rutas.BIENVENIDA) }
                }
            )
        }
        composable(Rutas.PANEL, arguments = listOf(argLong("usuarioId"))) {
            PanelScreen(
                viewModel = viewModel(factory = factory),
                onUnidades = { navController.navigate(Rutas.unidades(it)) },
                onEvaluaciones = { navController.navigate(Rutas.estudianteUnidades(it)) },
                onCerrarSesion = {
                    navController.navigate(Rutas.LOGIN) { popUpTo(Rutas.BIENVENIDA) }
                }
            )
        }
        composable(Rutas.UNIDADES, arguments = listOf(argBool("editable"))) { entrada ->
            val editable = entrada.arguments?.getBoolean("editable") ?: false
            UnidadesScreen(
                viewModel = viewModel(factory = factory),
                editable = editable,
                onAbrir = { navController.navigate(Rutas.unidadDetalle(it, editable)) },
                onNuevaUnidad = { navController.navigate(Rutas.UNIDAD_NUEVA) },
                onVolver = { navController.popBackStack() }
            )
        }
        composable(Rutas.UNIDAD_NUEVA) {
            UnidadFormScreen(
                viewModel = viewModel(factory = factory),
                onGuardada = { navController.popBackStack() },
                onVolver = { navController.popBackStack() }
            )
        }
        composable(Rutas.UNIDAD_DETALLE, arguments = listOf(argLong("unidadId"), argBool("editable"))) { entrada ->
            UnidadDetalleScreen(
                viewModel = viewModel(factory = factory),
                editable = entrada.arguments?.getBoolean("editable") ?: false,
                onEditarPreguntas = { navController.navigate(Rutas.preguntas(it)) },
                onVolver = { navController.popBackStack() }
            )
        }
        composable(Rutas.PREGUNTAS, arguments = listOf(argLong("unidadId"))) {
            PreguntasEditorScreen(
                viewModel = viewModel(factory = factory),
                onVolver = { navController.popBackStack() }
            )
        }
        composable(Rutas.ESTUDIANTE_UNIDADES, arguments = listOf(argLong("estudianteId"))) { entrada ->
            val estudianteId = entrada.arguments?.getLong("estudianteId") ?: -1L
            EstudianteUnidadesScreen(
                viewModel = viewModel(factory = factory),
                onRendir = { navController.navigate(Rutas.evaluacion(it, estudianteId)) },
                onVolver = { navController.popBackStack() }
            )
        }
        composable(Rutas.EVALUACION, arguments = listOf(argLong("unidadId"), argLong("estudianteId"))) {
            EvaluacionScreen(
                viewModel = viewModel(factory = factory),
                onVolver = { navController.popBackStack() }
            )
        }
    }
}

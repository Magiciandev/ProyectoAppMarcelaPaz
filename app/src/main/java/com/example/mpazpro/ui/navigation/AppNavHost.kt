package com.example.mpazpro.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.mpazpro.R
import com.example.mpazpro.data.model.Roles
import com.example.mpazpro.data.model.Usuario
import com.example.mpazpro.ui.screens.BienvenidaScreen
import com.example.mpazpro.ui.screens.LoginScreen
import com.example.mpazpro.ui.screens.PanelRolScreen
import com.example.mpazpro.ui.screens.UnidadDetalleScreen
import com.example.mpazpro.ui.screens.UnidadFormScreen
import com.example.mpazpro.ui.screens.UnidadesScreen
import com.example.mpazpro.viewmodel.SesionViewModel

// Nombres de las rutas en un solo lugar.
object Rutas {
    const val BIENVENIDA = "bienvenida"
    const val LOGIN = "login"
    const val UNIDADES = "unidades"
    const val UNIDAD_NUEVA = "unidad_nueva"
    const val UNIDAD_DETALLE = "unidad/{id}"
    const val PANEL_DOCENTE = "panel_docente"
    const val PANEL_ESTUDIANTE = "panel_estudiante"

    fun detalle(id: Long) = "unidad/$id"

    // Cada rol entra a su propio panel.
    fun inicioDe(rol: String): String = when (rol) {
        Roles.UTP -> UNIDADES
        Roles.DOCENTE -> PANEL_DOCENTE
        else -> PANEL_ESTUDIANTE
    }
}

// Grafo de navegación de la app. Cada pantalla recibe su ViewModel creado con la fábrica manual.
@Composable
fun AppNavHost(
    factory: ViewModelProvider.Factory,
    navController: NavHostController = rememberNavController()
) {
    // Este ViewModel se pide fuera de las pantallas, por eso dura toda la app (y sobrevive al giro).
    val sesion: SesionViewModel = viewModel(factory = factory)
    val usuario by sesion.usuario.collectAsStateWithLifecycle()

    val irALogin: () -> Unit = {
        navController.navigate(Rutas.LOGIN) { popUpTo(Rutas.BIENVENIDA) }
    }

    NavHost(navController = navController, startDestination = Rutas.BIENVENIDA) {
        composable(Rutas.BIENVENIDA) {
            BienvenidaScreen(onEntrar = { navController.navigate(Rutas.LOGIN) })
        }
        composable(Rutas.LOGIN) {
            LoginScreen(
                viewModel = viewModel(factory = factory),
                onLoginExitoso = { ingresado ->
                    sesion.iniciar(ingresado)
                    navController.navigate(Rutas.inicioDe(ingresado.rol)) {
                        popUpTo(Rutas.LOGIN) { inclusive = true }
                    }
                }
            )
        }
        composable(Rutas.UNIDADES) {
            ConAcceso(usuario, Roles.UTP, irALogin) {
                UnidadesScreen(
                    viewModel = viewModel(factory = factory),
                    onNuevaUnidad = { navController.navigate(Rutas.UNIDAD_NUEVA) },
                    onAbrirUnidad = { id -> navController.navigate(Rutas.detalle(id)) },
                    onCerrarSesion = { sesion.cerrar() }
                )
            }
        }
        composable(Rutas.UNIDAD_NUEVA) {
            ConAcceso(usuario, Roles.UTP, irALogin) {
                UnidadFormScreen(
                    viewModel = viewModel(factory = factory),
                    onGuardada = { navController.popBackStack() },
                    onVolver = { navController.popBackStack() }
                )
            }
        }
        composable(
            route = Rutas.UNIDAD_DETALLE,
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { entrada ->
            val id = entrada.arguments?.getLong("id") ?: 0L
            ConAcceso(usuario, Roles.UTP, irALogin) {
                UnidadDetalleScreen(
                    viewModel = viewModel(factory = factory),
                    unidadId = id,
                    onVolver = { navController.popBackStack() }
                )
            }
        }
        composable(Rutas.PANEL_DOCENTE) {
            ConAcceso(usuario, Roles.DOCENTE, irALogin) { actual ->
                PanelRolScreen(
                    titulo = stringResource(R.string.panel_docente_titulo),
                    usuario = actual,
                    descripcion = stringResource(R.string.panel_docente_desc),
                    onCerrarSesion = { sesion.cerrar() }
                )
            }
        }
        composable(Rutas.PANEL_ESTUDIANTE) {
            ConAcceso(usuario, Roles.ESTUDIANTE, irALogin) { actual ->
                PanelRolScreen(
                    titulo = stringResource(R.string.panel_estudiante_titulo),
                    usuario = actual,
                    descripcion = stringResource(R.string.panel_estudiante_desc),
                    onCerrarSesion = { sesion.cerrar() }
                )
            }
        }
    }
}

// Muestra la pantalla solo si hay sesión y el rol corresponde. Si no, vuelve al ingreso.
// Cerrar sesión también pasa por aquí: al quedar sin usuario, la pantalla regresa sola al login.
@Composable
private fun ConAcceso(
    usuario: Usuario?,
    rolRequerido: String,
    onSinAcceso: () -> Unit,
    contenido: @Composable (Usuario) -> Unit
) {
    if (usuario != null && usuario.rol == rolRequerido) {
        contenido(usuario)
    } else {
        LaunchedEffect(Unit) { onSinAcceso() }
    }
}

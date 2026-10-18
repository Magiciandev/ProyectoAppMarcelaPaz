package com.example.mpazpro.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mpazpro.ui.screens.BienvenidaScreen
import com.example.mpazpro.ui.screens.UnidadFormScreen
import com.example.mpazpro.ui.screens.UnidadesScreen

// Nombres de las rutas en un solo lugar.
object Rutas {
    const val BIENVENIDA = "bienvenida"
    const val UNIDADES = "unidades"
    const val UNIDAD_NUEVA = "unidad_nueva"
}

// Grafo de navegación de la app. Cada pantalla recibe su ViewModel creado con la fábrica manual.
@Composable
fun AppNavHost(
    factory: ViewModelProvider.Factory,
    navController: NavHostController = rememberNavController()
) {
    NavHost(navController = navController, startDestination = Rutas.BIENVENIDA) {
        composable(Rutas.BIENVENIDA) {
            BienvenidaScreen(onEntrar = { navController.navigate(Rutas.UNIDADES) })
        }
        composable(Rutas.UNIDADES) {
            UnidadesScreen(
                viewModel = viewModel(factory = factory),
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
    }
}

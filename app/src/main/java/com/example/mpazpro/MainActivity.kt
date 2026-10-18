package com.example.mpazpro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.example.mpazpro.data.local.AppDatabase
import com.example.mpazpro.data.local.SembradorDatos
import com.example.mpazpro.data.repository.UnidadRepository
import com.example.mpazpro.data.repository.UsuarioRepository
import com.example.mpazpro.ui.navigation.AppNavHost
import com.example.mpazpro.ui.theme.MpazProTheme
import com.example.mpazpro.viewmodel.MpazViewModelFactory
import kotlinx.coroutines.launch

// Actividad única: toda la navegación vive en Compose (AppNavHost).
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Aquí se arma la cadena de dependencias: base de datos -> repositorios -> fábrica de ViewModels.
        val baseDeDatos = AppDatabase.obtener(applicationContext)
        val fabrica = MpazViewModelFactory(
            unidadRepository = UnidadRepository(baseDeDatos.unidadDao()),
            usuarioRepository = UsuarioRepository(baseDeDatos.usuarioDao())
        )

        // Primera vez: carga los usuarios y unidades ficticios (si ya existen, no hace nada).
        val sembrador = SembradorDatos(baseDeDatos.usuarioDao(), baseDeDatos.unidadDao())
        lifecycleScope.launch { sembrador.sembrarSiVacio() }

        setContent {
            MpazProTheme {
                AppNavHost(factory = fabrica)
            }
        }
    }
}

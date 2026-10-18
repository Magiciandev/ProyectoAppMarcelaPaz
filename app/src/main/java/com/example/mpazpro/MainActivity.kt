package com.example.mpazpro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.mpazpro.data.local.AppDatabase
import com.example.mpazpro.data.local.AlmacenImagenes
import com.example.mpazpro.data.repository.AsignacionRepository
import com.example.mpazpro.data.repository.EvaluacionRepository
import com.example.mpazpro.data.repository.SemillaDatos
import com.example.mpazpro.data.repository.UnidadRepository
import com.example.mpazpro.data.repository.UsuarioRepository
import com.example.mpazpro.ui.navigation.AppNavHost
import com.example.mpazpro.ui.theme.MpazProTheme
import com.example.mpazpro.viewmodel.MpazViewModelFactory

// Actividad única: toda la navegación vive en Compose (AppNavHost).
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Aquí se arma la cadena de dependencias: base de datos -> repositorio -> fábrica de ViewModels.
        val baseDeDatos = AppDatabase.obtener(applicationContext)
        val semilla = SemillaDatos(
            baseDeDatos.usuarioDao(), baseDeDatos.unidadDao(),
            baseDeDatos.historialDao(), baseDeDatos.preguntaDao(), baseDeDatos.asignacionDao()
        )
        val fabrica = MpazViewModelFactory(
            unidadRepository = UnidadRepository(
                baseDeDatos.unidadDao(), baseDeDatos.historialDao(), AlmacenImagenes(applicationContext)
            ),
            usuarioRepository = UsuarioRepository(baseDeDatos.usuarioDao(), semilla),
            evaluacionRepository = EvaluacionRepository(baseDeDatos.preguntaDao(), baseDeDatos.evaluacionDao()),
            asignacionRepository = AsignacionRepository(baseDeDatos.asignacionDao())
        )

        setContent {
            MpazProTheme {
                AppNavHost(factory = fabrica)
            }
        }
    }
}

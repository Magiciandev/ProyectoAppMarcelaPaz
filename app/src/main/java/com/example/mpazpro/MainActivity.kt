package com.example.mpazpro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.example.mpazpro.data.local.AppDatabase
import com.example.mpazpro.data.local.AlmacenImagenes
import com.example.mpazpro.data.remote.AlmacenSharedPreferences
import com.example.mpazpro.data.remote.ApiConfig
import com.example.mpazpro.data.remote.AutenticacionRemota
import com.example.mpazpro.data.remote.ClienteApi
import com.example.mpazpro.data.remote.SesionStore
import com.example.mpazpro.data.repository.AsignacionRepository
import com.example.mpazpro.data.repository.AsignacionServidor
import com.example.mpazpro.data.repository.AprobacionServidor
import com.example.mpazpro.data.repository.CatalogoRepository
import com.example.mpazpro.data.repository.ActividadesServidor
import com.example.mpazpro.data.repository.EvaluacionRepository
import com.example.mpazpro.data.repository.InformeRepository
import com.example.mpazpro.data.repository.MonitoreoServidor
import com.example.mpazpro.ui.components.Notificaciones
import com.example.mpazpro.data.repository.SemillaDatos
import com.example.mpazpro.data.repository.PruebaServidor
import com.example.mpazpro.data.repository.UnidadRepository
import com.example.mpazpro.data.repository.UnidadServidor
import com.example.mpazpro.data.repository.UsuarioRepository
import com.example.mpazpro.ui.navigation.AppNavHost
import com.example.mpazpro.ui.theme.MpazProTheme
import com.example.mpazpro.viewmodel.MpazViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
        lifecycleScope.launch(Dispatchers.IO) {
            semilla.asegurar()
        }

        // Conexión con el backend: un solo cliente (el gateway) y la sesión (token) guardada en el teléfono.
        val sesion = SesionStore(AlmacenSharedPreferences(applicationContext))
        val cliente = ClienteApi(ApiConfig.baseUrl, sesion)

        val catalogoRepository = CatalogoRepository(cliente.contenidos, baseDeDatos.catalogoCacheDao())

        val fabrica = MpazViewModelFactory(
            unidadRepository = UnidadRepository(
                baseDeDatos.unidadDao(), baseDeDatos.historialDao(), AlmacenImagenes(applicationContext),
                UnidadServidor(cliente.contenidos), AprobacionServidor(cliente.admin), catalogoRepository
            ),
            usuarioRepository = UsuarioRepository(
                baseDeDatos.usuarioDao(), semilla, AutenticacionRemota(cliente.auth, sesion)
            ),
            evaluacionRepository = EvaluacionRepository(baseDeDatos.preguntaDao(), baseDeDatos.evaluacionDao()),
            asignacionRepository = AsignacionRepository(
                baseDeDatos.asignacionDao(), baseDeDatos.unidadDao(), AsignacionServidor(cliente.progreso)
            ),
            informeRepository = InformeRepository(
                baseDeDatos.informeDao(), baseDeDatos.usuarioDao(), baseDeDatos.preguntaDao()
            ),
            catalogoRepository = catalogoRepository,
            sesionStore = sesion,
            pruebaServidor = PruebaServidor(cliente.evaluaciones, cliente.progreso),
            actividadesServidor = ActividadesServidor(cliente.evaluaciones),
            monitoreoServidor = MonitoreoServidor(cliente.progreso)
        )

        Notificaciones.crearCanal(applicationContext)

        setContent {
            MpazProTheme {
                // Fondo base de toda la app: aporta el color de fondo del tema y el color de texto
                // «onBackground». Sin esto se veía el verde de la ventana y el texto salía negro.
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.onBackground
                ) {
                    AppNavHost(factory = fabrica)
                }
            }
        }
    }
}

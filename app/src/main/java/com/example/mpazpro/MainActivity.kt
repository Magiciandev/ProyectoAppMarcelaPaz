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
import com.example.mpazpro.data.repository.BandejaIntentos
import com.example.mpazpro.data.repository.AprobacionServidor
import com.example.mpazpro.data.repository.CatalogoRepository
import com.example.mpazpro.data.repository.ActividadesServidor
import com.example.mpazpro.data.repository.EvaluacionRepository
import com.example.mpazpro.data.repository.InformeRepository
import com.example.mpazpro.data.repository.InformeServidor
import com.example.mpazpro.data.repository.MonitoreoServidor
import com.example.mpazpro.ui.components.Notificaciones
import com.example.mpazpro.data.repository.SemillaDatos
import com.example.mpazpro.data.repository.PruebaServidor
import com.example.mpazpro.data.repository.UnidadRepository
import com.example.mpazpro.data.repository.UnidadServidor
import com.example.mpazpro.data.repository.UsuarioRepository
import com.example.mpazpro.sync.ProgramadorSync
import com.example.mpazpro.sync.crearAvanceServidor
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

        val actividadesServidor = ActividadesServidor(cliente.evaluaciones)
        // Lo que quede pendiente (avance o intentos) se envía en segundo plano en cuanto haya conexión.
        val pedirEnvio = { ProgramadorSync.programar(applicationContext) }
        // El avance del estudiante: los ítems completados se anotan en Room y se envían al servidor.
        val avanceServidor = crearAvanceServidor(cliente.progreso, actividadesServidor, baseDeDatos, pedirEnvio)

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
            actividadesServidor = actividadesServidor,
            monitoreoServidor = MonitoreoServidor(cliente.progreso),
            informeServidor = InformeServidor(cliente.progreso) {
                baseDeDatos.catalogoCacheDao().objetivos().associate { it.id to it.codigo }
            },
            avanceServidor = avanceServidor,
            // Intentos de prueba terminados: se guardan en Room y se envían cuando hay conexión.
            bandejaIntentos = BandejaIntentos(cliente.progreso, baseDeDatos.intentoPendienteDao(), alQuedarPendiente = pedirEnvio)
        )

        Notificaciones.crearCanal(applicationContext)
        // Al abrir la app se envía lo pendiente y se deja el envío periódico (cada 15 minutos, con conexión).
        ProgramadorSync.programar(applicationContext)
        ProgramadorSync.programarPeriodico(applicationContext)

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

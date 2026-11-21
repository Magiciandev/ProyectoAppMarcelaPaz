package com.example.mpazpro.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.mpazpro.data.model.Asignacion
import com.example.mpazpro.data.model.CacheAsignacionEstudiante
import com.example.mpazpro.data.model.CacheAsignatura
import com.example.mpazpro.data.model.CacheContenido
import com.example.mpazpro.data.model.CacheObjetivo
import com.example.mpazpro.data.model.CachePaquete
import com.example.mpazpro.data.model.CacheUnidad
import com.example.mpazpro.data.model.Configuracion
import com.example.mpazpro.data.model.HistorialUnidad
import com.example.mpazpro.data.model.Intento
import com.example.mpazpro.data.model.IntentoPendiente
import com.example.mpazpro.data.model.ItemCompletado
import com.example.mpazpro.data.model.Observacion
import com.example.mpazpro.data.model.Pregunta
import com.example.mpazpro.data.model.Resultado
import com.example.mpazpro.data.model.TotalesObjetivo
import com.example.mpazpro.data.model.Unidad
import com.example.mpazpro.data.model.Usuario

@Database(
    entities = [
        Unidad::class, Usuario::class, HistorialUnidad::class,
        Pregunta::class, Resultado::class, Intento::class,
        Asignacion::class, Configuracion::class, Observacion::class,
        // v5: caché del catálogo que entrega el backend
        CacheAsignatura::class, CacheUnidad::class, CacheObjetivo::class, CacheContenido::class,
        // v7: avance del estudiante (ítems completados y totales por objetivo)
        ItemCompletado::class, TotalesObjetivo::class,
        // v8: intentos de prueba terminados que esperan confirmación del servidor
        IntentoPendiente::class,
        // v9: asignaciones del estudiante y paquetes de evaluación para usar sin conexión
        CacheAsignacionEstudiante::class, CachePaquete::class
    ],
    // v9: caché de evaluación sin conexión
    version = 9,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun unidadDao(): UnidadDao
    abstract fun usuarioDao(): UsuarioDao
    abstract fun historialDao(): HistorialDao
    abstract fun preguntaDao(): PreguntaDao
    abstract fun evaluacionDao(): EvaluacionDao
    abstract fun asignacionDao(): AsignacionDao
    abstract fun informeDao(): InformeDao
    abstract fun catalogoCacheDao(): CatalogoCacheDao
    abstract fun avanceLocalDao(): AvanceLocalDao
    abstract fun intentoPendienteDao(): IntentoPendienteDao
    abstract fun cacheEvaluacionDao(): CacheEvaluacionDao

    companion object {
        @Volatile
        private var instancia: AppDatabase? = null

        // Una sola instancia para toda la app. Solo MainActivity la crea (el Context no llega al ViewModel).
        fun obtener(context: Context): AppDatabase =
            instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mpaz_pro.db"
                )
                    // Solo desarrollo: si cambia el esquema, se recrea la base (los datos son ficticios).
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instancia = it }
            }
    }
}

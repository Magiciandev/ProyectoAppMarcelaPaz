package com.example.mpazpro.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.mpazpro.data.model.HistorialUnidad
import com.example.mpazpro.data.model.Unidad
import com.example.mpazpro.data.model.Usuario

@Database(
    entities = [Unidad::class, HistorialUnidad::class, Usuario::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun unidadDao(): UnidadDao
    abstract fun usuarioDao(): UsuarioDao

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

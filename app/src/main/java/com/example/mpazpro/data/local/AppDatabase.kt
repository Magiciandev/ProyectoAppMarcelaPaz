package com.example.mpazpro.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.mpazpro.data.model.Unidad

@Database(entities = [Unidad::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun unidadDao(): UnidadDao

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

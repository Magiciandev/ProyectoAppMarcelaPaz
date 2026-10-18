package com.example.mpazpro.ui.components

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.mpazpro.R

// Recurso nativo 2: notificaciones locales. Vive en la capa de UI porque necesita Context
// (los ViewModel no lo tienen); el ViewModel solo informa qué pasó.
object Notificaciones {
    private const val CANAL = "informes"

    // Se llama una vez al iniciar. Con minSdk 29 el canal siempre es necesario.
    fun crearCanal(context: Context) {
        val canal = NotificationChannel(CANAL, context.getString(R.string.notif_canal), NotificationManager.IMPORTANCE_DEFAULT)
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(canal)
    }

    // En Android 13+ es falso hasta que se concede POST_NOTIFICATIONS; en Android 10 solo depende de los ajustes.
    fun permitidas(context: Context): Boolean = NotificationManagerCompat.from(context).areNotificationsEnabled()

    // El permiso en ejecución solo existe desde Android 13.
    fun requierePermisoEnEjecucion(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    @SuppressLint("MissingPermission") // se comprueba con permitidas() antes de notificar
    fun mostrar(context: Context, titulo: String, texto: String) {
        if (!permitidas(context)) return
        val notificacion = NotificationCompat.Builder(context, CANAL)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(titulo)
            .setContentText(texto)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(System.currentTimeMillis().toInt(), notificacion)
    }
}

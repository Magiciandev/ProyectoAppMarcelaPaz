package com.example.mpazpro.data.local

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.IOException

// Copia la imagen elegida al almacenamiento interno de la app: así sigue disponible
// aunque la foto original se mueva o se borre. Solo MainActivity le entrega el Context.
class AlmacenImagenes(private val context: Context) {

    fun copiar(origen: Uri, nombre: String): String {
        val carpeta = File(context.filesDir, "imagenes").apply { mkdirs() }
        val destino = File(carpeta, nombre)
        val entrada = context.contentResolver.openInputStream(origen)
            ?: throw IOException("No se pudo leer la imagen")
        entrada.use { e -> destino.outputStream().use { s -> e.copyTo(s) } }
        return destino.absolutePath
    }
}

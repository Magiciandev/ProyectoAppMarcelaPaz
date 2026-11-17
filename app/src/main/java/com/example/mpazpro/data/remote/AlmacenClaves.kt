package com.example.mpazpro.data.remote

import android.content.Context

// Un almacén sencillo de pares clave-valor. Es una interfaz para poder probar la sesión sin Android.
interface AlmacenClaves {
    fun leer(clave: String): String?
    fun escribir(clave: String, valor: String?)
}

/** Implementación real: SharedPreferences privadas de la app. */
class AlmacenSharedPreferences(context: Context) : AlmacenClaves {
    private val preferencias = context.applicationContext.getSharedPreferences("mpaz_sesion", Context.MODE_PRIVATE)

    override fun leer(clave: String): String? = preferencias.getString(clave, null)

    override fun escribir(clave: String, valor: String?) {
        preferencias.edit().apply {
            if (valor == null) remove(clave) else putString(clave, valor)
        }.apply()
    }
}

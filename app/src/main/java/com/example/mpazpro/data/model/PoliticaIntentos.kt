package com.example.mpazpro.data.model

// Cuenta de intentos con lo que aún no confirmó el servidor. Funciones puras, sin Android.
// Regla D7: de los intentos hechos sin conexión valen solo los primeros N (N = límite del docente). El servidor
// los procesa por fecha del dispositivo y rechaza el resto con LIMITE_INTENTOS; la app descuenta lo pendiente para
// no dejar rendir de más ni numerar mal.
object PoliticaIntentos {

    /** Intentos que el estudiante ya hizo: los que el servidor aceptó más los guardados sin enviar. */
    fun usados(delServidor: Int, pendientes: Int): Int = delServidor + pendientes.coerceAtLeast(0)

    /** Cuántos le quedan, contando los pendientes. Nunca baja de 0. */
    fun restantes(limite: Int, delServidor: Int, pendientes: Int): Int =
        (limite - usados(delServidor, pendientes)).coerceAtLeast(0)

    /** De los intentos pendientes, cuántos pueden llegar a valer: los que todavía caben dentro del límite. */
    fun queCabenEnElLimite(limite: Int, delServidor: Int, pendientes: Int): Int =
        pendientes.coerceAtLeast(0).coerceAtMost((limite - delServidor).coerceAtLeast(0))
}

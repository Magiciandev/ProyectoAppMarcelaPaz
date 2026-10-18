package com.example.mpazpro.ui.components

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Define si los íconos de las barras del sistema (hora, batería, navegación) son claros u oscuros.
// Cada pantalla lo indica según su fondo: claros sobre fondo oscuro y oscuros sobre fondo claro.
@Composable
fun IconosBarraEstado(iconosClaros: Boolean) {
    val vista = LocalView.current
    if (!vista.isInEditMode) {
        SideEffect {
            val ventana = vista.context.buscarActividad()?.window ?: return@SideEffect
            val controlador = WindowCompat.getInsetsController(ventana, vista)
            controlador.isAppearanceLightStatusBars = !iconosClaros
            controlador.isAppearanceLightNavigationBars = !iconosClaros
        }
    }
}

private tailrec fun Context.buscarActividad(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.buscarActividad()
    else -> null
}

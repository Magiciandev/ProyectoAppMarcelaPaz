package com.example.mpazpro

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.mpazpro.ui.screens.BienvenidaScreen
import com.example.mpazpro.ui.theme.MpazProTheme

// Actividad única: toda la navegación vivirá en Compose (Navigation Compose, Fase 2).
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Barras del sistema transparentes con íconos claros, porque la bienvenida es oscura.
        // Cuando existan pantallas claras, pasar a SystemBarStyle.auto(...).
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        setContent {
            MpazProTheme {
                BienvenidaScreen()
            }
        }
    }
}

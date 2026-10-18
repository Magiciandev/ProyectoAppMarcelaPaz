package com.example.mpazpro.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val EsquemaClaro = lightColorScheme(
    primary = ClaroPrimario,
    onPrimary = ClaroSobrePrimario,
    primaryContainer = ClaroContenedorPrimario,
    onPrimaryContainer = ClaroSobreContenedorPrimario,
    secondary = ClaroSecundario,
    onSecondary = ClaroSobreSecundario,
    secondaryContainer = ClaroContenedorSecundario,
    onSecondaryContainer = ClaroSobreContenedorSecundario,
    tertiary = ClaroTerciario,
    onTertiary = ClaroSobreTerciario,
    tertiaryContainer = ClaroContenedorTerciario,
    onTertiaryContainer = ClaroSobreContenedorTerciario,
    background = ClaroFondo,
    onBackground = ClaroSobreFondo,
    surface = ClaroFondo,
    onSurface = ClaroSobreFondo,
    surfaceVariant = ClaroVariante,
    onSurfaceVariant = ClaroSobreVariante,
    surfaceBright = ClaroSuperficieBrillante,
    surfaceDim = ClaroSuperficieTenue,
    surfaceContainerLowest = ClaroSuperficieMinima,
    surfaceContainerLow = ClaroSuperficieBaja,
    surfaceContainer = ClaroSuperficieMedia,
    surfaceContainerHigh = ClaroSuperficieAlta,
    surfaceContainerHighest = ClaroSuperficieMaxima
)

private val EsquemaOscuro = darkColorScheme(
    primary = OscuroPrimario,
    onPrimary = OscuroSobrePrimario,
    primaryContainer = OscuroContenedorPrimario,
    onPrimaryContainer = OscuroSobreContenedorPrimario,
    secondary = OscuroSecundario,
    onSecondary = OscuroSobreSecundario,
    secondaryContainer = OscuroContenedorSecundario,
    onSecondaryContainer = OscuroSobreContenedorSecundario,
    tertiary = OscuroTerciario,
    onTertiary = OscuroSobreTerciario,
    tertiaryContainer = OscuroContenedorTerciario,
    onTertiaryContainer = OscuroSobreContenedorTerciario,
    background = OscuroFondo,
    onBackground = OscuroSobreFondo,
    surface = OscuroFondo,
    onSurface = OscuroSobreFondo,
    surfaceVariant = OscuroVariante,
    onSurfaceVariant = OscuroSobreVariante,
    surfaceBright = OscuroSuperficieBrillante,
    surfaceDim = OscuroSuperficieTenue,
    surfaceContainerLowest = OscuroSuperficieMinima,
    surfaceContainerLow = OscuroSuperficieBaja,
    surfaceContainer = OscuroSuperficieMedia,
    surfaceContainerHigh = OscuroSuperficieAlta,
    surfaceContainerHighest = OscuroSuperficieMaxima
)

// Sin color dinámico a propósito: la app mantiene siempre su identidad visual.
@Composable
fun MpazProTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) EsquemaOscuro else EsquemaClaro

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

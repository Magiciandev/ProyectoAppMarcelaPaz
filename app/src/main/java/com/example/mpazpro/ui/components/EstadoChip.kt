package com.example.mpazpro.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mpazpro.data.model.EstadoUnidad
import com.example.mpazpro.ui.theme.MpazProTheme

// Etiqueta con el estado de una unidad; cada estado tiene su color.
@Composable
fun EstadoChip(estado: String, modifier: Modifier = Modifier) {
    val colores = MaterialTheme.colorScheme
    val (fondo, texto) = when (estado) {
        EstadoUnidad.PUBLICADO -> colores.primaryContainer to colores.onPrimaryContainer
        EstadoUnidad.EN_REVISION -> colores.secondaryContainer to colores.onSecondaryContainer
        else -> colores.surface to colores.onSurfaceVariant
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(fondo)
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(text = estado, style = MaterialTheme.typography.labelLarge, color = texto)
    }
}

@Preview(showBackground = true)
@Composable
private fun EstadoChipPreview() {
    MpazProTheme {
        EstadoChip(estado = EstadoUnidad.EN_REVISION)
    }
}

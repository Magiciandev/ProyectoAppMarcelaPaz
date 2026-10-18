package com.example.mpazpro.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import java.io.File

// Imagen de apoyo con borde redondeado. Si la unidad no tiene imagen, no dibuja nada.
@Composable
fun ImagenApoyo(ruta: String?, descripcion: String, modifier: Modifier = Modifier) {
    if (ruta == null) return
    AsyncImage(
        model = File(ruta),
        contentDescription = descripcion,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(16.dp))
    )
}

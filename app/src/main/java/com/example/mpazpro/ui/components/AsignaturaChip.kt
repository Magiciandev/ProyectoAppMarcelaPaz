package com.example.mpazpro.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mpazpro.data.model.Asignaturas
import com.example.mpazpro.ui.theme.MpazProTheme
import com.example.mpazpro.ui.theme.Pizarra
import com.example.mpazpro.ui.theme.TizaBlanca
import com.example.mpazpro.ui.theme.TizaCiencias
import com.example.mpazpro.ui.theme.TizaHistoria
import com.example.mpazpro.ui.theme.TizaLenguaje
import com.example.mpazpro.ui.theme.TizaMatematica

// Componente sin estado: recibe todo por parámetros (elevación de estado).
@Composable
fun AsignaturaChip(
    nombre: String,
    colorTiza: Color,
    modifier: Modifier = Modifier,
    inclinacion: Float = 0f
) {
    Box(
        modifier = modifier
            .rotate(inclinacion)
            .clip(RoundedCornerShape(14.dp))
            .background(colorTiza)
            .padding(horizontal = 18.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = nombre,
            style = MaterialTheme.typography.labelLarge,
            color = Pizarra
        )
    }
}

// Cada asignatura conserva siempre su color de tiza.
fun colorTizaDe(asignatura: String): Color = when (asignatura) {
    Asignaturas.LENGUAJE -> TizaLenguaje
    Asignaturas.MATEMATICA -> TizaMatematica
    Asignaturas.CIENCIAS -> TizaCiencias
    Asignaturas.HISTORIA -> TizaHistoria
    else -> TizaBlanca
}

@Preview(showBackground = true)
@Composable
private fun AsignaturaChipPreview() {
    MpazProTheme {
        AsignaturaChip(nombre = "Matemática", colorTiza = TizaMatematica, inclinacion = -2f)
    }
}

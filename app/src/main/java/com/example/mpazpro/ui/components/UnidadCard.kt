package com.example.mpazpro.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mpazpro.data.model.Asignaturas
import com.example.mpazpro.ui.theme.MpazProTheme

// Tarjeta de una unidad. Recibe textos simples, no la entidad de Room, para ser reutilizable.
@Composable
fun UnidadCard(
    titulo: String,
    detalle: String,
    asignatura: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = detalle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            AsignaturaChip(nombre = asignatura, colorTiza = colorTizaDe(asignatura))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun UnidadCardPreview() {
    MpazProTheme {
        UnidadCard(
            titulo = "Suma y resta",
            detalle = "OA05 · Borrador",
            asignatura = Asignaturas.MATEMATICA
        )
    }
}

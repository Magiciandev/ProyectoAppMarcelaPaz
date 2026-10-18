package com.example.mpazpro.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.mpazpro.R
import com.example.mpazpro.ui.theme.TextoSobreTiza
import com.example.mpazpro.ui.theme.TizaCiencias

enum class EstadoOpcion { NEUTRA, CORRECTA, INCORRECTA, BLOQUEADA }

// Alternativa de una pregunta. El color cambia con animación y siempre lleva ícono (no solo color).
@Composable
fun OpcionRespuesta(
    letra: String,
    texto: String,
    estado: EstadoOpcion,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val fondo by animateColorAsState(
        targetValue = when (estado) {
            EstadoOpcion.CORRECTA -> TizaCiencias
            EstadoOpcion.INCORRECTA -> MaterialTheme.colorScheme.errorContainer
            else -> MaterialTheme.colorScheme.surfaceVariant
        },
        label = "fondoOpcion"
    )
    val colorTexto = when (estado) {
        EstadoOpcion.CORRECTA -> TextoSobreTiza
        EstadoOpcion.INCORRECTA -> MaterialTheme.colorScheme.onErrorContainer
        else -> MaterialTheme.colorScheme.onSurface
    }
    Card(
        onClick = onClick,
        enabled = estado == EstadoOpcion.NEUTRA,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = fondo, contentColor = colorTexto,
            disabledContainerColor = fondo, disabledContentColor = colorTexto
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("$letra)", style = MaterialTheme.typography.titleLarge)
            Text(texto, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            when (estado) {
                EstadoOpcion.CORRECTA -> Icon(Icons.Filled.CheckCircle, stringResource(R.string.eval_correcta_desc))
                EstadoOpcion.INCORRECTA -> Icon(Icons.Filled.Close, stringResource(R.string.eval_incorrecta_desc))
                else -> Unit
            }
        }
    }
}

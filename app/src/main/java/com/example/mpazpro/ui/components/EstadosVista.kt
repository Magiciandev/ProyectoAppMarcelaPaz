package com.example.mpazpro.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.mpazpro.R

// Estado de carga común: ruedita con texto, para que ninguna pantalla quede en blanco.
@Composable
fun EstadoCarga(modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        CircularProgressIndicator()
        Text(stringResource(R.string.cargando), style = MaterialTheme.typography.bodyLarge)
    }
}

// Estado vacío común: ícono + frase corta. Aparece con un fundido y un leve zoom.
@Composable
fun EstadoVacio(mensaje: String, modifier: Modifier = Modifier) {
    val visible = remember { MutableTransitionState(false).apply { targetState = true } }
    AnimatedVisibility(visibleState = visible, enter = fadeIn() + scaleIn(initialScale = 0.9f)) {
        Column(
            modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(Icons.Filled.Info, contentDescription = null, modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary)
            Text(mensaje, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
        }
    }
}

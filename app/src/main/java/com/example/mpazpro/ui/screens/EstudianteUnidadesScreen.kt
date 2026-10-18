package com.example.mpazpro.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mpazpro.R
import com.example.mpazpro.ui.components.ScaffoldConVolver
import com.example.mpazpro.ui.components.UnidadCard
import com.example.mpazpro.viewmodel.EstudianteUnidadesUiState
import com.example.mpazpro.viewmodel.EstudianteUnidadesViewModel

@Composable
fun EstudianteUnidadesScreen(
    viewModel: EstudianteUnidadesViewModel,
    onRendir: (Long) -> Unit,
    onVolver: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ScaffoldConVolver(titulo = stringResource(R.string.est_titulo), onVolver = onVolver) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            when (val estado = uiState) {
                is EstudianteUnidadesUiState.Cargando -> CircularProgressIndicator()
                is EstudianteUnidadesUiState.Error ->
                    Text(estado.mensaje, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                is EstudianteUnidadesUiState.Listo ->
                    if (estado.items.isEmpty()) {
                        Text(stringResource(R.string.est_vacio), style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center, modifier = Modifier.padding(32.dp))
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(items = estado.items, key = { it.unidad.id }) { item ->
                                UnidadCard(
                                    titulo = item.unidad.titulo,
                                    detalle = item.ultimoPorcentaje?.let { stringResource(R.string.est_ultimo, it) }
                                        ?: stringResource(R.string.est_sin_intentos),
                                    asignatura = item.unidad.asignatura,
                                    onClick = { onRendir(item.unidad.id) }
                                )
                            }
                        }
                    }
            }
        }
    }
}

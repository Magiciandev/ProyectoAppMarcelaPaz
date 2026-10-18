package com.example.mpazpro.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mpazpro.R
import com.example.mpazpro.ui.components.ScaffoldConVolver
import com.example.mpazpro.viewmodel.InformesListaUiState
import com.example.mpazpro.viewmodel.InformesListaViewModel

// Docente: elige un estudiante para ver su informe.
@Composable
fun InformesListaScreen(
    viewModel: InformesListaViewModel,
    onAbrir: (estudianteId: Long) -> Unit,
    onVolver: () -> Unit
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    ScaffoldConVolver(titulo = stringResource(R.string.informes_titulo), onVolver = onVolver) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            when (val e = estado) {
                is InformesListaUiState.Cargando -> CircularProgressIndicator()
                is InformesListaUiState.Error -> Text(e.mensaje, color = MaterialTheme.colorScheme.error)
                is InformesListaUiState.Listo ->
                    if (e.estudiantes.isEmpty()) Text(stringResource(R.string.informes_vacio))
                    else LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(e.estudiantes, key = { it.id }) { est ->
                            Card(Modifier.fillMaxWidth().clickable { onAbrir(est.id) }) {
                                Column(Modifier.padding(16.dp)) {
                                    Text(est.nombre, style = MaterialTheme.typography.titleLarge)
                                    Text(est.curso.orEmpty(), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
            }
        }
    }
}

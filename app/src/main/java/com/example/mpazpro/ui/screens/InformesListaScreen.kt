package com.example.mpazpro.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import com.example.mpazpro.data.model.FechaHoraColegio
import com.example.mpazpro.ui.components.EstadoCarga
import com.example.mpazpro.ui.components.EstadoVacio
import com.example.mpazpro.ui.components.ScaffoldConVolver
import com.example.mpazpro.viewmodel.FilaMonitoreo
import com.example.mpazpro.viewmodel.InformesListaUiState
import com.example.mpazpro.viewmodel.InformesListaViewModel

// Docente: sus estudiantes según el servidor (avance, última conexión y sincronización) y elige uno para ver su informe.
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
                is InformesListaUiState.Cargando -> EstadoCarga()
                is InformesListaUiState.Error -> Column(
                    Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(e.mensaje, color = MaterialTheme.colorScheme.error)
                    Button(onClick = { viewModel.cargar() }) { Text(stringResource(R.string.informes_reintentar)) }
                }
                is InformesListaUiState.Listo ->
                    if (e.filas.isEmpty()) EstadoVacio(stringResource(R.string.informes_vacio))
                    else LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(e.filas, key = { it.estudiante.usuarioId }) { fila ->
                            TarjetaEstudiante(fila, onClick = { onAbrir(fila.estudiante.usuarioId) })
                        }
                    }
            }
        }
    }
}

@Composable
private fun TarjetaEstudiante(fila: FilaMonitoreo, onClick: () -> Unit) {
    val est = fila.estudiante
    val nunca = stringResource(R.string.informes_nunca)
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(est.nombre, style = MaterialTheme.typography.titleLarge)
            Text(est.curso, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                if (est.avanceGlobal != null) stringResource(R.string.informes_avance, est.avanceGlobal)
                else stringResource(R.string.informes_sin_avance)
            )
            Text(
                stringResource(R.string.informes_conexion, est.ultimaConexion?.let { FechaHoraColegio.formatear(it) } ?: nunca),
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                stringResource(R.string.informes_sincronizacion, est.ultimaSincronizacion?.let { FechaHoraColegio.formatear(it) } ?: nunca),
                style = MaterialTheme.typography.bodySmall
            )
            Text(fila.mensaje, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
        }
    }
}

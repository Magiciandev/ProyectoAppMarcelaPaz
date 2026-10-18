package com.example.mpazpro.ui.screens

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mpazpro.R
import com.example.mpazpro.data.model.Asignaturas
import com.example.mpazpro.data.model.EstadoUnidad
import com.example.mpazpro.data.model.HistorialUnidad
import com.example.mpazpro.data.model.TransicionesUnidad
import com.example.mpazpro.data.model.Unidad
import com.example.mpazpro.ui.components.AsignaturaChip
import com.example.mpazpro.ui.components.EstadoChip
import com.example.mpazpro.ui.components.IconosBarraEstado
import com.example.mpazpro.ui.components.colorTizaDe
import com.example.mpazpro.ui.theme.MpazProTheme
import com.example.mpazpro.viewmodel.DetalleUiState
import com.example.mpazpro.viewmodel.UnidadDetalleViewModel

// Pantalla con estado: pide cargar la unidad y observa el ViewModel.
@Composable
fun UnidadDetalleScreen(
    viewModel: UnidadDetalleViewModel,
    unidadId: Long,
    onVolver: () -> Unit
) {
    LaunchedEffect(unidadId) { viewModel.cargar(unidadId) }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val mensajeError by viewModel.mensajeError.collectAsStateWithLifecycle()

    UnidadDetalleContenido(
        uiState = uiState,
        mensajeError = mensajeError,
        onEnviar = viewModel::enviarARevision,
        onAprobar = viewModel::aprobar,
        onDevolver = viewModel::devolver,
        onVolver = onVolver
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnidadDetalleContenido(
    uiState: DetalleUiState,
    mensajeError: String?,
    onEnviar: () -> Unit,
    onAprobar: () -> Unit,
    onDevolver: () -> Unit,
    onVolver: () -> Unit
) {
    IconosBarraEstado(iconosClaros = isSystemInDarkTheme())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.detalle_titulo)) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.volver)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            when (uiState) {
                is DetalleUiState.Cargando -> CircularProgressIndicator()
                is DetalleUiState.NoEncontrada -> Text(
                    text = stringResource(R.string.detalle_no_encontrada),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(32.dp)
                )
                is DetalleUiState.Error -> Text(
                    text = uiState.mensaje,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(32.dp)
                )
                is DetalleUiState.Listo -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = uiState.unidad.titulo,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = uiState.unidad.objetivoAprendizaje,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AsignaturaChip(
                            nombre = uiState.unidad.asignatura,
                            colorTiza = colorTizaDe(uiState.unidad.asignatura)
                        )
                        EstadoChip(estado = uiState.unidad.estado)
                    }

                    Text(
                        text = stringResource(R.string.detalle_contenido),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(text = uiState.unidad.contenido, style = MaterialTheme.typography.bodyLarge)

                    if (mensajeError != null) {
                        Text(
                            text = mensajeError,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Button(
                        onClick = onAprobar,
                        enabled = uiState.puedeAprobar,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(stringResource(R.string.detalle_aprobar)) }
                    OutlinedButton(
                        onClick = onEnviar,
                        enabled = uiState.puedeEnviar,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(stringResource(R.string.detalle_enviar)) }
                    OutlinedButton(
                        onClick = onDevolver,
                        enabled = uiState.puedeDevolver,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(stringResource(R.string.detalle_devolver)) }

                    Text(
                        text = stringResource(R.string.detalle_historial),
                        style = MaterialTheme.typography.titleMedium
                    )
                    uiState.historial.forEach { entrada ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = entrada.fecha,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(text = entrada.evento, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun DetalleEnRevisionPreview() {
    MpazProTheme {
        UnidadDetalleContenido(
            uiState = DetalleUiState.Listo(
                unidad = Unidad(
                    id = 2, titulo = "Suma y resta", objetivoAprendizaje = "OA05",
                    asignatura = Asignaturas.MATEMATICA, estado = EstadoUnidad.EN_REVISION,
                    contenido = "Sumar es juntar cantidades. Restar es quitar una cantidad de otra."
                ),
                historial = listOf(
                    HistorialUnidad(2, 2, "2026-09-18", TransicionesUnidad.EVENTO_REVISION),
                    HistorialUnidad(1, 2, "2026-09-12", TransicionesUnidad.EVENTO_CREADA)
                )
            ),
            mensajeError = null,
            onEnviar = {}, onAprobar = {}, onDevolver = {}, onVolver = {}
        )
    }
}

package com.example.mpazpro.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mpazpro.R
import com.example.mpazpro.data.model.EstadoUnidad
import com.example.mpazpro.ui.components.AsignaturaChip
import com.example.mpazpro.ui.components.ScaffoldConVolver
import com.example.mpazpro.ui.components.colorTizaDe
import com.example.mpazpro.viewmodel.UnidadDetalleUiState
import com.example.mpazpro.viewmodel.UnidadDetalleViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun UnidadDetalleScreen(
    viewModel: UnidadDetalleViewModel,
    editable: Boolean,
    onEditarPreguntas: (Long) -> Unit,
    onVolver: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    ScaffoldConVolver(titulo = stringResource(R.string.detalle_titulo), onVolver = onVolver) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            when (val estado = uiState) {
                is UnidadDetalleUiState.Cargando -> CircularProgressIndicator()
                is UnidadDetalleUiState.NoEncontrada -> Text(stringResource(R.string.detalle_no_existe))
                is UnidadDetalleUiState.Error -> Text(estado.mensaje, color = MaterialTheme.colorScheme.error)
                is UnidadDetalleUiState.Listo -> {
                    val unidad = estado.unidad
                    val formato = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                    Column(
                        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(unidad.titulo, style = MaterialTheme.typography.headlineSmall)
                        Text("${unidad.objetivoAprendizaje} · ${unidad.estado}", style = MaterialTheme.typography.titleMedium)
                        AsignaturaChip(nombre = unidad.asignatura, colorTiza = colorTizaDe(unidad.asignatura))
                        Text(unidad.contenido, style = MaterialTheme.typography.bodyLarge)

                        if (editable) {
                            when (unidad.estado) {
                                EstadoUnidad.BORRADOR -> BotonAccion(stringResource(R.string.detalle_enviar), viewModel::enviarARevision)
                                EstadoUnidad.EN_REVISION -> {
                                    BotonAccion(stringResource(R.string.detalle_aprobar), viewModel::aprobar)
                                    OutlinedButton(onClick = viewModel::rechazar, modifier = Modifier.fillMaxWidth()) {
                                        Text(stringResource(R.string.detalle_rechazar))
                                    }
                                }
                                else -> OutlinedButton(onClick = viewModel::deshabilitar, modifier = Modifier.fillMaxWidth()) {
                                    Text(stringResource(R.string.detalle_deshabilitar))
                                }
                            }
                            OutlinedButton(onClick = { onEditarPreguntas(unidad.id) }, modifier = Modifier.fillMaxWidth()) {
                                Text(stringResource(R.string.detalle_preguntas))
                            }
                        }
                        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

                        HorizontalDivider()
                        Text(stringResource(R.string.detalle_historial), style = MaterialTheme.typography.titleMedium)
                        estado.historial.forEach { h ->
                            Text("${formato.format(Date(h.fecha))} · ${h.evento}", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BotonAccion(texto: String, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = Modifier.fillMaxWidth()) { Text(texto) }
}

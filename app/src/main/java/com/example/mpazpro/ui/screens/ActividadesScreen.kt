package com.example.mpazpro.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.mpazpro.ui.components.EstadoCarga
import com.example.mpazpro.ui.components.ScaffoldConVolver
import com.example.mpazpro.viewmodel.ActividadesUiState
import com.example.mpazpro.viewmodel.ActividadesViewModel

// Estudiante: practica las actividades de una unidad del servidor (lista → pregunta a pregunta → resultado).
@Composable
fun ActividadesScreen(viewModel: ActividadesViewModel, onVolver: () -> Unit) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Mientras practica, la barra muestra el nombre de la actividad.
    val titulo = (uiState as? ActividadesUiState.Practicando)?.titulo ?: stringResource(R.string.actividades_titulo)

    ScaffoldConVolver(titulo = titulo, onVolver = onVolver) { padding ->
        Box(Modifier.fillMaxSize().padding(padding).padding(16.dp), contentAlignment = Alignment.Center) {
            when (val estado = uiState) {
                is ActividadesUiState.Cargando -> EstadoCarga()
                is ActividadesUiState.Mensaje -> Text(
                    estado.texto, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center,
                    color = if (estado.esError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
                is ActividadesUiState.Lista -> Column(
                    modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(stringResource(R.string.actividades_elegir), style = MaterialTheme.typography.bodyLarge)
                    estado.actividades.forEach { actividad ->
                        OutlinedButton(onClick = { viewModel.abrir(actividad.id) }, modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                                Text(actividad.titulo, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    stringResource(R.string.actividad_detalle, actividad.preguntas.size, actividad.dificultad),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
                is ActividadesUiState.Practicando -> PreguntaVista(
                    estado.enCurso,
                    AccionesPregunta(viewModel::elegir, viewModel::deshacer, viewModel::comprobar, viewModel::siguiente)
                )
                is ActividadesUiState.Resultado -> Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(estado.titulo, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
                    Text(
                        stringResource(R.string.actividad_resultado, estado.aciertos, estado.total),
                        style = MaterialTheme.typography.displaySmall, textAlign = TextAlign.Center
                    )
                    Button(onClick = viewModel::volverALista, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                        Text(stringResource(R.string.actividad_otra))
                    }
                    OutlinedButton(onClick = onVolver, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                        Text(stringResource(R.string.eval_volver_lista))
                    }
                }
            }
        }
    }
}

package com.example.mpazpro.ui.screens

import com.example.mpazpro.ui.components.EstadoCarga
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
import com.example.mpazpro.ui.components.AsignaturaChip
import com.example.mpazpro.ui.components.ImagenApoyo
import com.example.mpazpro.ui.components.ScaffoldConVolver
import com.example.mpazpro.ui.components.colorTizaDe
import com.example.mpazpro.viewmodel.ContenidoUiState
import com.example.mpazpro.viewmodel.ContenidoViewModel

// Estudiante: lee el contenido de la unidad (con su imagen de apoyo) y pasa a la prueba.
@Composable
fun ContenidoScreen(viewModel: ContenidoViewModel, onRendir: () -> Unit, onVolver: () -> Unit) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ScaffoldConVolver(titulo = stringResource(R.string.contenido_titulo), onVolver = onVolver) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            when (val estado = uiState) {
                is ContenidoUiState.Cargando -> EstadoCarga()
                is ContenidoUiState.NoEncontrada -> Text(stringResource(R.string.detalle_no_existe))
                is ContenidoUiState.Error -> Text(estado.mensaje, color = MaterialTheme.colorScheme.error)
                is ContenidoUiState.Listo -> {
                    val u = estado.unidad
                    Column(
                        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(u.titulo, style = MaterialTheme.typography.headlineMedium)
                        AsignaturaChip(nombre = u.asignatura, colorTiza = colorTizaDe(u.asignatura))
                        ImagenApoyo(ruta = u.imagenRuta, descripcion = stringResource(R.string.imagen_desc, u.titulo))
                        Text(u.contenido, style = MaterialTheme.typography.bodyLarge)
                        Button(onClick = onRendir, modifier = Modifier.fillMaxWidth().height(60.dp)) {
                            Text(stringResource(R.string.contenido_rendir), style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
        }
    }
}

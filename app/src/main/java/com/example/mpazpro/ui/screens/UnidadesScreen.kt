package com.example.mpazpro.ui.screens

import com.example.mpazpro.ui.components.EstadoCarga
import com.example.mpazpro.ui.components.EstadoVacio
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
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
import com.example.mpazpro.data.model.Unidad
import com.example.mpazpro.ui.components.IconosBarraEstado
import com.example.mpazpro.ui.components.UnidadCard
import com.example.mpazpro.ui.theme.MpazProTheme
import com.example.mpazpro.viewmodel.UnidadesUiState
import com.example.mpazpro.viewmodel.UnidadesViewModel

// Pantalla con estado: observa el ViewModel y delega el dibujo en UnidadesContenido.
@Composable
fun UnidadesScreen(
    viewModel: UnidadesViewModel,
    editable: Boolean,
    onAbrir: (Long) -> Unit,
    onNuevaUnidad: () -> Unit,
    onVolver: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    UnidadesContenido(
        uiState = uiState, editable = editable, onAbrir = onAbrir,
        onNuevaUnidad = onNuevaUnidad, onVolver = onVolver
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnidadesContenido(
    uiState: UnidadesUiState,
    editable: Boolean,
    onAbrir: (Long) -> Unit,
    onNuevaUnidad: () -> Unit,
    onVolver: () -> Unit
) {
    IconosBarraEstado(iconosClaros = isSystemInDarkTheme())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.unidades_titulo)) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.volver)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (editable) {
                ExtendedFloatingActionButton(
                    onClick = onNuevaUnidad,
                    icon = { Icon(imageVector = Icons.Filled.Add, contentDescription = null) },
                    text = { Text(stringResource(R.string.unidades_nueva)) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            when (uiState) {
                is UnidadesUiState.Cargando -> EstadoCarga()
                is UnidadesUiState.Error -> Text(
                    text = uiState.mensaje,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(32.dp)
                )
                is UnidadesUiState.Listo -> {
                    if (uiState.unidades.isEmpty()) {
                        EstadoVacio(stringResource(R.string.unidades_vacio))
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = 16.dp, top = 16.dp, end = 16.dp, bottom = 96.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(items = uiState.unidades, key = { it.id }) { unidad ->
                                UnidadCard(
                                    titulo = unidad.titulo,
                                    detalle = unidad.objetivoAprendizaje,
                                    asignatura = unidad.asignatura,
                                    estado = unidad.estado,
                                    onClick = { onAbrir(unidad.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun UnidadesConDatosPreview() {
    MpazProTheme {
        UnidadesContenido(
            uiState = UnidadesUiState.Listo(
                listOf(
                    Unidad(
                        id = 2, titulo = "Los sentidos", objetivoAprendizaje = "OA04",
                        asignatura = Asignaturas.CIENCIAS, estado = EstadoUnidad.BORRADOR,
                        contenido = "Exploraremos los cinco sentidos."
                    ),
                    Unidad(
                        id = 1, titulo = "Suma y resta", objetivoAprendizaje = "OA05",
                        asignatura = Asignaturas.MATEMATICA, estado = EstadoUnidad.BORRADOR,
                        contenido = "Sumar es juntar cantidades."
                    )
                )
            ),
            editable = true,
            onAbrir = {},
            onNuevaUnidad = {},
            onVolver = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun UnidadesVaciasPreview() {
    MpazProTheme {
        UnidadesContenido(
            uiState = UnidadesUiState.Listo(emptyList()),
            editable = true,
            onAbrir = {},
            onNuevaUnidad = {},
            onVolver = {}
        )
    }
}

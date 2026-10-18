package com.example.mpazpro.ui.screens

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
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

// Panel de la Jefatura UTP: lista de unidades con su estado. Pantalla con estado.
@Composable
fun UnidadesScreen(
    viewModel: UnidadesViewModel,
    onNuevaUnidad: () -> Unit,
    onAbrirUnidad: (Long) -> Unit,
    onCerrarSesion: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    UnidadesContenido(
        uiState = uiState,
        onNuevaUnidad = onNuevaUnidad,
        onAbrirUnidad = onAbrirUnidad,
        onCerrarSesion = onCerrarSesion
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnidadesContenido(
    uiState: UnidadesUiState,
    onNuevaUnidad: () -> Unit,
    onAbrirUnidad: (Long) -> Unit,
    onCerrarSesion: () -> Unit
) {
    IconosBarraEstado(iconosClaros = isSystemInDarkTheme())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.unidades_titulo)) },
                actions = {
                    IconButton(onClick = onCerrarSesion) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = stringResource(R.string.cerrar_sesion)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNuevaUnidad,
                icon = { Icon(imageVector = Icons.Filled.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.unidades_nueva)) }
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
                is UnidadesUiState.Cargando -> CircularProgressIndicator()
                is UnidadesUiState.Error -> Text(
                    text = uiState.mensaje,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(32.dp)
                )
                is UnidadesUiState.Listo -> {
                    if (uiState.unidades.isEmpty()) {
                        Text(
                            text = stringResource(R.string.unidades_vacio),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(32.dp)
                        )
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
                                    onClick = { onAbrirUnidad(unidad.id) }
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
                        asignatura = Asignaturas.CIENCIAS, estado = EstadoUnidad.EN_REVISION,
                        contenido = "Exploraremos los cinco sentidos."
                    ),
                    Unidad(
                        id = 1, titulo = "Suma y resta", objetivoAprendizaje = "OA05",
                        asignatura = Asignaturas.MATEMATICA, estado = EstadoUnidad.PUBLICADO,
                        contenido = "Sumar es juntar cantidades."
                    )
                )
            ),
            onNuevaUnidad = {},
            onAbrirUnidad = {},
            onCerrarSesion = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun UnidadesVaciasPreview() {
    MpazProTheme {
        UnidadesContenido(
            uiState = UnidadesUiState.Listo(emptyList()),
            onNuevaUnidad = {},
            onAbrirUnidad = {},
            onCerrarSesion = {}
        )
    }
}

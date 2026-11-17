package com.example.mpazpro.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mpazpro.R
import com.example.mpazpro.ui.components.EstadoCarga
import com.example.mpazpro.ui.components.EstadoChip
import com.example.mpazpro.ui.components.EstadoVacio
import com.example.mpazpro.ui.components.ScaffoldConVolver
import com.example.mpazpro.viewmodel.AsignaturaCatalogo
import com.example.mpazpro.viewmodel.CatalogoUiState
import com.example.mpazpro.viewmodel.CatalogoViewModel

// Catálogo que entrega el servidor, guardado en Room. Cada perfil ve lo que su rol puede ver
// (UTP todo; docente lo autorizado y publicado; estudiante lo publicado).
@Composable
fun CatalogoScreen(viewModel: CatalogoViewModel, onVolver: () -> Unit) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    ScaffoldConVolver(titulo = stringResource(R.string.catalogo_titulo), onVolver = onVolver) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
            when (val e = estado) {
                is CatalogoUiState.Cargando -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { EstadoCarga() }
                is CatalogoUiState.Listo -> Contenido(e, onActualizar = viewModel::actualizar)
            }
        }
    }
}

@Composable
private fun Contenido(estado: CatalogoUiState.Listo, onActualizar: () -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = estado.sesion?.let { stringResource(R.string.catalogo_sesion, it) }
                        ?: stringResource(R.string.catalogo_sin_sesion),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(onClick = onActualizar, enabled = !estado.actualizando, modifier = Modifier.fillMaxWidth()) {
                    if (estado.actualizando) {
                        CircularProgressIndicator(Modifier.padding(end = 8.dp).size(18.dp), strokeWidth = 2.dp)
                    }
                    Text(stringResource(R.string.catalogo_actualizar))
                }
                estado.aviso?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        }
        if (estado.asignaturas.isEmpty()) {
            item { EstadoVacio(stringResource(R.string.catalogo_vacio), Modifier.fillMaxWidth()) }
        }
        items(estado.asignaturas, key = { it.asignatura.id }) { asignatura -> TarjetaAsignatura(asignatura) }
    }
}

@Composable
private fun TarjetaAsignatura(item: AsignaturaCatalogo) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(item.asignatura.nombre, style = MaterialTheme.typography.titleLarge)
            if (item.unidades.isEmpty()) {
                Text(
                    stringResource(R.string.catalogo_sin_unidades),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            item.unidades.forEach { unidad ->
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(unidad.unidad.titulo, style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f).padding(end = 8.dp))
                    EstadoChip(unidad.unidad.estado)
                }
                unidad.objetivos.forEach { o ->
                    Column(Modifier.padding(start = 12.dp)) {
                        Text("${o.objetivo.codigo} · ${o.objetivo.descripcion}", style = MaterialTheme.typography.bodyMedium)
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            EstadoChip(o.objetivo.estado)
                            Text(
                                pluralStringResource(R.plurals.catalogo_contenidos, o.cantidadContenidos, o.cantidadContenidos),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

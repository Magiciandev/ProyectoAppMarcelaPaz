package com.example.mpazpro.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mpazpro.R
import com.example.mpazpro.ui.components.CampoValidado
import com.example.mpazpro.ui.components.ScaffoldConVolver
import com.example.mpazpro.viewmodel.PreguntasEditorViewModel
import com.example.mpazpro.viewmodel.PreguntasUiState
import com.example.mpazpro.viewmodel.validation.ValidadorPregunta

private val LETRAS = listOf("A", "B", "C", "D")

@Composable
fun PreguntasEditorScreen(viewModel: PreguntasEditorViewModel, onVolver: () -> Unit) {
    val formulario by viewModel.formulario.collectAsStateWithLifecycle()
    val errores by viewModel.errores.collectAsStateWithLifecycle()
    val lista by viewModel.preguntas.collectAsStateWithLifecycle()
    val mensaje by viewModel.mensaje.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(mensaje) {
        mensaje?.let {
            snackbar.showSnackbar(it)
            viewModel.mensajeMostrado()
        }
    }

    ScaffoldConVolver(
        titulo = stringResource(R.string.preguntas_titulo), onVolver = onVolver, snackbarHostState = snackbar
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).imePadding(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                CampoValidado(
                    valor = formulario.texto, onValorChange = viewModel::actualizarTexto,
                    etiqueta = stringResource(R.string.preg_label_texto),
                    iconoInicial = Icons.Filled.Create, error = errores.texto,
                    maxCaracteres = ValidadorPregunta.TEXTO_MAX
                )
            }
            items(ValidadorPregunta.CANTIDAD_OPCIONES) { i ->
                Row(verticalAlignment = Alignment.Top) {
                    RadioButton(
                        selected = formulario.correcta == i,
                        onClick = { viewModel.elegirCorrecta(i) },
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    CampoValidado(
                        valor = formulario.opciones[i],
                        onValorChange = { viewModel.actualizarOpcion(i, it) },
                        etiqueta = stringResource(R.string.preg_label_opcion, LETRAS[i]),
                        iconoInicial = if (formulario.correcta == i) Icons.Filled.Check else Icons.Filled.Create,
                        error = errores.opciones[i],
                        maxCaracteres = ValidadorPregunta.OPCION_MAX
                    )
                }
            }
            item {
                errores.correcta?.let {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Filled.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                Button(onClick = viewModel::agregar, modifier = Modifier.fillMaxWidth().height(52.dp).padding(top = 8.dp)) {
                    Text(stringResource(R.string.preg_agregar))
                }
            }
            when (val estado = lista) {
                is PreguntasUiState.Cargando -> Unit
                is PreguntasUiState.Error -> item { Text(estado.mensaje, color = MaterialTheme.colorScheme.error) }
                is PreguntasUiState.Listo -> {
                    item {
                        Text(
                            stringResource(R.string.preg_lista, estado.preguntas.size),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                        if (estado.preguntas.isEmpty()) Text(stringResource(R.string.preg_vacio))
                    }
                    items(items = estado.preguntas, key = { it.id }) { p ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                androidx.compose.foundation.layout.Column(Modifier.weight(1f)) {
                                    Text(p.texto, style = MaterialTheme.typography.titleSmall)
                                    p.opciones().forEachIndexed { i, o ->
                                        Text(
                                            "${LETRAS[i]}) $o" + if (i == p.indiceCorrecta) "  ✔" else "",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                                IconButton(onClick = { viewModel.eliminar(p.id) }) {
                                    Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.preg_eliminar))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

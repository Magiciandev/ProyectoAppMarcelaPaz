package com.example.mpazpro.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import android.Manifest
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mpazpro.R
import com.example.mpazpro.data.model.CalculoProgreso
import com.example.mpazpro.data.model.DetalleUnidad
import com.example.mpazpro.data.model.InformeEstudiante
import com.example.mpazpro.ui.components.AsignaturaChip
import com.example.mpazpro.ui.components.CampoValidado
import com.example.mpazpro.ui.components.Notificaciones
import com.example.mpazpro.ui.components.ScaffoldConVolver
import com.example.mpazpro.ui.components.colorTizaDe
import com.example.mpazpro.viewmodel.AccionUiState
import com.example.mpazpro.viewmodel.InformeUiState
import com.example.mpazpro.viewmodel.InformeViewModel
import com.example.mpazpro.viewmodel.TipoReinicio
import com.example.mpazpro.viewmodel.validation.ValidadorObservacion

// Informe reservado al docente: % por asignatura, desglose, observación, reinicio y avisos.
@Composable
fun InformeScreen(viewModel: InformeViewModel, onVolver: () -> Unit) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    val borrador by viewModel.borrador.collectAsStateWithLifecycle()
    val errorObs by viewModel.errorObservacion.collectAsStateWithLifecycle()
    val pendiente by viewModel.pendiente.collectAsStateWithLifecycle()
    val accion by viewModel.accion.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    val tituloAviso = stringResource(R.string.notif_titulo)

    // Permiso de notificaciones: solo se pide desde Android 13 y por decisión del docente.
    var avisosActivos by remember { mutableStateOf(Notificaciones.permitidas(context)) }
    val lanzador = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        avisosActivos = concedido
    }

    LaunchedEffect(accion) {
        when (val a = accion) {
            is AccionUiState.Success -> {
                Notificaciones.mostrar(context, tituloAviso, a.mensaje) // no hace nada sin permiso
                snackbar.showSnackbar(a.mensaje); viewModel.consumirAccion()
            }
            is AccionUiState.Error -> { snackbar.showSnackbar(a.mensaje); viewModel.consumirAccion() }
            else -> Unit
        }
    }

    pendiente?.let { p ->
        val que = stringResource(if (p.tipo == TipoReinicio.PROGRESO) R.string.reinicio_progreso else R.string.reinicio_intentos)
        AlertDialog(
            onDismissRequest = viewModel::cancelarReinicio,
            title = { Text(stringResource(R.string.reinicio_confirmar_titulo)) },
            text = { Text(stringResource(R.string.reinicio_confirmar_texto, que.lowercase(), p.asignatura ?: stringResource(R.string.reinicio_todo))) },
            confirmButton = { TextButton(onClick = viewModel::confirmarReinicio) { Text(stringResource(R.string.reinicio_si)) } },
            dismissButton = { TextButton(onClick = viewModel::cancelarReinicio) { Text(stringResource(R.string.asignar_cancelar)) } }
        )
    }

    ScaffoldConVolver(titulo = stringResource(R.string.informe_titulo), onVolver = onVolver, snackbarHostState = snackbar) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            when (val e = estado) {
                is InformeUiState.Cargando -> CircularProgressIndicator()
                is InformeUiState.Error -> Text(e.mensaje, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(24.dp))
                is InformeUiState.Listo -> Column(
                    modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Cabecera(e.informe)
                    if (Notificaciones.requierePermisoEnEjecucion() && !avisosActivos) {
                        OutlinedButton(
                            onClick = { lanzador.launch(Manifest.permission.POST_NOTIFICATIONS) },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(stringResource(R.string.notif_activar)) }
                    }
                    e.informe.porAsignatura.forEach { r ->
                        val tiene = e.informe.detalle.any { it.asignatura == r.asignatura }
                        FilaAsignatura(
                            asignatura = r.asignatura, porcentaje = r.porcentaje, tieneEvaluaciones = tiene,
                            onReiniciarProgreso = { viewModel.pedirReinicio(TipoReinicio.PROGRESO, r.asignatura) },
                            onReiniciarIntentos = { viewModel.pedirReinicio(TipoReinicio.INTENTOS, r.asignatura) }
                        )
                    }
                    HorizontalDivider()
                    Text(stringResource(R.string.informe_detalle), style = MaterialTheme.typography.titleLarge)
                    if (e.informe.detalle.isEmpty()) Text(stringResource(R.string.informe_sin_evaluaciones))
                    e.informe.detalle.forEach { DetalleCard(it) }
                    HorizontalDivider()
                    CampoValidado(
                        valor = borrador ?: e.informe.observacion,
                        onValorChange = viewModel::onObservacion,
                        etiqueta = stringResource(R.string.observacion_etiqueta),
                        iconoInicial = Icons.Filled.Info,
                        error = errorObs,
                        maxCaracteres = ValidadorObservacion.MAXIMO,
                        minLineas = 3
                    )
                    Button(onClick = viewModel::guardarObservacion, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.observacion_guardar))
                    }
                    HorizontalDivider()
                    OutlinedButton(onClick = { viewModel.pedirReinicio(TipoReinicio.PROGRESO, null) }, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.reinicio_progreso_todo))
                    }
                    OutlinedButton(onClick = { viewModel.pedirReinicio(TipoReinicio.INTENTOS, null) }, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.reinicio_intentos_todo))
                    }
                }
            }
        }
    }
}

@Composable
private fun Cabecera(informe: InformeEstudiante) {
    val progreso by animateFloatAsState(informe.general / 100f, label = "progresoGeneral")
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(informe.estudiante.nombre, style = MaterialTheme.typography.headlineMedium)
        Text(informe.estudiante.curso.orEmpty(), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(stringResource(R.string.informe_general, informe.general), style = MaterialTheme.typography.titleLarge)
        LinearProgressIndicator(progress = { progreso }, modifier = Modifier.fillMaxWidth())
        Text(stringResource(R.string.informe_aviso_umbrales), style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun FilaAsignatura(
    asignatura: String, porcentaje: Int, tieneEvaluaciones: Boolean,
    onReiniciarProgreso: () -> Unit, onReiniciarIntentos: () -> Unit
) {
    val progreso by animateFloatAsState(porcentaje / 100f, label = "progresoAsignatura")
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()) {
                AsignaturaChip(asignatura, colorTizaDe(asignatura))
                Text("$porcentaje %", style = MaterialTheme.typography.titleLarge)
            }
            LinearProgressIndicator(progress = { progreso }, color = colorTizaDe(asignatura), modifier = Modifier.fillMaxWidth())
            Text(
                if (tieneEvaluaciones) CalculoProgreso.nivel(porcentaje).etiqueta else stringResource(R.string.informe_sin_evaluar),
                style = MaterialTheme.typography.bodyMedium
            )
            Row {
                TextButton(onClick = onReiniciarProgreso) { Text(stringResource(R.string.reinicio_progreso)) }
                TextButton(onClick = onReiniciarIntentos) { Text(stringResource(R.string.reinicio_intentos)) }
            }
        }
    }
}

@Composable
private fun DetalleCard(d: DetalleUnidad) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("${d.titulo} · ${d.porcentaje} %", style = MaterialTheme.typography.titleMedium)
            d.desglose.forEach { p ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        if (p.acierto) Icons.Filled.Check else Icons.Filled.Close,
                        contentDescription = stringResource(if (p.acierto) R.string.informe_acierto else R.string.informe_error),
                        tint = if (p.acierto) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                    Column {
                        Text(p.texto)
                        Text(
                            stringResource(R.string.informe_respondio, p.elegida ?: "—") +
                                if (p.acierto) "" else " · " + stringResource(R.string.informe_correcta, p.correcta),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

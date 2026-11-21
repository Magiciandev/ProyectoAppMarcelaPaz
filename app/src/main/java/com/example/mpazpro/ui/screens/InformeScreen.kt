package com.example.mpazpro.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mpazpro.R
import com.example.mpazpro.data.model.FichaEstudiante
import com.example.mpazpro.data.model.InformeFicha
import com.example.mpazpro.data.model.Nivel
import com.example.mpazpro.data.model.ObjetivoFicha
import com.example.mpazpro.ui.components.CampoValidado
import com.example.mpazpro.ui.components.EstadoCarga
import com.example.mpazpro.ui.components.Notificaciones
import com.example.mpazpro.ui.components.ScaffoldConVolver
import com.example.mpazpro.viewmodel.AccionUiState
import com.example.mpazpro.viewmodel.InformeUiState
import com.example.mpazpro.viewmodel.InformeViewModel
import com.example.mpazpro.viewmodel.validation.ValidadorObservacion

// Informe reservado al docente, con datos del servidor: resumen por objetivo, informe, observación, validación y reinicio.
@Composable
fun InformeScreen(viewModel: InformeViewModel, onVolver: () -> Unit) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    val borrador by viewModel.borrador.collectAsStateWithLifecycle()
    val errorObs by viewModel.errorObservacion.collectAsStateWithLifecycle()
    val reinicioPendiente by viewModel.reinicioPendiente.collectAsStateWithLifecycle()
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

    if (reinicioPendiente) {
        AlertDialog(
            onDismissRequest = viewModel::cancelarReinicio,
            title = { Text(stringResource(R.string.reinicio_confirmar_titulo)) },
            text = { Text(stringResource(R.string.reinicio_servidor_texto)) },
            confirmButton = { TextButton(onClick = viewModel::confirmarReinicio) { Text(stringResource(R.string.reinicio_si)) } },
            dismissButton = { TextButton(onClick = viewModel::cancelarReinicio) { Text(stringResource(R.string.asignar_cancelar)) } }
        )
    }

    ScaffoldConVolver(titulo = stringResource(R.string.informe_titulo), onVolver = onVolver, snackbarHostState = snackbar) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            when (val e = estado) {
                is InformeUiState.Cargando -> EstadoCarga()
                is InformeUiState.Error -> Column(
                    Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(e.mensaje, color = MaterialTheme.colorScheme.error)
                    Button(onClick = viewModel::cargar) { Text(stringResource(R.string.informes_reintentar)) }
                }
                is InformeUiState.Listo -> Column(
                    modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Cabecera(e.ficha)
                    if (Notificaciones.requierePermisoEnEjecucion() && !avisosActivos) {
                        OutlinedButton(
                            onClick = { lanzador.launch(Manifest.permission.POST_NOTIFICATIONS) },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(stringResource(R.string.notif_activar)) }
                    }
                    if (e.ficha.objetivos.isEmpty()) Text(stringResource(R.string.informe_sin_objetivos))
                    e.ficha.objetivos.forEach { TarjetaObjetivo(it) }
                    HorizontalDivider()
                    SeccionInforme(e.ficha.informe)
                    Button(onClick = viewModel::generarInforme, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(if (e.ficha.informe == null) R.string.informe_generar else R.string.informe_generar_nuevo))
                    }
                    if (e.ficha.informe != null) {
                        CampoValidado(
                            valor = borrador ?: e.ficha.informe.observacion,
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
                        OutlinedButton(
                            onClick = viewModel::validarInforme,
                            enabled = !e.ficha.informe.validado,
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(stringResource(R.string.informe_validar)) }
                    }
                    HorizontalDivider()
                    OutlinedButton(onClick = viewModel::pedirReinicio, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.reinicio_servidor))
                    }
                }
            }
        }
    }
}

@Composable
private fun Cabecera(ficha: FichaEstudiante) {
    val general = ficha.promedioGeneral
    val progreso by animateFloatAsState((general ?: 0) / 100f, label = "progresoGeneral")
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(ficha.nombre, style = MaterialTheme.typography.headlineMedium)
        Text(
            if (general != null) stringResource(R.string.informe_general, general) else stringResource(R.string.informe_sin_pruebas),
            style = MaterialTheme.typography.titleLarge
        )
        LinearProgressIndicator(progress = { progreso }, modifier = Modifier.fillMaxWidth())
        ficha.nivelGeneral?.let { Text(it.etiqueta, style = MaterialTheme.typography.bodyMedium) }
        Text(stringResource(R.string.informe_aviso_umbrales), style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun TarjetaObjetivo(o: ObjetivoFicha) {
    val progreso by animateFloatAsState(o.avance / 100f, label = "avanceObjetivo")
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(o.nombre, style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.informes_avance, o.avance))
            LinearProgressIndicator(progress = { progreso }, modifier = Modifier.fillMaxWidth())
            Text(stringResource(R.string.informe_intentos, o.intentosAceptados.toInt(), o.limiteIntentos))
            Text(
                if (o.mejorPorcentaje != null) {
                    stringResource(R.string.informe_mejor, o.mejorPorcentaje, (o.nivel ?: Nivel.REFUERZO).etiqueta)
                } else {
                    stringResource(R.string.informe_sin_evaluar)
                },
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun SeccionInforme(informe: InformeFicha?) {
    Text(stringResource(R.string.informe_seccion), style = MaterialTheme.typography.titleLarge)
    if (informe == null) {
        Text(stringResource(R.string.informe_sin_informe))
        return
    }
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(stringResource(R.string.informe_emitido, informe.emitidoEn), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.informe_puntaje, informe.puntajeGlobal, informe.nivel?.etiqueta ?: "—"))
            Text(stringResource(if (informe.validado) R.string.informe_validado else R.string.informe_pendiente_validar))
            if (informe.areasDeRefuerzo.isNotEmpty()) {
                Text(stringResource(R.string.informe_refuerzo, informe.areasDeRefuerzo.joinToString(", ")))
            }
            informe.respuestas.forEach {
                Text(stringResource(R.string.informe_respuestas, it.nombre, it.correctas, it.total),
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

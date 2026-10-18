package com.example.mpazpro.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
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
import com.example.mpazpro.data.model.Cursos
import com.example.mpazpro.ui.components.CampoDesplegable
import com.example.mpazpro.ui.components.CampoFecha
import com.example.mpazpro.ui.components.ScaffoldConVolver
import com.example.mpazpro.ui.components.formatearFecha
import com.example.mpazpro.viewmodel.AsignarViewModel
import com.example.mpazpro.viewmodel.GuardadoUiState
import com.example.mpazpro.viewmodel.validation.ValidadorAsignacion

// Docente: asigna la unidad a un curso, define fechas y límite de intentos.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AsignarScreen(viewModel: AsignarViewModel, onVolver: () -> Unit) {
    val f by viewModel.formulario.collectAsStateWithLifecycle()
    val e by viewModel.errores.collectAsStateWithLifecycle()
    val guardado by viewModel.guardado.collectAsStateWithLifecycle()
    val asignaciones by viewModel.asignaciones.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val msgOk = stringResource(R.string.asignar_guardada)

    LaunchedEffect(guardado) {
        when (val g = guardado) {
            is GuardadoUiState.Success -> { snackbar.showSnackbar(msgOk); viewModel.consumirGuardado() }
            is GuardadoUiState.Error -> { snackbar.showSnackbar(g.mensaje); viewModel.consumirGuardado() }
            else -> Unit
        }
    }

    ScaffoldConVolver(titulo = stringResource(R.string.asignar_titulo), onVolver = onVolver, snackbarHostState = snackbar) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CampoDesplegable(
                valor = f.curso, opciones = Cursos.todos, onSeleccion = viewModel::onCurso,
                etiqueta = stringResource(R.string.asignar_curso), iconoInicial = Icons.Filled.Info, error = e.curso
            )
            CampoFecha(valor = f.desde, onFecha = viewModel::onDesde, etiqueta = stringResource(R.string.asignar_desde),
                modifier = Modifier.fillMaxWidth(), error = e.desde)
            CampoFecha(valor = f.hasta, onFecha = viewModel::onHasta, etiqueta = stringResource(R.string.asignar_hasta),
                modifier = Modifier.fillMaxWidth(), error = e.hasta)

            Text(stringResource(R.string.asignar_intentos), style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                (ValidadorAsignacion.INTENTOS_MIN..ValidadorAsignacion.INTENTOS_MAX).forEach { n ->
                    FilterChip(selected = f.intentos == n, onClick = { viewModel.onIntentos(n) }, label = { Text("$n") })
                }
            }
            e.intentos?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Switch(checked = f.activa, onCheckedChange = viewModel::onActiva)
                Text(stringResource(R.string.asignar_activa))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = f.confirmado, onCheckedChange = viewModel::onConfirmado)
                Text(stringResource(R.string.asignar_confirmo))
            }
            e.confirmado?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            Button(onClick = viewModel::guardar, enabled = guardado !is GuardadoUiState.Loading,
                modifier = Modifier.fillMaxWidth().height(56.dp)) {
                if (guardado is GuardadoUiState.Loading) CircularProgressIndicator(Modifier.height(24.dp))
                else Text(stringResource(R.string.asignar_guardar))
            }

            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            Text(stringResource(R.string.asignar_vigentes), style = MaterialTheme.typography.titleMedium)
            if (asignaciones.isEmpty()) Text(stringResource(R.string.asignar_ninguna))
            asignaciones.forEach { a ->
                val estado = stringResource(if (a.activa) R.string.asignar_estado_activa else R.string.asignar_estado_pausada)
                Text("${a.curso} · ${formatearFecha(a.desde)} – ${formatearFecha(a.hasta)} · $estado")
            }
        }
    }
}

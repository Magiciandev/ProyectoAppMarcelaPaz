package com.example.mpazpro.ui.screens

import com.example.mpazpro.ui.components.EstadoCarga
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import com.example.mpazpro.data.model.Roles
import com.example.mpazpro.ui.components.IconosBarraEstado
import com.example.mpazpro.viewmodel.PanelUiState
import com.example.mpazpro.viewmodel.PanelViewModel

// Panel de inicio: cada rol ve solo sus accesos.
@Composable
fun PanelScreen(
    viewModel: PanelViewModel,
    onUnidades: (editable: Boolean) -> Unit,
    onEvaluaciones: (estudianteId: Long) -> Unit,
    onInformes: (docenteId: Long) -> Unit,
    onCerrarSesion: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    IconosBarraEstado(iconosClaros = isSystemInDarkTheme())

    Box(Modifier.fillMaxSize().safeDrawingPadding(), contentAlignment = Alignment.Center) {
        when (val estado = uiState) {
            is PanelUiState.Cargando -> EstadoCarga()
            is PanelUiState.Error -> Text(estado.mensaje, color = MaterialTheme.colorScheme.error)
            is PanelUiState.Listo -> {
                val usuario = estado.usuario
                Column(
                    modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
                ) {
                    Text(stringResource(R.string.panel_hola, usuario.nombre), style = MaterialTheme.typography.headlineLarge)
                    Text(
                        stringResource(R.string.panel_perfil, usuario.rol),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    when (usuario.rol) {
                        Roles.UTP -> BotonPanel(stringResource(R.string.panel_unidades_utp)) { onUnidades(true) }
                        Roles.DOCENTE -> {
                            BotonPanel(stringResource(R.string.panel_unidades_docente)) { onUnidades(false) }
                            BotonPanel(stringResource(R.string.panel_informes)) { onInformes(usuario.id) }
                        }
                        else -> BotonPanel(stringResource(R.string.panel_evaluaciones)) { onEvaluaciones(usuario.id) }
                    }
                    OutlinedButton(onClick = onCerrarSesion, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                        Text(stringResource(R.string.panel_cerrar))
                    }
                }
            }
        }
    }
}

@Composable
private fun BotonPanel(texto: String, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = Modifier.fillMaxWidth().height(64.dp)) {
        Text(texto, style = MaterialTheme.typography.titleMedium)
    }
}

package com.example.mpazpro.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.mpazpro.R

// Estructura común de las pantallas internas: barra superior con flecha de volver y snackbar.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScaffoldConVolver(
    titulo: String,
    onVolver: () -> Unit,
    snackbarHostState: SnackbarHostState = SnackbarHostState(),
    contenido: @Composable (PaddingValues) -> Unit
) {
    IconosBarraEstado(iconosClaros = isSystemInDarkTheme())
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(titulo) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.volver))
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        content = contenido
    )
}

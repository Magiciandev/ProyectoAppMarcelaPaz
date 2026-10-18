package com.example.mpazpro.ui.screens

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mpazpro.R
import com.example.mpazpro.data.model.Cursos
import com.example.mpazpro.data.model.Roles
import com.example.mpazpro.data.model.Usuario
import com.example.mpazpro.ui.components.IconosBarraEstado
import com.example.mpazpro.ui.theme.MpazProTheme

// Panel de entrada de un rol. Docente y Estudiante lo usan hasta que se construyan sus pantallas.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PanelRolScreen(
    titulo: String,
    usuario: Usuario,
    descripcion: String,
    onCerrarSesion: () -> Unit
) {
    IconosBarraEstado(iconosClaros = isSystemInDarkTheme())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(titulo) },
                actions = {
                    IconButton(onClick = onCerrarSesion) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = stringResource(R.string.cerrar_sesion)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.panel_saludo, usuario.nombre),
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
                if (usuario.curso != null) {
                    Text(
                        text = stringResource(R.string.panel_curso, usuario.curso),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Text(
                    text = descripcion,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PanelRolPreview() {
    MpazProTheme {
        PanelRolScreen(
            titulo = "Panel del estudiante",
            usuario = Usuario(
                id = 3, usuario = "est001", pin = "1234", nombre = "Estudiante 001",
                rol = Roles.ESTUDIANTE, curso = Cursos.A
            ),
            descripcion = "Aquí estarán tus asignaturas y evaluaciones.",
            onCerrarSesion = {}
        )
    }
}

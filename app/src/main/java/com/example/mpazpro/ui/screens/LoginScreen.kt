package com.example.mpazpro.ui.screens

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mpazpro.R
import com.example.mpazpro.data.model.Usuario
import com.example.mpazpro.ui.components.CampoValidado
import com.example.mpazpro.ui.components.IconosBarraEstado
import com.example.mpazpro.ui.theme.MpazProTheme
import com.example.mpazpro.viewmodel.LoginErrores
import com.example.mpazpro.viewmodel.LoginFormulario
import com.example.mpazpro.viewmodel.LoginUiState
import com.example.mpazpro.viewmodel.LoginViewModel
import com.example.mpazpro.viewmodel.validation.ValidadorLogin

// Pantalla con estado: lee el ViewModel, avisa cuando el ingreso fue correcto y delega el dibujo.
@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginExitoso: (Usuario) -> Unit
) {
    val formulario by viewModel.formulario.collectAsStateWithLifecycle()
    val errores by viewModel.errores.collectAsStateWithLifecycle()
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    LaunchedEffect(estado) {
        val actual = estado
        if (actual is LoginUiState.Success) {
            onLoginExitoso(actual.usuario)
            viewModel.reiniciar()
        }
    }

    LoginContenido(
        formulario = formulario,
        errores = errores,
        estado = estado,
        onUsuario = viewModel::actualizarUsuario,
        onPin = viewModel::actualizarPin,
        onIngresar = viewModel::iniciarSesion
    )
}

@Composable
fun LoginContenido(
    formulario: LoginFormulario,
    errores: LoginErrores,
    estado: LoginUiState,
    onUsuario: (String) -> Unit,
    onPin: (String) -> Unit,
    onIngresar: () -> Unit
) {
    IconosBarraEstado(iconosClaros = isSystemInDarkTheme())

    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .imePadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.login_titulo),
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))

            CampoValidado(
                valor = formulario.usuario,
                onValorChange = onUsuario,
                etiqueta = stringResource(R.string.login_label_usuario),
                iconoInicial = Icons.Filled.Person,
                error = errores.usuario,
                maxCaracteres = ValidadorLogin.USUARIO_MAX
            )
            CampoValidado(
                valor = formulario.pin,
                onValorChange = onPin,
                etiqueta = stringResource(R.string.login_label_pin),
                iconoInicial = Icons.Filled.Lock,
                error = errores.pin,
                teclado = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                ocultarTexto = true
            )

            if (estado is LoginUiState.Error) {
                Text(
                    text = estado.mensaje,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Button(
                onClick = onIngresar,
                enabled = estado !is LoginUiState.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (estado is LoginUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(stringResource(R.string.login_ingresando))
                } else {
                    Text(stringResource(R.string.login_ingresar))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.login_pista),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 720)
@Composable
private fun LoginConErroresPreview() {
    MpazProTheme {
        LoginContenido(
            formulario = LoginFormulario(usuario = "ab", pin = "12"),
            errores = LoginErrores(
                usuario = "Debe tener al menos 3 caracteres",
                pin = "El PIN debe tener 4 dígitos"
            ),
            estado = LoginUiState.Idle,
            onUsuario = {}, onPin = {}, onIngresar = {}
        )
    }
}

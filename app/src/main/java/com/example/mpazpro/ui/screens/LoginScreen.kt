package com.example.mpazpro.ui.screens

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mpazpro.R
import com.example.mpazpro.ui.components.CampoValidado
import com.example.mpazpro.ui.components.IconosBarraEstado
import com.example.mpazpro.viewmodel.LoginErrores
import com.example.mpazpro.viewmodel.LoginFormulario
import com.example.mpazpro.viewmodel.LoginUiState
import com.example.mpazpro.viewmodel.LoginViewModel
import com.example.mpazpro.viewmodel.validation.ValidadorLogin

@Composable
fun LoginScreen(viewModel: LoginViewModel, onIngreso: (Long) -> Unit) {
    val formulario by viewModel.formulario.collectAsStateWithLifecycle()
    val errores by viewModel.errores.collectAsStateWithLifecycle()
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    LaunchedEffect(estado) {
        val actual = estado
        if (actual is LoginUiState.Success) {
            onIngreso(actual.usuarioId)
            viewModel.reiniciar()
        }
    }

    LoginContenido(
        formulario = formulario, errores = errores, estado = estado,
        onUsuario = viewModel::actualizarUsuario, onPin = viewModel::actualizarPin,
        onIngresar = viewModel::ingresar
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
    ) {
        Text(
            text = stringResource(R.string.login_titulo),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        CampoValidado(
            valor = formulario.usuario, onValorChange = onUsuario,
            etiqueta = stringResource(R.string.login_usuario),
            iconoInicial = Icons.Filled.Person, error = errores.usuario,
            maxCaracteres = ValidadorLogin.USUARIO_MAX
        )
        CampoValidado(
            valor = formulario.pin, onValorChange = onPin,
            etiqueta = stringResource(R.string.login_pin),
            iconoInicial = Icons.Filled.Lock, error = errores.pin,
            teclado = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            visualTransformation = PasswordVisualTransformation()
        )
        if (estado is LoginUiState.Error) {
            Text(estado.mensaje, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }
        Button(
            onClick = onIngresar,
            enabled = estado !is LoginUiState.Loading,
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            if (estado is LoginUiState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp), strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(Modifier.width(12.dp))
                Text(stringResource(R.string.login_ingresando))
            } else {
                Text(stringResource(R.string.login_ingresar))
            }
        }
        Text(
            text = stringResource(R.string.login_ayuda),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        )
    }
}

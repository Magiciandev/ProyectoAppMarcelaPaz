package com.example.mpazpro.ui.screens

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mpazpro.R
import com.example.mpazpro.data.model.Asignaturas
import com.example.mpazpro.ui.components.CampoDesplegable
import com.example.mpazpro.ui.components.CampoValidado
import com.example.mpazpro.ui.components.IconosBarraEstado
import com.example.mpazpro.ui.theme.MpazProTheme
import com.example.mpazpro.viewmodel.GuardadoUiState
import com.example.mpazpro.viewmodel.UnidadErrores
import com.example.mpazpro.viewmodel.UnidadFormViewModel
import com.example.mpazpro.viewmodel.UnidadFormulario
import com.example.mpazpro.viewmodel.validation.ValidadorUnidad

// Pantalla con estado: lee el ViewModel, avisa cuando se guardó y delega el dibujo.
@Composable
fun UnidadFormScreen(
    viewModel: UnidadFormViewModel,
    onGuardada: () -> Unit,
    onVolver: () -> Unit
) {
    val formulario by viewModel.formulario.collectAsStateWithLifecycle()
    val errores by viewModel.errores.collectAsStateWithLifecycle()
    val guardado by viewModel.guardado.collectAsStateWithLifecycle()

    LaunchedEffect(guardado) {
        if (guardado is GuardadoUiState.Success) {
            onGuardada()
            viewModel.reiniciar()
        }
    }

    UnidadFormContenido(
        formulario = formulario,
        errores = errores,
        guardado = guardado,
        onTitulo = viewModel::actualizarTitulo,
        onObjetivo = viewModel::actualizarObjetivo,
        onAsignatura = viewModel::actualizarAsignatura,
        onContenido = viewModel::actualizarContenido,
        onGuardar = viewModel::guardar,
        onVolver = onVolver
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnidadFormContenido(
    formulario: UnidadFormulario,
    errores: UnidadErrores,
    guardado: GuardadoUiState,
    onTitulo: (String) -> Unit,
    onObjetivo: (String) -> Unit,
    onAsignatura: (String) -> Unit,
    onContenido: (String) -> Unit,
    onGuardar: () -> Unit,
    onVolver: () -> Unit
) {
    IconosBarraEstado(iconosClaros = isSystemInDarkTheme())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.form_titulo_pantalla)) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.volver)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CampoValidado(
                valor = formulario.titulo,
                onValorChange = onTitulo,
                etiqueta = stringResource(R.string.form_label_titulo),
                iconoInicial = Icons.Filled.Edit,
                error = errores.titulo,
                maxCaracteres = ValidadorUnidad.TITULO_MAX
            )
            CampoValidado(
                valor = formulario.objetivoAprendizaje,
                onValorChange = onObjetivo,
                etiqueta = stringResource(R.string.form_label_oa),
                iconoInicial = Icons.Filled.Star,
                error = errores.objetivoAprendizaje,
                teclado = KeyboardOptions(capitalization = KeyboardCapitalization.Characters)
            )
            CampoDesplegable(
                valor = formulario.asignatura,
                opciones = Asignaturas.todas,
                onSeleccion = onAsignatura,
                etiqueta = stringResource(R.string.form_label_asignatura),
                iconoInicial = Icons.Filled.Info,
                error = errores.asignatura
            )
            CampoValidado(
                valor = formulario.contenido,
                onValorChange = onContenido,
                etiqueta = stringResource(R.string.form_label_contenido),
                iconoInicial = Icons.Filled.Create,
                error = errores.contenido,
                maxCaracteres = ValidadorUnidad.CONTENIDO_MAX,
                minLineas = 4
            )

            if (guardado is GuardadoUiState.Error) {
                Text(
                    text = guardado.mensaje,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Button(
                onClick = onGuardar,
                enabled = guardado !is GuardadoUiState.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (guardado is GuardadoUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(stringResource(R.string.form_guardando))
                } else {
                    Text(stringResource(R.string.form_guardar))
                }
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun FormularioConErroresPreview() {
    MpazProTheme {
        UnidadFormContenido(
            formulario = UnidadFormulario(titulo = "Su", objetivoAprendizaje = "5"),
            errores = UnidadErrores(
                titulo = "Debe tener al menos 5 caracteres",
                objetivoAprendizaje = "Usa el formato OA y un número, por ejemplo OA05",
                asignatura = "Selecciona una asignatura",
                contenido = "El contenido es obligatorio"
            ),
            guardado = GuardadoUiState.Idle,
            onTitulo = {}, onObjetivo = {}, onAsignatura = {}, onContenido = {},
            onGuardar = {}, onVolver = {}
        )
    }
}

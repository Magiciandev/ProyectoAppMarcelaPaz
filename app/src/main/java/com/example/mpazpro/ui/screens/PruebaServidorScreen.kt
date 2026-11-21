package com.example.mpazpro.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mpazpro.R
import com.example.mpazpro.data.model.FormatoPuntaje
import com.example.mpazpro.data.model.TipoPreguntaPrueba
import com.example.mpazpro.ui.components.EstadoCarga
import com.example.mpazpro.ui.components.EstadoOpcion
import com.example.mpazpro.ui.components.OpcionRespuesta
import com.example.mpazpro.ui.components.ScaffoldConVolver
import com.example.mpazpro.viewmodel.PruebaServidorUiState
import com.example.mpazpro.viewmodel.PruebaServidorViewModel

// Prueba de una unidad que viene del servidor. Resuelve selección, completación y secuencia.
@Composable
fun PruebaServidorScreen(viewModel: PruebaServidorViewModel, onVolver: () -> Unit) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ScaffoldConVolver(titulo = stringResource(R.string.eval_titulo), onVolver = onVolver) { padding ->
        Box(Modifier.fillMaxSize().padding(padding).padding(16.dp), contentAlignment = Alignment.Center) {
            when (val estado = uiState) {
                is PruebaServidorUiState.Cargando, is PruebaServidorUiState.Enviando -> EstadoCarga()
                is PruebaServidorUiState.Mensaje -> TextoCentrado(estado.texto, estado.esError)
                is PruebaServidorUiState.ErrorAlEnviar -> Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    TextoCentrado(stringResource(R.string.prueba_error_envio, estado.mensaje), error = true)
                    Button(onClick = viewModel::reintentar, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                        Text(stringResource(R.string.prueba_reintentar))
                    }
                }
                is PruebaServidorUiState.EnCurso -> PreguntaVista(estado, viewModel)
                is PruebaServidorUiState.Finalizada -> ResultadoVista(estado, onVolver)
            }
        }
    }
}

@Composable
private fun TextoCentrado(texto: String, error: Boolean = false) {
    Text(
        text = texto, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center,
        color = if (error) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
    )
}

@Composable
private fun PreguntaVista(estado: PruebaServidorUiState.EnCurso, viewModel: PruebaServidorViewModel) {
    val pregunta = estado.pregunta
    val avance by animateFloatAsState(
        targetValue = (estado.indice + if (estado.corregida) 1 else 0) / estado.total.toFloat(),
        label = "avance"
    )
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).animateContentSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(stringResource(R.string.eval_progreso, estado.indice + 1, estado.total), style = MaterialTheme.typography.titleMedium)
        LinearProgressIndicator(
            progress = { avance },
            modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp))
        )
        Text(pregunta.enunciado, style = MaterialTheme.typography.headlineSmall)

        if (pregunta.tipo == TipoPreguntaPrueba.SECUENCIA) {
            SecuenciaVista(estado, viewModel)
        } else {
            pregunta.opciones.forEachIndexed { i, opcion ->
                val estadoOpcion = when {
                    !estado.corregida -> EstadoOpcion.NEUTRA
                    opcion.esCorrecta -> EstadoOpcion.CORRECTA
                    i in estado.seleccion -> EstadoOpcion.INCORRECTA
                    else -> EstadoOpcion.BLOQUEADA
                }
                OpcionRespuesta(('A' + i).toString(), opcion.texto, estadoOpcion, onClick = { viewModel.elegir(i) })
            }
        }

        if (estado.corregida) {
            val acerto = pregunta.esCorrecta(estado.seleccion)
            Text(
                text = when {
                    acerto -> stringResource(R.string.eval_acierto)
                    pregunta.tipo == TipoPreguntaPrueba.SECUENCIA ->
                        stringResource(R.string.prueba_orden_correcto, pregunta.ordenCorrecto())
                    else -> stringResource(R.string.eval_error)
                },
                style = MaterialTheme.typography.titleLarge
            )
            pregunta.explicacion?.let { Text(it, style = MaterialTheme.typography.bodyLarge) }
            Button(onClick = viewModel::siguiente, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                Text(stringResource(if (estado.esUltima) R.string.eval_terminar else R.string.eval_siguiente))
            }
        }
    }
}

// Secuencia: el estudiante toca las opciones en el orden que cree correcto y luego comprueba.
@Composable
private fun SecuenciaVista(estado: PruebaServidorUiState.EnCurso, viewModel: PruebaServidorViewModel) {
    val opciones = estado.pregunta.opciones
    val elegidas = estado.seleccion.joinToString(" → ") { opciones[it].texto }
    Text(stringResource(R.string.prueba_secuencia_instruccion), style = MaterialTheme.typography.bodyLarge)
    Text(
        stringResource(R.string.prueba_tu_orden, elegidas.ifEmpty { "—" }),
        style = MaterialTheme.typography.titleMedium
    )
    opciones.forEachIndexed { i, opcion ->
        if (i !in estado.seleccion) {
            OutlinedButton(
                onClick = { viewModel.elegir(i) },
                enabled = !estado.corregida,
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) { Text(opcion.texto, style = MaterialTheme.typography.titleMedium) }
        }
    }
    if (!estado.corregida) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = viewModel::deshacer, enabled = estado.seleccion.isNotEmpty()) {
                Text(stringResource(R.string.prueba_deshacer))
            }
            Button(onClick = viewModel::comprobar, enabled = estado.seleccion.size == opciones.size) {
                Text(stringResource(R.string.prueba_comprobar))
            }
        }
    }
}

@Composable
private fun ResultadoVista(estado: PruebaServidorUiState.Finalizada, onVolver: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            stringResource(
                R.string.prueba_resultado_puntos,
                FormatoPuntaje.texto(estado.obtenido), FormatoPuntaje.texto(estado.maximo)
            ),
            style = MaterialTheme.typography.displaySmall, textAlign = TextAlign.Center
        )
        Text(stringResource(R.string.prueba_porcentaje, estado.porcentaje), style = MaterialTheme.typography.headlineMedium)
        estado.mensaje?.let {
            Text(
                stringResource(R.string.prueba_no_registrado, it),
                style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.error
            )
        }
        Text(stringResource(R.string.eval_restantes, estado.intentosRestantes), style = MaterialTheme.typography.titleMedium)
        Button(onClick = onVolver, modifier = Modifier.fillMaxWidth().height(52.dp)) {
            Text(stringResource(R.string.eval_volver_lista))
        }
    }
}

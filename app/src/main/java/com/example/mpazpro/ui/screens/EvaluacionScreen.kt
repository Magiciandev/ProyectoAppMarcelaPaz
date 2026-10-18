package com.example.mpazpro.ui.screens

import com.example.mpazpro.ui.components.EstadoCarga
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mpazpro.R
import com.example.mpazpro.ui.components.EstadoOpcion
import com.example.mpazpro.ui.components.OpcionRespuesta
import com.example.mpazpro.ui.components.ScaffoldConVolver
import com.example.mpazpro.viewmodel.EvaluacionUiState
import com.example.mpazpro.viewmodel.EvaluacionViewModel

private val LETRAS = listOf("A", "B", "C", "D")

@Composable
fun EvaluacionScreen(viewModel: EvaluacionViewModel, onVolver: () -> Unit) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ScaffoldConVolver(titulo = stringResource(R.string.eval_titulo), onVolver = onVolver) { padding ->
        Box(Modifier.fillMaxSize().padding(padding).padding(16.dp), contentAlignment = Alignment.Center) {
            when (val estado = uiState) {
                is EvaluacionUiState.Cargando -> EstadoCarga()
                is EvaluacionUiState.Bloqueada -> Mensaje(stringResource(R.string.eval_bloqueada, estado.maximo))
                is EvaluacionUiState.SinPreguntas -> Mensaje(stringResource(R.string.eval_sin_preguntas))
                is EvaluacionUiState.Error -> Mensaje(estado.mensaje, error = true)
                is EvaluacionUiState.EnCurso -> PreguntaVista(estado, viewModel::responder, viewModel::siguiente)
                is EvaluacionUiState.Finalizada -> ResultadoVista(estado, onVolver)
            }
        }
    }
}

@Composable
private fun Mensaje(texto: String, error: Boolean = false) {
    Text(
        text = texto, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center,
        color = if (error) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
    )
}

@Composable
private fun PreguntaVista(
    estado: EvaluacionUiState.EnCurso,
    onResponder: (Int) -> Unit,
    onSiguiente: () -> Unit
) {
    // La barra avanza con animación al responder cada pregunta.
    val avance by animateFloatAsState(
        targetValue = (estado.indice + if (estado.seleccionada != null) 1 else 0) / estado.total.toFloat(),
        label = "avance"
    )
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(stringResource(R.string.eval_progreso, estado.indice + 1, estado.total), style = MaterialTheme.typography.titleMedium)
        LinearProgressIndicator(
            progress = { avance },
            modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp))
        )
        // Cada pregunta entra deslizando desde la derecha.
        AnimatedContent(
            targetState = estado.pregunta,
            transitionSpec = {
                (slideInHorizontally { it } + fadeIn()) togetherWith (slideOutHorizontally { -it } + fadeOut())
            },
            label = "pregunta"
        ) { pregunta ->
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(pregunta.texto, style = MaterialTheme.typography.headlineSmall)
                pregunta.opciones().forEachIndexed { i, texto ->
                    val sel = estado.seleccionada
                    val estadoOpcion = when {
                        sel == null -> EstadoOpcion.NEUTRA
                        i == pregunta.indiceCorrecta -> EstadoOpcion.CORRECTA
                        i == sel -> EstadoOpcion.INCORRECTA
                        else -> EstadoOpcion.BLOQUEADA
                    }
                    OpcionRespuesta(LETRAS[i], texto, estadoOpcion, onClick = { onResponder(i) })
                }
            }
        }
        val sel = estado.seleccionada
        AnimatedVisibility(visible = sel != null, enter = scaleIn() + fadeIn()) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                val acerto = sel == estado.pregunta.indiceCorrecta
                Text(
                    text = stringResource(if (acerto) R.string.eval_acierto else R.string.eval_error),
                    style = MaterialTheme.typography.titleLarge
                )
                Button(onClick = onSiguiente, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                    Text(stringResource(if (estado.esUltima) R.string.eval_terminar else R.string.eval_siguiente))
                }
            }
        }
    }
}

@Composable
private fun ResultadoVista(estado: EvaluacionUiState.Finalizada, onVolver: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    AnimatedVisibility(visible = visible, enter = scaleIn() + fadeIn()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                stringResource(R.string.eval_resultado, estado.correctas, estado.total),
                style = MaterialTheme.typography.displaySmall, textAlign = TextAlign.Center
            )
            Text(stringResource(R.string.eval_restantes, estado.intentosRestantes), style = MaterialTheme.typography.titleMedium)
            Button(onClick = onVolver, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                Text(stringResource(R.string.eval_volver_lista))
            }
        }
    }
}

package com.example.mpazpro.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mpazpro.R
import com.example.mpazpro.ui.components.AsignaturaChip
import com.example.mpazpro.ui.components.IconosBarraEstado
import com.example.mpazpro.ui.theme.MpazProTheme
import com.example.mpazpro.ui.theme.Pizarra
import com.example.mpazpro.ui.theme.PizarraOscura
import com.example.mpazpro.ui.theme.TizaBlanca
import com.example.mpazpro.ui.theme.TizaCiencias
import com.example.mpazpro.ui.theme.TizaHistoria
import com.example.mpazpro.ui.theme.TizaLenguaje
import com.example.mpazpro.ui.theme.TizaMatematica
import kotlinx.coroutines.delay

private const val TOTAL_PASOS = 7

// Pantalla de bienvenida: sin ViewModel ni datos, solo estética y el paso a la app.
// La entrada es una sola secuencia: título, cada "tiza" de asignatura y, al final, el botón.
@Composable
fun BienvenidaScreen(
    onEntrar: () -> Unit,
    modifier: Modifier = Modifier,
    animar: Boolean = true
) {
    IconosBarraEstado(iconosClaros = true)

    var pasoVisible by remember { mutableIntStateOf(if (animar) 0 else TOTAL_PASOS) }

    LaunchedEffect(Unit) {
        if (animar) {
            delay(300)
            repeat(TOTAL_PASOS) {
                pasoVisible++
                delay(220)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Pizarra, PizarraOscura)))
            .systemBarsPadding()
            .padding(horizontal = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Aparecer(visible = pasoVisible >= 1) {
                Text(
                    text = stringResource(R.string.bienvenida_titulo),
                    style = MaterialTheme.typography.displayMedium,
                    color = TizaBlanca,
                    textAlign = TextAlign.Center
                )
            }
            Aparecer(visible = pasoVisible >= 2) {
                Text(
                    text = stringResource(R.string.bienvenida_subtitulo),
                    style = MaterialTheme.typography.bodyLarge,
                    color = TizaBlanca.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Aparecer(visible = pasoVisible >= 3) {
                    AsignaturaChip(
                        nombre = stringResource(R.string.asignatura_lenguaje),
                        colorTiza = TizaLenguaje,
                        inclinacion = -2f
                    )
                }
                Aparecer(visible = pasoVisible >= 4) {
                    AsignaturaChip(
                        nombre = stringResource(R.string.asignatura_matematica),
                        colorTiza = TizaMatematica,
                        inclinacion = 1.5f
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Aparecer(visible = pasoVisible >= 5) {
                    AsignaturaChip(
                        nombre = stringResource(R.string.asignatura_ciencias),
                        colorTiza = TizaCiencias,
                        inclinacion = 2f
                    )
                }
                Aparecer(visible = pasoVisible >= 6) {
                    AsignaturaChip(
                        nombre = stringResource(R.string.asignatura_historia),
                        colorTiza = TizaHistoria,
                        inclinacion = -1.5f
                    )
                }
            }
            Aparecer(visible = pasoVisible >= 7) {
                Button(
                    onClick = onEntrar,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TizaBlanca,
                        contentColor = Pizarra
                    )
                ) {
                    Text(
                        text = stringResource(R.string.bienvenida_entrar),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }

        Text(
            text = stringResource(R.string.bienvenida_colegio),
            style = MaterialTheme.typography.labelLarge,
            color = TizaBlanca.copy(alpha = 0.6f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        )
    }
}

@Composable
private fun Aparecer(
    visible: Boolean,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(400)) +
            scaleIn(animationSpec = tween(400), initialScale = 0.8f)
    ) {
        content()
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun BienvenidaScreenPreview() {
    MpazProTheme {
        BienvenidaScreen(onEntrar = {}, animar = false)
    }
}

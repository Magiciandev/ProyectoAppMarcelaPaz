package com.example.mpazpro.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.mpazpro.R
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

// El selector de fecha de Material 3 trabaja en UTC; se formatea en UTC para no correr el día.
fun formatearFecha(millis: Long): String =
    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        .apply { timeZone = TimeZone.getTimeZone("UTC") }
        .format(millis)

// Campo de fecha de solo lectura; el ícono abre el selector. Solo recuerda si el diálogo está abierto.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampoFecha(
    valor: Long?,
    onFecha: (Long) -> Unit,
    etiqueta: String,
    modifier: Modifier = Modifier,
    error: String? = null
) {
    var abierto by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = valor?.let(::formatearFecha).orEmpty(),
        onValueChange = {},
        readOnly = true,
        label = { Text(etiqueta) },
        trailingIcon = {
            IconButton(onClick = { abierto = true }) {
                Icon(
                    imageVector = if (error != null) Icons.Filled.Warning else Icons.Filled.DateRange,
                    contentDescription = stringResource(R.string.asignar_elegir_fecha),
                    tint = if (error != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        isError = error != null,
        supportingText = { Text(error ?: "") },
        modifier = modifier
    )

    if (abierto) {
        val estado = rememberDatePickerState(initialSelectedDateMillis = valor)
        DatePickerDialog(
            onDismissRequest = { abierto = false },
            confirmButton = {
                TextButton(onClick = {
                    estado.selectedDateMillis?.let(onFecha)
                    abierto = false
                }) { Text(stringResource(R.string.asignar_aceptar)) }
            },
            dismissButton = {
                TextButton(onClick = { abierto = false }) { Text(stringResource(R.string.asignar_cancelar)) }
            }
        ) { DatePicker(state = estado) }
    }
}

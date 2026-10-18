package com.example.mpazpro.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.mpazpro.R
import com.example.mpazpro.ui.theme.MpazProTheme

// Campo de texto reutilizable con ícono inicial, ícono de error y mensaje visual.
// No tiene estado ni lógica: recibe el valor y el error por parámetros (elevación de estado).
@Composable
fun CampoValidado(
    valor: String,
    onValorChange: (String) -> Unit,
    etiqueta: String,
    iconoInicial: ImageVector,
    modifier: Modifier = Modifier,
    error: String? = null,
    maxCaracteres: Int? = null,
    minLineas: Int = 1,
    teclado: KeyboardOptions = KeyboardOptions.Default
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onValorChange,
        label = { Text(etiqueta) },
        leadingIcon = { Icon(imageVector = iconoInicial, contentDescription = null) },
        trailingIcon = {
            if (error != null) {
                Icon(
                    imageVector = Icons.Filled.Warning,
                    contentDescription = stringResource(R.string.campo_con_error),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        },
        isError = error != null,
        supportingText = {
            Text(text = error ?: contador(valor, maxCaracteres))
        },
        singleLine = minLineas == 1,
        minLines = minLineas,
        keyboardOptions = teclado,
        modifier = modifier.fillMaxWidth()
    )
}

private fun contador(valor: String, maxCaracteres: Int?): String =
    if (maxCaracteres != null) "${valor.length}/$maxCaracteres" else ""

@Preview(showBackground = true)
@Composable
private fun CampoValidadoPreview() {
    MpazProTheme {
        CampoValidado(
            valor = "Su",
            onValorChange = {},
            etiqueta = "Título de la unidad",
            iconoInicial = Icons.Filled.Edit,
            error = "Debe tener al menos 5 caracteres"
        )
    }
}

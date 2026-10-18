package com.example.mpazpro.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.mpazpro.R
import com.example.mpazpro.ui.theme.MpazProTheme

// Lista desplegable reutilizable (asignatura, curso, etc.). Solo recuerda si el menú está abierto;
// la opción elegida vive en el ViewModel y llega por parámetro.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampoDesplegable(
    valor: String,
    opciones: List<String>,
    onSeleccion: (String) -> Unit,
    etiqueta: String,
    iconoInicial: ImageVector,
    modifier: Modifier = Modifier,
    error: String? = null
) {
    var expandido by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expandido,
        onExpandedChange = { expandido = !expandido },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = valor,
            onValueChange = {},
            readOnly = true,
            label = { Text(etiqueta) },
            leadingIcon = { Icon(imageVector = iconoInicial, contentDescription = null) },
            trailingIcon = {
                if (error != null) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = stringResource(R.string.campo_con_error),
                        tint = MaterialTheme.colorScheme.error
                    )
                } else {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido)
                }
            },
            isError = error != null,
            supportingText = { Text(text = error ?: "") },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expandido,
            onDismissRequest = { expandido = false }
        ) {
            opciones.forEach { opcion ->
                DropdownMenuItem(
                    text = { Text(opcion) },
                    onClick = {
                        onSeleccion(opcion)
                        expandido = false
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CampoDesplegablePreview() {
    MpazProTheme {
        CampoDesplegable(
            valor = "Matemática",
            opciones = listOf("Lenguaje", "Matemática"),
            onSeleccion = {},
            etiqueta = "Asignatura",
            iconoInicial = Icons.Filled.Info
        )
    }
}

package com.example.lemacdatalab.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Formatea epoch millis para mostrarlo en pantalla (la fecha se guarda como Long). */
fun formatearFecha(millis: Long, conHora: Boolean = false): String {
    val patron = if (conHora) "dd/MM/yyyy HH:mm" else "dd/MM/yyyy"
    return SimpleDateFormat(patron, Locale.forLanguageTag("es-CL")).format(Date(millis))
}

/** Etiqueta informativa (no clicable), reemplaza a AssistChip(onClick = {}). */
@Composable
fun EtiquetaChip(
    texto: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = containerColor,
        contentColor = contentColor
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

/** Barra inferior para alternar entre las dos secciones de la app. */
@Composable
fun AppBottomBar(
    rutaActual: String,
    rutaAutocuidado: String,
    rutaSolicitudes: String,
    onSeleccionar: (String) -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            selected = rutaActual == rutaAutocuidado,
            onClick = { onSeleccionar(rutaAutocuidado) },
            icon = { Icon(Icons.Default.Favorite, contentDescription = null) },
            label = { Text("Autocuidado") }
        )
        NavigationBarItem(
            selected = rutaActual == rutaSolicitudes,
            onClick = { onSeleccionar(rutaSolicitudes) },
            icon = { Icon(Icons.Default.Build, contentDescription = null) },
            label = { Text("Solicitudes") }
        )
    }
}

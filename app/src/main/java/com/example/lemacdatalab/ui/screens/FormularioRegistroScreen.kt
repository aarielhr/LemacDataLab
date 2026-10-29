package com.example.lemacdatalab.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.lemacdatalab.data.model.Ambitos
import com.example.lemacdatalab.ui.viewmodel.RegistroViewModel
import com.example.lemacdatalab.utils.LocationHelper
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioRegistroScreen(
    viewModel: RegistroViewModel,
    onNavigateBack: () -> Unit,
    onRegistroCreado: (String) -> Unit
) {
    val uiState by viewModel.formUiState.collectAsState()
    var dropdownExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            viewModel.onObteniendoUbicacionChanged(true)
            LocationHelper.obtenerUbicacionActual(
                context = context,
                onSuccess = { lat, lon -> viewModel.onUbicacionObtenida(lat, lon) },
                onError = { msg -> viewModel.onUbicacionError(msg) }
            )
        } else {
            viewModel.onUbicacionError("Se requieren permisos de ubicación GPS para obtener las coordenadas.")
        }
    }

    val solicitarUbicacion = {
        viewModel.onObteniendoUbicacionChanged(true)
        if (LocationHelper.tienePermisosUbicacion(context)) {
            LocationHelper.obtenerUbicacionActual(
                context = context,
                onSuccess = { lat, lon -> viewModel.onUbicacionObtenida(lat, lon) },
                onError = { msg -> viewModel.onUbicacionError(msg) }
            )
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // Navega de inmediato; el mensaje de éxito se muestra en la pantalla de detalle.
    LaunchedEffect(uiState.registroCreadoId) {
        uiState.registroCreadoId?.let { onRegistroCreado(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuevo Registro de Autocuidado") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "1. Selecciona el Ámbito:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Selector de los 4 Ámbitos
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = uiState.ambitoSeleccionado == Ambitos.NEURODESARROLLO,
                            onClick = { viewModel.onAmbitoChanged(Ambitos.NEURODESARROLLO) },
                            label = { Text("Neurodesarrollo") }
                        )
                        FilterChip(
                            selected = uiState.ambitoSeleccionado == Ambitos.HABILIDADES_DBT,
                            onClick = { viewModel.onAmbitoChanged(Ambitos.HABILIDADES_DBT) },
                            label = { Text("Habilidades DBT") }
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = uiState.ambitoSeleccionado == Ambitos.ESTADO_ANIMO,
                            onClick = { viewModel.onAmbitoChanged(Ambitos.ESTADO_ANIMO) },
                            label = { Text("Estado de Ánimo") }
                        )
                        FilterChip(
                            selected = uiState.ambitoSeleccionado == Ambitos.ADICCIONES,
                            onClick = { viewModel.onAmbitoChanged(Ambitos.ADICCIONES) },
                            label = { Text("Adicciones") }
                        )
                    }
                }
            }

            Text(
                text = "2. Actividad / Habilidad Practicada *",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Dropdown / Entrada de Actividad
            Column {
                ExposedDropdownMenuBox(
                    expanded = dropdownExpanded,
                    onExpandedChange = { dropdownExpanded = !dropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = uiState.tipoActividad,
                        onValueChange = { viewModel.onTipoActividadChanged(it) },
                        label = { Text("Selecciona o escribe la actividad") },
                        placeholder = { Text("Ej: Pausa de autorregulación, STOP, etc.") },
                        isError = uiState.tipoActividadError != null,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded)
                        },
                        modifier = Modifier
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable)
                            .fillMaxWidth()
                    )

                    val sugerencias = Ambitos.getActividadesSugeridas(uiState.ambitoSeleccionado)
                    ExposedDropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false }
                    ) {
                        sugerencias.forEach { opcion ->
                            DropdownMenuItem(
                                text = { Text(opcion) },
                                onClick = {
                                    viewModel.onTipoActividadChanged(opcion)
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Animación para el mensaje de error de tipo de actividad
                AnimatedVisibility(
                    visible = uiState.tipoActividadError != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    uiState.tipoActividadError?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                        )
                    }
                }
            }

            Text(
                text = "3. Nivel de Intensidad / Malestar / Utilidad: ${uiState.nivelIntensidad.roundToInt()} / 10",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Slider de Intensidad (1 a 10)
            Column {
                Slider(
                    value = uiState.nivelIntensidad,
                    onValueChange = { viewModel.onNivelIntensidadChanged(it) },
                    valueRange = 1f..10f,
                    steps = 8,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("1 (Mínimo)", style = MaterialTheme.typography.labelSmall)
                    Text("5 (Medio)", style = MaterialTheme.typography.labelSmall)
                    Text("10 (Máximo)", style = MaterialTheme.typography.labelSmall)
                }
            }

            Text(
                text = "4. Desencadenante / Situación / Acción Protectora *",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Desencadenante / Estrategia
            Column {
                OutlinedTextField(
                    value = uiState.desencadenanteEstrategia,
                    onValueChange = { viewModel.onDesencadenanteChanged(it) },
                    label = { Text("Estrategia, emoción o desencadenante") },
                    placeholder = { Text("Ej: Estrés laboral, ejercicio respiratorio, etc.") },
                    isError = uiState.desencadenanteError != null,
                    trailingIcon = {
                        if (uiState.desencadenanteError != null) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Error",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                AnimatedVisibility(
                    visible = uiState.desencadenanteError != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    uiState.desencadenanteError?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                        )
                    }
                }

                if (uiState.desencadenanteError == null) {
                    Text(
                        text = "Requerido para DBT y Adicciones",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
            }

            Text(
                text = "5. Observaciones de Contexto (Opcional)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Observaciones
            OutlinedTextField(
                value = uiState.observacionesContexto,
                onValueChange = { viewModel.onObservacionesChanged(it) },
                label = { Text("Notas sobre el contexto personal") },
                placeholder = { Text("¿Dónde estabas? ¿Qué personas te acompañaban?") },
                minLines = 3,
                maxLines = 5,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "6. Ubicación GPS (Opcional)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (uiState.latitud != null && uiState.longitud != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "GPS",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Ubicación obtenida:\nLat: %.5f, Lon: %.5f".format(Locale.US, uiState.latitud, uiState.longitud),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            IconButton(onClick = { viewModel.onLimpiarUbicacion() }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Quitar ubicación"
                                )
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Captura tu ubicación geográfica al realizar el autorreporte.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f)
                            )
                            if (uiState.obteniendoUbicacion) {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .padding(start = 8.dp)
                                        .height(24.dp)
                                )
                            } else {
                                OutlinedButton(
                                    onClick = solicitarUbicacion,
                                    modifier = Modifier.padding(start = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        modifier = Modifier.padding(end = 4.dp)
                                    )
                                    Text("Obtener GPS")
                                }
                            }
                        }
                    }

                    if (uiState.ubicacionError != null) {
                        Text(
                            text = uiState.ubicacionError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Botón de Guardado
            Button(
                onClick = {
                    viewModel.validarYGuardar()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(text = "Guardar Autorreporte", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

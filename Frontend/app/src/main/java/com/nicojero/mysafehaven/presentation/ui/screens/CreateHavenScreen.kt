package com.nicojero.mysafehaven.presentation.ui.screens

import android.Manifest
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.nicojero.mysafehaven.presentation.viewmodel.HavenUiState
import com.nicojero.mysafehaven.presentation.viewmodel.HavenViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun CreateHavenScreen(
    viewModel: HavenViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onHavenCreated: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf("") }
    var longitude by remember { mutableStateOf("") }
    var radius by remember { mutableStateOf("100") }

    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isLoadingLocation by remember { mutableStateOf(false) }

    val createHavenState by viewModel.createHavenState.collectAsState()
    val havenLimits by viewModel.havenLimits.collectAsState()

    val scope = rememberCoroutineScope()

    // Manejo de permisos de ubicación
    val locationPermissions = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    LaunchedEffect(Unit) {
        viewModel.checkCanCreateHaven()
    }

    LaunchedEffect(createHavenState) {
        when (createHavenState) {
            is HavenUiState.Success<*> -> {
                viewModel.resetCreateHavenState()
                onHavenCreated()
            }
            is HavenUiState.Error -> {
                errorMessage = (createHavenState as HavenUiState.Error).message
                showError = true
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear Haven") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Info de límites
            havenLimits?.let { limits ->
                if (!limits.canCreate) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Límite alcanzado",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = "Has alcanzado el límite de havens gratuitos (${limits.maxHavens})",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = "Actualiza a Pro para crear havens ilimitados",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                } else {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = if (limits.isPro)
                                    "Havens ilimitados disponibles ⭐"
                                else
                                    "Te quedan ${limits.remainingHavens} havens disponibles",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre del Haven") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = havenLimits?.canCreate == true
            )

            // Sección de ubicación con botón GPS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = latitude,
                    onValueChange = { latitude = it },
                    label = { Text("Latitud") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    placeholder = { Text("28.123456") },
                    enabled = havenLimits?.canCreate == true && !isLoadingLocation
                )

                IconButton(
                    onClick = {
                        if (locationPermissions.allPermissionsGranted) {
                            // Obtener ubicación
                            isLoadingLocation = true
                            scope.launch {
                                try {
                                    val location = viewModel.getCurrentLocation()
                                        ?: viewModel.getLastKnownLocation()

                                    if (location != null) {
                                        latitude = location.latitude.toString()
                                        longitude = location.longitude.toString()
                                    } else {
                                        errorMessage = "No se pudo obtener la ubicación"
                                        showError = true
                                    }
                                } catch (e: Exception) {
                                    errorMessage = "Error al obtener ubicación: ${e.message}"
                                    showError = true
                                } finally {
                                    isLoadingLocation = false
                                }
                            }
                        } else {
                            // Solicitar permisos
                            locationPermissions.launchMultiplePermissionRequest()
                        }
                    },
                    enabled = havenLimits?.canCreate == true && !isLoadingLocation
                ) {
                    if (isLoadingLocation) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            Icons.Default.MyLocation,
                            contentDescription = "Obtener ubicación actual",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            OutlinedTextField(
                value = longitude,
                onValueChange = { longitude = it },
                label = { Text("Longitud") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                placeholder = { Text("-15.654321") },
                enabled = havenLimits?.canCreate == true && !isLoadingLocation
            )

            // Texto informativo sobre permisos
            if (!locationPermissions.allPermissionsGranted) {
                Text(
                    text = "📍 Toca el ícono de ubicación para permitir acceso al GPS",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            OutlinedTextField(
                value = radius,
                onValueChange = { radius = it },
                label = { Text("Radio (metros)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                supportingText = { Text("Rango recomendado: 50-500m") },
                enabled = havenLimits?.canCreate == true
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    val lat = latitude.toDoubleOrNull()
                    val lon = longitude.toDoubleOrNull()
                    val rad = radius.toDoubleOrNull()

                    when {
                        name.isBlank() -> {
                            errorMessage = "Ingresa un nombre"
                            showError = true
                        }
                        lat == null || lat < -90 || lat > 90 -> {
                            errorMessage = "Latitud inválida (-90 a 90)"
                            showError = true
                        }
                        lon == null || lon < -180 || lon > 180 -> {
                            errorMessage = "Longitud inválida (-180 a 180)"
                            showError = true
                        }
                        rad == null || rad <= 0 -> {
                            errorMessage = "Radio debe ser mayor a 0"
                            showError = true
                        }
                        else -> {
                            viewModel.createHaven(name, lat, lon, rad)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = havenLimits?.canCreate == true &&
                        createHavenState !is HavenUiState.Loading &&
                        !isLoadingLocation
            ) {
                if (createHavenState is HavenUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Crear Haven")
                }
            }
        }
    }

    if (showError) {
        AlertDialog(
            onDismissRequest = { showError = false },
            title = { Text("Error") },
            text = { Text(errorMessage) },
            confirmButton = {
                TextButton(onClick = { showError = false }) {
                    Text("OK")
                }
            }
        )
    }
}
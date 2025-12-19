package com.nicojero.mysafehaven.presentation.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
fun UpdateHavenScreen(
    havenId: Int,
    viewModel: HavenViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val havens by viewModel.havens.collectAsState()
    val updateHavenState by viewModel.updateHavenState.collectAsState()

    val haven = remember(havens, havenId) { havens.find { it.id == havenId } }

    val scope = rememberCoroutineScope()
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isLoadingLocation by remember { mutableStateOf(false) }

    val locationPermissions = rememberMultiplePermissionsState(
        permissions = listOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    LaunchedEffect(Unit) {
        if (havens.isEmpty()) {
            viewModel.loadHavens()
        }
    }

    LaunchedEffect(updateHavenState) {
        when (updateHavenState) {
            is HavenUiState.Success<*> -> {
                onNavigateBack()
                viewModel.resetUpdateHavenState()
            }
            is HavenUiState.Error -> {
                errorMessage = (updateHavenState as HavenUiState.Error).message
                showError = true
            }
            else -> {}
        }
    }

    if (haven == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        var name by remember(havenId) { mutableStateOf(haven.name) }
        var latitude by remember(havenId) { mutableStateOf(haven.latitude.toString()) }
        var longitude by remember(havenId) { mutableStateOf(haven.longitude.toString()) }
        var radius by remember(havenId) { mutableStateOf(haven.radius.toString()) }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Editar Haven") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre del Haven") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = latitude,
                        onValueChange = { },
                        label = { Text("Latitud") },
                        modifier = Modifier.weight(1f),
                        readOnly = true,
                    )

                    IconButton(
                        onClick = {
                            if (locationPermissions.allPermissionsGranted) {
                                isLoadingLocation = true
                                scope.launch {
                                    try {
                                        val location = viewModel.getCurrentLocation()
                                            ?: viewModel.getLastKnownLocation()

                                        if (location != null) {
                                            latitude = location.latitude.toString()
                                            longitude = location.longitude.toString()
                                        } else {
                                            errorMessage = "No se pudo obtener la ubicación actual"
                                            showError = true
                                        }
                                    } catch (e: Exception) {
                                        errorMessage = "Error de GPS: ${e.message}"
                                        showError = true
                                    } finally {
                                        isLoadingLocation = false
                                    }
                                }
                            } else {
                                locationPermissions.launchMultiplePermissionRequest()
                            }
                        },
                        enabled = !isLoadingLocation
                    ) {
                        if (isLoadingLocation) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = "Actualizar ubicación",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = longitude,
                    onValueChange = {  },
                    label = { Text("Longitud") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                )

                // Campo de Radio (Editable)
                OutlinedTextField(
                    value = radius,
                    onValueChange = { radius = it },
                    label = { Text("Radio (metros)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    supportingText = { Text("El radio define el área de cobertura") }
                )

                // Texto de ayuda si no hay permisos
                if (!locationPermissions.allPermissionsGranted) {
                    Text(
                        text = "Toca el icono para actualizar la posición GPS",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        val lat = latitude.toDoubleOrNull()
                        val lon = longitude.toDoubleOrNull()
                        val rad = radius.toDoubleOrNull()

                        if (name.isBlank()) {
                            errorMessage = "El nombre no puede estar vacío"
                            showError = true
                        } else if (lat != null && lon != null && rad != null) {
                            viewModel.updateHaven(havenId, name, lat, lon, rad)
                        } else {
                            errorMessage = "Verifica que el radio sea un número válido"
                            showError = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    enabled = updateHavenState !is HavenUiState.Loading && !isLoadingLocation
                ) {
                    if (updateHavenState is HavenUiState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Guardar Cambios")
                    }
                }
            }
        }
    }

    if (showError) {
        AlertDialog(
            onDismissRequest = { showError = false },
            title = { Text("Aviso") },
            text = { Text(errorMessage) },
            confirmButton = {
                TextButton(onClick = { showError = false }) { Text("Entendido") }
            }
        )
    }
}
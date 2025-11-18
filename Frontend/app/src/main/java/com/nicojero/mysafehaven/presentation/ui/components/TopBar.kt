package com.nicojero.mysafehaven.presentation.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RadarTopBar(
    isLoggedIn: Boolean,
    hasNotifications: Boolean,
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    TopAppBar(
        title = { Text("RADAR") },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        ),
        actions = {
            IconButton(onClick = onNotificationClick) {
                BadgedBox(
                    badge = {
                        if (hasNotifications) {
                            Badge(containerColor = Color(0xFFD88B8B))
                        }
                    }
                ) {
                    Icon(
                        Icons.Filled.Notifications,
                        contentDescription = "Notificaciones",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            IconButton(onClick = onProfileClick) {
                Icon(
                    if (isLoggedIn) Icons.Filled.AccountCircle else Icons.Filled.Person,
                    contentDescription = if (isLoggedIn) "Perfil" else "Iniciar sesión",
                    tint = if (isLoggedIn) Color(0xFF90EE90) else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    )
}
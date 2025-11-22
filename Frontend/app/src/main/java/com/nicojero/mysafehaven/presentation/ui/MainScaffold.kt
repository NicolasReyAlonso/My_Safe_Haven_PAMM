package com.nicojero.mysafehaven.presentation.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.nicojero.mysafehaven.domain.model.Notification
import com.nicojero.mysafehaven.domain.model.User
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.navigation.compose.rememberNavController
import com.nicojero.mysafehaven.data.remote.RetrofitClient
import com.nicojero.mysafehaven.data.repository.UserRepositoryImpl
import com.nicojero.mysafehaven.presentation.ui.components.BottomNavItem
import com.nicojero.mysafehaven.presentation.ui.components.RadarBottomNavigation
import com.nicojero.mysafehaven.presentation.ui.components.RadarTopBar
import com.nicojero.mysafehaven.presentation.ui.navigation.NavigationGraph
import com.nicojero.mysafehaven.presentation.ui.navigation.Screen


@Composable
fun MainScaffold() {
    val navController = rememberNavController()

    // Estado local en lugar de ViewModel para evitar problemas de dependencias
    var user by remember { mutableStateOf<User?>(null) }
    var notifications by remember { mutableStateOf<List<Notification>>(emptyList()) }
    var hasUnreadNotifications by remember { mutableStateOf(false) }

    // Cargar datos al iniciar (esto luego lo harás con el ViewModel correctamente)
    LaunchedEffect(Unit) {
        // Por ahora, datos de prueba
        user = User("1", "Usuario Demo", "demo@email.com", true)
        notifications = listOf(
            Notification("1", "Nueva notificación", false)
        )
        hasUnreadNotifications = notifications.any { !it.isRead }
    }

    val bottomNavItems = listOf(
        BottomNavItem(Screen.Home.route, Icons.Filled.Star, "Label"),
        BottomNavItem(Screen.Search.route, Icons.Filled.AddCircle, "Label"),
        BottomNavItem(Screen.Profile.route, Icons.Filled.Add, "Label"),
        BottomNavItem(Screen.Login.route, Icons.Filled.Add, "Label"),
        BottomNavItem(Screen.Profile.route, Icons.Filled.Add, "Label")

    )

    Scaffold(
        topBar = {
            RadarTopBar(
                isLoggedIn = user?.isLoggedIn ?: false,
                hasNotifications = hasUnreadNotifications,
                onNotificationClick = { /* Navegar a notificaciones */ },
                onProfileClick = { /* Navegar a perfil/login */ }
            )
        },
        bottomBar = {
            RadarBottomNavigation(navController, bottomNavItems)
        }
    ) { paddingValues ->
        NavigationGraph(
            navController = navController,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

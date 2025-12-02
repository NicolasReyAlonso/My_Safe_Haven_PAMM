package com.nicojero.mysafehaven.presentation.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.nicojero.mysafehaven.data.local.AuthDataStore
import com.nicojero.mysafehaven.data.repository.AuthRepository
import com.nicojero.mysafehaven.presentation.ui.components.BottomNavItem
import com.nicojero.mysafehaven.presentation.ui.components.RadarBottomNavigation
import com.nicojero.mysafehaven.presentation.ui.components.RadarTopBar
import com.nicojero.mysafehaven.presentation.ui.navigation.NavigationGraph
import com.nicojero.mysafehaven.presentation.ui.navigation.Screen
import com.nicojero.mysafehaven.presentation.viewmodel.AuthViewModel
import com.nicojero.mysafehaven.presentation.viewmodel.SessionState

@Composable
fun MainScaffold() {
    val navController = rememberNavController()
    val context = LocalContext.current

    // Inicializar repositorio y ViewModel
    val authDataStore = remember { AuthDataStore(context) }
    val authRepository = remember { AuthRepository(authDataStore) }
    val authViewModel = remember { AuthViewModel(authRepository) }

    val sessionState by authViewModel.sessionState.collectAsState()

    // Determinar si mostrar la UI completa
    val showFullUI = sessionState is SessionState.LoggedIn

    val bottomNavItems = listOf(
        BottomNavItem(Screen.Home.route, Icons.Filled.Home, "Inicio"),
        BottomNavItem(Screen.Search.route, Icons.Filled.Search, "Buscar"),
        BottomNavItem(Screen.Profile.route, Icons.Filled.Person, "Perfil")
    )

    Scaffold(
        topBar = {
            if (showFullUI) {
                RadarTopBar(
                    isLoggedIn = true,
                    hasNotifications = false,
                    onNotificationClick = { /* TODO */ },
                    onProfileClick = {
                        navController.navigate(Screen.Profile.route)
                    }
                )
            }
        },
        bottomBar = {
            if (showFullUI) {
                RadarBottomNavigation(navController, bottomNavItems)
            }
        }
    ) { paddingValues ->
        NavigationGraph(
            navController = navController,
            authViewModel = authViewModel,
            modifier = Modifier.padding(paddingValues)
        )
    }
}
package com.nicojero.mysafehaven.presentation.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
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

    // ✅ Usar hiltViewModel() en lugar de crear instancias manualmente
    val authViewModel: AuthViewModel = hiltViewModel()

    val sessionState by authViewModel.sessionState.collectAsState()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    // Determinar si mostrar la UI completa (solo en pantallas principales)
    val showFullUI = sessionState is SessionState.LoggedIn &&
            currentRoute in listOf(
        Screen.Home.route,
        Screen.Search.route,
        Screen.Profile.route,
        Screen.HavensList.route
    )

    val bottomNavItems = listOf(
        BottomNavItem(Screen.Home.route, Icons.Filled.Home, "Inicio"),
        BottomNavItem(Screen.HavensList.route, Icons.Filled.Place, "Havens"),
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
                        navController.navigate(Screen.Profile.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
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
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(paddingValues)
        )
    }
}
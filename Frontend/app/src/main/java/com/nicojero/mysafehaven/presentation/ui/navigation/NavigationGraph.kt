package com.nicojero.mysafehaven.presentation.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.nicojero.mysafehaven.presentation.ui.screens.*
import com.nicojero.mysafehaven.presentation.viewmodel.AuthViewModel



@Composable
fun NavigationGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = modifier
    ) {
        // Splash Screen - Verifica sesión
        composable(Screen.Splash.route) {
            SplashScreen(navController, authViewModel)
        }

        // Auth Screens
        composable(Screen.Login.route) {
            LoginScreen(navController, authViewModel)
        }

        composable(Screen.Register.route) {
            RegisterScreen(navController, authViewModel)
        }

        // Main Screens (requieren autenticación)
        composable(Screen.Home.route) {
            HomeScreen()
        }

        composable(Screen.Search.route) {
            SearchScreen()
        }

        composable(Screen.Profile.route) {
            ProfileScreen(authViewModel, navController)
        }
    }
}
package com.nicojero.mysafehaven.presentation.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.nicojero.mysafehaven.presentation.ui.screens.*
import com.nicojero.mysafehaven.presentation.viewmodel.AuthViewModel
import com.nicojero.mysafehaven.presentation.viewmodel.HavenViewModel

@Composable
fun NavigationGraph(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // ========== SPLASH ==========
        composable(Screen.Splash.route) {
            val authViewModel: AuthViewModel = hiltViewModel()
            SplashScreen(
                viewModel = authViewModel,
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // ========== AUTH ==========
        composable(Screen.Login.route) {
            val authViewModel: AuthViewModel = hiltViewModel()
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Register.route) {
            val authViewModel: AuthViewModel = hiltViewModel()
            RegisterScreen(
                viewModel = authViewModel,
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                }
            )
        }

        // ========== MAIN SCREENS ==========
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToHavens = {
                    navController.navigate(Screen.HavensList.route)
                }
            )
        }

        composable(Screen.Search.route) {
            SearchScreen()
        }

        composable(Screen.Profile.route) {
            val authViewModel: AuthViewModel = hiltViewModel()
            ProfileScreen(
                viewModel = authViewModel,
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // ========== HAVENS ==========
        composable(Screen.HavensList.route) {
            val havenViewModel: HavenViewModel = hiltViewModel()
            HavensListScreen(
                viewModel = havenViewModel,
                onHavenClick = { haven ->
                    navController.navigate(Screen.HavenDetail.createRoute(haven.id))
                },
                onCreateHavenClick = {
                    navController.navigate(Screen.CreateHaven.route)
                }
            )
        }

        composable(Screen.CreateHaven.route) {
            val havenViewModel: HavenViewModel = hiltViewModel()
            CreateHavenScreen(
                viewModel = havenViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onHavenCreated = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.HavenDetail.route,
            arguments = listOf(navArgument("havenId") { type = NavType.IntType })
        ) { backStackEntry ->
            val havenViewModel: HavenViewModel = hiltViewModel()
            val havenId = backStackEntry.arguments?.getInt("havenId") ?: return@composable

            HavenDetailScreen(
                havenId = havenId,
                viewModel = havenViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
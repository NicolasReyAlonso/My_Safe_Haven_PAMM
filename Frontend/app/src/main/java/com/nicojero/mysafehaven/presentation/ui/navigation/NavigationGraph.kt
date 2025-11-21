package com.nicojero.mysafehaven.presentation.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.nicojero.mysafehaven.presentation.ui.screens.HomeScreen
import com.nicojero.mysafehaven.presentation.ui.screens.LoginScreen
import com.nicojero.mysafehaven.presentation.ui.screens.ProfileScreen
import com.nicojero.mysafehaven.presentation.ui.screens.RegisterScreen
import com.nicojero.mysafehaven.presentation.ui.screens.SearchScreen

@Composable
fun NavigationGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Login.route) {
            LoginScreen(navController)
        }
        composable(Screen.Register.route) {
            RegisterScreen(navController)
        }
        composable(Screen.Home.route) {
            HomeScreen()
        }
        composable(Screen.Search.route) {
            SearchScreen()
        }
        composable(Screen.Profile.route) {
            ProfileScreen()
        }
    }
}
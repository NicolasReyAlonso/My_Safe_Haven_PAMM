package com.nicojero.mysafehaven.presentation.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object Search : Screen("search")
    object Profile : Screen("profile")

    // Havens
    object HavensList : Screen("havens_list")
    object CreateHaven : Screen("create_haven")
    object HavenDetail : Screen("haven_detail/{havenId}") {
        fun createRoute(havenId: Int) = "haven_detail/$havenId"
    }
}

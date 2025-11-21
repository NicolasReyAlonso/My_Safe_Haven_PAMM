package com.nicojero.mysafehaven.presentation.ui.navigation

<<<<<<< HEAD
sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object Search : Screen("search")
    object Profile : Screen("profile")
=======
sealed class `Screen`(val route: String) {
    object Home : `Screen`("home")
    object Search : `Screen`("search")
    object Profile : `Screen`("profile")
>>>>>>> e8b9d1aad904928b29b16c32d7b0574bf7d2bc91
}
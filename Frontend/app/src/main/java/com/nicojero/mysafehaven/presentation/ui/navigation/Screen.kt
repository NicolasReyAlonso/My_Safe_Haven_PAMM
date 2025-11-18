package com.nicojero.mysafehaven.presentation.ui.navigation

sealed class `Screen`(val route: String) {
    object Home : `Screen`("home")
    object Search : `Screen`("search")
    object Profile : `Screen`("profile")
}
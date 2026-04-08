package com.example.xinggui.navigation

sealed class AppRoute(val route: String) {
    data object Login : AppRoute("login")
    data object Register : AppRoute("register")
    data object RoleSelect : AppRoute("role_select")
    data object Main : AppRoute("main")
    data object MainReport : AppRoute("main/report")
    data object MainArchive : AppRoute("main/archive")
    data object MainGoals : AppRoute("main/goals")
    data object MainResources : AppRoute("main/resources")
}

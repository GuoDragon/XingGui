package com.example.xinggui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.xinggui.data.repository.AppRepository
import com.example.xinggui.data.repository.DataRepository
import com.example.xinggui.presentation.auth.login.LoginScreen
import com.example.xinggui.presentation.auth.register.RegisterScreen
import com.example.xinggui.presentation.auth.roleselect.RoleSelectScreen
import com.example.xinggui.presentation.main.MainScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    repository: AppRepository = DataRepository
) {
    val session = repository.getSessionState()
    val startDestination = when {
        session.isAuthenticated && session.activeRole != null -> AppRoute.Main.route
        session.isAuthenticated -> AppRoute.RoleSelect.route
        else -> AppRoute.Login.route
    }
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(AppRoute.Login.route) {
            LoginScreen(
                onNavigateToRegister = { navController.navigate(AppRoute.Register.route) },
                onNavigateToRoleSelect = { navController.navigate(AppRoute.RoleSelect.route) },
                repository = repository
            )
        }
        composable(AppRoute.Register.route) {
            RegisterScreen(
                onBackClick = { navController.popBackStack() },
                onNavigateToRoleSelect = { navController.navigate(AppRoute.RoleSelect.route) },
                repository = repository
            )
        }
        composable(AppRoute.RoleSelect.route) {
            RoleSelectScreen(
                onBackClick = { navController.popBackStack() },
                onNavigateToMain = {
                    navController.navigate(AppRoute.Main.route) {
                        popUpTo(AppRoute.Login.route) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                repository = repository
            )
        }
        composable(AppRoute.Main.route) {
            MainScreen(
                onLogoutSuccess = {
                    navController.navigate(AppRoute.Login.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
                repository = repository
            )
        }
    }
}

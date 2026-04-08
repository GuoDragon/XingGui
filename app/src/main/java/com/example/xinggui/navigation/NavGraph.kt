package com.example.xinggui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.xinggui.presentation.auth.login.LoginScreen
import com.example.xinggui.presentation.auth.register.RegisterScreen
import com.example.xinggui.presentation.auth.roleselect.RoleSelectScreen
import com.example.xinggui.presentation.main.MainScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = AppRoute.Login.route,
        modifier = modifier
    ) {
        composable(AppRoute.Login.route) {
            LoginScreen(
                onNavigateToRegister = { navController.navigate(AppRoute.Register.route) },
                onNavigateToRoleSelect = { navController.navigate(AppRoute.RoleSelect.route) }
            )
        }
        composable(AppRoute.Register.route) {
            RegisterScreen(
                onBackClick = { navController.popBackStack() },
                onNavigateToRoleSelect = { navController.navigate(AppRoute.RoleSelect.route) }
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
                }
            )
        }
        composable(AppRoute.Main.route) {
            MainScreen()
        }
    }
}

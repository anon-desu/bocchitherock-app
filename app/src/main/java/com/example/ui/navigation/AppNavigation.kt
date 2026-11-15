package com.example.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LoginScreen
import com.example.viewmodel.LoginViewModel
import com.example.viewmodel.MainViewModel
import kotlinx.serialization.Serializable

@Serializable
object HomeRoute

@Serializable
object LoginRoute

@Composable
fun BocchiNavHost(
    mainViewModel: MainViewModel,
    loginViewModelFactory: LoginViewModel.Factory
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = HomeRoute
    ) {
        composable<HomeRoute> {
            HomeScreen(
                viewModel = mainViewModel,
                onNavigateToLogin = {
                    if (navController.currentBackStackEntry?.destination?.hasRoute<HomeRoute>() == true) {
                        navController.navigate(LoginRoute) {
                            launchSingleTop = true
                        }
                    }
                }
            )
        }
        composable<LoginRoute> {
            LoginScreen(
                factory = loginViewModelFactory,
                onNavigateBack = {
                    if (navController.currentBackStackEntry?.destination?.hasRoute<LoginRoute>() == true) {
                        navController.popBackStack()
                    }
                }
            )
        }
    }
}

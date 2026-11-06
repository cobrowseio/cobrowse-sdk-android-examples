package io.cobrowse.sample.compose.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import io.cobrowse.sample.compose.ui.account.AccountScreen
import io.cobrowse.sample.compose.ui.account.AgentPresentScreen
import io.cobrowse.sample.compose.ui.login.LoginScreen
import io.cobrowse.sample.compose.ui.settings.SettingsScreen
import io.cobrowse.sample.data.LoginRepository

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Main : Screen("main")
    object Account : Screen("account")
    object AgentPresent : Screen("agent_present")
    object Settings : Screen("settings")
}

@Composable
fun AppNavigation(navController: NavHostController) {
    val viewModelFactory = remember { CobrowseViewModelFactory() }
    val startDestination = remember {
        if (LoginRepository.getInstance().isLoggedIn) Screen.Main.route else Screen.Login.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                viewModelFactory = viewModelFactory,
                onLoginSuccess = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Main.route) {
            MainScreen(
                viewModelFactory = viewModelFactory,
                onAccount = { navController.navigate(Screen.Account.route) }
            )
        }

        composable(Screen.Account.route) {
            AccountScreen(
                viewModelFactory = viewModelFactory,
                onAgentPresent = { navController.navigate(Screen.AgentPresent.route) },
                onSettings = { navController.navigate(Screen.Settings.route) },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AgentPresent.route) {
            AgentPresentScreen(
                viewModelFactory = viewModelFactory,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}

package io.cobrowse.sample.compose.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import io.cobrowse.sample.compose.ui.account.AccountScreen
import io.cobrowse.sample.compose.ui.login.LoginScreen
import io.cobrowse.sample.compose.ui.transactions.TransactionDetailScreen
import io.cobrowse.sample.data.LoginRepository

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Main : Screen("main")
    object Account : Screen("account")
    object TransactionDetail : Screen("transaction_detail/{url}") {
        fun createRoute(url: String) = "transaction_detail/$url"
    }
}

@Composable
fun AppNavigation(
    owner: ViewModelStoreOwner,
    navController: NavHostController = rememberNavController(),
) {
    val viewModelFactory = CobrowseViewModelFactory()
    val login = LoginRepository.getInstance()

    val startDestination = if (login.isLoggedIn) {
        Screen.Main.route
    } else {
        Screen.Login.route
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
                navController = navController,
                viewModelFactory = viewModelFactory
            )
        }

        composable(Screen.Account.route) {
            AccountScreen(
                viewModelFactory = viewModelFactory,
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.TransactionDetail.route,
            arguments = listOf(navArgument("url") { type = NavType.StringType })
        ) { backStackEntry ->
            val url = backStackEntry.arguments?.getString("url") ?: ""
            TransactionDetailScreen(
                url = url,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

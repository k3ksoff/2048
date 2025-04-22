package com.example.a2048.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.a2048.ui.screens.admin.AdminScreen
import com.example.a2048.ui.screens.auth.LoginScreen
import com.example.a2048.ui.screens.auth.RegisterScreen
import com.example.a2048.ui.screens.game.GameScreen
import com.example.a2048.ui.screens.leaderboard.LeaderboardScreen
import com.example.a2048.ui.screens.splash.SplashScreen

object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val GAME = "game"
    const val LEADERBOARD = "leaderboard"
    const val ADMIN = "admin"
}

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = Routes.SPLASH
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Routes.SPLASH) {
            SplashScreen(navController = navController)
        }
        
        composable(Routes.LOGIN) {
            LoginScreen(navController = navController)
        }
        
        composable(Routes.REGISTER) {
            RegisterScreen(navController = navController)
        }
        
        composable(Routes.GAME) {
            GameScreen(navController = navController)
        }
        
        composable(Routes.LEADERBOARD) {
            LeaderboardScreen(navController = navController)
        }
        
        composable(Routes.ADMIN) {
            AdminScreen(navController = navController)
        }
    }
} 
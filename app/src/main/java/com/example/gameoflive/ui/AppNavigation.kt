package com.example.gameoflive.ui

import android.net.Uri
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.gameoflive.presentation.game.GameLoaderViewModel
import androidx.hilt.navigation.compose.hiltViewModel

private object Routes {
    const val MENU = "menu"
    const val LOAD = "load"
    const val GAME = "game"
}

@Composable
fun AppNav() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.MENU) {
        composable(Routes.MENU) {
            MainMenuScreen(
                onNewSimulation = {
                    navController.navigate("${Routes.GAME}?save=new")
                },
                onLoadSimulation = {
                    navController.navigate(Routes.LOAD)
                }
            )
        }

        composable(Routes.LOAD) {
            LoadSimulationScreen(
                onBack = { navController.navigateUp() },
                onSelect = { fileName ->
                    val encoded = Uri.encode(fileName)
                    navController.navigate("${Routes.GAME}?save=$encoded")
                }
            )
        }

        composable(
            route = "${Routes.GAME}?save={save}",
            arguments = listOf(
                navArgument("save") {
                    type = NavType.StringType
                    defaultValue = "new"
                }
            )
        ) { backEntry ->
            val arg = backEntry.arguments?.getString("save") ?: "new"
            val decodedName = Uri.decode(arg)
            
            GameScreen(
                saveFileName = if (decodedName == "new") null else decodedName,
                onBackToMenu = {
                    navController.popBackStack(Routes.MENU, false)
                }
            )
        }
    }
} 
package com.example.pwta_projekt.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.pwta_projekt.ui.screens.ModelListScreen
import com.example.pwta_projekt.ui.screens.ModelViewerScreen

sealed class Screen(val route: String) {
    object ModelList : Screen("model_list")
    object ModelViewer : Screen("model_viewer/{modelPath}") {
        fun createRoute(modelPath: String): String {
            return "model_viewer/${modelPath.replace("/", "|")}"
        }
    }
}

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.ModelList.route
    ) {
        composable(Screen.ModelList.route) {
            ModelListScreen(
                onModelSelected = { modelInfo ->
                    val encodedPath = modelInfo.path.replace("/", "|")
                    navController.navigate(Screen.ModelViewer.createRoute(encodedPath))
                }
            )
        }

        composable(
            route = Screen.ModelViewer.route,
            arguments = listOf(
                navArgument("modelPath") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val encodedPath = backStackEntry.arguments?.getString("modelPath") ?: return@composable
            val modelPath = encodedPath.replace("|", "/")

            ModelViewerScreen(
                modelPath = modelPath,
                onBackPressed = { navController.popBackStack() }
            )
        }
    }
}

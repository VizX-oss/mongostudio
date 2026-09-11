package com.mongostudio.app.ui.navigation

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.mongostudio.app.ui.screens.*
import com.mongostudio.app.viewmodel.MongoStudioViewModel

// Shared slide transition specs — spring-based for natural, physics feel
private val slideSpec = spring<Float>(
    dampingRatio = 0.85f,
    stiffness = Spring.StiffnessMediumLow
)
private val fadeTween = tween<Float>(durationMillis = 200)

// Push forward: slide in from right, old screen slides out left
private val enterTransition = slideInHorizontally(
    animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow),
    initialOffsetX = { fullWidth -> fullWidth / 4 }
) + fadeIn(animationSpec = fadeTween)

private val exitTransition = slideOutHorizontally(
    animationSpec = spring(dampingRatio = 1f, stiffness = Spring.StiffnessMediumLow),
    targetOffsetX = { fullWidth -> -fullWidth / 5 }
) + fadeOut(animationSpec = tween(durationMillis = 180))

// Pop back: slide in from left, screen slides out to right
private val popEnterTransition = slideInHorizontally(
    animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow),
    initialOffsetX = { fullWidth -> -fullWidth / 4 }
) + fadeIn(animationSpec = fadeTween)

private val popExitTransition = slideOutHorizontally(
    animationSpec = spring(dampingRatio = 1f, stiffness = Spring.StiffnessMediumLow),
    targetOffsetX = { fullWidth -> fullWidth / 4 }
) + fadeOut(animationSpec = tween(durationMillis = 180))

@Composable
fun MongoStudioNavHost(
    navController: NavHostController,
    viewModel: MongoStudioViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Connection.route,
        enterTransition = { enterTransition },
        exitTransition = { exitTransition },
        popEnterTransition = { popEnterTransition },
        popExitTransition = { popExitTransition }
    ) {
        composable(Screen.Connection.route) {
            ConnectionScreen(
                viewModel = viewModel,
                onNavigateToDashboard = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Connection.route) { inclusive = true }
                    }
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(
                viewModel = viewModel,
                onNavigateToDatabase = { dbName ->
                    navController.navigate(Screen.DatabaseDetail.createRoute(dbName))
                },
                onNavigateToConsole = {
                    navController.navigate(Screen.Console.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onDisconnect = {
                    navController.navigate(Screen.Connection.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.DatabaseDetail.route,
            arguments = listOf(navArgument("dbName") { type = NavType.StringType })
        ) { backStackEntry ->
            val dbName = backStackEntry.arguments?.getString("dbName") ?: ""
            DatabaseDetailScreen(
                dbName = dbName,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDocuments = { db, col ->
                    navController.navigate(Screen.Documents.createRoute(db, col))
                },
                onNavigateToAggregation = { db, col ->
                    navController.navigate(Screen.Aggregation.createRoute(db, col))
                },
                onNavigateToIndexes = { db, col ->
                    navController.navigate(Screen.Indexes.createRoute(db, col))
                }
            )
        }

        composable(
            route = Screen.Documents.route,
            arguments = listOf(
                navArgument("dbName") { type = NavType.StringType },
                navArgument("colName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val dbName = backStackEntry.arguments?.getString("dbName") ?: ""
            val colName = backStackEntry.arguments?.getString("colName") ?: ""
            DocumentsScreen(
                dbName = dbName,
                colName = colName,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Aggregation.route,
            arguments = listOf(
                navArgument("dbName") { type = NavType.StringType },
                navArgument("colName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val dbName = backStackEntry.arguments?.getString("dbName") ?: ""
            val colName = backStackEntry.arguments?.getString("colName") ?: ""
            AggregationScreen(
                dbName = dbName,
                colName = colName,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Indexes.route,
            arguments = listOf(
                navArgument("dbName") { type = NavType.StringType },
                navArgument("colName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val dbName = backStackEntry.arguments?.getString("dbName") ?: ""
            val colName = backStackEntry.arguments?.getString("colName") ?: ""
            IndexesScreen(
                dbName = dbName,
                colName = colName,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Console.route) {
            ConsoleScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

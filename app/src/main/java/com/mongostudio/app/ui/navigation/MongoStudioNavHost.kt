package com.mongostudio.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.mongostudio.app.ui.screens.*
import com.mongostudio.app.viewmodel.MongoStudioViewModel

@Composable
fun MongoStudioNavHost(
    navController: NavHostController,
    viewModel: MongoStudioViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Connection.route
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

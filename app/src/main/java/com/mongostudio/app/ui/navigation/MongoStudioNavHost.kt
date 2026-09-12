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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.mongostudio.app.ui.screens.*
import com.mongostudio.app.viewmodel.MongoStudioViewModel

// Shared slide transition specs — spring-based for natural, physics feel (§8.1, §9.3)
private val enterTransition = slideInHorizontally(
    animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow),
    initialOffsetX = { fullWidth -> fullWidth / 4 }
) + fadeIn(animationSpec = tween(durationMillis = 200))

private val exitTransition = slideOutHorizontally(
    animationSpec = spring(dampingRatio = 1f, stiffness = Spring.StiffnessMediumLow),
    targetOffsetX = { fullWidth -> -fullWidth / 5 }
) + fadeOut(animationSpec = tween(durationMillis = 180))

private val popEnterTransition = slideInHorizontally(
    animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow),
    initialOffsetX = { fullWidth -> -fullWidth / 4 }
) + fadeIn(animationSpec = tween(durationMillis = 200))

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
        startDestination = Screen.Connection,
        enterTransition = { enterTransition },
        exitTransition = { exitTransition },
        popEnterTransition = { popEnterTransition },
        popExitTransition = { popExitTransition }
    ) {
        composable<Screen.Connection> {
            ConnectionScreen(
                viewModel = viewModel,
                onNavigateToDashboard = {
                    navController.navigate(Screen.Dashboard) {
                        popUpTo<Screen.Connection> { inclusive = true }
                    }
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings)
                }
            )
        }

        composable<Screen.Dashboard> {
            DashboardScreen(
                viewModel = viewModel,
                onNavigateToDatabase = { dbName ->
                    navController.navigate(Screen.DatabaseDetail(dbName = dbName))
                },
                onNavigateToConsole = {
                    navController.navigate(Screen.Console)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings)
                },
                onDisconnect = {
                    navController.navigate(Screen.Connection) {
                        popUpTo<Screen.Dashboard> { inclusive = true }
                    }
                }
            )
        }

        composable<Screen.DatabaseDetail> { backStackEntry ->
            val route = backStackEntry.toRoute<Screen.DatabaseDetail>()
            DatabaseDetailScreen(
                dbName = route.dbName,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDocuments = { db, col ->
                    navController.navigate(Screen.Documents(dbName = db, colName = col))
                },
                onNavigateToAggregation = { db, col ->
                    navController.navigate(Screen.Aggregation(dbName = db, colName = col))
                },
                onNavigateToIndexes = { db, col ->
                    navController.navigate(Screen.Indexes(dbName = db, colName = col))
                }
            )
        }

        composable<Screen.Documents> { backStackEntry ->
            val route = backStackEntry.toRoute<Screen.Documents>()
            DocumentsScreen(
                dbName = route.dbName,
                colName = route.colName,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<Screen.Aggregation> { backStackEntry ->
            val route = backStackEntry.toRoute<Screen.Aggregation>()
            AggregationScreen(
                dbName = route.dbName,
                colName = route.colName,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<Screen.Indexes> { backStackEntry ->
            val route = backStackEntry.toRoute<Screen.Indexes>()
            IndexesScreen(
                dbName = route.dbName,
                colName = route.colName,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<Screen.Console> {
            ConsoleScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<Screen.Settings> {
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

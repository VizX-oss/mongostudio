package com.mongostudio.app.ui.navigation

sealed class Screen(val route: String) {
    object Connection : Screen("connection")
    object Dashboard : Screen("dashboard")
    object DatabaseDetail : Screen("database/{dbName}") {
        fun createRoute(dbName: String) = "database/$dbName"
    }
    object Documents : Screen("documents/{dbName}/{colName}") {
        fun createRoute(dbName: String, colName: String) = "documents/$dbName/$colName"
    }
    object Aggregation : Screen("aggregation/{dbName}/{colName}") {
        fun createRoute(dbName: String, colName: String) = "aggregation/$dbName/$colName"
    }
    object Indexes : Screen("indexes/{dbName}/{colName}") {
        fun createRoute(dbName: String, colName: String) = "indexes/$dbName/$colName"
    }
    object Console : Screen("console")
    object Settings : Screen("settings")
}

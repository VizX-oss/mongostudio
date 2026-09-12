package com.mongostudio.app.ui.navigation

import kotlinx.serialization.Serializable

/**
 * Type-Safe Navigation Routes for MongoStudio (Navigation 2.8+) (§10).
 */
sealed interface Screen {
    @Serializable
    data object Connection : Screen

    @Serializable
    data object Dashboard : Screen

    @Serializable
    data class DatabaseDetail(val dbName: String) : Screen

    @Serializable
    data class Documents(val dbName: String, val colName: String) : Screen

    @Serializable
    data class Aggregation(val dbName: String, val colName: String) : Screen

    @Serializable
    data class Indexes(val dbName: String, val colName: String) : Screen

    @Serializable
    data object Console : Screen

    @Serializable
    data object Settings : Screen
}

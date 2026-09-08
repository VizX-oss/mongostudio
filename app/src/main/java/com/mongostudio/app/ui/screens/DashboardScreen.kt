package com.mongostudio.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mongostudio.app.data.model.DatabaseInfo
import com.mongostudio.app.data.model.FormatUtils
import com.mongostudio.app.ui.components.ConfirmDialog
import com.mongostudio.app.ui.components.MetricCard
import com.mongostudio.app.ui.components.TopHeader
import com.mongostudio.app.ui.theme.*
import com.mongostudio.app.viewmodel.MongoStudioViewModel

@Composable
fun DashboardScreen(
    viewModel: MongoStudioViewModel,
    onNavigateToDatabase: (String) -> Unit,
    onNavigateToConsole: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onDisconnect: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var newDbName by remember { mutableStateOf("") }
    var newColName by remember { mutableStateOf("") }
    var dbToDrop by remember { mutableStateOf<DatabaseInfo?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadOverview()
    }

    Scaffold(
        topBar = {
            TopHeader(
                title = uiState.activeClusterName ?: "MongoDB Cluster",
                subtitle = "v${uiState.activeClusterVersion ?: "Unknown"}",
                isConnectedToCluster = true,
                pingMs = uiState.activeClusterPingMs,
                onRefreshClick = { viewModel.loadOverview() },
                onConsoleClick = onNavigateToConsole,
                onSettingsClick = onNavigateToSettings,
                onDisconnectClick = {
                    viewModel.disconnect()
                    onDisconnect()
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = EmeraldPrimary,
                contentColor = TextOnPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Database")
            }
        },
        containerColor = BackgroundDark
    ) { padding ->
        val overview = uiState.overview

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Error banner
            if (uiState.errorMessage != null) {
                item {
                    Surface(
                        color = RoseAccent.copy(alpha = 0.15f),
                        shape = MaterialTheme.shapes.small,
                        border = androidx.compose.foundation.BorderStroke(1.dp, RoseAccent.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = uiState.errorMessage!!,
                            color = RoseAccent,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }

            // Metric cards row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = "Storage Size",
                        value = FormatUtils.formatBytes(overview?.totalSize ?: 0L),
                        icon = Icons.Default.Storage,
                        modifier = Modifier.weight(1f),
                        accentColor = EmeraldPrimary
                    )
                    MetricCard(
                        title = "Databases",
                        value = "${overview?.databases?.size ?: 0}",
                        icon = Icons.Default.Folder,
                        modifier = Modifier.weight(1f),
                        accentColor = SkyAccent
                    )
                }
            }

            item {
                val totalCollections = overview?.databases?.sumOf { it.collectionsCount } ?: 0
                val uptimeStr = FormatUtils.formatUptime(overview?.serverStatus?.uptime)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = "Collections",
                        value = "$totalCollections",
                        icon = Icons.Default.Dataset,
                        modifier = Modifier.weight(1f),
                        accentColor = AmberAccent
                    )
                    MetricCard(
                        title = "Uptime",
                        value = uptimeStr,
                        icon = Icons.Default.Schedule,
                        modifier = Modifier.weight(1f),
                        accentColor = PurpleAccent
                    )
                }
            }

            // Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Databases (${overview?.databases?.size ?: 0})",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    TextButton(onClick = { showCreateDialog = true }) {
                        Icon(Icons.Default.AddCircleOutline, contentDescription = null, tint = EmeraldLight, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New DB / Col", color = EmeraldLight)
                    }
                }
            }

            // Database items
            if (overview == null && uiState.isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = EmeraldPrimary)
                    }
                }
            } else {
                items(overview?.databases ?: emptyList()) { db ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToDatabase(db.name) },
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderDark),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.FolderOpen,
                                        contentDescription = null,
                                        tint = if (db.isSystemDb) TextMuted else EmeraldLight,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = db.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    if (db.isSystemDb) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(
                                            color = CardBorderDark,
                                            shape = MaterialTheme.shapes.extraSmall
                                        ) {
                                            Text(
                                                text = "SYSTEM",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = TextSecondary,
                                                fontSize = 9.sp,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (!db.isSystemDb) {
                                        IconButton(
                                            onClick = { dbToDrop = db },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.DeleteOutline,
                                                contentDescription = "Drop DB",
                                                tint = RoseAccent,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = "Open",
                                        tint = TextMuted,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${db.collectionsCount} collections • ${db.objectsCount} docs",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                                Text(
                                    text = FormatUtils.formatBytes(db.sizeOnDisk),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = EmeraldLight
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Drop DB Confirmation
    dbToDrop?.let { db ->
        ConfirmDialog(
            title = "Drop Database '${db.name}'?",
            message = "This will permanently destroy database '${db.name}' and all its collections and documents. This cannot be undone.",
            confirmText = "Drop Database",
            isDestructive = true,
            onConfirm = {
                viewModel.dropDatabase(db.name)
                dbToDrop = null
            },
            onDismiss = { dbToDrop = null }
        )
    }

    // Create DB / Collection Dialog
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            containerColor = SurfaceDark,
            title = { Text("Create Database & Collection", color = TextPrimary) },
            text = {
                Column {
                    Text("Database Name", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = newDbName,
                        onValueChange = { newDbName = it },
                        singleLine = true,
                        placeholder = { Text("e.g. ecommerce_db", color = TextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = CardDark,
                            unfocusedContainerColor = CardDark,
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = CardBorderDark
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Initial Collection Name", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = newColName,
                        onValueChange = { newColName = it },
                        singleLine = true,
                        placeholder = { Text("e.g. users", color = TextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = CardDark,
                            unfocusedContainerColor = CardDark,
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = CardBorderDark
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newDbName.isNotBlank() && newColName.isNotBlank()) {
                            viewModel.createCollection(newDbName.trim(), newColName.trim())
                            showCreateDialog = false
                            newDbName = ""
                            newColName = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = TextOnPrimary)
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

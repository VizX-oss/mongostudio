package com.mongostudio.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mongostudio.app.data.model.DatabaseInfo
import com.mongostudio.app.data.model.FormatUtils
import com.mongostudio.app.ui.components.*
import com.mongostudio.app.ui.theme.*
import com.mongostudio.app.viewmodel.MongoStudioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: MongoStudioViewModel,
    onNavigateToDatabase: (String) -> Unit,
    onNavigateToConsole: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onDisconnect: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val uiState by viewModel.uiState.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var newDbName by remember { mutableStateOf("") }
    var newColName by remember { mutableStateOf("") }
    var dbToDrop by remember { mutableStateOf<DatabaseInfo?>(null) }
    var selectedFilter by remember { mutableStateOf("all") }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.loadOverview()
    }

    val overview = uiState.overview
    val allDatabases = overview?.databases ?: emptyList()
    val filteredDatabases = remember(allDatabases, selectedFilter, searchQuery) {
        allDatabases.filter { db ->
            val matchesFilter = when (selectedFilter) {
                "user" -> !db.isSystemDb
                "system" -> db.isSystemDb
                else -> true
            }
            val matchesSearch = searchQuery.isBlank() || db.name.contains(searchQuery, ignoreCase = true)
            matchesFilter && matchesSearch
        }
    }

    Scaffold(
        topBar = {
            TopHeader(
                title = uiState.activeClusterName ?: "MongoDB Cluster",
                subtitle = "v${uiState.activeClusterVersion ?: "Unknown"} • Direct Wire",
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
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                tonalElevation = 3.dp
            ) {
                NavigationBarItem(
                    selected = false,
                    onClick = {
                        haptic.performClickFeedback()
                        onDisconnect()
                    },
                    icon = { Icon(Icons.Default.VpnKey, contentDescription = "Sessions") },
                    label = { Text("Sessions") }
                )
                NavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = { Icon(Icons.Default.Storage, contentDescription = "Databases") },
                    label = { Text("Databases") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = {
                        haptic.performClickFeedback()
                        onNavigateToConsole()
                    },
                    icon = { Icon(Icons.Default.Terminal, contentDescription = "Console") },
                    label = { Text("Console") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = {
                        haptic.performClickFeedback()
                        onNavigateToSettings()
                    },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") }
                )
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    haptic.performClickFeedback()
                    showCreateDialog = true
                },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("New Database", fontWeight = FontWeight.Bold) },
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Cluster Metrics Grid
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricCard(
                            title = "Total Size",
                            value = FormatUtils.formatBytes(overview?.totalSize ?: 0L),
                            icon = Icons.Default.Storage,
                            accentColor = EmeraldPrimary,
                            subValue = "${allDatabases.size} databases",
                            modifier = Modifier.weight(1f)
                        )

                        MetricCard(
                            title = "Collections",
                            value = allDatabases.sumOf { it.collectionsCount }.toString(),
                            icon = Icons.Default.Layers,
                            accentColor = SkyAccent,
                            subValue = "${allDatabases.sumOf { it.objectsCount }} total docs",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricCard(
                            title = "Latency",
                            value = "${uiState.activeClusterPingMs ?: 0} ms",
                            icon = Icons.Default.Speed,
                            accentColor = AmberAccent,
                            subValue = "v${uiState.activeClusterVersion ?: "Unknown"}",
                            modifier = Modifier.weight(1f)
                        )

                        MetricCard(
                            title = "Status",
                            value = if (uiState.isConnectedToCluster) "Online" else "Offline",
                            icon = Icons.Default.CheckCircleOutline,
                            accentColor = PurpleAccent,
                            subValue = "Direct TLS Socket",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Search and Filter Bar
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search databases...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedFilter == "all",
                            onClick = { selectedFilter = "all" },
                            label = { Text("All (${allDatabases.size})") },
                            shape = CircleShape
                        )
                        FilterChip(
                            selected = selectedFilter == "user",
                            onClick = { selectedFilter = "user" },
                            label = { Text("User DBs (${allDatabases.count { !it.isSystemDb }})") },
                            shape = CircleShape
                        )
                        FilterChip(
                            selected = selectedFilter == "system",
                            onClick = { selectedFilter = "system" },
                            label = { Text("System (${allDatabases.count { it.isSystemDb }})") },
                            shape = CircleShape
                        )
                    }
                }
            }

            // Loading Indicator
            if (uiState.isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }

            // Databases List
            if (!uiState.isLoading && filteredDatabases.isEmpty()) {
                item {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(36.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.FolderOpen,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(44.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "No databases match query",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            } else {
                items(filteredDatabases, key = { it.name }) { db ->
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = CircleShape,
                                        color = if (db.isSystemDb) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.primaryContainer,
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = if (db.isSystemDb) Icons.Default.Lock else Icons.Default.Folder,
                                                contentDescription = null,
                                                tint = if (db.isSystemDb) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Text(
                                            text = db.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${db.collectionsCount} collections • ${FormatUtils.formatBytes(db.sizeOnDisk)}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (!db.isSystemDb) {
                                        IconButton(
                                            onClick = {
                                                haptic.performClickFeedback()
                                                dbToDrop = db
                                            }
                                        ) {
                                            Icon(
                                                Icons.Default.DeleteOutline,
                                                contentDescription = "Drop database",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }

                                    FilledTonalButton(
                                        onClick = {
                                            haptic.performClickFeedback()
                                            onNavigateToDatabase(db.name)
                                        },
                                        shape = CircleShape,
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                    ) {
                                        Text("Explore", fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Spacing for FAB
            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }

    // Drop Database Dialog
    if (dbToDrop != null) {
        val target = dbToDrop!!
        ConfirmDialog(
            title = "Drop Database: ${target.name}",
            message = "Are you sure you want to completely DROP '${target.name}'? This will delete all collections and documents permanently.",
            confirmText = "Drop Database",
            dismissText = "Cancel",
            isDestructive = true,
            onConfirm = {
                viewModel.dropDatabase(target.name)
                dbToDrop = null
            },
            onDismiss = { dbToDrop = null }
        )
    }

    // Create Database Dialog
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Create New Database") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = newDbName,
                        onValueChange = { newDbName = it },
                        label = { Text("Database Name") },
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newColName,
                        onValueChange = { newColName = it },
                        label = { Text("Initial Collection Name") },
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium,
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
                    shape = CircleShape
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel")
                }
            },
            shape = MaterialTheme.shapes.extraLarge
        )
    }
}

package com.mongostudio.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
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
import com.mongostudio.app.ui.components.*
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
    var selectedFilter by remember { mutableStateOf("all") }

    LaunchedEffect(Unit) {
        viewModel.loadOverview()
    }

    val overview = uiState.overview
    val allDatabases = overview?.databases ?: emptyList()
    val filteredDatabases = remember(allDatabases, selectedFilter) {
        when (selectedFilter) {
            "user" -> allDatabases.filter { !it.isSystemDb }
            "system" -> allDatabases.filter { it.isSystemDb }
            else -> allDatabases
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
            ExpressiveFloatingToolbar(
                actions = listOf(
                    ToolbarAction(
                        id = "refresh",
                        icon = Icons.Default.Refresh,
                        contentDescription = "Refresh Overview",
                        onClick = { viewModel.loadOverview() }
                    ),
                    ToolbarAction(
                        id = "console",
                        icon = Icons.Default.Terminal,
                        contentDescription = "Open Console",
                        tint = EmeraldLight,
                        onClick = onNavigateToConsole
                    ),
                    ToolbarAction(
                        id = "settings",
                        icon = Icons.Default.Settings,
                        contentDescription = "Settings",
                        onClick = onNavigateToSettings
                    )
                ),
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = { showCreateDialog = true },
                        containerColor = EmeraldPrimary,
                        contentColor = TextOnPrimary,
                        shape = PillShape,
                        modifier = Modifier.height(44.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("New DB", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            )
        },
        containerColor = BackgroundDark
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Loading Wavy Progress
            if (uiState.isLoading && overview == null) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularWavySpinner(sizeDp = 48.dp, color = EmeraldLight)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Querying MongoDB wire protocol...",
                            style = MaterialTheme.typography.labelSmall,
                            color = EmeraldLight
                        )
                    }
                }
            }

            // Error banner
            if (uiState.errorMessage != null) {
                item {
                    Surface(
                        color = RoseContainer.copy(alpha = 0.35f),
                        shape = SquircleMedium,
                        border = androidx.compose.foundation.BorderStroke(1.dp, RoseAccent.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = RoseAccent)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = uiState.errorMessage!!,
                                color = RoseAccent,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { viewModel.clearError() }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = RoseAccent, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            // Metric Cards Grid
            item {
                StaggerEntrance(index = 0) {
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
                            value = "${allDatabases.size}",
                            icon = Icons.Default.Folder,
                            modifier = Modifier.weight(1f),
                            accentColor = SkyAccent
                        )
                    }
                }
            }

            item {
                val totalCollections = allDatabases.sumOf { it.collectionsCount }
                val uptimeStr = FormatUtils.formatUptime(overview?.serverStatus?.uptime)
                StaggerEntrance(index = 1) {
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
            }

            // Database Filter Connected Button Group
            item {
                val userCount = allDatabases.count { !it.isSystemDb }
                val sysCount = allDatabases.count { it.isSystemDb }

                StaggerEntrance(index = 2) {
                    ConnectedButtonGroup(
                        items = listOf(
                            ButtonGroupItem(id = "all", label = "All", count = allDatabases.size),
                            ButtonGroupItem(id = "user", label = "User DBs", count = userCount),
                            ButtonGroupItem(id = "system", label = "System", count = sysCount)
                        ),
                        selectedId = selectedFilter,
                        onItemSelected = { selectedFilter = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Database Items
            itemsIndexed(filteredDatabases, key = { _, db -> db.name }) { idx, db ->
                StaggerEntrance(index = idx + 3) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(AsymmetricCardShape)
                            .background(SurfaceContainer)
                            .border(1.dp, CardBorderDark, AsymmetricCardShape)
                            .pressMorph(onClick = { onNavigateToDatabase(db.name) })
                            .padding(18.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (db.isSystemDb) SurfaceContainerHighest
                                                else EmeraldContainer
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.FolderOpen,
                                            contentDescription = null,
                                            tint = if (db.isSystemDb) TextMuted else EmeraldLight,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = db.name,
                                            style = MaterialTheme.typography.titleMediumEmphasized,
                                            color = TextPrimary
                                        )
                                        if (db.isSystemDb) {
                                            Text(
                                                text = "System Catalog",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = TextMuted,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    if (!db.isSystemDb) {
                                        IconButton(
                                            onClick = { dbToDrop = db },
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(CircleShape)
                                                .background(SurfaceContainerHigh)
                                                .pressMorph()
                                        ) {
                                            Icon(
                                                Icons.Default.DeleteOutline,
                                                contentDescription = "Drop DB",
                                                tint = RoseAccent,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(SurfaceContainerHigh),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = "Open",
                                            tint = EmeraldLight,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ExpressiveTag(
                                    text = "${db.collectionsCount} collections",
                                    icon = Icons.Default.Layers,
                                    containerColor = SurfaceContainerHigh,
                                    contentColor = TextSecondary
                                )

                                ExpressiveTag(
                                    text = "${db.objectsCount} docs",
                                    icon = Icons.Default.Description,
                                    containerColor = SurfaceContainerHigh,
                                    contentColor = TextSecondary
                                )

                                Spacer(modifier = Modifier.weight(1f))

                                Text(
                                    text = FormatUtils.formatBytes(db.sizeOnDisk),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldLight
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }

    // Drop DB Confirmation
    dbToDrop?.let { db ->
        ConfirmDialog(
            title = "Drop Database '${db.name}'?",
            message = "Permanently drop database '${db.name}' and all containing collections? This action cannot be reversed.",
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
            containerColor = SurfaceContainer,
            shape = SquircleLarge,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(EmeraldContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = EmeraldLight, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Create New Database", fontWeight = FontWeight.Bold, color = TextPrimary)
                }
            },
            text = {
                Column {
                    Text("DATABASE NAME", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = newDbName,
                        onValueChange = { newDbName = it },
                        singleLine = true,
                        placeholder = { Text("e.g. analytics_db", color = TextMuted) },
                        shape = SquircleSmall,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SurfaceContainerHigh,
                            unfocusedContainerColor = SurfaceContainerHigh,
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = CardBorderDark
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("INITIAL COLLECTION NAME", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = newColName,
                        onValueChange = { newColName = it },
                        singleLine = true,
                        placeholder = { Text("e.g. events", color = TextMuted) },
                        shape = SquircleSmall,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SurfaceContainerHigh,
                            unfocusedContainerColor = SurfaceContainerHigh,
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
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = TextOnPrimary),
                    shape = PillShape
                ) {
                    Text("Create", fontWeight = FontWeight.Bold)
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

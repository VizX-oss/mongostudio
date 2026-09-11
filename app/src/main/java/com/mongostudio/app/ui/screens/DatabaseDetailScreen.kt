package com.mongostudio.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mongostudio.app.data.model.CollectionInfo
import com.mongostudio.app.data.model.FormatUtils
import com.mongostudio.app.ui.components.*
import com.mongostudio.app.ui.theme.*
import com.mongostudio.app.viewmodel.MongoStudioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatabaseDetailScreen(
    dbName: String,
    viewModel: MongoStudioViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToDocuments: (String, String) -> Unit,
    onNavigateToAggregation: (String, String) -> Unit,
    onNavigateToIndexes: (String, String) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val uiState by viewModel.uiState.collectAsState()
    val themeSettings by viewModel.themePreferences.themeSettings.collectAsState()

    var showAddCollectionDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var newColName by remember { mutableStateOf("") }
    var colToDrop by remember { mutableStateOf<CollectionInfo?>(null) }

    LaunchedEffect(dbName) {
        viewModel.selectDatabase(dbName)
    }

    val dbInfo = uiState.overview?.databases?.find { it.name == dbName }
    val collections = uiState.collections

    Scaffold(
        topBar = {
            TopHeader(
                title = dbName,
                subtitle = "Database Explorer",
                isConnectedToCluster = true,
                pingMs = uiState.activeClusterPingMs,
                onBackClick = onNavigateBack,
                onThemeClick = { showThemeDialog = true },
                onRefreshClick = { viewModel.loadCollections(dbName) }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    haptic.performClickFeedback()
                    showAddCollectionDialog = true
                },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("New Collection", fontWeight = FontWeight.Bold) },
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
            // Database Header Summary Card
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Database Summary",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            if (dbInfo?.isSystemDb == true) {
                                SuggestionChip(
                                    onClick = {},
                                    label = { Text("System DB") },
                                    icon = { Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(14.dp)) },
                                    shape = CircleShape
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AssistChip(
                                onClick = {},
                                label = { Text("${collections.size} Collections") },
                                leadingIcon = { Icon(Icons.Default.Layers, contentDescription = null, modifier = Modifier.size(14.dp)) },
                                shape = CircleShape
                            )
                            AssistChip(
                                onClick = {},
                                label = { Text(FormatUtils.formatBytes(dbInfo?.sizeOnDisk ?: 0L)) },
                                leadingIcon = { Icon(Icons.Default.Storage, contentDescription = null, modifier = Modifier.size(14.dp)) },
                                shape = CircleShape
                            )
                        }
                    }
                }
            }

            // Collections Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Collections (${collections.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Loading state with M3 Expressive Squiggly Spinner
            if (uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularWavySpinner(
                            sizeDp = 44.dp,
                            strokeWidth = 3.5.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Empty state
            if (!uiState.isLoading && collections.isEmpty()) {
                item {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        )
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(36.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.LayersClear,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(44.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "No collections in database",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Tap 'New Collection' to create one.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            } else {
                items(collections, key = { it.name }) { col ->
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                haptic.performClickFeedback()
                                onNavigateToDocuments(dbName, col.name)
                            },
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.secondaryContainer,
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Description,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Text(
                                            text = col.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${col.docCount} documents • ${FormatUtils.formatBytes(col.size)}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = {
                                        haptic.performClickFeedback()
                                        colToDrop = col
                                    },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f))
                                ) {
                                    Icon(
                                        Icons.Default.DeleteOutline,
                                        contentDescription = "Drop collection",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            // Enhanced Collection Actions Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilledTonalButton(
                                    onClick = {
                                        haptic.performClickFeedback()
                                        onNavigateToDocuments(dbName, col.name)
                                    },
                                    shape = PillShape,
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    ),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                    modifier = Modifier.weight(1.2f)
                                ) {
                                    Icon(Icons.Default.FindInPage, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Documents", fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = {
                                        haptic.performClickFeedback()
                                        onNavigateToIndexes(dbName, col.name)
                                    },
                                    shape = PillShape,
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.FormatListNumbered, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Indexes", fontWeight = FontWeight.Medium)
                                }

                                OutlinedButton(
                                    onClick = {
                                        haptic.performClickFeedback()
                                        onNavigateToAggregation(dbName, col.name)
                                    },
                                    shape = PillShape,
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Hub, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Pipeline", fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }
            }

            // Bottom space for FAB
            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }

    // Appearance & Theme Settings Dialog
    if (showThemeDialog) {
        ThemeSettingsDialog(
            settings = themeSettings,
            onFollowSystemThemeChange = { viewModel.setFollowSystemTheme(it) },
            onDarkModeChange = { viewModel.setDarkMode(it) },
            onAmoledModeChange = { viewModel.setAmoledMode(it) },
            onDismiss = { showThemeDialog = false }
        )
    }

    // Drop Collection Confirmation Dialog
    if (colToDrop != null) {
        val target = colToDrop!!
        ConfirmDialog(
            title = "Drop Collection",
            message = "Are you sure you want to drop '${target.name}' from database '$dbName'? All documents will be deleted.",
            confirmText = "Drop Collection",
            dismissText = "Cancel",
            isDestructive = true,
            onConfirm = {
                viewModel.dropCollection(dbName, target.name)
                colToDrop = null
            },
            onDismiss = { colToDrop = null }
        )
    }

    // Add Collection Dialog
    if (showAddCollectionDialog) {
        AlertDialog(
            onDismissRequest = { showAddCollectionDialog = false },
            title = { Text("New Collection") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Create a new collection in database '$dbName'",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = newColName,
                        onValueChange = { newColName = it },
                        label = { Text("Collection Name") },
                        placeholder = { Text("e.g. orders, logs") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newColName.isNotBlank()) {
                            viewModel.createCollection(dbName, newColName.trim())
                            newColName = ""
                            showAddCollectionDialog = false
                        }
                    },
                    enabled = newColName.isNotBlank(),
                    shape = CircleShape
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCollectionDialog = false }) {
                    Text("Cancel")
                }
            },
            shape = MaterialTheme.shapes.extraLarge
        )
    }
}

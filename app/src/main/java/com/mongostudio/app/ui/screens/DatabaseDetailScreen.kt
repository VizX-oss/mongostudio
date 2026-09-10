package com.mongostudio.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mongostudio.app.data.model.CollectionInfo
import com.mongostudio.app.data.model.FormatUtils
import com.mongostudio.app.ui.components.*
import com.mongostudio.app.ui.theme.*
import com.mongostudio.app.viewmodel.MongoStudioViewModel

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
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
    var showAddCollectionDialog by remember { mutableStateOf(false) }
    var newColName by remember { mutableStateOf("") }
    var colToDrop by remember { mutableStateOf<CollectionInfo?>(null) }

    LaunchedEffect(dbName) {
        viewModel.selectDatabase(dbName)
    }

    val dbInfo = uiState.overview?.databases?.find { it.name == dbName }

    Scaffold(
        topBar = {
            TopHeader(
                title = dbName,
                subtitle = "Database Explorer",
                isConnectedToCluster = true,
                pingMs = uiState.activeClusterPingMs,
                onBackClick = onNavigateBack,
                onRefreshClick = { viewModel.loadCollections(dbName) }
            )
        },
        bottomBar = {
            ExpressiveFloatingToolbar(
                actions = listOf(
                    ToolbarAction(
                        id = "refresh",
                        icon = Icons.Default.Refresh,
                        contentDescription = "Refresh Collections",
                        onClick = { viewModel.loadCollections(dbName) }
                    )
                ),
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = { showAddCollectionDialog = true },
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
                            Text("New Collection", fontWeight = FontWeight.Bold, fontSize = 13.sp)
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
            if (uiState.isLoading && uiState.collections.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularWavySpinner(sizeDp = 44.dp, color = EmeraldLight)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Reading collections catalog...", style = MaterialTheme.typography.labelSmall, color = EmeraldLight)
                    }
                }
            }

            // DB Overview Summary Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(AsymmetricCardShape)
                        .background(SurfaceContainer)
                        .border(1.dp, CardBorderDark, AsymmetricCardShape)
                        .padding(20.dp)
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
                                        .background(EmeraldContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Storage, contentDescription = null, tint = EmeraldVibrant, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = dbName,
                                        style = MaterialTheme.typography.titleLargeEmphasized,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Database Catalog",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary
                                    )
                                }
                            }

                            ExpressiveTag(
                                text = FormatUtils.formatBytes(dbInfo?.sizeOnDisk ?: 0L),
                                icon = Icons.Default.CloudQueue,
                                containerColor = SurfaceContainerHigh,
                                contentColor = EmeraldLight
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("COLLECTIONS", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                Text("${uiState.collections.size}", style = MaterialTheme.typography.titleMediumEmphasized, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                            Column {
                                Text("TOTAL DOCS", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                Text("${dbInfo?.objectsCount ?: 0}", style = MaterialTheme.typography.titleMediumEmphasized, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                            Column {
                                Text("STORAGE", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                Text(FormatUtils.formatBytes(dbInfo?.storageSize ?: 0L), style = MaterialTheme.typography.titleMediumEmphasized, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                            Column {
                                Text("INDEXES", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                Text(FormatUtils.formatBytes(dbInfo?.indexSize ?: 0L), style = MaterialTheme.typography.titleMediumEmphasized, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                        }
                    }
                }
            }

            // Collections Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Collections (${uiState.collections.size})",
                        style = MaterialTheme.typography.titleMediumEmphasized,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }

            if (uiState.collections.isEmpty() && !uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(SquircleMedium)
                            .background(SurfaceContainer)
                            .border(1.dp, CardBorderDark, SquircleMedium)
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.DatasetLinked, contentDescription = null, tint = TextMuted, modifier = Modifier.size(44.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("No collections in this database", style = MaterialTheme.typography.titleMedium, color = TextSecondary)
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    haptic.performClickFeedback()
                                    showAddCollectionDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = TextOnPrimary),
                                shape = PillShape,
                                modifier = Modifier.pressMorph()
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Create First Collection", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                itemsIndexed(uiState.collections) { idx, col ->
                    StaggerEntrance(index = idx) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(SquircleMedium)
                                .background(SurfaceContainer)
                                .border(1.dp, CardBorderDark, SquircleMedium)
                                .pressMorph(onClick = {
                                    haptic.performClickFeedback()
                                    onNavigateToDocuments(dbName, col.name)
                                })
                                .padding(16.dp)
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
                                                .size(34.dp)
                                                .clip(CircleShape)
                                                .background(SurfaceContainerHigh),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Default.TableChart,
                                                contentDescription = null,
                                                tint = EmeraldLight,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = col.name,
                                                style = MaterialTheme.typography.titleMediumEmphasized,
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary
                                            )
                                            Text(
                                                text = "${col.docCount} docs • ${FormatUtils.formatBytes(col.storageSize)}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = TextSecondary
                                            )
                                        }
                                    }

                                    IconButton(
                                        onClick = {
                                            haptic.performConfirmFeedback()
                                            colToDrop = col
                                        },
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(SurfaceContainerHigh)
                                    ) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Drop Collection", tint = RoseAccent, modifier = Modifier.size(16.dp))
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Playful Action Pills Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            haptic.performClickFeedback()
                                            onNavigateToDocuments(dbName, col.name)
                                        },
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = EmeraldContainer,
                                            contentColor = EmeraldLight
                                        ),
                                        shape = PillShape
                                    ) {
                                        Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Browse", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            haptic.performClickFeedback()
                                            onNavigateToAggregation(dbName, col.name)
                                        },
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        modifier = Modifier.weight(1f),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderDark),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AmberAccent),
                                        shape = PillShape
                                    ) {
                                        Icon(Icons.Default.Functions, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Pipeline", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            haptic.performClickFeedback()
                                            onNavigateToIndexes(dbName, col.name)
                                        },
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        modifier = Modifier.weight(1f),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderDark),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SkyAccent),
                                        shape = PillShape
                                    ) {
                                        Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Indexes", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    }
                                }
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

    // Drop Collection Dialog
    colToDrop?.let { col ->
        ConfirmDialog(
            title = "Drop Collection '${col.name}'?",
            message = "All documents in collection '${col.name}' will be permanently deleted.",
            confirmText = "Drop Collection",
            isDestructive = true,
            onConfirm = {
                viewModel.dropCollection(dbName, col.name)
                colToDrop = null
            },
            onDismiss = { colToDrop = null }
        )
    }

    // Add Collection Dialog
    if (showAddCollectionDialog) {
        AlertDialog(
            onDismissRequest = { showAddCollectionDialog = false },
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
                    Text("Add New Collection", fontWeight = FontWeight.Bold, color = TextPrimary)
                }
            },
            text = {
                Column {
                    Text("COLLECTION NAME", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = newColName,
                        onValueChange = { newColName = it },
                        singleLine = true,
                        placeholder = { Text("e.g. customers, events, logs", color = TextMuted) },
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
                        if (newColName.isNotBlank()) {
                            viewModel.createCollection(dbName, newColName.trim())
                            showAddCollectionDialog = false
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
                TextButton(onClick = { showAddCollectionDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

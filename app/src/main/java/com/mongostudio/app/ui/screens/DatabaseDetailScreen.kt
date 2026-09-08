package com.mongostudio.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
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
import com.mongostudio.app.data.model.CollectionInfo
import com.mongostudio.app.data.model.FormatUtils
import com.mongostudio.app.ui.components.ConfirmDialog
import com.mongostudio.app.ui.components.TopHeader
import com.mongostudio.app.ui.theme.*
import com.mongostudio.app.viewmodel.MongoStudioViewModel

@Composable
fun DatabaseDetailScreen(
    dbName: String,
    viewModel: MongoStudioViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToDocuments: (String, String) -> Unit,
    onNavigateToAggregation: (String, String) -> Unit,
    onNavigateToIndexes: (String, String) -> Unit
) {
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddCollectionDialog = true },
                containerColor = EmeraldPrimary,
                contentColor = TextOnPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Collection")
            }
        },
        containerColor = BackgroundDark
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // DB Overview Stats Banner
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.medium)
                        .background(SurfaceDark)
                        .border(1.dp, CardBorderDark, MaterialTheme.shapes.medium)
                        .padding(16.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Database Summary",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Surface(
                                color = EmeraldContainer,
                                shape = MaterialTheme.shapes.extraSmall
                            ) {
                                Text(
                                    text = FormatUtils.formatBytes(dbInfo?.sizeOnDisk ?: 0L),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = EmeraldLight,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Collections", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                Text("${uiState.collections.size}", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                            }
                            Column {
                                Text("Total Docs", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                Text("${dbInfo?.objectsCount ?: 0}", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                            }
                            Column {
                                Text("Storage Size", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                Text(FormatUtils.formatBytes(dbInfo?.storageSize ?: 0L), style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                            }
                            Column {
                                Text("Index Size", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                Text(FormatUtils.formatBytes(dbInfo?.indexSize ?: 0L), style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                            }
                        }
                    }
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
                        text = "Collections (${uiState.collections.size})",
                        style = MaterialTheme.typography.titleLarge,
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
                            .clip(MaterialTheme.shapes.medium)
                            .background(SurfaceDark)
                            .border(1.dp, CardBorderDark, MaterialTheme.shapes.medium)
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.DatasetLinked, contentDescription = null, tint = TextMuted, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No collections in this database", color = TextSecondary)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { showAddCollectionDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = TextOnPrimary)
                            ) {
                                Text("Create Collection")
                            }
                        }
                    }
                }
            } else {
                items(uiState.collections) { col ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToDocuments(dbName, col.name) },
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
                                        Icons.Default.TableChart,
                                        contentDescription = null,
                                        tint = EmeraldLight,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = col.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                }
                                IconButton(
                                    onClick = { colToDrop = col },
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Drop Collection", tint = RoseAccent, modifier = Modifier.size(18.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${col.docCount} documents",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                                Text(
                                    text = FormatUtils.formatBytes(col.storageSize),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = EmeraldLight
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Action Chips
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { onNavigateToDocuments(dbName, col.name) },
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                    modifier = Modifier.weight(1f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.5f))
                                ) {
                                    Icon(Icons.Default.Visibility, contentDescription = null, tint = EmeraldLight, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Browse", fontSize = 12.sp, color = EmeraldLight)
                                }

                                OutlinedButton(
                                    onClick = { onNavigateToAggregation(dbName, col.name) },
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                    modifier = Modifier.weight(1f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderDark)
                                ) {
                                    Icon(Icons.Default.Functions, contentDescription = null, tint = AmberAccent, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Aggregate", fontSize = 12.sp, color = TextPrimary)
                                }

                                OutlinedButton(
                                    onClick = { onNavigateToIndexes(dbName, col.name) },
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                    modifier = Modifier.weight(1f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderDark)
                                ) {
                                    Icon(Icons.Default.Key, contentDescription = null, tint = SkyAccent, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Indexes", fontSize = 12.sp, color = TextPrimary)
                                }
                            }
                        }
                    }
                }
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
            containerColor = SurfaceDark,
            title = { Text("Add New Collection", color = TextPrimary) },
            text = {
                Column {
                    Text("Collection Name", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = newColName,
                        onValueChange = { newColName = it },
                        singleLine = true,
                        placeholder = { Text("e.g. orders, logs, products", color = TextMuted) },
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
                        if (newColName.isNotBlank()) {
                            viewModel.createCollection(dbName, newColName.trim())
                            showAddCollectionDialog = false
                            newColName = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = TextOnPrimary)
                ) {
                    Text("Create")
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

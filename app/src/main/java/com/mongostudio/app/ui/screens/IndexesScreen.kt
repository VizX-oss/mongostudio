package com.mongostudio.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.gson.Gson
import com.mongostudio.app.data.model.IndexInfo
import com.mongostudio.app.ui.components.*
import com.mongostudio.app.ui.theme.*
import com.mongostudio.app.viewmodel.MongoStudioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IndexesScreen(
    dbName: String,
    colName: String,
    viewModel: MongoStudioViewModel,
    onNavigateBack: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val themeSettings by viewModel.themeSettings.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    var showCreateIndexDialog by remember { mutableStateOf(false) }
    var fieldName by remember { mutableStateOf("") }
    var isDescending by remember { mutableStateOf(false) }
    var isUnique by remember { mutableStateOf(false) }
    var indexToDrop by remember { mutableStateOf<IndexInfo?>(null) }
    val gson = remember { Gson() }

    LaunchedEffect(dbName, colName) {
        viewModel.loadIndexes(dbName, colName)
    }

    val indexes = uiState.indexes

    Scaffold(
        topBar = {
            TopHeader(
                title = "Indexes",
                subtitle = "$dbName • $colName",
                isConnectedToCluster = true,
                pingMs = uiState.activeClusterPingMs,
                onBackClick = onNavigateBack,
                onRefreshClick = {
                    haptic.performClickFeedback()
                    viewModel.loadIndexes(dbName, colName)
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    haptic.performClickFeedback()
                    showCreateIndexDialog = true
                },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Create Index", fontWeight = FontWeight.Bold) },
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Collection Indexes ($colName)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Indexes enhance query performance on frequently queried fields. Total active: ${indexes.size}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if (uiState.isLoading) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                            CircularWavySpinner(
                                sizeDp = 44.dp,
                                strokeWidth = 3.5.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                if (!uiState.isLoading && indexes.isEmpty()) {
                    item {
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            )
                        ) {
                            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                Text("No indexes found", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                } else {
                    itemsIndexed(indexes, key = { _, it -> it.name ?: "" }) { index, idx ->
                        val isDefaultIdIndex = idx.name == "_id_"
                        StaggerEntrance(index = index) {
                            ElevatedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateItem(),
                                shape = MaterialTheme.shapes.large,
                                colors = CardDefaults.elevatedCardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                )
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Default.Key,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = idx.name ?: "Unnamed Index",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }

                                        if (!isDefaultIdIndex) {
                                            IconButton(
                                                onClick = {
                                                    haptic.performClickFeedback()
                                                    indexToDrop = idx
                                                },
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f))
                                            ) {
                                                Icon(
                                                    Icons.Default.DeleteOutline,
                                                    contentDescription = "Drop index",
                                                    tint = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }

                                    // Key Pattern
                                    Text(
                                        text = "Key Pattern: ${gson.toJson(idx.key ?: emptyMap<String, Any>())}",
                                        style = MonospaceCodeStyle.copy(fontSize = 12.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    // Badges Row
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        if (idx.unique == true) {
                                            SuggestionChip(
                                                onClick = {},
                                                label = { Text("Unique") },
                                                shape = CircleShape
                                            )
                                        }
                                        if (isDefaultIdIndex) {
                                            SuggestionChip(
                                                onClick = {},
                                                label = { Text("Primary Key") },
                                                shape = CircleShape
                                            )
                                        }
                                        idx.v?.let { v ->
                                            SuggestionChip(
                                                onClick = {},
                                                label = { Text("v$v") },
                                                shape = CircleShape
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(72.dp))
                }
            }

            VelocityAwareScrollbar(
                listState = listState,
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }
    }

    // Drop Index Confirm Dialog with Hold to Drop
    if (indexToDrop != null) {
        val target = indexToDrop!!
        ConfirmDialog(
            title = "Drop Index: ${target.name}",
            message = "Are you sure you want to drop index '${target.name}' from collection '$colName'?",
            confirmText = "Hold to Drop",
            dismissText = "Cancel",
            isDestructive = true,
            requireHoldToConfirm = true,
            onConfirm = {
                target.name?.let { viewModel.dropIndex(dbName, colName, it) }
                indexToDrop = null
            },
            onDismiss = { indexToDrop = null }
        )
    }

    // Create Index Dialog
    if (showCreateIndexDialog) {
        AlertDialog(
            onDismissRequest = { showCreateIndexDialog = false },
            title = { Text("Create New Index") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = fieldName,
                        onValueChange = { fieldName = it },
                        label = { Text("Field Name (e.g. email, status)") },
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(checked = isDescending, onCheckedChange = { isDescending = it })
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Descending Order (-1)")
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(checked = isUnique, onCheckedChange = { isUnique = it })
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Unique Constraint")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (fieldName.isNotBlank()) {
                            val dir = if (isDescending) -1 else 1
                            val keysJson = """{"${fieldName.trim()}": $dir}"""
                            viewModel.createIndex(dbName, colName, keysJson, isUnique)
                            showCreateIndexDialog = false
                            fieldName = ""
                            isDescending = false
                            isUnique = false
                        }
                    },
                    shape = CircleShape
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateIndexDialog = false }) {
                    Text("Cancel")
                }
            },
            shape = MaterialTheme.shapes.extraLarge
        )
    }
}

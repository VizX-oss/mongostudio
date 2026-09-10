package com.mongostudio.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.google.gson.Gson
import com.mongostudio.app.data.model.IndexInfo
import com.mongostudio.app.ui.components.*
import com.mongostudio.app.ui.theme.*
import com.mongostudio.app.viewmodel.MongoStudioViewModel

@Composable
fun IndexesScreen(
    dbName: String,
    colName: String,
    viewModel: MongoStudioViewModel,
    onNavigateBack: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val uiState by viewModel.uiState.collectAsState()
    var showCreateIndexDialog by remember { mutableStateOf(false) }
    var fieldName by remember { mutableStateOf("") }
    var isDescending by remember { mutableStateOf(false) }
    var isUnique by remember { mutableStateOf(false) }
    var indexToDrop by remember { mutableStateOf<IndexInfo?>(null) }
    val gson = remember { Gson() }

    LaunchedEffect(dbName, colName) {
        viewModel.loadIndexes(dbName, colName)
    }

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
        bottomBar = {
            ExpressiveFloatingToolbar(
                actions = listOf(
                    ToolbarAction(
                        id = "refresh",
                        icon = Icons.Default.Refresh,
                        contentDescription = "Refresh Indexes",
                        onClick = {
                            haptic.performClickFeedback()
                            viewModel.loadIndexes(dbName, colName)
                        }
                    )
                ),
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = {
                            haptic.performClickFeedback()
                            showCreateIndexDialog = true
                        },
                        containerColor = EmeraldPrimary,
                        contentColor = TextOnPrimary,
                        shape = PillShape,
                        modifier = Modifier
                            .height(44.dp)
                            .pressMorph()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("New Index", fontWeight = FontWeight.Bold, fontSize = 13.sp)
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Active Indexes (${uiState.indexes.size})",
                        style = MaterialTheme.typography.titleMediumEmphasized,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    ExpressiveTag(
                        text = "B-Tree",
                        icon = Icons.Default.Speed,
                        containerColor = SurfaceContainerHigh,
                        contentColor = SkyAccent
                    )
                }
            }

            itemsIndexed(uiState.indexes) { i, idx ->
                val isDefaultIdIndex = idx.name == "_id_"

                StaggerEntrance(index = i) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(SquircleMedium)
                            .background(SurfaceContainer)
                            .border(1.dp, CardBorderDark, SquircleMedium)
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
                                            .background(SkyAccent.copy(alpha = 0.16f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Key, contentDescription = null, tint = SkyAccent, modifier = Modifier.size(18.dp))
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = idx.name,
                                        style = MaterialTheme.typography.titleMediumEmphasized,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                }

                                if (!isDefaultIdIndex) {
                                    IconButton(
                                        onClick = {
                                            haptic.performConfirmFeedback()
                                            indexToDrop = idx
                                        },
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(SurfaceContainerHigh)
                                    ) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Drop Index", tint = RoseAccent, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Keys: ${gson.toJson(idx.key ?: emptyMap<String, Any>())}",
                            style = MonospaceCodeStyle.copy(fontSize = 12.sp),
                            color = SkyAccent
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isDefaultIdIndex) {
                                ExpressiveTag(
                                    text = "PRIMARY KEY",
                                    containerColor = SurfaceContainerHigh,
                                    contentColor = TextSecondary
                                )
                            }
                            if (idx.unique == true) {
                                ExpressiveTag(
                                    text = "UNIQUE",
                                    containerColor = EmeraldContainer,
                                    contentColor = EmeraldLight
                                )
                            }
                            idx.v?.let { v ->
                                ExpressiveTag(
                                    text = "v$v",
                                    containerColor = SurfaceContainerHighest,
                                    contentColor = TextMuted
                                )
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

    // Create Index Dialog
    if (showCreateIndexDialog) {
        AlertDialog(
            onDismissRequest = { showCreateIndexDialog = false },
            containerColor = SurfaceContainer,
            shape = SquircleLarge,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SkyAccent.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = SkyAccent, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Create New Index", fontWeight = FontWeight.Bold, color = TextPrimary)
                }
            },
            text = {
                Column {
                    Text("FIELD NAME", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = fieldName,
                        onValueChange = { fieldName = it },
                        singleLine = true,
                        placeholder = { Text("e.g. email or username", color = TextMuted) },
                        shape = SquircleSmall,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SurfaceContainerHigh,
                            unfocusedContainerColor = SurfaceContainerHigh,
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = CardBorderDark
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Descending (-1)", style = MaterialTheme.typography.bodyMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                            Text("Sort order for indexed key", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        }
                        Switch(
                            checked = isDescending,
                            onCheckedChange = { isDescending = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = TextOnPrimary, checkedTrackColor = EmeraldPrimary)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Unique Constraint", style = MaterialTheme.typography.bodyMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                            Text("Enforce distinct values", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        }
                        Switch(
                            checked = isUnique,
                            onCheckedChange = { isUnique = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = TextOnPrimary, checkedTrackColor = EmeraldPrimary)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (fieldName.isNotBlank()) {
                            val dir = if (isDescending) -1 else 1
                            val keysJson = """{"${fieldName.trim()}": $dir}"""
                            viewModel.createIndex(keysJson, isUnique)
                            showCreateIndexDialog = false
                            fieldName = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = TextOnPrimary),
                    shape = PillShape
                ) {
                    Text("Create Index", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateIndexDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    // Drop Index Confirmation
    indexToDrop?.let { idx ->
        ConfirmDialog(
            title = "Drop Index '${idx.name}'?",
            message = "Remove index '${idx.name}' from collection '$colName'? Query speeds relying on this field will be impacted.",
            confirmText = "Drop Index",
            isDestructive = true,
            onConfirm = {
                viewModel.dropIndex(idx.name)
                indexToDrop = null
            },
            onDismiss = { indexToDrop = null }
        )
    }
}

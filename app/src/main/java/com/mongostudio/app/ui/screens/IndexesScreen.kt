package com.mongostudio.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.google.gson.Gson
import com.mongostudio.app.data.model.IndexInfo
import com.mongostudio.app.ui.components.ConfirmDialog
import com.mongostudio.app.ui.components.TopHeader
import com.mongostudio.app.ui.theme.*
import com.mongostudio.app.viewmodel.MongoStudioViewModel

@Composable
fun IndexesScreen(
    dbName: String,
    colName: String,
    viewModel: MongoStudioViewModel,
    onNavigateBack: () -> Unit
) {
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
                isServerReachable = uiState.isServerReachable,
                serverPingMs = uiState.serverPingMs,
                isConnectedToCluster = true,
                onBackClick = onNavigateBack,
                onRefreshClick = { viewModel.loadIndexes(dbName, colName) }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreateIndexDialog = true },
                containerColor = EmeraldPrimary,
                contentColor = TextOnPrimary,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Create Index", fontWeight = FontWeight.Bold) }
            )
        },
        containerColor = BackgroundDark
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
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
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }

            items(uiState.indexes) { idx ->
                val isDefaultIdIndex = idx.name == "_id_"

                Card(
                    modifier = Modifier.fillMaxWidth(),
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
                                Icon(Icons.Default.Key, contentDescription = null, tint = EmeraldLight, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = idx.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }

                            if (!isDefaultIdIndex) {
                                IconButton(
                                    onClick = { indexToDrop = idx },
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Drop Index", tint = RoseAccent, modifier = Modifier.size(18.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Keys: ${gson.toJson(idx.key ?: emptyMap<String, Any>())}",
                            style = MonospaceCodeStyle.copy(fontSize = 12.sp),
                            color = SkyAccent
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (isDefaultIdIndex) {
                                Surface(color = CardBorderDark, shape = MaterialTheme.shapes.extraSmall) {
                                    Text("DEFAULT PRIMARY KEY", color = TextSecondary, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }
                            if (idx.unique == true) {
                                Surface(color = EmeraldContainer, shape = MaterialTheme.shapes.extraSmall) {
                                    Text("UNIQUE", color = EmeraldLight, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }
                            idx.v?.let { v ->
                                Surface(color = CardDark, shape = MaterialTheme.shapes.extraSmall) {
                                    Text("VERSION $v", color = TextMuted, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Create Index Dialog
    if (showCreateIndexDialog) {
        AlertDialog(
            onDismissRequest = { showCreateIndexDialog = false },
            containerColor = SurfaceDark,
            title = { Text("Create New Index", color = TextPrimary) },
            text = {
                Column {
                    Text("Field Name", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = fieldName,
                        onValueChange = { fieldName = it },
                        singleLine = true,
                        placeholder = { Text("e.g. email or username", color = TextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = CardDark,
                            unfocusedContainerColor = CardDark,
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = CardBorderDark
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Sort Direction (Descending -1)", color = TextPrimary)
                        Switch(
                            checked = isDescending,
                            onCheckedChange = { isDescending = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = TextOnPrimary, checkedTrackColor = EmeraldPrimary)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Unique Constraint", color = TextPrimary)
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
                            val optionsJson = if (isUnique) """{"unique": true}""" else null
                            viewModel.createIndex(keysJson, optionsJson)
                            showCreateIndexDialog = false
                            fieldName = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = TextOnPrimary)
                ) {
                    Text("Create")
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
            message = "This will remove the index '${idx.name}' from collection '$colName'. Query performance on this field may degrade.",
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

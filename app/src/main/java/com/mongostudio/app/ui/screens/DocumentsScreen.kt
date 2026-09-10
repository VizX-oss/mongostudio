package com.mongostudio.app.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.GsonBuilder
import com.mongostudio.app.ui.components.*
import com.mongostudio.app.ui.theme.*
import com.mongostudio.app.viewmodel.MongoStudioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentsScreen(
    dbName: String,
    colName: String,
    viewModel: MongoStudioViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val uiState by viewModel.uiState.collectAsState()
    val prettyGson = remember { GsonBuilder().setPrettyPrinting().serializeNulls().create() }

    var isFilterExpanded by remember { mutableStateOf(false) }
    var filterText by remember { mutableStateOf(uiState.filterJson) }
    var sortText by remember { mutableStateOf(uiState.sortJson) }
    var projectionText by remember { mutableStateOf(uiState.projectionJson) }

    var editingDocId by remember { mutableStateOf<String?>(null) }
    var editingDocJson by remember { mutableStateOf<String?>(null) }
    var isAddingDocument by remember { mutableStateOf(false) }
    var docToDeleteId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(dbName, colName) {
        viewModel.selectCollection(dbName, colName)
    }

    val queryResult = uiState.queryResult
    val docs = queryResult?.documents ?: emptyList()

    Scaffold(
        topBar = {
            TopHeader(
                title = colName,
                subtitle = "$dbName • Documents",
                isConnectedToCluster = true,
                pingMs = uiState.activeClusterPingMs,
                onBackClick = onNavigateBack,
                onRefreshClick = { viewModel.runQuery() }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    haptic.performClickFeedback()
                    isAddingDocument = true
                },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Insert Document", fontWeight = FontWeight.Bold) },
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
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Query Filter & Pipeline Card
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.FilterList,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Query & Filter",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                FilterChip(
                                    selected = isFilterExpanded,
                                    onClick = { isFilterExpanded = !isFilterExpanded },
                                    label = { Text(if (isFilterExpanded) "Less Options" else "Sort / Project") },
                                    leadingIcon = {
                                        Icon(
                                            if (isFilterExpanded) Icons.Default.ExpandLess else Icons.Default.Tune,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    },
                                    shape = CircleShape
                                )
                            }
                        }

                        // Filter Presets
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            SuggestionChip(
                                onClick = {
                                    filterText = ""
                                    viewModel.updateQueryParams("", sortText, projectionText)
                                },
                                label = { Text("All { }") },
                                shape = CircleShape
                            )
                            SuggestionChip(
                                onClick = {
                                    filterText = """{"_id": ""}"""
                                },
                                label = { Text("By ID") },
                                shape = CircleShape
                            )
                            SuggestionChip(
                                onClick = {
                                    filterText = """{"status": "active"}"""
                                },
                                label = { Text("Status Active") },
                                shape = CircleShape
                            )
                        }

                        // JSON Filter Input
                        OutlinedTextField(
                            value = filterText,
                            onValueChange = { filterText = it },
                            label = { Text("Filter JSON: { key: value }") },
                            placeholder = { Text("""{"field": "value"}""") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            textStyle = MonospaceCodeStyle.copy(fontSize = 13.sp),
                            shape = MaterialTheme.shapes.medium,
                            trailingIcon = {
                                if (filterText.isNotBlank()) {
                                    IconButton(onClick = {
                                        filterText = ""
                                        viewModel.updateQueryParams("", sortText, projectionText)
                                    }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        )

                        AnimatedVisibility(visible = isFilterExpanded) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedTextField(
                                    value = sortText,
                                    onValueChange = { sortText = it },
                                    label = { Text("Sort JSON (e.g. { \"_id\": -1 })") },
                                    placeholder = { Text("""{"_id": -1}""") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    textStyle = MonospaceCodeStyle.copy(fontSize = 13.sp),
                                    shape = MaterialTheme.shapes.medium
                                )

                                OutlinedTextField(
                                    value = projectionText,
                                    onValueChange = { projectionText = it },
                                    label = { Text("Projection JSON (e.g. { \"name\": 1 })") },
                                    placeholder = { Text("""{"name": 1, "email": 1}""") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    textStyle = MonospaceCodeStyle.copy(fontSize = 13.sp),
                                    shape = MaterialTheme.shapes.medium
                                )
                            }
                        }

                        // Run Query Button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = {
                                    haptic.performClickFeedback()
                                    viewModel.updateQueryParams(filterText, sortText, projectionText)
                                },
                                shape = CircleShape,
                                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
                            ) {
                                Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Execute Query", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Pagination and Stats Bar
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (queryResult != null) {
                            "Found ${queryResult.total} documents • Page ${queryResult.page} of ${queryResult.totalPages}"
                        } else {
                            "Loading documents..."
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        FilledTonalIconButton(
                            onClick = {
                                if ((queryResult?.page ?: 1) > 1) {
                                    viewModel.runQuery(page = (queryResult?.page ?: 1) - 1)
                                }
                            },
                            enabled = (queryResult?.page ?: 1) > 1,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Previous")
                        }

                        FilledTonalIconButton(
                            onClick = {
                                if ((queryResult?.page ?: 1) < (queryResult?.totalPages ?: 1)) {
                                    viewModel.runQuery(page = (queryResult?.page ?: 1) + 1)
                                }
                            },
                            enabled = (queryResult?.page ?: 1) < (queryResult?.totalPages ?: 1),
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "Next")
                        }
                    }
                }
            }

            // Loading indicator
            if (uiState.isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }

            // Empty State
            if (!uiState.isLoading && docs.isEmpty()) {
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
                                    Icons.Default.Description,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "No documents found",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Try adjusting your filter or tap 'Insert Document' to add one.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Documents List Cards
            itemsIndexed(docs) { index, doc ->
                val docId = doc["_id"]?.toString() ?: "doc_$index"
                val jsonString = remember(doc) { prettyGson.toJson(doc) }

                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Document Header Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f, fill = false)
                            ) {
                                SuggestionChip(
                                    onClick = {
                                        val clip = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clip.setPrimaryClip(ClipData.newPlainText("Document ID", docId))
                                        Toast.makeText(context, "Copied _id: $docId", Toast.LENGTH_SHORT).show()
                                    },
                                    label = {
                                        Text(
                                            text = "_id: $docId",
                                            style = MonospaceCodeStyle.copy(fontSize = 11.sp),
                                            maxLines = 1
                                        )
                                    },
                                    icon = {
                                        Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(14.dp))
                                    },
                                    shape = CircleShape
                                )
                            }

                            // Document Card Actions
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                FilledTonalButton(
                                    onClick = {
                                        haptic.performClickFeedback()
                                        editingDocId = docId
                                        editingDocJson = jsonString
                                    },
                                    shape = CircleShape,
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Edit", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                IconButton(
                                    onClick = {
                                        val clip = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clip.setPrimaryClip(ClipData.newPlainText("Document JSON", jsonString))
                                        Toast.makeText(context, "Copied JSON to clipboard", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy JSON", modifier = Modifier.size(18.dp))
                                }

                                IconButton(
                                    onClick = {
                                        haptic.performClickFeedback()
                                        docToDeleteId = docId
                                    },
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Icon(
                                        Icons.Default.DeleteOutline,
                                        contentDescription = "Delete Document",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Document Body (Formatted Syntax Highlighting)
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = JsonSyntaxHighlighter.highlightJson(jsonString),
                                    style = MonospaceCodeStyle.copy(fontSize = 12.sp),
                                    maxLines = 14
                                )
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

    // Interactive Edit Document Modal
    if (editingDocId != null && editingDocJson != null) {
        JsonEditorModal(
            title = "Edit Document: ${editingDocId?.take(16)}...",
            initialJson = editingDocJson!!,
            onDismiss = {
                editingDocId = null
                editingDocJson = null
            },
            onSave = { updatedJson ->
                viewModel.updateDocument(editingDocId!!, updatedJson)
                editingDocId = null
                editingDocJson = null
            },
            confirmButtonText = "Push Changes to MongoDB",
            isLoading = uiState.isLoading
        )
    }

    // Insert New Document Modal
    if (isAddingDocument) {
        val sampleNewDoc = """
        {
          "title": "New Document",
          "status": "active",
          "created_at": "${java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US).format(java.util.Date())}"
        }
        """.trimIndent()

        JsonEditorModal(
            title = "Insert New Document into $colName",
            initialJson = sampleNewDoc,
            onDismiss = { isAddingDocument = false },
            onSave = { newJson ->
                viewModel.insertDocument(newJson)
                isAddingDocument = false
            },
            confirmButtonText = "Insert into MongoDB",
            isLoading = uiState.isLoading
        )
    }

    // Delete Document Confirm Dialog
    if (docToDeleteId != null) {
        ConfirmDialog(
            title = "Delete Document",
            message = "Are you sure you want to permanently delete document '${docToDeleteId}' from '$colName'?",
            confirmText = "Delete",
            dismissText = "Cancel",
            isDestructive = true,
            onConfirm = {
                docToDeleteId?.let { viewModel.deleteDocument(it) }
                docToDeleteId = null
            },
            onDismiss = { docToDeleteId = null }
        )
    }
}

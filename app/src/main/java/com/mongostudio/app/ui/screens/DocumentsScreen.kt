package com.mongostudio.app.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.GsonBuilder
import com.mongostudio.app.ui.components.ConfirmDialog
import com.mongostudio.app.ui.components.JsonEditorModal
import com.mongostudio.app.ui.components.JsonViewerCard
import com.mongostudio.app.ui.components.TopHeader
import com.mongostudio.app.ui.theme.*
import com.mongostudio.app.viewmodel.MongoStudioViewModel

@Composable
fun DocumentsScreen(
    dbName: String,
    colName: String,
    viewModel: MongoStudioViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
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

    Scaffold(
        topBar = {
            TopHeader(
                title = colName,
                subtitle = "$dbName • Documents",
                isServerReachable = uiState.isServerReachable,
                serverPingMs = uiState.serverPingMs,
                isConnectedToCluster = true,
                onBackClick = onNavigateBack,
                onRefreshClick = { viewModel.runQuery() }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { isAddingDocument = true },
                containerColor = EmeraldPrimary,
                contentColor = TextOnPrimary,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Insert Doc", fontWeight = FontWeight.Bold) }
            )
        },
        containerColor = BackgroundDark
    ) { padding ->
        val queryResult = uiState.queryResult

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Spacer(modifier = Modifier.height(2.dp)) }

            // Query Filter Accordion
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderDark),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.FilterList, contentDescription = null, tint = EmeraldLight, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Query, Sort & Projection",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                            }
                            IconButton(
                                onClick = { isFilterExpanded = !isFilterExpanded },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = if (isFilterExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = TextSecondary
                                )
                            }
                        }

                        AnimatedVisibility(visible = isFilterExpanded) {
                            Column(modifier = Modifier.padding(top = 12.dp)) {
                                Text("Filter JSON", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = filterText,
                                    onValueChange = { filterText = it },
                                    placeholder = { Text("{\"status\": \"active\"}", color = TextMuted) },
                                    textStyle = MonospaceCodeStyle.copy(fontSize = 12.sp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = CardDark,
                                        unfocusedContainerColor = CardDark,
                                        focusedBorderColor = EmeraldPrimary,
                                        unfocusedBorderColor = CardBorderDark
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Sort JSON", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        OutlinedTextField(
                                            value = sortText,
                                            onValueChange = { sortText = it },
                                            placeholder = { Text("{\"createdAt\": -1}", color = TextMuted) },
                                            textStyle = MonospaceCodeStyle.copy(fontSize = 12.sp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedContainerColor = CardDark,
                                                unfocusedContainerColor = CardDark,
                                                focusedBorderColor = EmeraldPrimary,
                                                unfocusedBorderColor = CardBorderDark
                                            ),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Projection JSON", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        OutlinedTextField(
                                            value = projectionText,
                                            onValueChange = { projectionText = it },
                                            placeholder = { Text("{\"name\": 1, \"email\": 1}", color = TextMuted) },
                                            textStyle = MonospaceCodeStyle.copy(fontSize = 12.sp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedContainerColor = CardDark,
                                                unfocusedContainerColor = CardDark,
                                                focusedBorderColor = EmeraldPrimary,
                                                unfocusedBorderColor = CardBorderDark
                                            ),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(
                                        onClick = {
                                            filterText = ""
                                            sortText = ""
                                            projectionText = ""
                                            viewModel.updateQueryParams("", "", "")
                                            viewModel.runQuery(page = 1)
                                        }
                                    ) {
                                        Text("Reset", color = TextSecondary)
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Button(
                                        onClick = {
                                            viewModel.updateQueryParams(filterText, sortText, projectionText)
                                            viewModel.runQuery(page = 1)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = TextOnPrimary),
                                        shape = MaterialTheme.shapes.small
                                    ) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Apply Query")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Pagination & Stats Toolbar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${queryResult?.total ?: 0} Total Documents",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                if ((queryResult?.page ?: 1) > 1) {
                                    viewModel.runQuery(page = (queryResult?.page ?: 1) - 1)
                                }
                            },
                            enabled = (queryResult?.page ?: 1) > 1,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Page", tint = TextPrimary)
                        }

                        Text(
                            text = "Page ${queryResult?.page ?: 1} / ${queryResult?.totalPages ?: 1}",
                            style = MaterialTheme.typography.labelSmall,
                            color = EmeraldLight,
                            modifier = Modifier.padding(horizontal = 6.dp)
                        )

                        IconButton(
                            onClick = {
                                if ((queryResult?.page ?: 1) < (queryResult?.totalPages ?: 1)) {
                                    viewModel.runQuery(page = (queryResult?.page ?: 1) + 1)
                                }
                            },
                            enabled = (queryResult?.page ?: 1) < (queryResult?.totalPages ?: 1),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "Next Page", tint = TextPrimary)
                        }
                    }
                }
            }

            // Document List
            if (uiState.isLoading && queryResult == null) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = EmeraldPrimary)
                    }
                }
            } else if (queryResult?.documents.isNullOrEmpty()) {
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
                            Icon(Icons.Default.FindInPage, contentDescription = null, tint = TextMuted, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No documents found", color = TextSecondary)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { isAddingDocument = true },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = TextOnPrimary)
                            ) {
                                Text("Insert First Document")
                            }
                        }
                    }
                }
            } else {
                items(queryResult!!.documents) { doc ->
                    val docId = doc["_id"]?.toString() ?: ""

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderDark),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = EmeraldContainer,
                                    shape = MaterialTheme.shapes.extraSmall
                                ) {
                                    Text(
                                        text = "_id: $docId",
                                        style = MonospaceCodeStyle.copy(fontSize = 11.sp),
                                        color = EmeraldLight,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = {
                                            editingDocId = docId
                                            editingDocJson = prettyGson.toJson(doc)
                                        },
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = "Direct Push Edit",
                                            tint = SkyAccent,
                                            modifier = Modifier.size(17.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = { docToDeleteId = docId },
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.DeleteOutline,
                                            contentDescription = "Delete Document",
                                            tint = RoseAccent,
                                            modifier = Modifier.size(17.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            JsonViewerCard(
                                data = doc,
                                maxCollapsedLines = 8,
                                canCopy = true
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(70.dp)) }
        }
    }

    if (editingDocId != null && editingDocJson != null) {
        JsonEditorModal(
            title = "Direct Push: ${editingDocId!!.take(16)}...",
            initialJson = editingDocJson!!,
            confirmButtonText = "Push Changes to DB",
            isLoading = uiState.isLoading,
            onDismiss = {
                editingDocId = null
                editingDocJson = null
            },
            onSave = { updatedJson ->
                viewModel.updateDocument(editingDocId!!, updatedJson)
                editingDocId = null
                editingDocJson = null
            }
        )
    }

    if (isAddingDocument) {
        val templateJson = """{
  "name": "Sample Document",
  "status": "active",
  "count": 1,
  "tags": ["mongo", "android"]
}"""

        JsonEditorModal(
            title = "Insert New Document",
            initialJson = templateJson,
            confirmButtonText = "Insert Document",
            isLoading = uiState.isLoading,
            onDismiss = { isAddingDocument = false },
            onSave = { newDocJson ->
                viewModel.createDocument(newDocJson)
                isAddingDocument = false
            }
        )
    }

    docToDeleteId?.let { docId ->
        ConfirmDialog(
            title = "Delete Document?",
            message = "Permanently delete document with _id: $docId?",
            confirmText = "Delete Document",
            isDestructive = true,
            onConfirm = {
                viewModel.deleteDocument(docId)
                docToDeleteId = null
            },
            onDismiss = { docToDeleteId = null }
        )
    }
}

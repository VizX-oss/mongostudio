package com.mongostudio.app.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.GsonBuilder
import com.mongostudio.app.ui.components.*
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

    val queryResult = uiState.queryResult

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
        bottomBar = {
            // Expressive Floating Pagination & Action Bar
            ExpressiveFloatingToolbar(
                actions = listOf(
                    ToolbarAction(
                        id = "prev_page",
                        icon = Icons.Default.ChevronLeft,
                        contentDescription = "Previous Page",
                        onClick = {
                            if ((queryResult?.page ?: 1) > 1) {
                                viewModel.runQuery(page = (queryResult?.page ?: 1) - 1)
                            }
                        }
                    ),
                    ToolbarAction(
                        id = "next_page",
                        icon = Icons.Default.ChevronRight,
                        contentDescription = "Next Page",
                        onClick = {
                            if ((queryResult?.page ?: 1) < (queryResult?.totalPages ?: 1)) {
                                viewModel.runQuery(page = (queryResult?.page ?: 1) + 1)
                            }
                        }
                    )
                ),
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = { isAddingDocument = true },
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
                            Text("Insert Doc", fontWeight = FontWeight.Bold, fontSize = 13.sp)
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
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Query Filter & Pipeline Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(SquircleLarge)
                        .background(SurfaceContainer)
                        .border(1.dp, CardBorderDark, SquircleLarge)
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
                                        .background(EmeraldContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.FilterList, contentDescription = null, tint = EmeraldVibrant, modifier = Modifier.size(18.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Query Engine",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "${queryResult?.total ?: 0} documents matching",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { isFilterExpanded = !isFilterExpanded },
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(SurfaceContainerHigh)
                                ) {
                                    Icon(
                                        imageVector = if (isFilterExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Expand Filters",
                                        tint = TextPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        AnimatedVisibility(visible = isFilterExpanded) {
                            Column(modifier = Modifier.padding(top = 14.dp)) {
                                // Filter chips
                                Text("QUICK FILTERS", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(PillShape)
                                            .background(SurfaceContainerHigh)
                                            .clickable { filterText = "{}" }
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text("{}", style = MonospaceCodeStyle.copy(fontSize = 11.sp), color = TextPrimary)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(PillShape)
                                            .background(SurfaceContainerHigh)
                                            .clickable { filterText = """{"status": "active"}""" }
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text("status: active", style = MaterialTheme.typography.labelSmall, color = EmeraldLight)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(PillShape)
                                            .background(SurfaceContainerHigh)
                                            .clickable { sortText = """{"_id": -1}""" }
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text("latest first", style = MaterialTheme.typography.labelSmall, color = CyanAccent)
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Text("FILTER JSON", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = filterText,
                                    onValueChange = { filterText = it },
                                    placeholder = { Text("{\"status\": \"active\"}", color = TextMuted) },
                                    textStyle = MonospaceCodeStyle.copy(fontSize = 12.sp),
                                    shape = SquircleSmall,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = SurfaceContainerHigh,
                                        unfocusedContainerColor = SurfaceContainerHigh,
                                        focusedBorderColor = EmeraldPrimary,
                                        unfocusedBorderColor = CardBorderDark
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("SORT JSON", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        OutlinedTextField(
                                            value = sortText,
                                            onValueChange = { sortText = it },
                                            placeholder = { Text("{\"createdAt\": -1}", color = TextMuted) },
                                            textStyle = MonospaceCodeStyle.copy(fontSize = 12.sp),
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

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("PROJECTION JSON", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        OutlinedTextField(
                                            value = projectionText,
                                            onValueChange = { projectionText = it },
                                            placeholder = { Text("{\"name\": 1}", color = TextMuted) },
                                            textStyle = MonospaceCodeStyle.copy(fontSize = 12.sp),
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
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
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
                                        shape = PillShape
                                    ) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Execute Query", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Pagination Status Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Results (${queryResult?.documents?.size ?: 0} of ${queryResult?.total ?: 0})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    ExpressiveTag(
                        text = "Page ${queryResult?.page ?: 1} of ${queryResult?.totalPages ?: 1}",
                        containerColor = SurfaceContainerHigh,
                        contentColor = EmeraldLight
                    )
                }
            }

            // Loading state
            if (uiState.isLoading) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    ) {
                        WavyProgressIndicator(color = EmeraldLight)
                    }
                }
            }

            // Documents
            if (queryResult?.documents.isNullOrEmpty() && !uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(SquircleMedium)
                            .background(SurfaceContainer)
                            .border(1.dp, CardBorderDark, SquircleMedium)
                            .padding(36.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.FindInPage, contentDescription = null, tint = TextMuted, modifier = Modifier.size(44.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("No documents matched filter", style = MaterialTheme.typography.titleMedium, color = TextSecondary)
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { isAddingDocument = true },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = TextOnPrimary),
                                shape = PillShape
                            ) {
                                Text("Insert Document", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                items(queryResult?.documents ?: emptyList()) { doc ->
                    val docId = doc["_id"]?.toString() ?: ""

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
                                ExpressiveTag(
                                    text = "_id: $docId",
                                    containerColor = EmeraldContainer,
                                    contentColor = EmeraldLight
                                )

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    IconButton(
                                        onClick = {
                                            editingDocId = docId
                                            editingDocJson = prettyGson.toJson(doc)
                                        },
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(SurfaceContainerHigh)
                                    ) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = "Edit Document",
                                            tint = SkyAccent,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = { docToDeleteId = docId },
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(SurfaceContainerHigh)
                                    ) {
                                        Icon(
                                            Icons.Default.DeleteOutline,
                                            contentDescription = "Delete Document",
                                            tint = RoseAccent,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            JsonViewerCard(
                                data = doc,
                                maxCollapsedLines = 8,
                                canCopy = true
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(70.dp))
            }
        }
    }

    if (editingDocId != null && editingDocJson != null) {
        JsonEditorModal(
            title = "Edit: ${editingDocId!!.take(16)}...",
            initialJson = editingDocJson!!,
            confirmButtonText = "Push to MongoDB",
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
  "name": "Expressive Sample",
  "status": "active",
  "count": 1,
  "tags": ["mongodb", "android", "expressive"]
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
            message = "Permanently remove document with _id: $docId?",
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

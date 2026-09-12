package com.mongostudio.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.mongostudio.app.ui.components.*
import com.mongostudio.app.ui.theme.*
import com.mongostudio.app.viewmodel.MongoStudioViewModel

@Composable
fun AggregationScreen(
    dbName: String,
    colName: String,
    viewModel: MongoStudioViewModel,
    onNavigateBack: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val themeSettings by viewModel.themeSettings.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    var pipelineText by remember {
        mutableStateOf("""[
  {
    "${'$'}match": {}
  },
  {
    "${'$'}limit": 20
  }
]""")
    }

    Scaffold(
        topBar = {
            TopHeader(
                title = "Aggregation Pipeline",
                subtitle = "$dbName • $colName",
                isConnectedToCluster = true,
                pingMs = uiState.activeClusterPingMs,
                onBackClick = onNavigateBack
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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Preset Stage Chips
                item {
                    Column {
                        Text(
                            text = "PIPELINE TEMPLATES",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SuggestionChip(
                                onClick = {
                                    haptic.performClickFeedback()
                                    pipelineText = """[
  {
    "${'$'}group": {
      "_id": "${'$'}status",
      "total": { "${'$'}sum": 1 }
    }
  }
]"""
                                },
                                label = { Text("${'$'}group by status") },
                                icon = { Icon(Icons.Default.AutoGraph, contentDescription = null, modifier = Modifier.size(14.dp)) },
                                shape = CircleShape
                            )

                            SuggestionChip(
                                onClick = {
                                    haptic.performClickFeedback()
                                    pipelineText = """[
  {
    "${'$'}sort": { "_id": -1 }
  },
  {
    "${'$'}limit": 10
  }
]"""
                                },
                                label = { Text("${'$'}sort & limit 10") },
                                icon = { Icon(Icons.Default.Sort, contentDescription = null, modifier = Modifier.size(14.dp)) },
                                shape = CircleShape
                            )

                            SuggestionChip(
                                onClick = {
                                    haptic.performClickFeedback()
                                    pipelineText = """[
  {
    "${'$'}match": { "count": { "${'$'}gt": 0 } }
  },
  {
    "${'$'}project": { "name": 1, "count": 1 }
  }
]"""
                                },
                                label = { Text("${'$'}match & project") },
                                icon = { Icon(Icons.Default.FilterAlt, contentDescription = null, modifier = Modifier.size(14.dp)) },
                                shape = CircleShape
                            )
                        }
                    }
                }

                // Pipeline Editor Container
                item {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Functions,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Pipeline Stages",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                ExpressiveTag(
                                    text = "JSON Array",
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = pipelineText,
                                onValueChange = { pipelineText = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 140.dp, max = 260.dp),
                                shape = MaterialTheme.shapes.medium,
                                textStyle = MonospaceCodeStyle.copy(fontSize = 12.5.sp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                )
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            AnimatedVisibility(
                                visible = uiState.isLoading,
                                enter = fadeIn(),
                                exit = fadeOut()
                            ) {
                                Column(modifier = Modifier.padding(bottom = 12.dp)) {
                                    WavyProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                }
                            }

                            Button(
                                onClick = {
                                    haptic.performClickFeedback()
                                    viewModel.runAggregation(dbName, colName, pipelineText)
                                },
                                enabled = !uiState.isLoading,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .pressMorph(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                shape = CircleShape
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Execute Aggregation", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Error banner
                if (uiState.errorMessage != null) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = uiState.errorMessage!!,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }
                }

                // Results Section
                val aggResult = uiState.aggregateResult
                if (aggResult != null) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Pipeline Output",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            ExpressiveTag(
                                text = "${aggResult.count} items returned",
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                contentColor = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    if (aggResult.results.isEmpty()) {
                        item {
                            ElevatedCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.medium,
                                colors = CardDefaults.elevatedCardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                )
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Pipeline returned 0 items", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    } else {
                        itemsIndexed(aggResult.results) { idx, resItem ->
                            StaggerEntrance(index = idx) {
                                ElevatedCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .animateItem(),
                                    shape = MaterialTheme.shapes.medium,
                                    colors = CardDefaults.elevatedCardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                    )
                                ) {
                                    Box(modifier = Modifier.padding(14.dp)) {
                                        JsonViewerCard(
                                            data = resItem,
                                            maxCollapsedLines = 10,
                                            canCopy = true
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            VelocityAwareScrollbar(
                listState = listState,
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }
    }
}

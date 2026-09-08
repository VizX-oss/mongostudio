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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mongostudio.app.ui.components.JsonViewerCard
import com.mongostudio.app.ui.components.TopHeader
import com.mongostudio.app.ui.theme.*
import com.mongostudio.app.viewmodel.MongoStudioViewModel

@Composable
fun AggregationScreen(
    dbName: String,
    colName: String,
    viewModel: MongoStudioViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

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
                isServerReachable = uiState.isServerReachable,
                serverPingMs = uiState.serverPingMs,
                isConnectedToCluster = true,
                onBackClick = onNavigateBack
            )
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
            // Pipeline Presets Row
            item {
                Text(
                    text = "Quick Presets",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SuggestionChip(
                        onClick = {
                            pipelineText = """[
  {
    "${'$'}group": {
      "_id": "${'$'}status",
      "count": { "${'$'}sum": 1 }
    }
  }
]"""
                        },
                        label = { Text("Group by Status", fontSize = 12.sp) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = SurfaceDark,
                            labelColor = TextPrimary
                        ),
                        border = SuggestionChipDefaults.suggestionChipBorder(
                            enabled = true,
                            borderColor = CardBorderDark
                        )
                    )

                    SuggestionChip(
                        onClick = {
                            pipelineText = """[
  {
    "${'$'}sort": { "_id": -1 }
  },
  {
    "${'$'}limit": 10
  }
]"""
                        },
                        label = { Text("Recent 10", fontSize = 12.sp) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = SurfaceDark,
                            labelColor = TextPrimary
                        ),
                        border = SuggestionChipDefaults.suggestionChipBorder(
                            enabled = true,
                            borderColor = CardBorderDark
                        )
                    )
                }
            }

            // Pipeline Editor Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderDark),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Pipeline Stages (JSON Array)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = pipelineText,
                            onValueChange = { pipelineText = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 140.dp, max = 260.dp),
                            textStyle = MonospaceCodeStyle.copy(fontSize = 12.5.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = CardDark,
                                unfocusedContainerColor = CardDark,
                                focusedBorderColor = EmeraldPrimary,
                                unfocusedBorderColor = CardBorderDark
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { viewModel.runAggregate(pipelineText) },
                            enabled = !uiState.isLoading,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EmeraldPrimary,
                                contentColor = TextOnPrimary
                            ),
                            shape = MaterialTheme.shapes.small
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = TextOnPrimary, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Executing Pipeline...")
                            } else {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Execute Pipeline", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Error banner
            if (uiState.errorMessage != null) {
                item {
                    Surface(
                        color = RoseAccent.copy(alpha = 0.15f),
                        shape = MaterialTheme.shapes.small,
                        border = androidx.compose.foundation.BorderStroke(1.dp, RoseAccent.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = uiState.errorMessage!!,
                            color = RoseAccent,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(12.dp)
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
                            text = "Pipeline Output (${aggResult.count} items)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }

                if (aggResult.results.isEmpty()) {
                    item {
                        Text("Pipeline returned empty result set", color = TextSecondary)
                    }
                } else {
                    items(aggResult.results) { resItem ->
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

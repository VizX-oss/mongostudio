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
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                isConnectedToCluster = true,
                pingMs = uiState.activeClusterPingMs,
                onBackClick = onNavigateBack
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Preset Stage Chips
            item {
                Column {
                    Text(
                        text = "PIPELINE TEMPLATES",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(PillShape)
                                .background(SurfaceContainerHigh)
                                .border(1.dp, AmberAccent.copy(alpha = 0.3f), PillShape)
                                .pressMorph(onClick = {
                                    haptic.performClickFeedback()
                                    pipelineText = """[
  {
    "${'$'}group": {
      "_id": "${'$'}status",
      "total": { "${'$'}sum": 1 }
    }
  }
]"""
                                })
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("${'$'}group by status", style = MaterialTheme.typography.labelSmall, color = AmberAccent, fontWeight = FontWeight.SemiBold)
                        }

                        Box(
                            modifier = Modifier
                                .clip(PillShape)
                                .background(SurfaceContainerHigh)
                                .border(1.dp, CyanAccent.copy(alpha = 0.3f), PillShape)
                                .pressMorph(onClick = {
                                    haptic.performClickFeedback()
                                    pipelineText = """[
  {
    "${'$'}sort": { "_id": -1 }
  },
  {
    "${'$'}limit": 10
  }
]"""
                                })
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("${'$'}sort & limit 10", style = MaterialTheme.typography.labelSmall, color = CyanAccent, fontWeight = FontWeight.SemiBold)
                        }

                        Box(
                            modifier = Modifier
                                .clip(PillShape)
                                .background(SurfaceContainerHigh)
                                .border(1.dp, PurpleAccent.copy(alpha = 0.3f), PillShape)
                                .pressMorph(onClick = {
                                    haptic.performClickFeedback()
                                    pipelineText = """[
  {
    "${'$'}match": { "count": { "${'$'}gt": 0 } }
  },
  {
    "${'$'}project": { "name": 1, "count": 1 }
  }
]"""
                                })
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("${'$'}match & project", style = MaterialTheme.typography.labelSmall, color = PurpleAccent, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // Pipeline Editor Container
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(SquircleLarge)
                        .background(SurfaceContainer)
                        .border(1.dp, CardBorderDark, SquircleLarge)
                        .padding(18.dp)
                ) {
                    Column {
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
                                        .background(AmberAccent.copy(alpha = 0.16f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Functions, contentDescription = null, tint = AmberAccent, modifier = Modifier.size(18.dp))
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
                            shape = SquircleSmall,
                            textStyle = MonospaceCodeStyle.copy(fontSize = 12.5.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = SurfaceContainerHigh,
                                unfocusedContainerColor = SurfaceContainerHigh,
                                focusedBorderColor = EmeraldPrimary,
                                unfocusedBorderColor = CardBorderDark
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        AnimatedVisibility(
                            visible = uiState.isLoading,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            Column(modifier = Modifier.padding(bottom = 12.dp)) {
                                WavyProgressIndicator(color = AmberAccent)
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
                                containerColor = EmeraldPrimary,
                                contentColor = TextOnPrimary
                            ),
                            shape = PillShape
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
                    Surface(
                        color = RoseContainer.copy(alpha = 0.35f),
                        shape = SquircleMedium,
                        border = androidx.compose.foundation.BorderStroke(1.dp, RoseAccent.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = uiState.errorMessage!!,
                            color = RoseAccent,
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
                            containerColor = SurfaceContainerHigh,
                            contentColor = EmeraldLight
                        )
                    }
                }

                if (aggResult.results.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(SquircleMedium)
                                .background(SurfaceContainer)
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Pipeline returned 0 items", color = TextSecondary)
                        }
                    }
                } else {
                    itemsIndexed(aggResult.results) { idx, resItem ->
                        StaggerEntrance(index = idx) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(SquircleMedium)
                                    .background(SurfaceContainer)
                                    .border(1.dp, CardBorderDark, SquircleMedium)
                                    .padding(14.dp)
                            ) {
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

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

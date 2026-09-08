package com.mongostudio.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mongostudio.app.ui.components.*
import com.mongostudio.app.ui.theme.*
import com.mongostudio.app.viewmodel.MongoStudioViewModel

@Composable
fun ConsoleScreen(
    viewModel: MongoStudioViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var dbName by remember { mutableStateOf(uiState.selectedDatabase ?: "admin") }
    var commandText by remember { mutableStateOf("""{ "ping": 1 }""") }

    Scaffold(
        topBar = {
            TopHeader(
                title = "Raw Console",
                subtitle = "Wire Protocol Terminal",
                isConnectedToCluster = true,
                pingMs = uiState.activeClusterPingMs,
                onBackClick = onNavigateBack
            )
        },
        containerColor = BackgroundDark
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Preset commands
            item {
                Column {
                    Text(
                        text = "COMMAND SHORTCUTS",
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
                                .clickable { commandText = """{ "ping": 1 }""" }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("ping", style = MonospaceCodeStyle.copy(fontSize = 11.sp), color = EmeraldLight)
                        }

                        Box(
                            modifier = Modifier
                                .clip(PillShape)
                                .background(SurfaceContainerHigh)
                                .clickable { commandText = """{ "buildInfo": 1 }""" }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("buildInfo", style = MonospaceCodeStyle.copy(fontSize = 11.sp), color = CyanAccent)
                        }

                        Box(
                            modifier = Modifier
                                .clip(PillShape)
                                .background(SurfaceContainerHigh)
                                .clickable { commandText = """{ "serverStatus": 1 }""" }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("serverStatus", style = MonospaceCodeStyle.copy(fontSize = 11.sp), color = AmberAccent)
                        }

                        Box(
                            modifier = Modifier
                                .clip(PillShape)
                                .background(SurfaceContainerHigh)
                                .clickable { commandText = """{ "dbStats": 1 }""" }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("dbStats", style = MonospaceCodeStyle.copy(fontSize = 11.sp), color = PurpleAccent)
                        }
                    }
                }
            }

            // Command Card
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
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Terminal, contentDescription = null, tint = EmeraldLight, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Database Command Runner",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text("TARGET DATABASE", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = dbName,
                            onValueChange = { dbName = it },
                            singleLine = true,
                            shape = SquircleSmall,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = SurfaceContainerHigh,
                                unfocusedContainerColor = SurfaceContainerHigh,
                                focusedBorderColor = EmeraldPrimary,
                                unfocusedBorderColor = CardBorderDark
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text("COMMAND JSON", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = commandText,
                            onValueChange = { commandText = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 90.dp, max = 200.dp),
                            textStyle = MonospaceCodeStyle.copy(fontSize = 12.5.sp),
                            shape = SquircleSmall,
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
                                WavyProgressIndicator(color = EmeraldLight)
                            }
                        }

                        Button(
                            onClick = { viewModel.executeRawCommand(dbName, commandText) },
                            enabled = !uiState.isLoading,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = TextOnPrimary),
                            shape = PillShape
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Run Command", fontWeight = FontWeight.Bold)
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
            val result = uiState.rawCommandResult
            if (result != null) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Server Output",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        ExpressiveTag(
                            text = if (result["ok"] == 1.0 || result["ok"] == 1) "ok: 1" else "result",
                            containerColor = if (result["ok"] == 1.0 || result["ok"] == 1) EmeraldContainer else RoseContainer,
                            contentColor = if (result["ok"] == 1.0 || result["ok"] == 1) EmeraldLight else RoseAccent
                        )
                    }
                }

                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(SquircleMedium)
                            .background(SurfaceContainer)
                            .border(1.dp, CardBorderDark, SquircleMedium)
                            .padding(16.dp)
                    ) {
                        JsonViewerCard(
                            data = result,
                            maxCollapsedLines = 20,
                            canCopy = true
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

package com.mongostudio.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.mongostudio.app.ui.components.JsonViewerCard
import com.mongostudio.app.ui.components.TopHeader
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
                title = "Database Console",
                subtitle = "Raw Command Execution",
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
            // Preset commands
            item {
                Text("Command Presets", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SuggestionChip(
                        onClick = { commandText = """{ "ping": 1 }""" },
                        label = { Text("ping", fontSize = 12.sp) },
                        colors = SuggestionChipDefaults.suggestionChipColors(containerColor = SurfaceDark, labelColor = TextPrimary),
                        border = SuggestionChipDefaults.suggestionChipBorder(enabled = true, borderColor = CardBorderDark)
                    )
                    SuggestionChip(
                        onClick = { commandText = """{ "buildInfo": 1 }""" },
                        label = { Text("buildInfo", fontSize = 12.sp) },
                        colors = SuggestionChipDefaults.suggestionChipColors(containerColor = SurfaceDark, labelColor = TextPrimary),
                        border = SuggestionChipDefaults.suggestionChipBorder(enabled = true, borderColor = CardBorderDark)
                    )
                    SuggestionChip(
                        onClick = { commandText = """{ "dbStats": 1 }""" },
                        label = { Text("dbStats", fontSize = 12.sp) },
                        colors = SuggestionChipDefaults.suggestionChipColors(containerColor = SurfaceDark, labelColor = TextPrimary),
                        border = SuggestionChipDefaults.suggestionChipBorder(enabled = true, borderColor = CardBorderDark)
                    )
                }
            }

            // Command Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderDark),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Target Database", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = dbName,
                            onValueChange = { dbName = it },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = CardDark,
                                unfocusedContainerColor = CardDark,
                                focusedBorderColor = EmeraldPrimary,
                                unfocusedBorderColor = CardBorderDark
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text("Command (JSON Object)", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = commandText,
                            onValueChange = { commandText = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 100.dp, max = 200.dp),
                            textStyle = MonospaceCodeStyle.copy(fontSize = 12.5.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = CardDark,
                                unfocusedContainerColor = CardDark,
                                focusedBorderColor = EmeraldPrimary,
                                unfocusedBorderColor = CardBorderDark
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = { viewModel.executeRawCommand(dbName, commandText) },
                            enabled = !uiState.isLoading,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = TextOnPrimary),
                            shape = MaterialTheme.shapes.small
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = TextOnPrimary, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Running...")
                            } else {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Run Database Command", fontWeight = FontWeight.Bold)
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

            // Result
            val cmdResult = uiState.rawCommandResult
            if (cmdResult != null) {
                item {
                    Text(
                        text = "Execution Result",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    JsonViewerCard(
                        data = cmdResult.result ?: mapOf("success" to cmdResult.success),
                        maxCollapsedLines = 15,
                        canCopy = true
                    )
                }
            }
        }
    }
}

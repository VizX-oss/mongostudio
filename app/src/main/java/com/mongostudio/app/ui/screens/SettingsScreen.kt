package com.mongostudio.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.mongostudio.app.ui.components.TopHeader
import com.mongostudio.app.ui.theme.*
import com.mongostudio.app.viewmodel.MongoStudioViewModel

@Composable
fun SettingsScreen(
    viewModel: MongoStudioViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var serverHostInput by remember { mutableStateOf(uiState.serverUrl) }

    Scaffold(
        topBar = {
            TopHeader(
                title = "Settings",
                subtitle = "API Host & Preferences",
                isServerReachable = uiState.isServerReachable,
                serverPingMs = uiState.serverPingMs,
                isConnectedToCluster = uiState.isConnectedToCluster,
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
            // Server Endpoint Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderDark),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Dns, contentDescription = null, tint = EmeraldLight, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "MongoStudio Server API Host",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Default for Android Emulator is http://10.0.2.2:4000. For physical devices on the same Wi-Fi, use your machine's LAN IP (e.g. http://192.168.1.150:4000).",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = serverHostInput,
                            onValueChange = { serverHostInput = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            textStyle = MonospaceCodeStyle.copy(fontSize = 13.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = CardDark,
                                unfocusedContainerColor = CardDark,
                                focusedBorderColor = EmeraldPrimary,
                                unfocusedBorderColor = CardBorderDark
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            OutlinedButton(
                                onClick = {
                                    viewModel.testServerHealth()
                                },
                                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderDark)
                            ) {
                                Icon(Icons.Default.NetworkCheck, contentDescription = null, tint = SkyAccent, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Ping Test", color = TextPrimary)
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Button(
                                onClick = {
                                    viewModel.setServerUrl(serverHostInput.trim())
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = TextOnPrimary)
                            ) {
                                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Save Endpoint")
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Status pill
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(MaterialTheme.shapes.small)
                                .background(if (uiState.isServerReachable) EmeraldContainer.copy(alpha = 0.4f) else RoseAccent.copy(alpha = 0.15f))
                                .border(1.dp, if (uiState.isServerReachable) EmeraldPrimary.copy(alpha = 0.4f) else RoseAccent.copy(alpha = 0.3f), MaterialTheme.shapes.small)
                                .padding(10.dp)
                        ) {
                            Text(
                                text = if (uiState.isServerReachable) "Connection Live: ${uiState.serverPingMs ?: 0}ms round-trip" else "Server unreachable. Ensure node server is running and accessible.",
                                color = if (uiState.isServerReachable) EmeraldLight else RoseAccent,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            // About Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderDark),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = EmeraldLight, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "About MongoStudio Mobile",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "A modern, native Android client for MongoDB management built with Jetpack Compose & Material 3 Expressive, modeled on the principles of MongoStudio Pro.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Version: 1.0.0", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        Text("Stack: Kotlin 2.0 • Jetpack Compose • Material 3 Expressive • OkHttp", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    }
                }
            }
        }
    }
}

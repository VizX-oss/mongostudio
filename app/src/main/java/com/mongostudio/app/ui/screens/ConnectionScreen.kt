package com.mongostudio.app.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mongostudio.app.data.model.SavedConnection
import com.mongostudio.app.ui.components.*
import com.mongostudio.app.ui.theme.*
import com.mongostudio.app.viewmodel.MongoStudioViewModel

@Composable
fun ConnectionScreen(
    viewModel: MongoStudioViewModel,
    onNavigateToDashboard: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val haptics = androidx.compose.ui.platform.LocalHapticFeedback.current

    var uriText by remember { mutableStateOf("mongodb://10.0.2.2:27017") }
    var clusterName by remember { mutableStateOf("Local MongoDB") }
    var saveConnection by remember { mutableStateOf(true) }
    var itemToDelete by remember { mutableStateOf<SavedConnection?>(null) }
    var showPresetsDropdown by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isConnectedToCluster) {
        if (uiState.isConnectedToCluster) {
            onNavigateToDashboard()
        }
    }

    Scaffold(
        topBar = {
            TopHeader(
                title = "MongoStudio",
                subtitle = "Material 3 Expressive",
                isConnectedToCluster = false,
                onSettingsClick = onNavigateToSettings,
                onRefreshClick = {
                    viewModel.loadSavedConnections()
                }
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
            // Hero Expressive Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(AsymmetricCardShape)
                        .background(SurfaceContainer)
                        .border(1.dp, CardBorderDark, AsymmetricCardShape)
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldContainer)
                                    .border(1.dp, EmeraldLight.copy(alpha = 0.4f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Dataset,
                                    contentDescription = null,
                                    tint = EmeraldVibrant,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            ExpressiveLiveBadge(
                                label = "100% Standalone",
                                isActive = true,
                                activeColor = EmeraldLight
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Direct MongoDB Engine",
                            style = MaterialTheme.typography.headlineSmallEmphasized,
                            color = TextPrimary,
                            letterSpacing = (-0.5).sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Native wire-protocol connection straight to MongoDB Atlas or local clusters. Zero middleware or backend server needed.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            // Error Banner
            if (uiState.errorMessage != null) {
                item {
                    Surface(
                        color = RoseContainer.copy(alpha = 0.35f),
                        shape = SquircleMedium,
                        border = androidx.compose.foundation.BorderStroke(1.dp, RoseAccent.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(RoseAccent.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = RoseAccent, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = uiState.errorMessage!!,
                                color = RoseAccent,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { viewModel.clearError() }, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = RoseAccent, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            // Connection Form Container
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(SquircleLarge)
                        .background(SurfaceContainer)
                        .border(1.dp, CardBorderDark, SquircleLarge)
                        .padding(20.dp)
                ) {
                    Column {
                        Text(
                            text = "Connection Details",
                            style = MaterialTheme.typography.titleMediumEmphasized,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Quick Presets Chips
                        Text(
                            text = "QUICK TEMPLATES",
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
                                    .pressMorph(onClick = {
                                        haptics.performClickFeedback()
                                        uriText = "mongodb://10.0.2.2:27017"
                                        clusterName = "Android Emulator DB"
                                    })
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Emulator (10.0.2.2)", style = MaterialTheme.typography.labelSmall, color = TextPrimary)
                            }

                            Box(
                                modifier = Modifier
                                    .clip(PillShape)
                                    .background(SurfaceContainerHigh)
                                    .pressMorph(onClick = {
                                        haptics.performClickFeedback()
                                        uriText = "mongodb+srv://user:password@cluster0.mongodb.net/?appName=MongoStudio"
                                        clusterName = "Atlas Cloud"
                                    })
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Atlas SRV", style = MaterialTheme.typography.labelSmall, color = CyanAccent)
                            }

                            Box(
                                modifier = Modifier
                                    .clip(PillShape)
                                    .background(SurfaceContainerHigh)
                                    .pressMorph(onClick = {
                                        haptics.performClickFeedback()
                                        uriText = "mongodb://localhost:27017"
                                        clusterName = "Localhost"
                                    })
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Localhost", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Connection URI Input
                        Text("CONNECTION URI", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = uriText,
                            onValueChange = { uriText = it },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = MonospaceCodeStyle.copy(fontSize = 13.sp),
                            shape = SquircleSmall,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = SurfaceContainerHigh,
                                unfocusedContainerColor = SurfaceContainerHigh,
                                focusedBorderColor = EmeraldPrimary,
                                unfocusedBorderColor = CardBorderDark
                            ),
                            placeholder = { Text("mongodb+srv://...", color = TextMuted) },
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Cluster Nickname
                        Text("CLUSTER NICKNAME", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = clusterName,
                            onValueChange = { clusterName = it },
                            modifier = Modifier.fillMaxWidth(),
                            shape = SquircleSmall,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = SurfaceContainerHigh,
                                unfocusedContainerColor = SurfaceContainerHigh,
                                focusedBorderColor = EmeraldPrimary,
                                unfocusedBorderColor = CardBorderDark
                            ),
                            placeholder = { Text("e.g. Production Cluster", color = TextMuted) },
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Save to Vault Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Save to Encrypted Vault", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                Text("AES-256 encrypted on this device", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            }
                            Switch(
                                checked = saveConnection,
                                onCheckedChange = { saveConnection = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = TextOnPrimary,
                                    checkedTrackColor = EmeraldPrimary,
                                    uncheckedTrackColor = SurfaceContainerHighest
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Connecting Wavy Progress
                        AnimatedVisibility(
                            visible = uiState.isConnecting,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            Column(modifier = Modifier.padding(bottom = 14.dp)) {
                                WavyProgressIndicator(color = EmeraldVibrant)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Resolving SRV records & handshaking wire protocol...",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = EmeraldLight,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Expressive Split Button for Connect
                        Box {
                            ExpressiveSplitButton(
                                text = if (uiState.isConnecting) "Connecting..." else "Connect to Cluster",
                                icon = Icons.Default.Bolt,
                                enabled = !uiState.isConnecting && uriText.isNotBlank(),
                                onPrimaryClick = {
                                    viewModel.connect(uriText, clusterName, saveConnection)
                                },
                                onTrailingClick = {
                                    showPresetsDropdown = true
                                },
                                modifier = Modifier.fillMaxWidth()
                            )

                            DropdownMenu(
                                expanded = showPresetsDropdown,
                                onDismissRequest = { showPresetsDropdown = false },
                                modifier = Modifier
                                    .clip(SquircleMedium)
                                    .background(SurfaceContainerHigh)
                                    .border(1.dp, CardBorderDark, SquircleMedium)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Local Emulator (10.0.2.2)", color = TextPrimary) },
                                    leadingIcon = { Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = EmeraldLight) },
                                    onClick = {
                                        uriText = "mongodb://10.0.2.2:27017"
                                        clusterName = "Android Emulator"
                                        showPresetsDropdown = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("MongoDB Atlas SRV", color = TextPrimary) },
                                    leadingIcon = { Icon(Icons.Default.Cloud, contentDescription = null, tint = CyanAccent) },
                                    onClick = {
                                        uriText = "mongodb+srv://username:password@cluster0.mongodb.net/?appName=Cluster0"
                                        clusterName = "MongoDB Atlas"
                                        showPresetsDropdown = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Docker Container (127.0.0.1)", color = TextPrimary) },
                                    leadingIcon = { Icon(Icons.Default.DeveloperBoard, contentDescription = null, tint = PurpleAccent) },
                                    onClick = {
                                        uriText = "mongodb://127.0.0.1:27017"
                                        clusterName = "Docker MongoDB"
                                        showPresetsDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Encrypted Vault Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Encrypted Vault (${uiState.savedConnections.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    ExpressiveTag(
                        text = "AES-256",
                        icon = Icons.Default.Lock,
                        containerColor = SurfaceContainerHigh,
                        contentColor = EmeraldLight
                    )
                }
            }

            if (uiState.savedConnections.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(SquircleMedium)
                            .background(SurfaceContainer)
                            .border(1.dp, CardBorderDark, SquircleMedium)
                            .padding(28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(SurfaceContainerHigh),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Key, contentDescription = null, tint = TextMuted, modifier = Modifier.size(24.dp))
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("No saved clusters yet", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Your authenticated clusters are encrypted with hardware-backed keys.", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        }
                    }
                }
            } else {
                itemsIndexed(uiState.savedConnections, key = { _, item -> item.id }) { index, saved ->
                    StaggerEntrance(index = index) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(SquircleMedium)
                                .background(SurfaceContainer)
                                .border(1.dp, CardBorderDark, SquircleMedium)
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = saved.name,
                                        style = MaterialTheme.typography.titleMediumEmphasized,
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = saved.maskedUri,
                                        style = MonospaceCodeStyle.copy(fontSize = 11.sp),
                                        color = TextSecondary
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    IconButton(
                                        onClick = {
                                            haptics.performConfirmFeedback()
                                            itemToDelete = saved
                                        },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(SurfaceContainerHigh)
                                            .pressMorph()
                                    ) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = RoseAccent, modifier = Modifier.size(18.dp))
                                    }

                                    Button(
                                        onClick = {
                                            haptics.performClickFeedback()
                                            viewModel.connectSaved(saved)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = TextOnPrimary),
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                        shape = PillShape,
                                        modifier = Modifier.pressMorph()
                                    ) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Connect", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
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
    }

    itemToDelete?.let { saved ->
        ConfirmDialog(
            title = "Delete Saved Connection?",
            message = "Remove '${saved.name}' from your encrypted vault?",
            confirmText = "Delete",
            onConfirm = {
                viewModel.deleteSaved(saved.id)
                itemToDelete = null
            },
            onDismiss = { itemToDelete = null }
        )
    }
}

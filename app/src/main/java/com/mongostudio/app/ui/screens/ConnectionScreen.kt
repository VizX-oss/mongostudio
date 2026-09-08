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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mongostudio.app.data.model.SavedConnection
import com.mongostudio.app.ui.components.ConfirmDialog
import com.mongostudio.app.ui.components.TopHeader
import com.mongostudio.app.ui.theme.*
import com.mongostudio.app.viewmodel.MongoStudioViewModel

@Composable
fun ConnectionScreen(
    viewModel: MongoStudioViewModel,
    onNavigateToDashboard: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    var uriText by remember { mutableStateOf("mongodb://10.0.2.2:27017") }
    var clusterName by remember { mutableStateOf("Local MongoDB") }
    var saveConnection by remember { mutableStateOf(true) }
    var itemToDelete by remember { mutableStateOf<SavedConnection?>(null) }

    LaunchedEffect(uiState.isConnectedToCluster) {
        if (uiState.isConnectedToCluster) {
            onNavigateToDashboard()
        }
    }

    Scaffold(
        topBar = {
            TopHeader(
                title = "MongoStudio Mobile",
                subtitle = "Standalone Native Client",
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Standalone Architecture Badge
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.medium)
                        .background(SurfaceDark)
                        .border(1.dp, CardBorderDark, MaterialTheme.shapes.medium)
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(MaterialTheme.shapes.small)
                                .background(EmeraldContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = EmeraldLight,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "100% Standalone & Direct",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Zero middleware servers. Connects directly to MongoDB Atlas or self-hosted clusters with built-in DoH SRV resolver & AES-256 encrypted vault.",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            // Error Banner
            if (uiState.errorMessage != null) {
                item {
                    Surface(
                        color = RoseAccent.copy(alpha = 0.15f),
                        shape = MaterialTheme.shapes.small,
                        border = androidx.compose.foundation.BorderStroke(1.dp, RoseAccent.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = RoseAccent)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = uiState.errorMessage!!,
                                color = RoseAccent,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { viewModel.clearError() }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = RoseAccent, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            // Connection Form Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderDark),
                    shape = MaterialTheme.shapes.large
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Direct MongoDB Connection",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Supports Atlas (mongodb+srv://) and Direct TCP (mongodb://)",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // URI Input
                        Text("Connection URI", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = uriText,
                            onValueChange = { uriText = it },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = MonospaceCodeStyle.copy(fontSize = 13.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = CardDark,
                                unfocusedContainerColor = CardDark,
                                focusedBorderColor = EmeraldPrimary,
                                unfocusedBorderColor = CardBorderDark
                            ),
                            placeholder = { Text("mongodb+srv://user:pass@cluster.mongodb.net", color = TextMuted) },
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Cluster Name
                        Text("Cluster Nickname", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = clusterName,
                            onValueChange = { clusterName = it },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = CardDark,
                                unfocusedContainerColor = CardDark,
                                focusedBorderColor = EmeraldPrimary,
                                unfocusedBorderColor = CardBorderDark
                            ),
                            placeholder = { Text("e.g. Production Cluster or Dev DB", color = TextMuted) },
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Save to Vault Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Save to Encrypted Vault", style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
                                Text("AES-256 encrypted on this device for quick access", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            }
                            Switch(
                                checked = saveConnection,
                                onCheckedChange = { saveConnection = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = TextOnPrimary,
                                    checkedTrackColor = EmeraldPrimary
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Connect Button
                        Button(
                            onClick = { viewModel.connect(uriText, clusterName, saveConnection) },
                            enabled = !uiState.isConnecting,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EmeraldPrimary,
                                contentColor = TextOnPrimary
                            ),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            if (uiState.isConnecting) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = TextOnPrimary, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Resolving & Connecting Directly...", fontWeight = FontWeight.SemiBold)
                            } else {
                                Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Connect to MongoDB", fontSize = 15.sp, fontWeight = FontWeight.Bold)
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
                }
            }

            if (uiState.savedConnections.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.medium)
                            .background(SurfaceDark)
                            .border(1.dp, CardBorderDark, MaterialTheme.shapes.medium)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.LockOpen, contentDescription = null, tint = TextMuted, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No saved connections yet", style = MaterialTheme.typography.bodyLarge, color = TextSecondary)
                            Text("Saved clusters will appear here with encrypted credentials.", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        }
                    }
                }
            } else {
                items(uiState.savedConnections) { saved ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderDark),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = saved.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = saved.maskedUri,
                                    style = MonospaceCodeStyle.copy(fontSize = 11.sp),
                                    color = TextSecondary
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { itemToDelete = saved }) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = RoseAccent)
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                Button(
                                    onClick = { viewModel.connectSaved(saved) },
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = TextOnPrimary),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                    shape = MaterialTheme.shapes.small
                                ) {
                                    Text("Connect", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    itemToDelete?.let { saved ->
        ConfirmDialog(
            title = "Delete Saved Connection?",
            message = "Remove '${saved.name}' from your vault? You will need to re-enter credentials to connect again.",
            confirmText = "Remove",
            onConfirm = {
                viewModel.deleteSaved(saved.id)
                itemToDelete = null
            },
            onDismiss = { itemToDelete = null }
        )
    }
}

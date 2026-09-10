package com.mongostudio.app.ui.screens

import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mongostudio.app.data.model.SavedConnection
import com.mongostudio.app.ui.components.ConfirmDialog
import com.mongostudio.app.ui.components.ExpressiveLiveBadge
import com.mongostudio.app.ui.components.TopHeader
import com.mongostudio.app.ui.theme.*
import com.mongostudio.app.viewmodel.MongoStudioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionScreen(
    viewModel: MongoStudioViewModel,
    onNavigateToDashboard: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val uiState by viewModel.uiState.collectAsState()

    var uriText by remember { mutableStateOf("mongodb+srv://username:password@2nd-alt-ub.9zwqoto.mongodb.net/?appName=2nd-Alt-UB") }
    var clusterName by remember { mutableStateOf("2nd-Alt-UB Production") }
    var isUriPasswordVisible by remember { mutableStateOf(false) }
    var saveConnection by remember { mutableStateOf(true) }
    var selectedColorTag by remember { mutableStateOf("emerald") }

    var itemToDelete by remember { mutableStateOf<SavedConnection?>(null) }
    var itemToEdit by remember { mutableStateOf<SavedConnection?>(null) }
    var editNameText by remember { mutableStateOf("") }
    var editUriText by remember { mutableStateOf("") }
    var editColorTag by remember { mutableStateOf("emerald") }

    val colorOptions = listOf(
        "emerald" to EmeraldPrimary,
        "sky" to SkyAccent,
        "amber" to AmberAccent,
        "purple" to PurpleAccent,
        "rose" to RoseAccent
    )

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
                onRefreshClick = { viewModel.loadSavedConnections() }
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
            // Hero Expressive Card
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Dataset,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }

                            ExpressiveLiveBadge(
                                label = "Direct Atlas & Wire",
                                isActive = true,
                                activeColor = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "MongoDB Mobile Client",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Connect directly to MongoDB Atlas clusters and standalone instances via secure TLS wire protocol with hardware-backed encryption.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Error Banner
            if (uiState.errorMessage != null) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = uiState.errorMessage!!,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { viewModel.clearError() }, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Dismiss", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            // Connection Form Card
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Cluster Connection",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            val isAtlas = uriText.startsWith("mongodb+srv://", ignoreCase = true)
                            AssistChip(
                                onClick = {},
                                label = {
                                    Text(
                                        if (isAtlas) "Atlas SRV" else "Direct IP/Host",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        if (isAtlas) Icons.Default.CloudQueue else Icons.Default.Dns,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = if (isAtlas) EmeraldLight else SkyAccent
                                    )
                                },
                                shape = CircleShape
                            )
                        }

                        // Presets Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SuggestionChip(
                                onClick = {
                                    haptic.performClickFeedback()
                                    val clip = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val text = clip.primaryClip?.getItemAt(0)?.text?.toString() ?: ""
                                    if (text.startsWith("mongodb", ignoreCase = true)) {
                                        uriText = text
                                        Toast.makeText(context, "Pasted from clipboard", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Clipboard does not contain a MongoDB URI", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                label = { Text("Paste URI") },
                                icon = { Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(14.dp)) },
                                shape = CircleShape
                            )

                            SuggestionChip(
                                onClick = {
                                    haptic.performClickFeedback()
                                    uriText = "mongodb+srv://username:password@2nd-alt-ub.9zwqoto.mongodb.net/?appName=2nd-Alt-UB"
                                    clusterName = "2nd-Alt-UB Atlas"
                                },
                                label = { Text("2nd-Alt-UB") },
                                icon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp)) },
                                shape = CircleShape
                            )

                            SuggestionChip(
                                onClick = {
                                    haptic.performClickFeedback()
                                    uriText = "mongodb://10.0.2.2:27017"
                                    clusterName = "Localhost"
                                },
                                label = { Text("Localhost") },
                                icon = { Icon(Icons.Default.Computer, contentDescription = null, modifier = Modifier.size(14.dp)) },
                                shape = CircleShape
                            )
                        }

                        // URI Input Field
                        OutlinedTextField(
                            value = uriText,
                            onValueChange = {
                                uriText = it
                                viewModel.clearPingTest()
                            },
                            label = { Text("MongoDB Connection String") },
                            placeholder = { Text("mongodb+srv://user:pass@cluster.mongodb.net/...") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = false,
                            maxLines = 3,
                            shape = MaterialTheme.shapes.medium,
                            leadingIcon = {
                                Icon(Icons.Default.Link, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            },
                            trailingIcon = {
                                IconButton(onClick = { isUriPasswordVisible = !isUriPasswordVisible }) {
                                    Icon(
                                        imageVector = if (isUriPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (isUriPasswordVisible) "Hide password" else "Show password"
                                    )
                                }
                            },
                            visualTransformation = if (isUriPasswordVisible) VisualTransformation.None else PasswordVisualTransformation()
                        )

                        // Cluster Name Input Field
                        OutlinedTextField(
                            value = clusterName,
                            onValueChange = { clusterName = it },
                            label = { Text("Session Nickname / Alias") },
                            placeholder = { Text("e.g. Production Cluster") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = MaterialTheme.shapes.medium,
                            leadingIcon = {
                                Icon(Icons.Default.Label, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                            }
                        )

                        // Ping Test Diagnostic Banner
                        if (uiState.pingTestResult != null) {
                            val res = uiState.pingTestResult!!
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = MaterialTheme.shapes.medium,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Ping Successful: ${res.pingMs}ms",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Text(
                                            text = "Server v${res.serverVersion} • Handshake Verified",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            }
                        }

                        if (uiState.pingTestError != null) {
                            Surface(
                                color = MaterialTheme.colorScheme.errorContainer,
                                shape = MaterialTheme.shapes.medium,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Ping failed: ${uiState.pingTestError}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }

                        // Save Checkbox and Color Selection
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = saveConnection,
                                onCheckedChange = { saveConnection = it }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Save to Hardware-Encrypted Vault",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        if (saveConnection) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.padding(start = 12.dp)
                            ) {
                                Text(
                                    text = "Tag:",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                colorOptions.forEach { (tag, color) ->
                                    FilterChip(
                                        selected = selectedColorTag == tag,
                                        onClick = { selectedColorTag = tag },
                                        label = { Text(tag.replaceFirstChar { it.uppercase() }) },
                                        leadingIcon = {
                                            Box(
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .clip(CircleShape)
                                                    .background(color)
                                            )
                                        },
                                        shape = CircleShape
                                    )
                                }
                            }
                        }

                        // Actions Row: Ping Test + Connect Button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = {
                                    haptic.performClickFeedback()
                                    viewModel.pingTest(uriText)
                                },
                                enabled = !uiState.isTestingPing && !uiState.isConnecting,
                                shape = CircleShape,
                                modifier = Modifier.weight(1f)
                            ) {
                                if (uiState.isTestingPing) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                } else {
                                    Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                                Text("Ping Test")
                            }

                            Button(
                                onClick = {
                                    haptic.performClickFeedback()
                                    viewModel.connect(
                                        uri = uriText,
                                        name = clusterName,
                                        save = saveConnection,
                                        colorTag = selectedColorTag
                                    )
                                },
                                enabled = !uiState.isConnecting,
                                shape = CircleShape,
                                modifier = Modifier.weight(1.3f)
                            ) {
                                if (uiState.isConnecting) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Connecting...")
                                } else {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Connect", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Saved Sessions Section Header
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Encrypted Vault Sessions",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Badge(containerColor = MaterialTheme.colorScheme.secondaryContainer) {
                        Text(
                            text = "${uiState.savedConnections.size}",
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Saved Sessions List
            if (uiState.savedConnections.isEmpty()) {
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
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.Security,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "No saved sessions in vault",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Save connection profiles to reconnect with one tap",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            } else {
                items(uiState.savedConnections, key = { it.id }) { saved ->
                    val tagColor = when (saved.colorTag) {
                        "sky" -> SkyAccent
                        "amber" -> AmberAccent
                        "purple" -> PurpleAccent
                        "rose" -> RoseAccent
                        else -> EmeraldPrimary
                    }

                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(tagColor)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = saved.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Row {
                                    IconButton(
                                        onClick = {
                                            haptic.performClickFeedback()
                                            itemToEdit = saved
                                            editNameText = saved.name
                                            editUriText = ""
                                            editColorTag = saved.colorTag
                                        },
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = "Edit session",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            haptic.performClickFeedback()
                                            itemToDelete = saved
                                        },
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.DeleteOutline,
                                            contentDescription = "Delete session",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = saved.maskedUri,
                                style = MonospaceCodeStyle.copy(fontSize = 12.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Saved: ${saved.savedAt.take(10)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )

                                FilledTonalButton(
                                    onClick = {
                                        haptic.performClickFeedback()
                                        viewModel.connectSaved(saved)
                                    },
                                    shape = CircleShape,
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Connect", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (itemToDelete != null) {
        val target = itemToDelete!!
        ConfirmDialog(
            title = "Delete Saved Session",
            message = "Are you sure you want to remove '${target.name}' from the encrypted vault?",
            confirmText = "Delete",
            dismissText = "Cancel",
            isDestructive = true,
            onConfirm = {
                viewModel.deleteSaved(target.id)
                itemToDelete = null
            },
            onDismiss = { itemToDelete = null }
        )
    }

    // Edit Session Dialog
    if (itemToEdit != null) {
        val target = itemToEdit!!
        AlertDialog(
            onDismissRequest = { itemToEdit = null },
            title = { Text("Edit Session Profile") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = editNameText,
                        onValueChange = { editNameText = it },
                        label = { Text("Session Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    )

                    OutlinedTextField(
                        value = editUriText,
                        onValueChange = { editUriText = it },
                        label = { Text("Update URI (leave empty to keep current)") },
                        placeholder = { Text("New MongoDB URI...") },
                        singleLine = false,
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    )

                    Text("Color Tag:", style = MaterialTheme.typography.labelMedium)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        colorOptions.forEach { (tag, color) ->
                            FilterChip(
                                selected = editColorTag == tag,
                                onClick = { editColorTag = tag },
                                label = { Text(tag.replaceFirstChar { it.uppercase() }) },
                                leadingIcon = {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                    )
                                },
                                shape = CircleShape
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateSavedConnection(
                            id = target.id,
                            newName = editNameText,
                            newUri = editUriText.ifBlank { null },
                            colorTag = editColorTag
                        )
                        itemToEdit = null
                    },
                    shape = CircleShape
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToEdit = null }) {
                    Text("Cancel")
                }
            },
            shape = MaterialTheme.shapes.extraLarge
        )
    }
}

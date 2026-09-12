package com.mongostudio.app.ui.screens

import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mongostudio.app.data.model.SavedConnection
import com.mongostudio.app.ui.components.*
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
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val themeSettings by viewModel.themePreferences.themeSettings.collectAsStateWithLifecycle()

    var uriText by remember { mutableStateOf("") }
    var clusterName by remember { mutableStateOf("") }
    var isUriPasswordVisible by remember { mutableStateOf(false) }
    var saveConnection by remember { mutableStateOf(true) }

    var showThemeDialog by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<SavedConnection?>(null) }
    var itemToEdit by remember { mutableStateOf<SavedConnection?>(null) }
    var editNameText by remember { mutableStateOf("") }
    var editUriText by remember { mutableStateOf("") }

    LaunchedEffect(uiState.isConnectedToCluster) {
        if (uiState.isConnectedToCluster) {
            onNavigateToDashboard()
        }
    }

    Scaffold(
        topBar = {
            TopHeader(
                title = "MongoDB Studio",
                subtitle = "Wire TLS Client",
                isConnectedToCluster = false,
                isDark = themeSettings.isDarkMode,
                onToggleDayNight = { viewModel.setDarkMode(!themeSettings.isDarkMode) },
                onThemeClick = { showThemeDialog = true },
                onSettingsClick = onNavigateToSettings,
                onRefreshClick = { viewModel.loadSavedConnections() }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        val listState = rememberLazyListState()

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
                // Hero Expressive Card with Signature AsymmetricHeroShape (§5.6)
                item {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = AsymmetricHeroShape,
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(22.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // App Icon Logo Container
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(
                                                    MaterialTheme.colorScheme.primary,
                                                    MaterialTheme.colorScheme.tertiary
                                                )
                                            )
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = Color.White.copy(alpha = 0.35f),
                                            shape = RoundedCornerShape(16.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Storage,
                                        contentDescription = "MongoDB Studio Logo",
                                        tint = Color.White,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }

                                ExpressiveLiveBadge(
                                    label = "Native Wire TLS",
                                    isActive = true,
                                    activeColor = MaterialTheme.colorScheme.primary
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "MongoDB Studio",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Autonomous Android MongoDB manager communicating directly with Atlas and standalone servers via BSON wire protocol with AES-256 hardware vault.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }

                // Error Banner
                item {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = uiState.errorMessage != null,
                        enter = androidx.compose.animation.fadeIn(
                            animationSpec = androidx.compose.animation.core.tween(200)
                        ) + androidx.compose.animation.expandVertically(),
                        exit = androidx.compose.animation.fadeOut(
                            animationSpec = androidx.compose.animation.core.tween(150)
                        ) + androidx.compose.animation.shrinkVertically()
                    ) {
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
                                    text = uiState.errorMessage ?: "",
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
                                            if (isAtlas) "Atlas SRV" else "Direct Host",
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            if (isAtlas) Icons.Default.CloudQueue else Icons.Default.Dns,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = if (isAtlas) MaterialTheme.colorScheme.primary else SkyAccent
                                        )
                                    },
                                    shape = CircleShape
                                )
                            }

                            // Presets Row with Connected Buttons
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
                                        uriText = "mongodb://10.0.2.2:27017"
                                        clusterName = "Localhost Emulator"
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
                                placeholder = { Text("mongodb+srv://user:pass@cluster.net/...") },
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
                                label = { Text("Cluster Alias / Nickname (Optional)") },
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
                                                text = "Server v${res.serverVersion} • Wire TLS Handshake Verified",
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

                            // Save Checkbox
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
                                    text = "Save to Hardware-Encrypted KeyStore Vault",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            // Actions Row with CircularWavySpinner / RadarRippleLoader
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
                                        CircularWavySpinner(
                                            sizeDp = 18.dp,
                                            strokeWidth = 2.dp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                    } else {
                                        Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                    }
                                    Text("Ping Test")
                                }

                                Button(
                                    onClick = {
                                        haptic.performConfirmFeedback()
                                        viewModel.connect(
                                            uri = uriText,
                                            name = clusterName.ifBlank { null },
                                            save = saveConnection
                                        )
                                    },
                                    enabled = !uiState.isConnecting && !uiState.isTestingPing && uriText.isNotBlank(),
                                    shape = CircleShape,
                                    modifier = Modifier.weight(1.3f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                ) {
                                    if (uiState.isConnecting) {
                                        CircularWavySpinner(
                                            sizeDp = 18.dp,
                                            strokeWidth = 2.dp,
                                            color = MaterialTheme.colorScheme.onPrimary
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

                // Saved Sessions List with Swipe-to-Dismiss (§8.5) and StaggerEntrance (§9.5)
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
                    itemsIndexed(
                        items = uiState.savedConnections,
                        key = { _, conn -> conn.id },
                        contentType = { _, _ -> "saved_connection" }
                    ) { index, saved ->
                        StaggerEntrance(index = index, staggerMs = 30L) {
                            val dismissState = rememberSwipeToDismissBoxState(
                                confirmValueChange = { value ->
                                    if (value == SwipeToDismissBoxValue.EndToStart) {
                                        haptic.performConfirmFeedback()
                                        itemToDelete = saved
                                        false // Don't auto-dismiss until confirmed in dialog
                                    } else false
                                }
                            )

                            SwipeToDismissBox(
                                state = dismissState,
                                enableDismissFromStartToEnd = false,
                                backgroundContent = {
                                    val bgCol by animateColorAsState(
                                        targetValue = if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) {
                                            MaterialTheme.colorScheme.errorContainer
                                        } else Color.Transparent,
                                        label = "DismissBg"
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(MaterialTheme.shapes.large)
                                            .background(bgCol)
                                            .padding(end = 20.dp),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    }
                                }
                            ) {
                                ElevatedCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .animateItem(),
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
                                                Surface(
                                                    shape = CircleShape,
                                                    color = MaterialTheme.colorScheme.primaryContainer,
                                                    modifier = Modifier.size(34.dp)
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Icon(
                                                            Icons.Default.Dns,
                                                            contentDescription = null,
                                                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                    }
                                                }
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
            }

            // Velocity-Aware Auto-Hiding Scrollbar (§8.4)
            VelocityAwareScrollbar(
                listState = listState,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(vertical = 8.dp)
            )
        }
    }

    // Appearance & Theme Settings Dialog
    if (showThemeDialog) {
        ThemeSettingsDialog(
            settings = themeSettings,
            onFollowSystemThemeChange = { viewModel.setFollowSystemTheme(it) },
            onDarkModeChange = { viewModel.setDarkMode(it) },
            onAmoledModeChange = { viewModel.setAmoledMode(it) },
            onPalettePresetChange = { viewModel.setPalettePreset(it) },
            onDismiss = { showThemeDialog = false }
        )
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
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateSavedConnection(
                            id = target.id,
                            newName = editNameText,
                            newUri = editUriText.ifBlank { null }
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

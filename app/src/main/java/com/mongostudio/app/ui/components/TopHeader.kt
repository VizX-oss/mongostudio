package com.mongostudio.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mongostudio.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopHeader(
    title: String,
    subtitle: String? = null,
    isServerReachable: Boolean,
    serverPingMs: Long?,
    isConnectedToCluster: Boolean,
    onBackClick: (() -> Unit)? = null,
    onRefreshClick: (() -> Unit)? = null,
    onConsoleClick: (() -> Unit)? = null,
    onSettingsClick: (() -> Unit)? = null,
    onDisconnectClick: (() -> Unit)? = null
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }
        },
        navigationIcon = {
            if (onBackClick != null) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
            }
        },
        actions = {
            // Status Pill
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (isConnectedToCluster) EmeraldContainer else CardDark)
                    .border(1.dp, if (isConnectedToCluster) EmeraldPrimary else CardBorderDark, CircleShape)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isServerReachable) EmeraldPrimary else RoseAccent)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = when {
                            isConnectedToCluster -> "Cluster Live"
                            isServerReachable -> "${serverPingMs ?: 0}ms"
                            else -> "Offline"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isConnectedToCluster) EmeraldLight else TextSecondary,
                        fontSize = 10.sp
                    )
                }
            }

            if (onRefreshClick != null) {
                IconButton(onClick = onRefreshClick) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = TextSecondary)
                }
            }

            if (isConnectedToCluster && onConsoleClick != null) {
                IconButton(onClick = onConsoleClick) {
                    Icon(Icons.Default.Terminal, contentDescription = "Console", tint = EmeraldLight)
                }
            }

            if (onSettingsClick != null) {
                IconButton(onClick = onSettingsClick) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings", tint = TextSecondary)
                }
            }

            if (isConnectedToCluster && onDisconnectClick != null) {
                IconButton(onClick = onDisconnectClick) {
                    Icon(Icons.Default.PowerSettingsNew, contentDescription = "Disconnect", tint = RoseAccent)
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = SurfaceDark
        )
    )
}

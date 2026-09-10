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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mongostudio.app.ui.theme.*

/**
 * Material 3 Expressive Header with status badge, pill actions, and modern hierarchy.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopHeader(
    title: String,
    subtitle: String? = null,
    isConnectedToCluster: Boolean,
    pingMs: Long? = null,
    onBackClick: (() -> Unit)? = null,
    onRefreshClick: (() -> Unit)? = null,
    onConsoleClick: (() -> Unit)? = null,
    onSettingsClick: (() -> Unit)? = null,
    onDisconnectClick: (() -> Unit)? = null
) {
    val haptics = androidx.compose.ui.platform.LocalHapticFeedback.current

    Surface(
        color = SurfaceDark,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Navigation & Title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    if (onBackClick != null) {
                        IconButton(
                            onClick = {
                                haptics.performClickFeedback()
                                onBackClick()
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(SurfaceContainerHigh)
                                .pressMorph()
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = TextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                    }

                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLargeEmphasized,
                            color = TextPrimary,
                            letterSpacing = (-0.3).sp
                        )
                        if (subtitle != null) {
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Actions & Status
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Live Status Badge
                    ExpressiveLiveBadge(
                        label = when {
                            isConnectedToCluster && pingMs != null -> "${pingMs}ms"
                            isConnectedToCluster -> "Live"
                            else -> "Standalone"
                        },
                        isActive = isConnectedToCluster,
                        activeColor = EmeraldLight,
                        inactiveColor = TextMuted
                    )

                    if (onRefreshClick != null) {
                        IconButton(
                            onClick = {
                                haptics.performClickFeedback()
                                onRefreshClick()
                            },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(SurfaceContainerHigh)
                                .pressMorph()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    if (isConnectedToCluster && onConsoleClick != null) {
                        IconButton(
                            onClick = {
                                haptics.performClickFeedback()
                                onConsoleClick()
                            },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(EmeraldContainer)
                                .pressMorph()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Terminal,
                                contentDescription = "Console",
                                tint = EmeraldLight,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    if (onSettingsClick != null) {
                        IconButton(
                            onClick = {
                                haptics.performClickFeedback()
                                onSettingsClick()
                            },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(SurfaceContainerHigh)
                                .pressMorph()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    if (isConnectedToCluster && onDisconnectClick != null) {
                        IconButton(
                            onClick = {
                                haptics.performConfirmFeedback()
                                onDisconnectClick()
                            },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(RoseAccent.copy(alpha = 0.15f))
                                .pressMorph()
                        ) {
                            Icon(
                                imageVector = Icons.Default.PowerSettingsNew,
                                contentDescription = "Disconnect",
                                tint = RoseAccent,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

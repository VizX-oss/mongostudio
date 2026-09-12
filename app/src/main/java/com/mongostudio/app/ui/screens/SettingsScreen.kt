package com.mongostudio.app.ui.screens

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mongostudio.app.ui.components.*
import com.mongostudio.app.ui.theme.*
import com.mongostudio.app.viewmodel.MongoStudioViewModel

@Composable
fun SettingsScreen(
    viewModel: MongoStudioViewModel,
    onNavigateBack: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val themeSettings by viewModel.themePreferences.themeSettings.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    Scaffold(
        topBar = {
            TopHeader(
                title = "Settings",
                subtitle = "Preferences & Engine",
                isConnectedToCluster = uiState.isConnectedToCluster,
                pingMs = uiState.activeClusterPingMs,
                onBackClick = onNavigateBack
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
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
                // Appearance & Theme Card
                item {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.extraLarge,
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.Palette,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Appearance & Theme",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Material 3 Expressive palettes & modes",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                            // Color Palette Selector Row
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    text = "COLOR PALETTE PRESET",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp
                                )

                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    val availablePresets = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                        ColorPalettePreset.entries
                                    } else {
                                        ColorPalettePreset.entries.filter { it != ColorPalettePreset.DYNAMIC }
                                    }

                                    items(availablePresets) { preset ->
                                        val isSelected = themeSettings.palettePreset == preset
                                        val swatchColor = if (preset == ColorPalettePreset.DYNAMIC) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            AppPalettes.getPreset(preset).primaryColor
                                        }

                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier
                                                .clip(MaterialTheme.shapes.medium)
                                                .clickable {
                                                    haptic.performClickFeedback()
                                                    viewModel.setPalettePreset(preset)
                                                }
                                                .padding(6.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(42.dp)
                                                    .clip(CircleShape)
                                                    .background(swatchColor)
                                                    .border(
                                                        width = if (isSelected) 3.dp else 1.dp,
                                                        color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                                        shape = CircleShape
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (isSelected) {
                                                    Icon(
                                                        Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = preset.displayName.split(" ").first(),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                            // 1. Follow System Theme Toggle
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.BrightnessAuto,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "Follow System Theme",
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Match device day / night setting",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Switch(
                                    checked = themeSettings.followSystemTheme,
                                    onCheckedChange = {
                                        haptic.performClickFeedback()
                                        viewModel.setFollowSystemTheme(it)
                                    }
                                )
                            }

                            val isDarkActive = if (themeSettings.followSystemTheme) isSystemInDarkTheme() else themeSettings.isDarkMode

                            // 2. Manual Dark Mode Toggle (visible only when followSystemTheme is OFF)
                            AnimatedVisibility(
                                visible = !themeSettings.followSystemTheme,
                                enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) +
                                        expandVertically(
                                            animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow),
                                            clip = true
                                        ),
                                exit = fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) +
                                       shrinkVertically(
                                           animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow),
                                           clip = true
                                       )
                            ) {
                                Column {
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                    Spacer(modifier = Modifier.height(16.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = if (themeSettings.isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.secondary,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(
                                                    text = "Dark Mode",
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = if (themeSettings.isDarkMode) "Dark theme enabled" else "Light theme enabled",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                        Switch(
                                            checked = themeSettings.isDarkMode,
                                            onCheckedChange = {
                                                haptic.performClickFeedback()
                                                viewModel.setDarkMode(it)
                                            }
                                        )
                                    }
                                }
                            }

                            // 3. AMOLED Mode Toggle (visible whenever dark theme is active, even if followSystemTheme is on)
                            AnimatedVisibility(
                                visible = isDarkActive,
                                enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) +
                                        expandVertically(
                                            animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow),
                                            clip = true
                                        ),
                                exit = fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) +
                                       shrinkVertically(
                                           animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow),
                                           clip = true
                                       )
                            ) {
                                Column {
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Default.Contrast,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.tertiary,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(
                                                    text = "AMOLED Pitch Black",
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = "Pure #000000 black for OLED displays",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                        Switch(
                                            checked = themeSettings.isAmoledMode,
                                            onCheckedChange = {
                                                haptic.performClickFeedback()
                                                viewModel.setAmoledMode(it)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Standalone Engine Card
                item {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        )
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        modifier = Modifier.size(38.dp),
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.Dns, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(20.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "Standalone Architecture",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "100% Native Wire Protocol",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "MongoStudio runs completely autonomous on your Android device. It speaks the native MongoDB wire protocol (BSON / OP_MSG) directly to Atlas and self-hosted instances.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 20.sp
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            val features = listOf(
                                "Direct Wire Protocol over TCP/TLS socket",
                                "Atlas SRV via built-in DNS-over-HTTPS (DoH)",
                                "Embedded SCRAM-SHA-256 & SCRAM-SHA-1 authenticators",
                                "Hardware-backed AES-256 GCM KeyStore Vault",
                                "Zero external backend, proxy, or middleware servers"
                            )

                            features.forEach { feature ->
                                Row(
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(feature, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }
                    }
                }

                // About Card
                item {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        )
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(20.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "MongoDB Studio",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Version 1.0.0 • Material 3 Expressive",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Native Android client for MongoDB cluster administration built with Jetpack Compose.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            VelocityAwareScrollbar(
                listState = listState,
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }
    }
}

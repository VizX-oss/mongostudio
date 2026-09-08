package com.mongostudio.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
fun SettingsScreen(
    viewModel: MongoStudioViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopHeader(
                title = "Settings",
                subtitle = "Architecture & Engine",
                isConnectedToCluster = uiState.isConnectedToCluster,
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
            // Standalone Engine Card
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(EmeraldContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Dns, contentDescription = null, tint = EmeraldVibrant, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Standalone Architecture",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "100% Native Wire Protocol",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = EmeraldLight
                                    )
                                }
                            }

                            ExpressiveTag(
                                text = "ZERO NODE.JS",
                                containerColor = SurfaceContainerHigh,
                                contentColor = CyanAccent
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "MongoStudio runs completely autonomous on your Android device. It speaks the native MongoDB wire protocol (BSON / OP_MSG) directly to Atlas and self-hosted instances.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Features list
                        val features = listOf(
                            "Direct Wire Protocol over TCP/TLS socket",
                            "Atlas SRV via built-in DNS-over-HTTPS (DoH)",
                            "Embedded SCRAM-SHA-256 & SCRAM-SHA-1 authenticators",
                            "Local AES-256 PBKDF2 Encrypted Vault",
                            "Zero external backend, proxy, or middleware servers"
                        )

                        features.forEach { feature ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 3.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(EmeraldContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = EmeraldLight, modifier = Modifier.size(12.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(feature, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                            }
                        }
                    }
                }
            }

            // Connection Status Card
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(SkyAccent.copy(alpha = 0.16f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Speed, contentDescription = null, tint = SkyAccent, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Cluster Health",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }

                            ExpressiveLiveBadge(
                                label = if (uiState.isConnectedToCluster) "Connected" else "Idle",
                                isActive = uiState.isConnectedToCluster,
                                activeColor = EmeraldLight,
                                inactiveColor = TextMuted
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        if (uiState.isConnectedToCluster) {
                            Text("Active Cluster: ${uiState.activeClusterName}", color = TextPrimary, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("MongoDB Version: v${uiState.activeClusterVersion ?: "Unknown"}", color = EmeraldLight, style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Socket Latency: ${uiState.activeClusterPingMs ?: 0}ms", color = TextSecondary, style = MaterialTheme.typography.labelSmall)

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = { viewModel.disconnect() },
                                colors = ButtonDefaults.buttonColors(containerColor = RoseAccent, contentColor = Color.White),
                                shape = PillShape,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.PowerSettingsNew, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Disconnect from Cluster", fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Text("Not connected to any cluster", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Connect directly from the main screen using a mongodb:// or mongodb+srv:// URI.", color = TextMuted, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            // About Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(SquircleMedium)
                        .background(SurfaceContainer)
                        .border(1.dp, CardBorderDark, SquircleMedium)
                        .padding(18.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(PurpleAccent.copy(alpha = 0.16f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = PurpleAccent, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "About MongoStudio",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Native Android client for MongoDB cluster administration built with Material 3 Expressive and Jetpack Compose.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ExpressiveTag(text = "MD3 Expressive", containerColor = SurfaceContainerHigh, contentColor = EmeraldLight)
                            ExpressiveTag(text = "Kotlin 2.0", containerColor = SurfaceContainerHigh, contentColor = SkyAccent)
                            ExpressiveTag(text = "Driver 4.8.2", containerColor = SurfaceContainerHigh, contentColor = AmberAccent)
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

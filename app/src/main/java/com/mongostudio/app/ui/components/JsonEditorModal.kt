package com.mongostudio.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.mongostudio.app.ui.theme.*

@Composable
fun JsonEditorModal(
    title: String,
    initialJson: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    confirmButtonText: String = "Push to DB",
    isLoading: Boolean = false
) {
    var text by remember { mutableStateOf(initialJson) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val prettyGson = remember { GsonBuilder().setPrettyPrinting().create() }

    fun validate(): Boolean {
        return try {
            JsonParser.parseString(text)
            errorMessage = null
            true
        } catch (e: Exception) {
            errorMessage = "Invalid JSON: ${e.localizedMessage ?: "syntax error"}"
            false
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundDark.copy(alpha = 0.95f))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.92f),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderDark),
                shape = MaterialTheme.shapes.large
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleLarge,
                                color = TextPrimary
                            )
                            Text(
                                text = "Direct Push Editor",
                                style = MaterialTheme.typography.labelSmall,
                                color = EmeraldLight
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Toolbar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = {
                                try {
                                    val element = JsonParser.parseString(text)
                                    text = prettyGson.toJson(element)
                                    errorMessage = null
                                } catch (e: Exception) {
                                    errorMessage = "Cannot format: ${e.message}"
                                }
                            },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderDark)
                        ) {
                            Icon(Icons.Default.AutoFixHigh, contentDescription = null, tint = EmeraldLight, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Format JSON", style = MaterialTheme.typography.labelSmall, color = TextPrimary)
                        }
                    }

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            color = RoseAccent.copy(alpha = 0.15f),
                            shape = MaterialTheme.shapes.small,
                            border = androidx.compose.foundation.BorderStroke(1.dp, RoseAccent.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = errorMessage!!,
                                color = RoseAccent,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Editor Field
                    OutlinedTextField(
                        value = text,
                        onValueChange = {
                            text = it
                            if (errorMessage != null) validate()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        textStyle = MonospaceCodeStyle.copy(fontSize = 13.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = CardDark,
                            unfocusedContainerColor = CardDark,
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = CardBorderDark,
                            cursorColor = EmeraldLight
                        ),
                        shape = MaterialTheme.shapes.medium,
                        placeholder = { Text("Enter valid JSON document...", style = MonospaceCodeStyle, color = TextMuted) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel", color = TextSecondary)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(
                            onClick = {
                                if (validate()) {
                                    onSave(text)
                                }
                            },
                            enabled = !isLoading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EmeraldPrimary,
                                contentColor = TextOnPrimary
                            ),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = TextOnPrimary, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Pushing...", color = TextOnPrimary)
                            } else {
                                Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(confirmButtonText, color = TextOnPrimary)
                            }
                        }
                    }
                }
            }
        }
    }
}

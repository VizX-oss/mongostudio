package com.mongostudio.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.gson.*
import com.mongostudio.app.ui.theme.*

data class VisualField(
    var key: String,
    var value: String,
    var type: String // "string", "number", "boolean", "json"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JsonEditorModal(
    title: String,
    initialJson: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    confirmButtonText: String = "Push to DB",
    isLoading: Boolean = false
) {
    val haptic = LocalHapticFeedback.current
    var text by remember { mutableStateOf(initialJson) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Raw JSON, 1: Visual Fields
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val prettyGson = remember { GsonBuilder().setPrettyPrinting().serializeNulls().create() }

    // Visual fields list parsed from JSON
    val visualFields = remember { mutableStateListOf<VisualField>() }

    fun syncFromRawToVisual() {
        try {
            val element = JsonParser.parseString(text)
            if (element.isJsonObject) {
                visualFields.clear()
                val obj = element.asJsonObject
                for ((k, v) in obj.entrySet()) {
                    val (type, valStr) = when {
                        v.isJsonPrimitive && v.asJsonPrimitive.isBoolean -> "boolean" to v.asBoolean.toString()
                        v.isJsonPrimitive && v.asJsonPrimitive.isNumber -> "number" to v.asNumber.toString()
                        v.isJsonPrimitive && v.asJsonPrimitive.isString -> "string" to v.asString
                        v.isJsonNull -> "null" to "null"
                        else -> "json" to v.toString()
                    }
                    visualFields.add(VisualField(k, valStr, type))
                }
            }
            errorMessage = null
        } catch (e: Exception) {
            errorMessage = "Invalid JSON: ${e.localizedMessage ?: "syntax error"}"
        }
    }

    fun syncFromVisualToRaw() {
        val rootObj = JsonObject()
        for (field in visualFields) {
            if (field.key.isBlank()) continue
            try {
                when (field.type) {
                    "number" -> {
                        val num = field.value.toDoubleOrNull()
                        if (num != null) {
                            if (num % 1.0 == 0.0) rootObj.addProperty(field.key, num.toLong())
                            else rootObj.addProperty(field.key, num)
                        } else {
                            rootObj.addProperty(field.key, field.value)
                        }
                    }
                    "boolean" -> rootObj.addProperty(field.key, field.value.toBoolean())
                    "null" -> rootObj.add(field.key, JsonNull.INSTANCE)
                    "json" -> {
                        val parsedSub = try { JsonParser.parseString(field.value) } catch (e: Exception) { JsonPrimitive(field.value) }
                        rootObj.add(field.key, parsedSub)
                    }
                    else -> rootObj.addProperty(field.key, field.value)
                }
            } catch (e: Exception) {
                rootObj.addProperty(field.key, field.value)
            }
        }
        text = prettyGson.toJson(rootObj)
    }

    LaunchedEffect(initialJson) {
        try {
            val elem = JsonParser.parseString(initialJson)
            text = prettyGson.toJson(elem)
            syncFromRawToVisual()
        } catch (e: Exception) {
            text = initialJson
        }
    }

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
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainer,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.CloudUpload,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Material Expressive Document Editor",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Mode Selector Tabs (Visual vs Code)
                PrimaryTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.clip(CircleShape)
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = {
                            haptic.performClickFeedback()
                            if (selectedTab == 1) syncFromVisualToRaw()
                            selectedTab = 0
                        },
                        text = { Text("Code (Raw JSON)") },
                        icon = { Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = {
                            haptic.performClickFeedback()
                            if (validate()) {
                                syncFromRawToVisual()
                                selectedTab = 1
                            }
                        },
                        text = { Text("Visual Fields") },
                        icon = { Icon(Icons.Default.AccountTree, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Action Toolbar (Format, Validate, Add Field)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (selectedTab == 0) {
                        SuggestionChip(
                            onClick = {
                                try {
                                    val element = JsonParser.parseString(text)
                                    text = prettyGson.toJson(element)
                                    errorMessage = null
                                } catch (e: Exception) {
                                    errorMessage = "Cannot format: ${e.message}"
                                }
                            },
                            label = { Text("Format / Prettify") },
                            icon = { Icon(Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(14.dp)) },
                            shape = CircleShape
                        )

                        SuggestionChip(
                            onClick = {
                                if (validate()) {
                                    errorMessage = null
                                }
                            },
                            label = { Text("Validate Syntax") },
                            icon = { Icon(Icons.Default.CheckCircleOutline, contentDescription = null, modifier = Modifier.size(14.dp)) },
                            shape = CircleShape
                        )
                    } else {
                        Button(
                            onClick = {
                                visualFields.add(VisualField("new_field", "", "string"))
                            },
                            shape = CircleShape,
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add Field")
                        }

                        Text(
                            text = "${visualFields.size} fields",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Error Banner if invalid
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorMessage!!,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Editor Content Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    if (selectedTab == 0) {
                        // Raw JSON Code Area
                        OutlinedTextField(
                            value = text,
                            onValueChange = {
                                text = it
                                if (errorMessage != null) validate()
                            },
                            modifier = Modifier.fillMaxSize(),
                            textStyle = MonospaceCodeStyle.copy(fontSize = 13.sp),
                            shape = MaterialTheme.shapes.medium,
                            placeholder = { Text("Enter valid JSON document...", style = MonospaceCodeStyle) }
                        )
                    } else {
                        // Visual Tree Editor List
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            itemsIndexed(visualFields) { index, field ->
                                ElevatedCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = MaterialTheme.shapes.medium,
                                    colors = CardDefaults.elevatedCardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            OutlinedTextField(
                                                value = field.key,
                                                onValueChange = {
                                                    field.key = it
                                                    syncFromVisualToRaw()
                                                },
                                                label = { Text("Key") },
                                                singleLine = true,
                                                enabled = field.key != "_id",
                                                modifier = Modifier.weight(1f),
                                                shape = MaterialTheme.shapes.small
                                            )

                                            Spacer(modifier = Modifier.width(8.dp))

                                            // Type Selector
                                            val types = listOf("string", "number", "boolean", "json")
                                            var typeDropdownExpanded by remember { mutableStateOf(false) }
                                            Box {
                                                FilterChip(
                                                    selected = true,
                                                    onClick = { typeDropdownExpanded = true },
                                                    label = { Text(field.type.uppercase(), style = MaterialTheme.typography.labelSmall) },
                                                    shape = CircleShape
                                                )
                                                DropdownMenu(
                                                    expanded = typeDropdownExpanded,
                                                    onDismissRequest = { typeDropdownExpanded = false }
                                                ) {
                                                    types.forEach { t ->
                                                        DropdownMenuItem(
                                                            text = { Text(t.uppercase()) },
                                                            onClick = {
                                                                field.type = t
                                                                typeDropdownExpanded = false
                                                                syncFromVisualToRaw()
                                                            }
                                                        )
                                                    }
                                                }
                                            }

                                            if (field.key != "_id") {
                                                IconButton(
                                                    onClick = {
                                                        visualFields.removeAt(index)
                                                        syncFromVisualToRaw()
                                                    }
                                                ) {
                                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete Field", tint = MaterialTheme.colorScheme.error)
                                                }
                                            }
                                        }

                                        OutlinedTextField(
                                            value = field.value,
                                            onValueChange = {
                                                field.value = it
                                                syncFromVisualToRaw()
                                            },
                                            label = { Text("Value") },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = MaterialTheme.shapes.small
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                AnimatedVisibility(visible = isLoading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp))
                }

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            haptic.performClickFeedback()
                            onDismiss()
                        }
                    ) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Button(
                        onClick = {
                            haptic.performClickFeedback()
                            if (selectedTab == 1) syncFromVisualToRaw()
                            if (validate()) {
                                onSave(text)
                            }
                        },
                        enabled = !isLoading,
                        shape = CircleShape
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(confirmButtonText, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

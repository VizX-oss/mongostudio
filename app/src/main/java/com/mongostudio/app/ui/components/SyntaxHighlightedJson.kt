package com.mongostudio.app.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.mongostudio.app.ui.theme.*

private val prettyGson: Gson = GsonBuilder().setPrettyPrinting().serializeNulls().create()

object JsonSyntaxHighlighter {

    fun formatAndHighlight(data: Any?): AnnotatedString {
        val jsonStr = try {
            if (data is String) {
                val parsed = prettyGson.fromJson(data, Any::class.java)
                prettyGson.toJson(parsed)
            } else {
                prettyGson.toJson(data)
            }
        } catch (e: Exception) {
            data?.toString() ?: "null"
        }
        return highlightJson(jsonStr)
    }

    fun highlightJson(json: String): AnnotatedString {
        return buildAnnotatedString {
            var inString = false
            var isKey = false
            val currentToken = StringBuilder()
            var i = 0

            while (i < json.length) {
                val c = json[i]

                if (c == '"') {
                    if (inString) {
                        currentToken.append(c)
                        inString = false
                        val text = currentToken.toString()

                        var j = i + 1
                        while (j < json.length && json[j].isWhitespace()) j++
                        isKey = j < json.length && json[j] == ':'

                        val color = if (isKey) JsonKeyColor else JsonStringColor
                        val start = length
                        append(text)
                        addStyle(SpanStyle(color = color), start, length)
                        currentToken.clear()
                    } else {
                        inString = true
                        currentToken.append(c)
                    }
                    i++
                    continue
                }

                if (inString) {
                    currentToken.append(c)
                    if (c == '\\' && i + 1 < json.length) {
                        i++
                        currentToken.append(json[i])
                    }
                    i++
                    continue
                }

                when {
                    c in listOf('{', '}', '[', ']') -> {
                        val start = length
                        append(c)
                        addStyle(SpanStyle(color = JsonBracketColor), start, length)
                    }
                    c == ':' || c == ',' -> {
                        append(c)
                    }
                    c.isDigit() || c == '-' -> {
                        val numStart = i
                        while (i < json.length && (json[i].isDigit() || json[i] in listOf('.', 'e', 'E', '-', '+'))) {
                            i++
                        }
                        val numStr = json.substring(numStart, i)
                        val start = length
                        append(numStr)
                        addStyle(SpanStyle(color = JsonNumberColor), start, length)
                        continue
                    }
                    json.startsWith("true", i) -> {
                        val start = length
                        append("true")
                        addStyle(SpanStyle(color = JsonBooleanColor), start, length)
                        i += 4
                        continue
                    }
                    json.startsWith("false", i) -> {
                        val start = length
                        append("false")
                        addStyle(SpanStyle(color = JsonBooleanColor), start, length)
                        i += 5
                        continue
                    }
                    json.startsWith("null", i) -> {
                        val start = length
                        append("null")
                        addStyle(SpanStyle(color = JsonNullColor), start, length)
                        i += 4
                        continue
                    }
                    else -> {
                        append(c)
                    }
                }
                i++
            }
        }
    }
}

@Composable
fun JsonViewerCard(
    data: Any?,
    modifier: Modifier = Modifier,
    maxCollapsedLines: Int = 8,
    canCopy: Boolean = true
) {
    val context = LocalContext.current
    var isExpanded by remember { mutableStateOf(false) }
    val formattedJson = remember(data) {
        try {
            if (data is String) prettyGson.toJson(prettyGson.fromJson(data, Any::class.java))
            else prettyGson.toJson(data)
        } catch (e: Exception) {
            data?.toString() ?: "null"
        }
    }
    val highlightedText = remember(data) { JsonSyntaxHighlighter.formatAndHighlight(data) }
    val lineCount = remember(formattedJson) { formattedJson.lines().size }
    val needsExpansion = lineCount > maxCollapsedLines

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(SquircleSmall)
            .background(SurfaceContainerHigh)
            .border(1.dp, CardBorderDark, SquircleSmall)
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "BSON DOCUMENT",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontSize = 10.sp,
                    letterSpacing = 0.8.sp
                )
                if (canCopy) {
                    Box(
                        modifier = Modifier
                            .clip(PillShape)
                            .background(SurfaceContainerHighest)
                            .clickable {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("JSON Document", formattedJson))
                                Toast.makeText(context, "Copied BSON to clipboard", Toast.LENGTH_SHORT).show()
                            }
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy JSON",
                                tint = EmeraldLight,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy", style = MaterialTheme.typography.labelSmall, color = EmeraldLight, fontSize = 11.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            SelectionContainer {
                Text(
                    text = highlightedText,
                    style = MonospaceCodeStyle,
                    maxLines = if (isExpanded || !needsExpansion) Int.MAX_VALUE else maxCollapsedLines,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }

            if (needsExpansion) {
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(PillShape)
                        .background(SurfaceContainerHighest)
                        .clickable { isExpanded = !isExpanded }
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = EmeraldLight,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isExpanded) "Collapse" else "Show All (${lineCount} lines)",
                            style = MaterialTheme.typography.labelSmall,
                            color = EmeraldLight,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

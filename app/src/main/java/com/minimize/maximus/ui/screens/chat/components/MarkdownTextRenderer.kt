package com.minimize.maximus.ui.screens.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minimize.maximus.ui.theme.Matter

/**
 * Rich Markdown Renderer designed for Compose to render AI fitness & nutrition advice.
 */
@Composable
fun RichMarkdownMessage(
    content: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val blocks = remember(content) { parseMarkdownBlocks(content) }
    val textColor = MaterialTheme.colorScheme.onSurface

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        blocks.forEach { block ->
            when (block) {
                is MarkdownBlock.Heading -> {
                    val (style, size, topSpace) = when (block.level) {
                        1 -> Triple(FontWeight.Black, 20.sp, 6.dp)
                        2 -> Triple(FontWeight.Black, 18.sp, 5.dp)
                        3 -> Triple(FontWeight.Bold, 16.sp, 4.dp)
                        else -> Triple(FontWeight.Bold, 14.5.sp, 2.dp)
                    }

                    if (topSpace > 0.dp) {
                        Spacer(Modifier.height(topSpace))
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (block.level <= 2) {
                            Box(
                                modifier = Modifier
                                    .width(3.5.dp)
                                    .height(18.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(accentColor)
                            )
                        }
                        Text(
                            text = parseInlineMarkdown(block.text, textColor, accentColor),
                            fontSize = size,
                            fontWeight = style,
                            color = if (block.level <= 2) accentColor else textColor,
                            lineHeight = (size.value + 6).sp
                        )
                    }
                }

                is MarkdownBlock.BulletItem -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(top = 7.dp)
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(accentColor)
                        )
                        Text(
                            text = parseInlineMarkdown(block.text, textColor, accentColor),
                            style = MaterialTheme.typography.bodyMedium,
                            color = textColor,
                            lineHeight = 20.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                is MarkdownBlock.NumberedItem -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = accentColor.copy(alpha = 0.15f),
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Text(
                                text = block.number,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = accentColor,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = parseInlineMarkdown(block.text, textColor, accentColor),
                            style = MaterialTheme.typography.bodyMedium,
                            color = textColor,
                            lineHeight = 20.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                is MarkdownBlock.Divider -> {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                is MarkdownBlock.Callout -> {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = accentColor.copy(alpha = 0.08f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (block.title.isNotBlank()) {
                                Text(
                                    text = parseInlineMarkdown(block.title, accentColor, accentColor),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp,
                                    color = accentColor
                                )
                            }
                            Text(
                                text = parseInlineMarkdown(block.body, textColor, accentColor),
                                style = MaterialTheme.typography.bodySmall,
                                color = textColor,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }

                is MarkdownBlock.Paragraph -> {
                    Text(
                        text = parseInlineMarkdown(block.text, textColor, accentColor),
                        style = MaterialTheme.typography.bodyMedium,
                        color = textColor,
                        lineHeight = 21.sp
                    )
                }
            }
        }
    }
}

// ── BLOCK PARSER ─────────────────────────────────────────────────────────

sealed interface MarkdownBlock {
    data class Heading(val level: Int, val text: String) : MarkdownBlock
    data class BulletItem(val text: String) : MarkdownBlock
    data class NumberedItem(val number: String, val text: String) : MarkdownBlock
    object Divider : MarkdownBlock
    data class Callout(val title: String, val body: String) : MarkdownBlock
    data class Paragraph(val text: String) : MarkdownBlock
}

private fun parseMarkdownBlocks(raw: String): List<MarkdownBlock> {
    val lines = raw.lines()
    val blocks = mutableListOf<MarkdownBlock>()
    var i = 0

    while (i < lines.size) {
        val line = lines[i].trimEnd()
        val trimmed = line.trim()

        if (trimmed.isEmpty()) {
            i++
            continue
        }

        // Horizontal Rule (---, ***, ___)
        if (trimmed.matches(Regex("^(---|- - -|\\*\\*\\*|___)$"))) {
            blocks.add(MarkdownBlock.Divider)
            i++
            continue
        }

        // Callout Block (> quote)
        if (trimmed.startsWith(">")) {
            val calloutBody = trimmed.removePrefix(">").trim()
            blocks.add(MarkdownBlock.Callout(title = "", body = calloutBody))
            i++
            continue
        }

        // Headings (#, ##, ###, ####)
        val headingMatch = Regex("^(#{1,6})\\s+(.*)$").find(trimmed)
        if (headingMatch != null) {
            val level = headingMatch.groupValues[1].length
            val headingText = headingMatch.groupValues[2]

            // Check if heading looks like a callout e.g. "### 💡 Coach's Direct Order"
            if (headingText.contains("💡") || headingText.contains("⚠️") || headingText.contains("Coach's Direct Order", ignoreCase = true)) {
                // Collect any immediately following paragraph as body of callout
                var body = ""
                if (i + 1 < lines.size && !lines[i + 1].trim().startsWith("#") && !lines[i + 1].trim().startsWith("*") && !lines[i + 1].trim().startsWith("---")) {
                    body = lines[i + 1].trim()
                    i++
                }
                blocks.add(MarkdownBlock.Callout(title = headingText, body = body))
            } else {
                blocks.add(MarkdownBlock.Heading(level = level, text = headingText))
            }
            i++
            continue
        }

        // Bullet Items (* , - , + , • )
        val bulletMatch = Regex("^([*+\\-•])\\s+(.*)$").find(trimmed)
        if (bulletMatch != null) {
            blocks.add(MarkdownBlock.BulletItem(text = bulletMatch.groupValues[2]))
            i++
            continue
        }

        // Numbered Items (1. , 2. )
        val numberedMatch = Regex("^(\\d+[.)])\\s+(.*)$").find(trimmed)
        if (numberedMatch != null) {
            blocks.add(
                MarkdownBlock.NumberedItem(
                    number = numberedMatch.groupValues[1],
                    text = numberedMatch.groupValues[2]
                )
            )
            i++
            continue
        }

        // Default Paragraph
        blocks.add(MarkdownBlock.Paragraph(text = trimmed))
        i++
    }

    return blocks
}

// ── INLINE SPAN PARSER ───────────────────────────────────────────────────

/**
 * Parses bold (**text**), italic (*text* or _text_), and inline code (`code`) into AnnotatedString.
 */
fun parseInlineMarkdown(
    text: String,
    baseColor: Color,
    accentColor: Color
): AnnotatedString {
    return buildAnnotatedString {
        // Regex pattern matches:
        // Group 1 & 2: **bold**
        // Group 3 & 4: `code`
        // Group 5 & 6: *italic* or _italic_
        val pattern = Regex("(\\*\\*(.*?)\\*\\*)|(`(.*?)`)|((\\*|_)(.*?)\\6)")
        var currentIndex = 0

        pattern.findAll(text).forEach { matchResult ->
            val range = matchResult.range
            if (range.first > currentIndex) {
                append(text.substring(currentIndex, range.first))
            }

            when {
                // **bold**
                matchResult.groups[2] != null -> {
                    val boldText = matchResult.groups[2]!!.value
                    val start = length
                    append(boldText)
                    addStyle(
                        SpanStyle(
                            fontWeight = FontWeight.Black,
                            fontFamily = Matter,
                            color = baseColor
                        ),
                        start,
                        length
                    )
                }

                // `inline code`
                matchResult.groups[4] != null -> {
                    val codeText = matchResult.groups[4]!!.value
                    val start = length
                    append(codeText)
                    addStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        ),
                        start,
                        length
                    )
                }

                // *italic*
                matchResult.groups[7] != null -> {
                    val italicText = matchResult.groups[7]!!.value
                    val start = length
                    append(italicText)
                    addStyle(
                        SpanStyle(
                            fontStyle = FontStyle.Italic,
                            fontFamily = Matter
                        ),
                        start,
                        length
                    )
                }
            }

            currentIndex = range.last + 1
        }

        if (currentIndex < text.length) {
            append(text.substring(currentIndex))
        }
    }
}

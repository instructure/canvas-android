/*
 * Copyright (C) 2026 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.instructure.student.features.pages.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.instructure.pandautils.compose.CanvasTheme

class ExplainBottomSheet : BottomSheetDialogFragment() {

    var statusText by mutableStateOf("")
    var explanationText by mutableStateOf("")
    var isLoading by mutableStateOf(true)
    var selectedText by mutableStateOf("")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                CanvasTheme {
                    ExplainContent(
                        selectedText = selectedText,
                        statusText = statusText,
                        explanationText = explanationText,
                        isLoading = isLoading,
                    )
                }
            }
        }
    }

    companion object {
        const val TAG = "ExplainBottomSheet"
    }
}

@Composable
private fun ExplainContent(
    selectedText: String,
    statusText: String,
    explanationText: String,
    isLoading: Boolean,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = com.instructure.pandautils.R.drawable.ic_ai_sparkle),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = colorResource(id = com.instructure.pandares.R.color.textDarkest),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(com.instructure.pandares.R.string.aiExplanation),
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = colorResource(id = com.instructure.pandares.R.color.textDarkest),
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "\"$selectedText\"",
            fontStyle = FontStyle.Italic,
            fontSize = 14.sp,
            color = colorResource(id = com.instructure.pandares.R.color.textDark),
            maxLines = 4,
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (explanationText.isNotBlank()) {
            val codeBackground = colorResource(id = com.instructure.pandares.R.color.backgroundLight)
            val textColor = colorResource(id = com.instructure.pandares.R.color.textDarkest)
            val annotated = remember(explanationText) { parseMarkdown(explanationText, codeBackground, textColor) }
            Text(
                text = annotated,
                fontSize = 15.sp,
                color = textColor,
                lineHeight = 22.sp,
            )

            if (isLoading) {
                Spacer(modifier = Modifier.height(8.dp))
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = colorResource(id = com.instructure.pandares.R.color.textDark),
                )
            } else {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(com.instructure.pandares.R.string.aiExplanationDisclaimer),
                    fontSize = 12.sp,
                    color = colorResource(id = com.instructure.pandares.R.color.textDark),
                    fontStyle = FontStyle.Italic,
                )
            }
        } else if (isLoading) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = colorResource(id = com.instructure.pandares.R.color.textDark),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = statusText,
                    fontSize = 14.sp,
                    color = colorResource(id = com.instructure.pandares.R.color.textDark),
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

private fun parseMarkdown(text: String, codeBackground: Color, textColor: Color): AnnotatedString {
    return buildAnnotatedString {
        val lines = text.lines()
        lines.forEachIndexed { lineIndex, line ->
            if (lineIndex > 0) append("\n")

            when {
                line.startsWith("### ") -> {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 15.sp)) {
                        appendInlineMarkdown(line.removePrefix("### "), codeBackground)
                    }
                }
                line.startsWith("## ") -> {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp)) {
                        appendInlineMarkdown(line.removePrefix("## "), codeBackground)
                    }
                }
                line.startsWith("# ") -> {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 17.sp)) {
                        appendInlineMarkdown(line.removePrefix("# "), codeBackground)
                    }
                }
                line.matches(Regex("""^[-*]\s+.+""")) -> {
                    append("  \u2022 ")
                    appendInlineMarkdown(line.replaceFirst(Regex("""^[-*]\s+"""), ""), codeBackground)
                }
                line.matches(Regex("""^\d+[.)]\s+.+""")) -> {
                    val prefix = Regex("""^(\d+[.)]\s+)""").find(line)?.value ?: ""
                    append("  $prefix")
                    appendInlineMarkdown(line.removePrefix(prefix), codeBackground)
                }
                else -> {
                    appendInlineMarkdown(line, codeBackground)
                }
            }
        }
    }
}

private fun AnnotatedString.Builder.appendInlineMarkdown(text: String, codeBackground: Color) {
    val regex = Regex("""(\*\*\*(.+?)\*\*\*|\*\*(.+?)\*\*|\*(.+?)\*|`(.+?)`)""")
    var lastIndex = 0

    for (match in regex.findAll(text)) {
        if (match.range.first > lastIndex) {
            append(text.substring(lastIndex, match.range.first))
        }

        when {
            match.groups[2] != null -> {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic)) {
                    append(match.groups[2]!!.value)
                }
            }
            match.groups[3] != null -> {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(match.groups[3]!!.value)
                }
            }
            match.groups[4] != null -> {
                withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                    append(match.groups[4]!!.value)
                }
            }
            match.groups[5] != null -> {
                withStyle(SpanStyle(fontFamily = FontFamily.Monospace, background = codeBackground)) {
                    append(match.groups[5]!!.value)
                }
            }
        }

        lastIndex = match.range.last + 1
    }

    if (lastIndex < text.length) {
        append(text.substring(lastIndex))
    }
}

/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
 *
 */
package com.instructure.horizon.features.notebook.common.composable

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.features.notebook.common.model.NotebookType
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import kotlin.math.min

@Composable
fun NotebookHighlightedText(
    text: String,
    type: NotebookType?,
    maxLines: Int? = null,
    modifier: Modifier = Modifier
) {
    var lineCount = 1
    val lineList = mutableListOf<Float>()
    val highlightColor = type?.highlightColor?.let { colorResource(type.highlightColor) }
    val lineColor = type?.lineColor?.let { colorResource(type.lineColor) }
    val dashedEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
    val pathEffect = if (type == NotebookType.Confusing) dashedEffect else null

    Text(
        text = text,
        style = HorizonTypography.p1,
        color = HorizonColors.Text.body(),
        maxLines = maxLines ?: Int.MAX_VALUE,
        overflow = TextOverflow.Ellipsis,
        onTextLayout = { textLayoutResult ->
            lineCount = if (maxLines == null) textLayoutResult.lineCount else min(textLayoutResult.lineCount, maxLines)
            for(i in 0 until  lineCount) {
                lineList.add(textLayoutResult.getLineRight(i))
            }
        },
        modifier = modifier
            .drawWithContent {
                if (highlightColor != null && lineColor != null) {
                    val strokeWidth = 1.dp.toPx()
                    val lineHeight = size.height / lineCount
                    for (i in 1..lineCount) {
                        val verticalOffset = i * lineHeight
                        val lineWidth = lineList[i - 1]

                        drawLine(
                            color = highlightColor,
                            strokeWidth = lineHeight,
                            start = Offset(0f, verticalOffset - lineHeight / 2 + i * strokeWidth),
                            end = Offset(lineWidth, verticalOffset - lineHeight / 2 + i * strokeWidth)
                        )

                        drawLine(
                            color = highlightColor,
                            strokeWidth = strokeWidth,
                            start = Offset(0f, verticalOffset + i * strokeWidth + strokeWidth / 2),
                            end = Offset(lineWidth, verticalOffset + i * strokeWidth + strokeWidth / 2),
                            pathEffect = null
                        )

                        drawLine(
                            color = lineColor,
                            strokeWidth = strokeWidth,
                            start = Offset(0f, verticalOffset + i * strokeWidth + strokeWidth / 2),
                            end = Offset(lineWidth, verticalOffset + i * strokeWidth + strokeWidth / 2),
                            pathEffect = pathEffect
                        )
                    }
                }
                drawContent()
            }
    )
}

@Composable
@Preview
private fun NotebookHighlightedTextImportantPreview() {
    ContextKeeper.appContext = LocalContext.current
    NotebookHighlightedText(
        text = "This is an important note",
        type = NotebookType.Important
    )
}

@Composable
@Preview
private fun NotebookHighlightedTextConfusingPreview() {
    ContextKeeper.appContext = LocalContext.current
    NotebookHighlightedText(
        text = "This is a confusing note",
        type = NotebookType.Confusing
    )
}

@Composable
@Preview
private fun NotebookHighlightedTextNoLabelPreview() {
    ContextKeeper.appContext = LocalContext.current
    NotebookHighlightedText(
        text = "This is a not selected note",
        type = null
    )
}

@Composable
@Preview
private fun NotebookHighlightedMultilineTextConfusingPreview() {
    ContextKeeper.appContext = LocalContext.current
    NotebookHighlightedText(
        text = "This is a confusing note\nthat spans multiple lines\n and a short",
        type = NotebookType.Confusing
    )
}
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

import androidx.compose.foundation.background
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.features.notebook.common.model.NotebookType
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.pandautils.compose.modifiers.conditional

@Composable
fun NotebookHighlightedText(
    text: String,
    type: NotebookType?,
) {
    val lineColor = type?.color?.let { colorResource(type.color) }
    Text(
        text = text,
        style = HorizonTypography.p1,
        color = HorizonColors.Text.body(),
        modifier = Modifier
            .conditional(lineColor != null) {
                background(lineColor!!.copy(alpha = 0.2f))
            }
            .drawBehind {
                if (lineColor != null) {
                    val strokeWidthPx = 1.dp.toPx()
                    val verticalOffset = size.height - 2.sp.toPx()
                    drawLine(
                        color = lineColor,
                        strokeWidth = strokeWidthPx,
                        start = Offset(0f, verticalOffset),
                        end = Offset(size.width, verticalOffset)
                    )
                }
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
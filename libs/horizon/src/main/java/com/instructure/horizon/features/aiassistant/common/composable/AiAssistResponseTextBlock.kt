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
package com.instructure.horizon.features.aiassistant.common.composable

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.horizon.horizonui.molecules.TextLink
import com.instructure.horizon.horizonui.molecules.TextLinkColor

data class AiAssistResponseTextBlockSource(
    val label: String,
    val url: String,
)

@Composable
fun AiAssistResponseTextBlock(
    text: String,
    sources: List<AiAssistResponseTextBlockSource>,
    selectedFeedback: AiAssistFeedbackType?,
    onSourceSelected: (AiAssistResponseTextBlockSource) -> Unit,
    onPositiveFeedbackSelected: () -> Unit,
    onNegativeFeedbackSelected: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
    ) {
        Text(
            text = text,
            style = HorizonTypography.p1,
            color = HorizonColors.Text.surfaceColored(),
        )

        HorizonSpace(SpaceSize.SPACE_8)

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.horizontalScroll(rememberScrollState())
        ) {
            sources.forEach { source ->
                TextLink(
                    text = source.label,
                    textLinkColor = TextLinkColor.Beige,
                    onClick = { onSourceSelected(source) },
                )

                if (source != sources.last()) {
                    HorizonSpace(SpaceSize.SPACE_4)

                    VerticalDivider(
                        thickness = 1.dp,
                        color = HorizonColors.Text.surfaceColored()
                    )

                    HorizonSpace(SpaceSize.SPACE_4)
                }
            }
        }

        HorizonSpace(SpaceSize.SPACE_8)

        AiAssistFeedback(
            selected = selectedFeedback,
            onPositiveFeedbackSelected = onPositiveFeedbackSelected,
            onNegativeFeedbackSelected = onNegativeFeedbackSelected
        )
    }
}

@Composable
@Preview
private fun AiAssistResponseTextBlockPreview() {
    AiAssistResponseTextBlock(
        text = "This is a sample response text block.",
        sources = listOf(
            AiAssistResponseTextBlockSource("Source 1", "https://example.com/1"),
            AiAssistResponseTextBlockSource("Source 2", "https://example.com/2"),
        ),
        selectedFeedback = null,
        onSourceSelected = {},
        onPositiveFeedbackSelected = {},
        onNegativeFeedbackSelected = {},
    )
}
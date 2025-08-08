/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package com.instructure.horizon.horizonui.molecules

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.foundation.HorizonBorder
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonCornerRadius
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.pandautils.compose.modifiers.conditional
import com.instructure.pandautils.compose.modifiers.ifNotNull

data class StatusChipState(
    val label: String,
    val color: StatusChipColor,
    val fill: Boolean = false,
    val iconRes: Int? = null
)

sealed class StatusChipColor(val contentColor: Color, val fillColor: Color = Color.Transparent, val borderColor: Color? = null) {
    data object Grey : StatusChipColor(
        contentColor = HorizonColors.Text.title(),
        fillColor = HorizonColors.PrimitivesGrey.grey11()
    )

    data object Green : StatusChipColor(
        contentColor = HorizonColors.PrimitivesGreen.green82(),
        fillColor = HorizonColors.PrimitivesGreen.green12()
    )

    data object Honey : StatusChipColor(
        contentColor = HorizonColors.PrimitivesHoney.honey90(),
        fillColor = HorizonColors.PrimitivesHoney.honey12()
    )

    data object WhiteWithBorder : StatusChipColor(
        contentColor = HorizonColors.Text.title(),
        borderColor = HorizonColors.LineAndBorder.lineStroke()
    )

    data object White : StatusChipColor(
        contentColor = HorizonColors.Text.title(),
        fillColor = HorizonColors.Surface.pageSecondary()
    )
}

@Composable
fun StatusChip(
    state: StatusChipState,
    modifier: Modifier = Modifier
) {
    val horizontalPadding = if (state.iconRes != null) 8.dp else 12.dp
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .conditional(state.fill) {
                background(color = state.color.fillColor, shape = HorizonCornerRadius.level1)
            }
            .ifNotNull(state.color.borderColor) { borderColor ->
                border(HorizonBorder.level1(borderColor), HorizonCornerRadius.level1)
            }
            .padding(horizontal = horizontalPadding, vertical = 2.dp)
    ) {
        if (state.iconRes != null) {
            Icon(
                painter = painterResource(state.iconRes),
                contentDescription = null,
                modifier = Modifier
                    .size(16.dp)
                    .padding(end = 4.dp),
                tint = state.color.contentColor
            )
        }
        Text(state.label, style = HorizonTypography.p2, color = state.color.contentColor)
    }
}

@Composable
@Preview(showBackground = true)
private fun StatusChipPreviewGreen() {
    StatusChipPreview(
        color = StatusChipColor.Green,
        iconRes = R.drawable.check_circle_full
    )
}

@Composable
@Preview(showBackground = true)
private fun StatusChipPreviewGrey() {
    StatusChipPreview(
        color = StatusChipColor.Grey,
        iconRes = R.drawable.add
    )
}

@Composable
@Preview(showBackground = true)
private fun StatusChipPreviewHoney() {
    StatusChipPreview(
        color = StatusChipColor.Honey,
        iconRes = R.drawable.add
    )
}

@Composable
@Preview(showBackground = true)
private fun StatusChipPreviewWhiteWithBorder() {
    StatusChipPreview(
        color = StatusChipColor.WhiteWithBorder,
        iconRes = R.drawable.add
    )
}

@Composable
@Preview(showBackground = true)
private fun StatusChipPreviewWhite() {
    StatusChipPreview(
        color = StatusChipColor.White,
        iconRes = R.drawable.add
    )
}

@Composable
private fun StatusChipPreview(color: StatusChipColor, iconRes: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        StatusChip(
            state = StatusChipState(
                label = "Sample Status",
                color = color,
                fill = true,
                iconRes = iconRes
            )
        )

        StatusChip(
            state = StatusChipState(
                label = "Sample Status",
                color = color,
                fill = false,
                iconRes = iconRes
            )
        )
        StatusChip(
            state = StatusChipState(
                label = "Sample Status",
                color = color,
                fill = true,
                iconRes = null
            )
        )

        StatusChip(
            state = StatusChipState(
                label = "Sample Status",
                color = color,
                fill = false,
                iconRes = null
            )
        )
    }
}
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
package com.instructure.horizon.features.aiassistant.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.SpaceSize

enum class AiAssistFeedbackType {
    POSITIVE,
    NEGATIVE
}

@Composable
fun AiAssistFeedback(
    selected: AiAssistFeedbackType?,
    onPositiveFeedbackSelected: () -> Unit,
    onNegativeFeedbackSelected: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AiAssistPositiveFeedbackIcon(
            selected = selected == AiAssistFeedbackType.POSITIVE,
            onClick = onPositiveFeedbackSelected,
        )

        HorizonSpace(SpaceSize.SPACE_8)

        AiAssistNegativeFeedbackIcon(
            selected = selected == AiAssistFeedbackType.NEGATIVE,
            onClick = onNegativeFeedbackSelected,
        )
    }
}

@Composable
private fun AiAssistPositiveFeedbackIcon(
    selected: Boolean,
    onClick: () -> Unit,
) {
    Icon(
        painter = if (selected) {
            painterResource(R.drawable.thumb_up_filled)
        } else {
            painterResource(R.drawable.thumb_up)
        },
        contentDescription = stringResource(R.string.a11y_aiAssistPositiveFeedback),
        tint = HorizonColors.Text.surfaceColored(),
        modifier = Modifier.clickable { onClick() },
    )
}

@Composable
private fun AiAssistNegativeFeedbackIcon(
    selected: Boolean,
    onClick: () -> Unit,
) {
    Icon(
        painter = if (selected) {
            painterResource(R.drawable.thumb_down_filled)
        } else {
            painterResource(R.drawable.thumb_down)
        },
        contentDescription = stringResource(R.string.a11y_aiAssistNegativeFeedback),
        tint = HorizonColors.Text.surfaceColored(),
        modifier = Modifier.clickable { onClick() },
    )
}

@Composable
@Preview
private fun AiAssistFeedbackUnselectedPreview() {
    AiAssistFeedback(
        selected = null,
        onPositiveFeedbackSelected = {},
        onNegativeFeedbackSelected = {},
    )
}

@Composable
@Preview
private fun AiAssistFeedbackPositivePreview() {
    AiAssistFeedback(
        selected = AiAssistFeedbackType.POSITIVE,
        onPositiveFeedbackSelected = {},
        onNegativeFeedbackSelected = {},
    )
}

@Composable
@Preview
private fun AiAssistFeedbackNegativedPreview() {
    AiAssistFeedback(
        selected = AiAssistFeedbackType.NEGATIVE,
        onPositiveFeedbackSelected = {},
        onNegativeFeedbackSelected = {},
    )
}
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
package com.instructure.horizon.features.aiassistant.quiz

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.instructure.horizon.R
import com.instructure.horizon.features.aiassistant.common.AiAssistFeedback
import com.instructure.horizon.features.aiassistant.common.AiAssistFeedbackType
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.horizon.horizonui.molecules.Button
import com.instructure.horizon.horizonui.molecules.ButtonColor
import com.instructure.horizon.horizonui.molecules.ButtonWidth

@Composable
fun AiAssistQuizRatingFooter(
    selectedFeedback: AiAssistFeedbackType?,
    onPositiveFeedbackSelected: () -> Unit,
    onNegativeFeedbackSelected: () -> Unit,
    onRegenerateSelected: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "How was this question?",
                style = HorizonTypography.buttonTextLarge,
                color = HorizonColors.Text.surfaceColored(),
            )

            Spacer(modifier = Modifier.weight(1f))

            AiAssistFeedback(
                selected = selectedFeedback,
                onPositiveFeedbackSelected = onPositiveFeedbackSelected,
                onNegativeFeedbackSelected = onNegativeFeedbackSelected,
            )
        }

        HorizonSpace(SpaceSize.SPACE_16)

        Button(
            label = stringResource(R.string.aiAssistQuizRateRegenrateLabel),
            color = ButtonColor.Inverse,
            width = ButtonWidth.FILL,
            onClick = onRegenerateSelected,
        )
    }
}
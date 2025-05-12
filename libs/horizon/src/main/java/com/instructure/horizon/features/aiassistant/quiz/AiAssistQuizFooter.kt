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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.horizon.horizonui.molecules.Button
import com.instructure.horizon.horizonui.molecules.ButtonColor
import com.instructure.horizon.horizonui.molecules.ButtonWidth

@Composable
fun AiAssistQuizFooter(
    checkButtonEnabled: Boolean,
    onCheckAnswerSelected: () -> Unit,
    onRegenerateSelected: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
    ) {
        Button(
            label = stringResource(R.string.aiAssistCheckAnswerLabel),
            color = ButtonColor.Inverse,
            width = ButtonWidth.FILL,
            onClick = onCheckAnswerSelected,
            enabled = checkButtonEnabled,
        )

        HorizonSpace(SpaceSize.SPACE_12)

        Button(
            label = stringResource(R.string.aiAssistRegenerateLabel),
            color = ButtonColor.Black,
            width = ButtonWidth.FILL,
            onClick = onRegenerateSelected,
        )
    }
}

@Composable
@Preview
private fun AiAssistQuizFooterPreview() {
    AiAssistQuizFooter(
        checkButtonEnabled = true,
        onCheckAnswerSelected = {},
        onRegenerateSelected = {},
        modifier = Modifier,
    )
}
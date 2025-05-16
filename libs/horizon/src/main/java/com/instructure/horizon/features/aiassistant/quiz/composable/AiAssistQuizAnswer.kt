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
package com.instructure.horizon.features.aiassistant.quiz.composable

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.foundation.HorizonBorder
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonCornerRadius
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.pandautils.compose.modifiers.conditional

enum class AiAssistQuizAnswerStatus {
    UNSELECTED,
    SELECTED,
    CORRECT,
    INCORRECT,
}
@Composable
fun AiAssistQuizAnswer(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    status: AiAssistQuizAnswerStatus = AiAssistQuizAnswerStatus.UNSELECTED,
) {
    AnimatedContent(
        status,
        label = "AiAssistQuizAnswer",
    ){ status ->
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier
                .clip(HorizonCornerRadius.level2)
                .clickable { onClick() }
                .conditional(status != AiAssistQuizAnswerStatus.UNSELECTED) {
                    this
                        .background(HorizonColors.Surface.cardPrimary())
                }
                .conditional(status == AiAssistQuizAnswerStatus.UNSELECTED) {
                    this
                        .border(
                            HorizonBorder.level1(HorizonColors.Surface.pageSecondary()),
                            shape = HorizonCornerRadius.level2,
                        )
                }
                .padding(16.dp)
        ) {
            Text(
                text = text,
                style = HorizonTypography.p2,
                color = when (status) {
                    AiAssistQuizAnswerStatus.UNSELECTED -> HorizonColors.Text.surfaceColored()
                    AiAssistQuizAnswerStatus.SELECTED -> HorizonColors.Text.body()
                    AiAssistQuizAnswerStatus.CORRECT -> HorizonColors.Text.success()
                    AiAssistQuizAnswerStatus.INCORRECT -> HorizonColors.Text.error()
                },
                modifier = Modifier.weight(1f)
            )

            when (status) {
                AiAssistQuizAnswerStatus.UNSELECTED -> {}
                AiAssistQuizAnswerStatus.SELECTED -> Icon(
                    painter = painterResource(R.drawable.check),
                    contentDescription = stringResource(R.string.a11y_selected),
                    tint = HorizonColors.Icon.default(),
                    modifier = Modifier.size(20.dp)
                )

                AiAssistQuizAnswerStatus.CORRECT -> Icon(
                    painter = painterResource(R.drawable.check_circle),
                    contentDescription = stringResource(R.string.a11y_correct),
                    tint = HorizonColors.Icon.success(),
                    modifier = Modifier.size(20.dp)
                )

                AiAssistQuizAnswerStatus.INCORRECT -> Icon(
                    painter = painterResource(R.drawable.cancel),
                    contentDescription = stringResource(R.string.a11y_incorrect),
                    tint = HorizonColors.Icon.error(),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
@Preview
private fun AiAssistQuizAnswerUnselectedPreview() {
    AiAssistQuizAnswer(
        text = "This is a quiz answer",
        status = AiAssistQuizAnswerStatus.UNSELECTED,
        onClick = {}
    )
}

@Composable
@Preview
private fun AiAssistQuizAnswerSelectedPreview() {
    AiAssistQuizAnswer(
        text = "This is a quiz answer",
        status = AiAssistQuizAnswerStatus.SELECTED,
        onClick = {}
    )
}

@Composable
@Preview
private fun AiAssistQuizAnswerCorrectPreview() {
    AiAssistQuizAnswer(
        text = "This is a quiz answer",
        status = AiAssistQuizAnswerStatus.CORRECT,
        onClick = {}
    )
}

@Composable
@Preview
private fun AiAssistQuizAnswerIncorrectPreview() {
    AiAssistQuizAnswer(
        text = "This is a quiz answer",
        status = AiAssistQuizAnswerStatus.INCORRECT,
        onClick = {}
    )
}
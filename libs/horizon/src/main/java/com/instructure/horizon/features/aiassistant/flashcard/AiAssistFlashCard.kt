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
package com.instructure.horizon.features.aiassistant.flashcard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonCornerRadius
import com.instructure.horizon.horizonui.foundation.HorizonTypography

@Composable
fun AiAssistFlashCard(
    question: String,
    answer: String,
    isFlippedToAnswer: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val flipAnimation by animateFloatAsState(
        targetValue = if (isFlippedToAnswer) 180f else 0f,
        label = "FlipAnimation",
        animationSpec = tween(durationMillis = 1000)
    )
    val zAxisDistance = 20f

    val cardModifier = modifier
        .fillMaxSize()
        .clickable { onClick() }
        .graphicsLayer {
            rotationY = flipAnimation
            cameraDistance = zAxisDistance
        }
    Box {
        if (flipAnimation >= 90f) {
            AnswerContent(
                answerText = answer,
                modifier = cardModifier
                    .scale(scaleX = -1f, scaleY = 1f)
            )
        } else {
            QuestionContent(
                questionText = question,
                modifier = cardModifier
            )
        }
    }
}

@Composable
private fun QuestionContent(
    questionText: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(HorizonCornerRadius.level2)
            .background(Color.White.copy(alpha = 0.1f))
            .padding(24.dp)
    ) {
        Text(
            text = stringResource(R.string.aiAsistFlashcardQuestionTitle),
            style = HorizonTypography.p1,
            color = HorizonColors.Text.surfaceColored(),
        )

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = questionText,
            style = HorizonTypography.sh3,
            color = HorizonColors.Text.surfaceColored(),
        )

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = stringResource(R.string.aiAssistFlashcardTapToFlip),
            style = HorizonTypography.labelSmall,
            color = HorizonColors.Text.surfaceColored(),
        )
    }
}

@Composable
private fun AnswerContent(
    answerText: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(HorizonCornerRadius.level2)
            .background(HorizonColors.Surface.cardPrimary())
            .padding(24.dp)
    ) {
        Text(
            text = stringResource(R.string.aiAsistFlashcardAnswerTitle),
            style = HorizonTypography.p1,
            color = HorizonColors.Text.body(),
        )

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = answerText,
            style = HorizonTypography.sh3,
            color = HorizonColors.Text.body(),
        )

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = stringResource(R.string.aiAssistFlashcardTapToFlip),
            style = HorizonTypography.labelSmall,
            color = HorizonColors.Text.body()
        )
    }
}

@Composable
@Preview
private fun AiAssistFlashCardQuestionPreview() {
    AiAssistFlashCard(
        question = "What is the capital of France?",
        answer = "Paris",
        isFlippedToAnswer = false,
        onClick = {},
        modifier = Modifier.padding(16.dp)
    )
}

@Composable
@Preview
private fun AiAssistFlashCardAnswerPreview() {
    AiAssistFlashCard(
        question = "What is the capital of France?",
        answer = "Paris",
        isFlippedToAnswer = true,
        onClick = {},
    )
}
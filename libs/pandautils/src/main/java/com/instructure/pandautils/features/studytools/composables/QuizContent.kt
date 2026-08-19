/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
package com.instructure.pandautils.features.studytools.composables

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.Preview
import com.instructure.instui.compose.container.Card
import com.instructure.instui.compose.indicator.Icon as InstUIIcon
import com.instructure.instui.compose.indicator.IconColor
import com.instructure.instui.compose.indicator.IconSize
import com.instructure.instui.compose.input.Button
import com.instructure.instui.token.component.InstUISharedTokens
import com.instructure.instui.token.component.InstUIText as InstUITextTokens
import com.instructure.instui.token.icon.InstUIIcons
import com.instructure.instui.token.icon.check
import com.instructure.instui.token.icon.circleCheck
import com.instructure.instui.token.icon.xCircle
import com.instructure.instui.token.semantic.InstUILayoutSizes
import com.instructure.instui.token.semantic.InstUISemanticColors
import com.instructure.pandautils.R
import com.instructure.pandautils.domain.models.studytools.QuizQuestion
import com.instructure.pandautils.domain.models.studytools.StudyToolResult
import com.instructure.instui.compose.text.Text as InstUIText

/**
 * One question of [quiz] at a time. All questions arrive in a single response — there is no
 * per-question network call — so "next question" is pure client-side navigation over data already
 * in hand, kept as local Compose state rather than on [com.instructure.pandautils.features.studytools.StudyToolsViewModel].
 * Every reselect of the tool passes through [com.instructure.pandautils.features.studytools.StudyToolState.Generating]
 * first, which unmounts this composable, so progress naturally resets on regenerate with no extra
 * keying needed.
 */
@Composable
fun QuizContent(
    quiz: StudyToolResult.Quiz,
    modifier: Modifier = Modifier
) {
    var currentQuestionIndex by rememberSaveable { mutableIntStateOf(0) }
    // A restored index can outrun a shorter regenerated quiz's question list (e.g. process death on
    // question 5 of 6, restored later against a freshly-generated 3-question quiz) - coerced back
    // into range instead of bailing into a blank body under the header. StudyToolsRepositoryImpl
    // already guarantees a non-empty list, so this early return only guards coerceIn from an empty
    // range, not a reachable "no questions" case.
    if (quiz.questions.isEmpty()) return
    currentQuestionIndex = currentQuestionIndex.coerceIn(quiz.questions.indices)
    val currentQuestion = quiz.questions[currentQuestionIndex]
    // Keyed on the question index so a question change resets these for free.
    var selectedAnswerIndex by rememberSaveable(currentQuestionIndex) { mutableStateOf<Int?>(null) }
    var checked by rememberSaveable(currentQuestionIndex) { mutableStateOf(false) }
    val isLastQuestion = currentQuestionIndex == quiz.questions.lastIndex

    Column(
        verticalArrangement = Arrangement.spacedBy(InstUILayoutSizes.Spacing.SpaceMd.spaceMd),
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = InstUILayoutSizes.Spacing.SpaceLg.spaceLg,
                vertical = InstUILayoutSizes.Spacing.SpaceSm.spaceSm
            )
            .testTag("studyToolsQuiz")
    ) {
        InstUIText(
            text = stringResource(
                id = R.string.studyTools_quizQuestionProgress,
                currentQuestionIndex + 1,
                quiz.questions.size
            ),
            style = InstUITextTokens.contentSmall,
            color = InstUISemanticColors.Text.muted(),
            modifier = Modifier.testTag("studyToolsQuizProgress")
        )

        InstUIText(
            text = currentQuestion.question,
            style = InstUITextTokens.contentImportant,
            color = InstUISemanticColors.Text.base(),
            modifier = Modifier.testTag("studyToolsQuizQuestion")
        )

        // Landscape has less vertical room to scroll through, so pairs of answers share a row
        // instead of stacking one per line - the server always returns exactly 4 options per
        // question, so a plain chunked(2) is enough without a general grid component.
        val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
        val answerRows = currentQuestion.answers.withIndex().let { indexed ->
            if (isLandscape) indexed.chunked(2) else indexed.map { listOf(it) }
        }

        Column(verticalArrangement = Arrangement.spacedBy(InstUILayoutSizes.Spacing.SpaceSm.spaceSm)) {
            answerRows.forEach { row ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(InstUILayoutSizes.Spacing.SpaceSm.spaceSm),
                    // Equalizes both cells' height within a pair so one answer wrapping to a second
                    // line doesn't leave its neighbor looking short.
                    modifier = Modifier.height(IntrinsicSize.Max)
                ) {
                    row.forEach { (index, answer) ->
                        val status = currentQuestion.answerStatus(index, selectedAnswerIndex, checked)

                        QuizAnswerRow(
                            text = answer,
                            status = status,
                            selected = index == selectedAnswerIndex,
                            enabled = !checked,
                            onClick = { selectedAnswerIndex = index },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .testTag("studyToolsQuizAnswer_$index")
                        )
                    }
                }
            }
        }

        if (checked && isLastQuestion) {
            // liveRegion: every other question keeps focus on the action button as its label
            // changes, so the state change is heard - the last question removes the button
            // entirely instead, and without this the focused node just disappears with nothing
            // announced. Matches RegenerateError/StudyToolsSkeleton in StudyToolContent.kt.
            InstUIText(
                text = stringResource(id = R.string.studyTools_quizCompleted),
                style = InstUITextTokens.contentSmall,
                color = InstUISemanticColors.Text.muted(),
                modifier = Modifier
                    .semantics { liveRegion = LiveRegionMode.Polite }
                    .testTag("studyToolsQuizCompleted")
            )
        } else {
            Button(
                text = stringResource(
                    id = if (checked) R.string.studyTools_nextQuestion else R.string.studyTools_checkAnswer
                ),
                onClick = {
                    if (checked) {
                        currentQuestionIndex++
                    } else {
                        checked = true
                    }
                },
                enabled = if (checked) true else selectedAnswerIndex != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("studyToolsQuizActionButton")
            )
        }
    }
}

private fun QuizQuestion.answerStatus(
    index: Int,
    selectedAnswerIndex: Int?,
    checked: Boolean
): QuizAnswerStatus = when {
    !checked -> if (index == selectedAnswerIndex) QuizAnswerStatus.SELECTED else QuizAnswerStatus.UNSELECTED
    index == correctAnswerIndex -> QuizAnswerStatus.CORRECT
    index == selectedAnswerIndex -> QuizAnswerStatus.INCORRECT
    else -> QuizAnswerStatus.UNSELECTED
}

private enum class QuizAnswerStatus { UNSELECTED, SELECTED, CORRECT, INCORRECT }

/**
 * A single, mutually-exclusive answer option.
 */
@Composable
private fun QuizAnswerRow(
    text: String,
    status: QuizAnswerStatus,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (status == QuizAnswerStatus.UNSELECTED) {
        Color.Transparent
    } else {
        InstUISemanticColors.Background.container()
    }
    // Every status keeps a visible, status-tinted border - it must not disappear once an answer is
    // picked or checked, only change color to reflect selected/correct/incorrect.
    val borderColor = when (status) {
        QuizAnswerStatus.UNSELECTED -> InstUISemanticColors.Stroke.muted()
        QuizAnswerStatus.SELECTED -> InstUISemanticColors.Stroke.base()
        QuizAnswerStatus.CORRECT -> InstUISemanticColors.Stroke.success()
        QuizAnswerStatus.INCORRECT -> InstUISemanticColors.Stroke.error()
    }
    val textColor = when (status) {
        QuizAnswerStatus.UNSELECTED, QuizAnswerStatus.SELECTED -> InstUISemanticColors.Text.base()
        QuizAnswerStatus.CORRECT -> InstUISemanticColors.Text.success()
        QuizAnswerStatus.INCORRECT -> InstUISemanticColors.Text.error()
    }
    // Correctness is a separate axis from selection - announced through stateDescription, not
    // through `selected`, so the row the user actually picked is what TalkBack calls "selected"
    // regardless of whether it turned out right or wrong.
    val stateLabel = when (status) {
        QuizAnswerStatus.SELECTED -> stringResource(R.string.a11y_selected)
        QuizAnswerStatus.CORRECT -> stringResource(R.string.studyTools_quizAnswerCorrectContentDescription)
        QuizAnswerStatus.INCORRECT -> stringResource(R.string.studyTools_quizAnswerIncorrectContentDescription)
        QuizAnswerStatus.UNSELECTED -> null
    }

    Card(
        backgroundColor = backgroundColor,
        borderColor = borderColor,
        // Clipped before the selectable so its ripple is bounded to the card's rounded corners
        // instead of spilling into a rectangle - Card itself clips to the same shape internally.
        modifier = modifier
            .clip(RoundedCornerShape(InstUISharedTokens.BorderRadius.Card.md))
            .selectable(
                selected = selected,
                enabled = enabled,
                role = Role.RadioButton,
                onClick = onClick
            )
            .semantics { stateLabel?.let { stateDescription = it } }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            InstUIText(
                text = text,
                style = InstUITextTokens.content,
                color = textColor,
                modifier = Modifier.weight(1f)
            )

            when (status) {
                QuizAnswerStatus.SELECTED -> InstUIIcon(
                    imageVector = InstUIIcons.check,
                    contentDescription = null,
                    size = IconSize.Small,
                    color = IconColor.Base
                )

                QuizAnswerStatus.CORRECT -> InstUIIcon(
                    imageVector = InstUIIcons.circleCheck,
                    contentDescription = null,
                    size = IconSize.Small,
                    color = IconColor.Success
                )

                QuizAnswerStatus.INCORRECT -> InstUIIcon(
                    imageVector = InstUIIcons.xCircle,
                    contentDescription = null,
                    size = IconSize.Small,
                    color = IconColor.Error
                )

                QuizAnswerStatus.UNSELECTED -> Unit
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun QuizContentPreview() {
    QuizContent(
        quiz = StudyToolResult.Quiz(
            listOf(
                QuizQuestion(
                    question = "What process do plants use to convert light into energy?",
                    answers = listOf("Respiration", "Photosynthesis", "Fermentation", "Transpiration"),
                    correctAnswerIndex = 1
                )
            )
        )
    )
}

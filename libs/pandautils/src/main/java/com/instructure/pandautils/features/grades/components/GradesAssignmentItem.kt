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

package com.instructure.pandautils.features.grades.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import android.content.res.Configuration
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.instui.compose.InstUITheme
import com.instructure.canvasapi2.utils.NumberHelper
import com.instructure.instui.compose.LocalCourseColor
import com.instructure.instui.compose.indicator.Pill
import com.instructure.instui.compose.input.TextInput
import com.instructure.instui.compose.input.TextInputSize
import com.instructure.instui.compose.list.ListItem
import com.instructure.instui.compose.list.ListItemLeading
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.composables.CheckpointItem
import com.instructure.pandautils.compose.composables.SubmissionState
import com.instructure.pandautils.compose.composables.SubmissionStateLabel
import com.instructure.pandautils.designsystem.DesignSystem
import com.instructure.pandautils.designsystem.LocalDesignSystem
import com.instructure.pandautils.features.grades.AssignmentUiState
import com.instructure.pandautils.features.grades.GradesAction
import com.instructure.pandautils.utils.DisplayGrade
import com.instructure.pandautils.utils.drawableId
import com.instructure.pandautils.utils.orDefault
import com.instructure.instui.compose.text.Text as InstUIText

@Composable
fun GradesAssignmentItem(
    uiState: AssignmentUiState,
    actionHandler: (GradesAction) -> Unit,
    contextColor: Int,
    showWhatIfScore: Boolean,
    modifier: Modifier = Modifier,
) {
    when (LocalDesignSystem.current) {
        DesignSystem.Legacy -> LegacyAssignmentItem(uiState, actionHandler, contextColor, showWhatIfScore, modifier)
        DesignSystem.InstUI -> InstUIAssignmentItem(uiState, actionHandler, showWhatIfScore, modifier)
    }
}

// region InstUI

@Composable
private fun InstUIAssignmentItem(
    uiState: AssignmentUiState,
    actionHandler: (GradesAction) -> Unit,
    showWhatIfScore: Boolean,
    modifier: Modifier = Modifier,
) {
    val courseColor = LocalCourseColor.current
    val pillData = mapSubmissionStateToPill(uiState.submissionStateLabel)
    val gradeText = uiState.displayGrade.text
    val hasWhatIfScore = showWhatIfScore && uiState.whatIfScore != null
    val displayScore = if (hasWhatIfScore) {
        val whatIf = NumberHelper.formatDecimal(uiState.whatIfScore, 2, true)
        val max = NumberHelper.formatDecimal(uiState.maxScore.orDefault(), 2, true)
        "$whatIf/$max"
    } else {
        gradeText
    }

    Column(modifier = modifier.fillMaxWidth()) {
        ListItem(
            title = uiState.name,
            subtext1 = uiState.dueDate,
            leading = ListItemLeading.Icon(mapAssignmentIcon(uiState.iconRes)),
            pill = pillData?.let { data ->
                { Pill(text = data.text, variant = data.variant) }
            },
            score = if (displayScore.isNotEmpty()) {
                {
                    InstUIText(
                        text = displayScore,
                        color = courseColor,
                        style = com.instructure.instui.token.component.InstUIText.contentImportant,
                    )
                }
            } else null,
            onClick = { actionHandler(GradesAction.AssignmentClick(uiState.id)) },
        )
        if (showWhatIfScore) {
            WhatIfScoreField(
                uiState = uiState,
                actionHandler = actionHandler,
            )
        }
    }
}

@Composable
private fun WhatIfScoreField(
    uiState: AssignmentUiState,
    actionHandler: (GradesAction) -> Unit,
) {
    var text by remember(uiState.id, uiState.whatIfScore) {
        mutableStateOf(uiState.whatIfScore?.let { NumberHelper.formatDecimal(it, 2, true) }.orEmpty())
    }
    val maxText = NumberHelper.formatDecimal(uiState.maxScore.orDefault(), 2, true)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        InstUIText(
            text = stringResource(R.string.whatIfScoreLabel),
            style = com.instructure.instui.token.component.InstUIText.content,
            modifier = Modifier.weight(1f),
        )
        TextInput(
            value = text,
            onValueChange = { newValue ->
                text = newValue
                val parsed = newValue.toDoubleOrNull()
                actionHandler(GradesAction.UpdateWhatIfScore(uiState.id, parsed))
            },
            size = TextInputSize.Small,
            placeholder = "-",
            textAlign = TextAlign.End,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            modifier = Modifier
                .width(96.dp)
                .semantics { testTag = "whatIfScoreField" }
        )
        Spacer(modifier = Modifier.width(8.dp))
        InstUIText(
            text = "/$maxText",
            style = com.instructure.instui.token.component.InstUIText.content,
        )
    }
}

// endregion

// region Legacy

@Composable
private fun whatIfTextColor(hasWhatIfScore: Boolean): Color =
    colorResource(if (hasWhatIfScore) R.color.textLightest else R.color.textDarkest)

@Composable
private fun whatIfSecondaryTextColor(hasWhatIfScore: Boolean): Color =
    colorResource(if (hasWhatIfScore) R.color.textLightest else R.color.textDark)

@Composable
private fun whatIfIconTint(hasWhatIfScore: Boolean, contextColor: Int): Color =
    if (hasWhatIfScore) colorResource(R.color.textLightest) else Color(contextColor)

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LegacyAssignmentItem(
    uiState: AssignmentUiState,
    actionHandler: (GradesAction) -> Unit,
    contextColor: Int,
    showWhatIfScore: Boolean,
    modifier: Modifier = Modifier,
) {
    val hasWhatIfScore = showWhatIfScore && uiState.whatIfScore != null

    val iconRotation by animateFloatAsState(
        targetValue = if (uiState.checkpointsExpanded) 180f else 0f,
        label = "expandedIconRotation"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = if (hasWhatIfScore) Color(color = contextColor) else Color.Transparent
            )
            .clickable { actionHandler(GradesAction.AssignmentClick(uiState.id)) }
            .padding(start = 12.dp, top = 12.dp, bottom = 12.dp)
            .semantics {
                role = Role.Button
                testTag = "assignmentItem"
            }
    ) {
        Row(modifier = Modifier.weight(1f)) {
            Spacer(modifier = Modifier.width(12.dp))
            Icon(
                painter = painterResource(id = uiState.iconRes),
                contentDescription = null,
                tint = whatIfIconTint(hasWhatIfScore, contextColor),
                modifier = Modifier
                    .size(24.dp)
                    .semantics { drawableId = uiState.iconRes }
            )
            Spacer(modifier = Modifier.width(18.dp))
            Column {
                Text(
                    text = uiState.name,
                    color = whatIfTextColor(hasWhatIfScore),
                    fontSize = 16.sp
                )
                if (uiState.checkpoints.isNotEmpty()) {
                    uiState.checkpoints.forEach {
                        Text(
                            text = it.dueDate,
                            color = whatIfSecondaryTextColor(hasWhatIfScore),
                            fontSize = 14.sp,
                            modifier = Modifier.testTag("assignmentDueDate")
                        )
                    }
                    SubmissionState(
                        submissionStateLabel = uiState.submissionStateLabel,
                        testTag = "submissionStateLabel",
                        colorOverride = if (hasWhatIfScore) R.color.textLightest else null
                    )
                } else {
                    FlowRow {
                        Text(
                            text = uiState.dueDate,
                            color = whatIfSecondaryTextColor(hasWhatIfScore),
                            fontSize = 14.sp,
                            modifier = Modifier.testTag("assignmentDueDate")
                        )
                        if (uiState.submissionStateLabel != SubmissionStateLabel.None) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Box(
                                Modifier
                                    .height(16.dp)
                                    .width(1.dp)
                                    .clip(RoundedCornerShape(1.dp))
                                    .background(
                                        colorResource(
                                            id = if (hasWhatIfScore) R.color.borderLight else R.color.borderMedium
                                        )
                                    )
                                    .align(Alignment.CenterVertically)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            SubmissionState(
                                submissionStateLabel = uiState.submissionStateLabel,
                                testTag = "submissionStateLabel",
                                colorOverride = if (hasWhatIfScore) R.color.textLightest else null
                            )
                        }
                    }
                }
                val displayGrade = uiState.displayGrade
                val gradeText = displayGrade.text
                if (gradeText.isNotEmpty() || hasWhatIfScore) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (gradeText.isNotEmpty()) {
                            Text(
                                text = gradeText,
                                color = whatIfIconTint(hasWhatIfScore, contextColor),
                                fontSize = 16.sp,
                                modifier = Modifier
                                    .semantics { contentDescription = displayGrade.contentDescription }
                                    .testTag("gradeText")
                            )
                        }
                        if (hasWhatIfScore) {
                            if (gradeText.isNotEmpty()) {
                                Box(
                                    Modifier
                                        .height(16.dp)
                                        .width(1.dp)
                                        .clip(RoundedCornerShape(1.dp))
                                        .background(colorResource(id = R.color.borderLight))
                                        .align(Alignment.CenterVertically)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = colorResource(R.color.backgroundLightest),
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 4.dp)
                            ) {
                                val whatIfScore = NumberHelper.formatDecimal(uiState.whatIfScore, 2, true)
                                val maxScore = NumberHelper.formatDecimal(uiState.maxScore.orDefault(), 2, true)
                                val whatIfScoreText = "$whatIfScore/$maxScore"
                                Text(
                                    text = stringResource(id = R.string.whatIfScoreDisplay, whatIfScoreText),
                                    color = Color(contextColor),
                                    fontSize = 16.sp,
                                    modifier = Modifier.testTag("whatIfGradeText")
                                )
                            }
                        }
                    }
                }
                AnimatedVisibility(visible = uiState.checkpointsExpanded) {
                    Column(modifier = Modifier.padding(top = 8.dp)) {
                        uiState.checkpoints.forEach {
                            CheckpointItem(
                                discussionCheckpointUiState = it,
                                contextColor = Color(contextColor),
                                colorOverride = if (hasWhatIfScore) R.color.textLightest else null
                            )
                        }
                    }
                }
            }
        }
        if (showWhatIfScore) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .requiredSize(48.dp)
                    .clip(CircleShape)
                    .clickable { actionHandler(GradesAction.ShowWhatIfScoreDialog(uiState.id)) }
                    .semantics {
                        testTag = "editWhatIfScore"
                        role = Role.Button
                    }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_edit),
                    tint = whatIfTextColor(hasWhatIfScore),
                    contentDescription = stringResource(id = R.string.editWhatIfScore),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        if (uiState.checkpoints.isNotEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .requiredSize(48.dp)
                    .clip(CircleShape)
                    .clickable { actionHandler(GradesAction.ToggleCheckpointsExpanded(uiState.id)) }
                    .semantics {
                        testTag = "expandDiscussionCheckpoints"
                        role = Role.Button
                    }
            ) {
                val expandButtonContentDescription = stringResource(
                    if (uiState.checkpointsExpanded) {
                        R.string.content_description_collapse_content_with_param
                    } else {
                        R.string.content_description_expand_content_with_param
                    },
                    stringResource(R.string.a11y_discussion_checkpoints)
                )
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_down),
                    tint = whatIfTextColor(hasWhatIfScore),
                    contentDescription = expandButtonContentDescription,
                    modifier = Modifier
                        .size(20.dp)
                        .rotate(iconRotation)
                )
            }
        }
    }
}

private fun previewAssignment(
    id: Long,
    name: String = "Essay Assignment",
    dueDate: String = "Due Nov 15, 2024 at 11:59 PM",
    grade: String = "85/100",
    state: SubmissionStateLabel = SubmissionStateLabel.Graded,
    whatIf: Double? = null,
) = AssignmentUiState(
    id = id,
    iconRes = R.drawable.ic_assignment,
    name = name,
    dueDate = dueDate,
    displayGrade = DisplayGrade(grade, grade),
    submissionStateLabel = state,
    score = 85.0,
    maxScore = 100.0,
    whatIfScore = whatIf,
)

@Preview(name = "GradesAssignmentItem Legacy", showBackground = true)
@Composable
private fun GradesAssignmentItemLegacyPreview() {
    CompositionLocalProvider(LocalDesignSystem provides DesignSystem.Legacy) {
        GradesAssignmentItem(
            uiState = previewAssignment(id = 1),
            actionHandler = {},
            contextColor = 0xFF00828E.toInt(),
            showWhatIfScore = false,
        )
    }
}

@Preview(name = "GradesAssignmentItem InstUI — Light", showBackground = true)
@Preview(name = "GradesAssignmentItem InstUI — Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun GradesAssignmentItemInstUIPreview() {
    InstUITheme(courseColor = Color(0xFF00828E)) {
        CompositionLocalProvider(LocalDesignSystem provides DesignSystem.InstUI) {
            androidx.compose.foundation.layout.Column {
                GradesAssignmentItem(
                    uiState = previewAssignment(id = 1),
                    actionHandler = {},
                    contextColor = 0xFF00828E.toInt(),
                    showWhatIfScore = false,
                )
                GradesAssignmentItem(
                    uiState = previewAssignment(
                        id = 2,
                        name = "Overdue Quiz",
                        grade = "",
                        state = SubmissionStateLabel.Missing,
                    ),
                    actionHandler = {},
                    contextColor = 0xFF00828E.toInt(),
                    showWhatIfScore = false,
                )
            }
        }
    }
}

@Preview(name = "GradesAssignmentItem InstUI What-If", showBackground = true)
@Composable
private fun GradesAssignmentItemInstUIWhatIfPreview() {
    InstUITheme(courseColor = Color(0xFF00828E)) {
        CompositionLocalProvider(LocalDesignSystem provides DesignSystem.InstUI) {
            GradesAssignmentItem(
                uiState = previewAssignment(id = 1, whatIf = 95.0),
                actionHandler = {},
                contextColor = 0xFF00828E.toInt(),
                showWhatIfScore = true,
            )
        }
    }
}

// endregion
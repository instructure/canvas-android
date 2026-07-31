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
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.instui.compose.InstUITheme
import com.instructure.canvasapi2.utils.NumberHelper
import com.instructure.instui.compose.LocalCourseColor
import com.instructure.instui.compose.indicator.Icon as InstUIIcon
import com.instructure.instui.compose.indicator.Pill
import com.instructure.instui.compose.indicator.PillVariant
import com.instructure.instui.compose.indicator.IconSize
import com.instructure.instui.compose.list.AccordionIcon
import com.instructure.instui.compose.list.ListItem
import com.instructure.instui.compose.list.ListItemContentLine
import com.instructure.instui.compose.list.ListItemLineSeparator
import com.instructure.instui.compose.list.ListItemLineStyle
import com.instructure.instui.compose.list.ListItemTrailing
import com.instructure.instui.token.icon.InstUIIcons
import com.instructure.instui.token.icon.messageSquare
import com.instructure.instui.token.icon.pencil
import com.instructure.instui.token.icon.x
import com.instructure.instui.token.semantic.InstUILayoutSizes
import com.instructure.instui.token.semantic.InstUISemanticColors
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.composables.CheckpointItem
import com.instructure.pandautils.compose.composables.DiscussionCheckpointUiState
import com.instructure.pandautils.compose.composables.SubmissionState
import com.instructure.pandautils.compose.composables.SubmissionStateLabel
import com.instructure.pandautils.designsystem.DesignSystem
import com.instructure.pandautils.designsystem.LocalDesignSystem
import com.instructure.pandautils.features.grades.AssignmentUiState
import com.instructure.pandautils.features.grades.GradesAction
import com.instructure.pandautils.utils.DisplayGrade
import com.instructure.pandautils.utils.drawableId
import com.instructure.pandautils.utils.orDefault

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
    val hasWhatIfScore = showWhatIfScore && uiState.whatIfScore != null
    val hasCheckpoints = uiState.checkpoints.isNotEmpty()

    // In What-If mode show only the What-If score in place of the real grade.
    val scoreText = if (hasWhatIfScore) {
        val whatIf = NumberHelper.formatDecimal(uiState.whatIfScore, 2, true)
        val max = NumberHelper.formatDecimal(uiState.maxScore.orDefault(), 2, true)
        "$whatIf/$max"
    } else {
        uiState.displayGrade.text.ifEmpty { null }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(if (hasWhatIfScore) courseColor else Color.Transparent)
    ) {
        ListItem(
            modifier = Modifier.semantics { testTag = "assignmentItem" },
            isOnColor = hasWhatIfScore,
            // The What-If pencil is centered vertically; the comment badge + chevron sit at the top.
            trailingAlignment = if (showWhatIfScore) Alignment.CenterVertically else Alignment.Top,
            content = {
                ListItemContentLine(
                    text = uiState.name,
                    style = ListItemLineStyle.Title,
                    isOnColor = hasWhatIfScore,
                )
                ListItemContentLine(
                    text = uiState.dueDate,
                    style = ListItemLineStyle.SupportingText,
                    icon = mapAssignmentIcon(uiState.iconRes),
                    iconTint = if (hasWhatIfScore) InstUISemanticColors.Text.inverse() else courseColor,
                    isOnColor = hasWhatIfScore,
                    modifier = Modifier.semantics { testTag = "assignmentDueDate" },
                )
                if (scoreText != null || pillData != null) {
                    ListItemContentLine(
                        text = scoreText,
                        style = ListItemLineStyle.ImportantInfo,
                        isOnColor = hasWhatIfScore,
                        trailing = {
                            pillData?.let { data ->
                                if (scoreText != null) ListItemLineSeparator(hasWhatIfScore)
                                Pill(text = data.text, variant = data.variant, icon = data.icon)
                            }
                        },
                    )
                }
            },
            trailing = if (showWhatIfScore || uiState.commentCount > 0 || hasCheckpoints) {
                ListItemTrailing.Custom {
                    InstUIAssignmentTrailing(
                        uiState = uiState,
                        actionHandler = actionHandler,
                        showWhatIfScore = showWhatIfScore,
                        hasWhatIfScore = hasWhatIfScore,
                        hasCheckpoints = hasCheckpoints,
                    )
                }
            } else {
                null
            },
            onClick = { actionHandler(GradesAction.AssignmentClick(uiState.id)) },
        )
        // Checkpoints can't be expanded while in What-If mode (no chevron), so only show them otherwise.
        if (hasCheckpoints && !showWhatIfScore) {
            AnimatedVisibility(visible = uiState.checkpointsExpanded) {
                // Indent the checkpoint rows to nest them under the parent assignment.
                Column(
                    modifier = Modifier.padding(
                        start = InstUILayoutSizes.Spacing.SpaceLg.spaceLg,
                        bottom = InstUILayoutSizes.Spacing.SpaceSm.spaceSm,
                    )
                ) {
                    uiState.checkpoints.forEach {
                        InstUICheckpointItem(
                            uiState = it,
                            onClick = { actionHandler(GradesAction.AssignmentClick(uiState.id)) },
                            onCourseBackground = hasWhatIfScore,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CommentBadge(
    count: Int,
    hasUnread: Boolean,
    modifier: Modifier = Modifier,
) {
    val badgeContentDescription = if (hasUnread) {
        stringResource(R.string.gradesUnreadCommentsContentDescription, count)
    } else {
        stringResource(R.string.gradesCommentsContentDescription, count)
    }
    Box(
        modifier = modifier
            .testTag("commentBadge")
            .semantics { contentDescription = badgeContentDescription },
    ) {
        // Reserve room for the unread dot inside the badge bounds so it can never bleed into
        // adjacent trailing content (e.g. the checkpoint accordion arrow) or the row above.
        val dotInset = if (hasUnread) 3.dp else 0.dp
        Pill(
            text = count.toString(),
            variant = PillVariant.Default,
            icon = InstUIIcons.messageSquare,
            modifier = Modifier.padding(top = dotInset, end = dotInset),
        )
        if (hasUnread) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(InstUISemanticColors.Background.base())
                    .padding(1.dp)
                    .clip(CircleShape)
                    .background(InstUISemanticColors.Background.info())
                    .testTag("unreadCommentDot"),
            )
        }
    }
}

@Composable
private fun InstUIAssignmentTrailing(
    uiState: AssignmentUiState,
    actionHandler: (GradesAction) -> Unit,
    showWhatIfScore: Boolean,
    hasWhatIfScore: Boolean,
    hasCheckpoints: Boolean,
) {
    val courseColor = LocalCourseColor.current
    val onCourseColor = InstUISemanticColors.Text.inverse()
    when {
        // In What-If mode the pencil is a focused edit affordance and takes over the whole trailing —
        // the comment badge and checkpoint chevron are intentionally hidden while editing.
        showWhatIfScore -> {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable { actionHandler(GradesAction.ShowWhatIfScoreDialog(uiState.id)) }
                    .semantics {
                        testTag = "editWhatIfScore"
                        role = Role.Button
                    },
            ) {
                InstUIIcon(
                    imageVector = InstUIIcons.pencil,
                    tint = if (hasWhatIfScore) onCourseColor else courseColor,
                    size = IconSize.Large,
                    contentDescription = stringResource(R.string.editWhatIfScore),
                )
            }
        }

        uiState.commentCount > 0 || hasCheckpoints -> {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(InstUILayoutSizes.Spacing.SpaceMd.spaceMd),
            ) {
                if (uiState.commentCount > 0) {
                    CommentBadge(
                        count = uiState.commentCount,
                        hasUnread = uiState.hasUnreadComments,
                    )
                }
                if (hasCheckpoints) {
                    AccordionIcon(
                        expanded = uiState.checkpointsExpanded,
                        onClick = { actionHandler(GradesAction.ToggleCheckpointsExpanded(uiState.id)) },
                        contentDescription = checkpointAccordionContentDescription(uiState.checkpointsExpanded),
                        size = IconSize.Large,
                        modifier = Modifier.testTag("expandDiscussionCheckpoints"),
                    )
                }
            }
        }
    }
}

@Composable
private fun InstUICheckpointItem(
    uiState: DiscussionCheckpointUiState,
    onClick: () -> Unit,
    onCourseBackground: Boolean,
    modifier: Modifier = Modifier,
) {
    val gradeText = uiState.displayGrade.text
    val pillData = mapSubmissionStateToPill(uiState.submissionStateLabel)
    ListItem(
        modifier = modifier,
        isOnColor = onCourseBackground,
        onClick = onClick,
        content = {
            ListItemContentLine(
                text = uiState.name,
                style = ListItemLineStyle.Title,
                isOnColor = onCourseBackground,
            )
            ListItemContentLine(
                text = uiState.dueDate,
                style = ListItemLineStyle.SupportingText,
                isOnColor = onCourseBackground,
            )
            if (gradeText.isNotEmpty() || pillData != null) {
                ListItemContentLine(
                    text = gradeText.ifEmpty { null },
                    style = ListItemLineStyle.ImportantInfo,
                    isOnColor = onCourseBackground,
                    trailing = {
                        pillData?.let { data ->
                            if (gradeText.isNotEmpty()) ListItemLineSeparator(onCourseBackground)
                            Pill(text = data.text, variant = data.variant, icon = data.icon)
                        }
                    },
                )
            }
        },
    )
}

@Composable
private fun checkpointAccordionContentDescription(expanded: Boolean): String = stringResource(
    if (expanded) {
        R.string.content_description_collapse_content_with_param
    } else {
        R.string.content_description_expand_content_with_param
    },
    stringResource(R.string.a11y_discussion_checkpoints),
)

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
    commentCount: Int = 0,
    hasUnreadComments: Boolean = false,
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
    commentCount = commentCount,
    hasUnreadComments = hasUnreadComments,
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
            Column {
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

@Preview(name = "GradesAssignmentItem InstUI Comments — Light", showBackground = true)
@Preview(name = "GradesAssignmentItem InstUI Comments — Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun GradesAssignmentItemInstUICommentsPreview() {
    InstUITheme(courseColor = Color(0xFF00828E)) {
        CompositionLocalProvider(LocalDesignSystem provides DesignSystem.InstUI) {
            Column {
                GradesAssignmentItem(
                    uiState = previewAssignment(
                        id = 1,
                        name = "Interstellar Matter - ASTR 30300",
                        commentCount = 5,
                        hasUnreadComments = false,
                    ),
                    actionHandler = {},
                    contextColor = 0xFF00828E.toInt(),
                    showWhatIfScore = false,
                )
                GradesAssignmentItem(
                    uiState = previewAssignment(
                        id = 2,
                        name = "Radiation Processes - ASTR 25400",
                        commentCount = 2,
                        hasUnreadComments = true,
                    ),
                    actionHandler = {},
                    contextColor = 0xFF00828E.toInt(),
                    showWhatIfScore = false,
                )
            }
        }
    }
}

@Preview(name = "GradesAssignmentItem InstUI Comments + Checkpoints", showBackground = true)
@Preview(name = "GradesAssignmentItem InstUI Comments + Checkpoints — Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun GradesAssignmentItemInstUICommentsCheckpointsPreview() {
    InstUITheme(courseColor = Color(0xFF00828E)) {
        CompositionLocalProvider(LocalDesignSystem provides DesignSystem.InstUI) {
            GradesAssignmentItem(
                uiState = previewAssignment(
                    id = 1,
                    name = "Energy, Space and Time - PHSC 12600 Matter",
                    grade = "-/10",
                    state = SubmissionStateLabel.NotSubmitted,
                    commentCount = 5,
                    hasUnreadComments = true,
                ).copy(
                    checkpoints = listOf(
                        DiscussionCheckpointUiState(
                            name = "Reply to topic",
                            dueDate = "Due Jul 6, 2025 12:00",
                            submissionStateLabel = SubmissionStateLabel.NotSubmitted,
                            displayGrade = DisplayGrade("-/5", "-/5"),
                            pointsPossible = 5,
                        )
                    )
                ),
                actionHandler = {},
                contextColor = 0xFF00828E.toInt(),
                showWhatIfScore = false,
            )
        }
    }
}

@Preview(name = "GradesAssignmentItem InstUI Checkpoints Expanded", showBackground = true)
@Preview(name = "GradesAssignmentItem InstUI Checkpoints Expanded — Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun GradesAssignmentItemInstUICheckpointsExpandedPreview() {
    InstUITheme(courseColor = Color(0xFF00828E)) {
        CompositionLocalProvider(LocalDesignSystem provides DesignSystem.InstUI) {
            GradesAssignmentItem(
                uiState = previewAssignment(
                    id = 1,
                    name = "Energy, Space and Time - PHSC 12600 Matter",
                    grade = "-/10",
                    state = SubmissionStateLabel.NotSubmitted,
                ).copy(
                    checkpointsExpanded = true,
                    checkpoints = listOf(
                        DiscussionCheckpointUiState(
                            name = "Reply to topic",
                            dueDate = "Due Jul 6, 2025 12:00",
                            submissionStateLabel = SubmissionStateLabel.NotSubmitted,
                            displayGrade = DisplayGrade("-/5", "-/5"),
                            pointsPossible = 5,
                        ),
                        DiscussionCheckpointUiState(
                            name = "Additional replies (2)",
                            dueDate = "Due Jul 9, 2025 12:00",
                            submissionStateLabel = SubmissionStateLabel.Graded,
                            displayGrade = DisplayGrade("4/5", "4 out of 5"),
                            pointsPossible = 5,
                        ),
                    )
                ),
                actionHandler = {},
                contextColor = 0xFF00828E.toInt(),
                showWhatIfScore = false,
            )
        }
    }
}

@Preview(name = "GradesAssignmentItem InstUI What-If", showBackground = true)
@Preview(name = "GradesAssignmentItem InstUI What-If — Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun GradesAssignmentItemInstUIWhatIfPreview() {
    InstUITheme(courseColor = Color(0xFF00828E)) {
        CompositionLocalProvider(LocalDesignSystem provides DesignSystem.InstUI) {
            Column {
                GradesAssignmentItem(
                    uiState = previewAssignment(id = 1, whatIf = 95.0),
                    actionHandler = {},
                    contextColor = 0xFF00828E.toInt(),
                    showWhatIfScore = true,
                )
                GradesAssignmentItem(
                    uiState = previewAssignment(id = 2, name = "Bonus Quiz", whatIf = 140.0),
                    actionHandler = {},
                    contextColor = 0xFF00828E.toInt(),
                    showWhatIfScore = true,
                )
                GradesAssignmentItem(
                    uiState = previewAssignment(id = 3, name = "Unscored Essay"),
                    actionHandler = {},
                    contextColor = 0xFF00828E.toInt(),
                    showWhatIfScore = true,
                )
                GradesAssignmentItem(
                    uiState = previewAssignment(id = 4, name = "Checkpointed Discussion", whatIf = 8.0).copy(
                        commentCount = 2,
                        hasUnreadComments = true,
                        checkpointsExpanded = true,
                        checkpoints = listOf(
                            DiscussionCheckpointUiState(
                                name = "Reply to topic",
                                dueDate = "Due Jul 6, 2025 12:00",
                                submissionStateLabel = SubmissionStateLabel.Graded,
                                displayGrade = DisplayGrade("4/5", "4 out of 5"),
                                pointsPossible = 5,
                            )
                        )
                    ),
                    actionHandler = {},
                    contextColor = 0xFF00828E.toInt(),
                    showWhatIfScore = true,
                )
            }
        }
    }
}

// endregion
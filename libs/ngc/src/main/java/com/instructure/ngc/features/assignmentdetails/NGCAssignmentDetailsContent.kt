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

package com.instructure.ngc.features.assignmentdetails

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowInsetsControllerCompat
import com.instructure.canvasapi2.utils.NumberHelper
import com.instructure.instui.compose.LocalCourseColor
import com.instructure.instui.compose.container.Card
import com.instructure.instui.compose.container.Elevation
import com.instructure.instui.compose.container.ToggleDetails
import com.instructure.instui.compose.indicator.Icon
import com.instructure.instui.compose.indicator.IconColor
import com.instructure.instui.compose.indicator.IconSize
import com.instructure.instui.compose.indicator.Pill
import com.instructure.instui.compose.indicator.PillVariant
import com.instructure.instui.compose.indicator.ProgressCircle
import com.instructure.instui.compose.input.Button
import com.instructure.instui.compose.list.Separator
import com.instructure.instui.compose.navigation.TopBar
import com.instructure.instui.compose.text.Heading
import com.instructure.instui.compose.text.HeadingLevel
import com.instructure.instui.compose.text.Text
import com.instructure.instui.token.component.InstUISharedTokens
import com.instructure.instui.token.component.InstUIText
import com.instructure.instui.token.icon.InstUIIcons
import com.instructure.instui.token.icon.check
import com.instructure.instui.token.icon.chevronDown
import com.instructure.instui.token.icon.chevronLeft
import com.instructure.instui.token.icon.chevronRight
import com.instructure.instui.token.icon.circleCheck
import com.instructure.instui.token.icon.clipboardPenLine
import com.instructure.instui.token.icon.hourglass
import com.instructure.instui.token.icon.lock
import com.instructure.instui.token.icon.messageCircle
import com.instructure.instui.token.icon.paperclip
import com.instructure.instui.token.icon.plus
import com.instructure.instui.token.icon.star
import com.instructure.instui.token.icon.x
import com.instructure.instui.token.semantic.InstUILayoutSizes
import com.instructure.instui.token.semantic.InstUISemanticColors
import com.instructure.ngc.R
import com.instructure.ngc.ngcHtmlFormatColors
import com.instructure.ngc.ngcHtmlFormatColorsLight
import com.instructure.ngc.ngcThemeSwitchButtonColors
import com.instructure.ngc.ngcThemeSwitchButtonColorsLight
import com.instructure.pandautils.compose.composables.ComposeCanvasWebViewWrapper
import com.instructure.pandautils.compose.composables.ComposeEmbeddedWebViewCallbacks
import com.instructure.pandautils.compose.composables.ComposeWebViewCallbacks
import com.instructure.pandautils.compose.composables.Loading
import com.instructure.pandautils.compose.composables.UserAvatar
import com.instructure.pandautils.features.assignmentdetails.AssignmentDetailsAttemptViewData
import com.instructure.pandautils.features.assignments.details.AssignmentDetailsScreenState
import com.instructure.pandautils.features.assignments.details.AssignmentDetailsUiState
import com.instructure.pandautils.features.assignments.details.DiscussionHeaderUiState
import com.instructure.pandautils.features.assignments.details.QuizViewViewData
import com.instructure.pandautils.features.assignments.details.gradecellview.GradeCellViewData
import com.instructure.pandautils.features.assignments.details.mobius.gradeCell.GradeCellViewState
import com.instructure.pandautils.features.grades.components.mapSubmissionStateToPill
import com.instructure.pandautils.utils.WindowInsetsHelper
import com.instructure.pandautils.utils.getActivityOrNull
import java.text.DateFormat

const val NGC_ASSIGNMENT_DETAILS_TAG = "ngcAssignmentDetailsPage"

/**
 * Stateless NGC Assignment Details content. Renders the shared [AssignmentDetailsUiState]
 * with InstUI components. All interactions are surfaced as parameters so the host wires them
 * to the shared ViewModel.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NGCAssignmentDetailsContent(
    courseName: String,
    uiState: AssignmentDetailsUiState,
    onNavigateBack: () -> Unit,
    onSubmitClick: () -> Unit,
    webViewCallbacks: ComposeWebViewCallbacks,
    embeddedWebViewCallbacks: ComposeEmbeddedWebViewCallbacks,
    modifier: Modifier = Modifier,
    rubric: NgcRubricUiState? = null,
    onRubricClick: () -> Unit = {},
    previewState: NgcSubmissionPreviewUiState = NgcSubmissionPreviewUiState.NoPreview,
    onShowMore: () -> Unit = {},
    onCommentsClick: () -> Unit = {},
    onFullScreenMedia: (url: String, contentType: String, displayName: String, thumbnailUrl: String) -> Unit = { _, _, _, _ -> },
) {
    val courseColor = LocalCourseColor.current
    val onColor = InstUISemanticColors.Text.onColor()
    val density = LocalDensity.current
    // Match the status bar icons to the toolbar text color (e.g. light text → light icons),
    // restoring the previous appearance when leaving the screen.
    val context = LocalContext.current
    DisposableEffect(onColor) {
        val window = context.getActivityOrNull()?.window
        if (window == null) {
            onDispose { }
        } else {
            val previousLightStatusBar = WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars
            WindowInsetsHelper.setStatusBarAppearance(window, isLightStatusBar = onColor.luminance() < 0.5f)
            onDispose {
                WindowInsetsHelper.setStatusBarAppearance(window, isLightStatusBar = previousLightStatusBar)
            }
        }
    }
    // Height of the floating submit button area, used to pad the scrollable content so its last
    // items can scroll clear of the button.
    var submitBarHeight by remember { mutableStateOf(0.dp) }
    // Hoisted so the floating button can observe whether more content is below it.
    val scrollState = rememberScrollState()
    Scaffold(
        modifier = modifier.testTag(NGC_ASSIGNMENT_DETAILS_TAG),
        containerColor = InstUISemanticColors.Background.page(),
        topBar = {
            TopBar(
                title = uiState.assignmentName.ifEmpty { courseName },
                subtitle = courseName.takeIf { uiState.assignmentName.isNotEmpty() },
                containerColor = courseColor,
                contentColor = onColor,
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("ngcAssignmentDetails_back"),
                    ) {
                        Icon(imageVector = InstUIIcons.chevronLeft, tint = onColor)
                    }
                },
            )
        },
    ) { paddingValues ->
        when (val state = uiState.screenState) {
            is AssignmentDetailsScreenState.Loading -> {
                Loading(
                    color = courseColor,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .testTag("ngcAssignmentDetails_loading"),
                )
            }

            is AssignmentDetailsScreenState.Error -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(text = state.message, style = InstUIText.content)
                    Button(
                        text = stringResource(R.string.ngc_retry),
                        onClick = uiState.actions.onRefresh,
                        modifier = Modifier.padding(top = InstUILayoutSizes.Spacing.SpaceMd.spaceMd),
                    )
                }
            }

            is AssignmentDetailsScreenState.Content -> {
                // Hide the submit/new-attempt button entirely once all allowed attempts are used.
                val showSubmitButton = uiState.submitVisible && uiState.hasRemainingAttempts
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                    ContentBody(
                        uiState = uiState,
                        courseColor = courseColor,
                        scrollState = scrollState,
                        webViewCallbacks = webViewCallbacks,
                        embeddedWebViewCallbacks = embeddedWebViewCallbacks,
                        rubric = rubric,
                        onRubricClick = onRubricClick,
                        previewState = previewState,
                        onShowMore = onShowMore,
                        onCommentsClick = onCommentsClick,
                        onFullScreenMedia = onFullScreenMedia,
                        contentBottomPadding = if (showSubmitButton) submitBarHeight else 0.dp,
                        modifier = Modifier.fillMaxSize(),
                    )
                    if (showSubmitButton) {
                        // Quizzes always keep their "View Quiz" / "Open the Quiz" label; only regular
                        // resubmittable submissions become "New attempt".
                        val submitLabel = if (uiState.attempts.isNotEmpty() && !uiState.isQuiz) {
                            stringResource(R.string.ngc_newAttempt)
                        } else {
                            uiState.submitButtonText
                        }
                        // Float with a FAB-like shadow while content scrolls behind it; drop the
                        // shadow once scrolled to the bottom, where the button settles on the content.
                        val buttonElevation = if (scrollState.canScrollForward) 6.dp else 0.dp
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .onSizeChanged { submitBarHeight = with(density) { it.height.toDp() } }
                                .padding(InstUILayoutSizes.Spacing.SpaceLg.spaceLg),
                        ) {
                            Button(
                                text = submitLabel,
                                onClick = onSubmitClick,
                                enabled = uiState.submitEnabled,
                                elevation = buttonElevation,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("ngcAssignmentDetails_submitButton"),
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun ContentBody(
    uiState: AssignmentDetailsUiState,
    courseColor: Color,
    scrollState: ScrollState,
    webViewCallbacks: ComposeWebViewCallbacks,
    embeddedWebViewCallbacks: ComposeEmbeddedWebViewCallbacks,
    rubric: NgcRubricUiState?,
    onRubricClick: () -> Unit,
    previewState: NgcSubmissionPreviewUiState,
    onShowMore: () -> Unit,
    onCommentsClick: () -> Unit,
    onFullScreenMedia: (url: String, contentType: String, displayName: String, thumbnailUrl: String) -> Unit,
    modifier: Modifier = Modifier,
    contentBottomPadding: Dp = 0.dp,
) {
    val pullRefreshState = rememberPullRefreshState(
        refreshing = uiState.isRefreshing,
        onRefresh = uiState.actions.onRefresh,
    )
    val dateFormat = remember { DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT) }
    val spacing = InstUILayoutSizes.Spacing.SpaceLg.spaceLg

    Box(modifier = modifier.pullRefresh(pullRefreshState)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(start = spacing, top = spacing, end = spacing, bottom = spacing + contentBottomPadding),
            verticalArrangement = Arrangement.spacedBy(InstUILayoutSizes.Spacing.SpaceLg.spaceLg),
        ) {
            // Each major section is separated by a full-width divider, per design. The grade
            // section is only shown when there is a submission (state != EMPTY).
            val gradeCell = uiState.gradeCell
            val sections = buildList<@Composable () -> Unit> {
                add { TopInfoBlock(uiState, courseColor, dateFormat) }
                if (uiState.reminders.isNotEmpty()) {
                    add { ReminderSection(uiState, dateFormat) }
                }
                if (gradeCell != null && gradeCell.state != GradeCellViewData.State.EMPTY) {
                    add { NGCGradeCard(gradeCell, uiState.points, uiState.actions.onGradeCellClick) }
                }
                if (!uiState.fullLocked) {
                    add { DescriptionSection(uiState, webViewCallbacks, embeddedWebViewCallbacks) }
                }
                // Rubric row (opens the rubric bottom sheet); only when the assignment has a rubric.
                if (rubric != null) {
                    add { RubricRow(onRubricClick) }
                }
                // Quiz settings (classic quizzes only — present when the quiz details loaded).
                uiState.quizDetails?.let { quiz ->
                    add { QuizSettingsSection(quiz) }
                }
                if (uiState.checkpoints.isNotEmpty()) {
                    add { CheckpointSection(uiState) }
                }
                if (uiState.lockedMessage.isNotEmpty()) {
                    add { LockedCard(uiState.lockedMessage) }
                }
                if (uiState.allowedFileTypes.isNotEmpty()) {
                    add { LabeledRow(label = stringResource(R.string.ngc_allowedFileTypes), value = uiState.allowedFileTypes) }
                }
                // "Available until" — only when the assignment locks on a specific date.
                uiState.availableUntilDate?.let { date ->
                    add {
                        LabeledRow(
                            label = stringResource(R.string.ngc_availableUntil),
                            value = dateFormat.format(date),
                        )
                    }
                }
                // Submission section: attempt selector + inline preview of the selected attempt,
                // with the rubric/comments cells. Before a submission exists there's nothing to
                // preview, so the rubric row stands alone here (same position as the submission
                // section) rather than mid-screen.
                if (uiState.attempts.isNotEmpty()) {
                    add {
                        SubmissionSection(
                            uiState = uiState,
                            previewState = previewState,
                            rubric = rubric,
                            onShowMore = onShowMore,
                            onRubricClick = onRubricClick,
                            onCommentsClick = onCommentsClick,
                            onFullScreenMedia = onFullScreenMedia,
                        )
                    }
                } else if (rubric != null) {
                    add { RubricRow(onRubricClick) }
                }
            }
            sections.forEachIndexed { index, section ->
                if (index > 0) {
                    Separator(modifier = Modifier.testTag("ngcAssignmentDetails_divider"))
                }
                section()
            }
        }

        PullRefreshIndicator(
            refreshing = uiState.isRefreshing,
            state = pullRefreshState,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .testTag("ngcAssignmentDetails_pullRefreshIndicator"),
        )
    }
}

@Composable
private fun TopInfoBlock(
    uiState: AssignmentDetailsUiState,
    courseColor: Color,
    dateFormat: DateFormat,
) {
    Column(verticalArrangement = Arrangement.spacedBy(InstUILayoutSizes.Spacing.SpaceXs.spaceXs)) {
        Heading(
            text = uiState.assignmentName,
            level = HeadingLevel.H2,
            modifier = Modifier.testTag("ngcAssignmentDetails_assignmentName"),
        )
        // "N out of M attempts | submission types [...]" per design.
        val metadata = listOf(uiState.attemptsText, uiState.submissionTypes)
            .filter { it.isNotEmpty() }
            .joinToString(" | ")
        if (metadata.isNotEmpty()) {
            Text(
                text = metadata,
                style = InstUIText.content,
                color = InstUISemanticColors.Text.muted(),
                modifier = Modifier.testTag("ngcAssignmentDetails_headerMetadata"),
            )
        }
        val dueDate = uiState.reminders.firstOrNull()?.dueDate
        Text(
            text = dueDate?.let { dateFormat.format(it) } ?: stringResource(R.string.ngc_noDueDate),
            style = InstUIText.content,
            modifier = Modifier.testTag("ngcAssignmentDetails_dueDate"),
        )
        // Reuse the NGC Grades screen's status pill mapping so the statuses look identical.
        val statusPill = uiState.submissionStateLabel
            ?.takeIf { uiState.submissionStatusVisible }
            ?.let { mapSubmissionStateToPill(it) }
        if (uiState.points.isNotEmpty() || statusPill != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(InstUILayoutSizes.Spacing.SpaceSm.spaceSm),
            ) {
                if (uiState.points.isNotEmpty()) {
                    Text(
                        text = uiState.points,
                        style = InstUIText.contentImportant,
                        color = courseColor,
                        modifier = Modifier.testTag("ngcAssignmentDetails_points"),
                    )
                }
                statusPill?.let { pill ->
                    Pill(
                        text = pill.text,
                        variant = pill.variant,
                        modifier = Modifier.testTag("ngcAssignmentDetails_submissionStatus"),
                    )
                }
            }
        }
    }
}

@Composable
private fun NGCGradeCard(
    gradeCell: GradeCellViewData,
    pointsPossible: String,
    onClick: () -> Unit,
) {
    // In the design the grade meter/markers use the "plum" accent, which maps to the course color.
    val courseColor = LocalCourseColor.current
    // Sweep the meter from 0 to its value on open. Guard against NaN (0-points assignments give 0/0)
    // since Animatable.animateTo(NaN) crashes.
    val targetProgress = gradeCell.chartPercent.let { if (it.isNaN()) 0f else it.coerceIn(0f, 1f) }
    val animatedProgress = remember { Animatable(0f) }
    LaunchedEffect(targetProgress) {
        animatedProgress.animateTo(targetProgress, tween(durationMillis = 900, easing = FastOutSlowInEasing))
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(InstUISharedTokens.BorderRadius.Card.md))
            .clickable { onClick() }
            .testTag("ngcAssignmentDetails_gradeCard"),
        elevation = Elevation.Level1,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(InstUILayoutSizes.Spacing.SpaceLg.spaceLg),
        ) {
            when (gradeCell.state) {
                GradeCellViewData.State.GRADED -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(InstUILayoutSizes.Spacing.SpaceMd.spaceMd),
                    ) {
                        ProgressCircle(
                            progress = animatedProgress.value,
                            meterColor = courseColor,
                            trackColor = InstUISemanticColors.Stroke.muted(),
                        ) {
                            when {
                                gradeCell.showCompleteIcon -> Icon(InstUIIcons.check, color = IconColor.Base, size = IconSize.Small)
                                gradeCell.showIncompleteIcon -> Icon(InstUIIcons.x, color = IconColor.Base, size = IconSize.Small)
                                gradeCell.score.isNotEmpty() -> Text(
                                    text = gradeCell.score,
                                    style = InstUIText.contentImportant,
                                    modifier = Modifier.testTag("ngcAssignmentDetails_gradeScore"),
                                )
                            }
                        }
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(InstUILayoutSizes.Spacing.SpaceXs.spaceXs),
                        ) {
                            if (pointsPossible.isNotEmpty()) {
                                Text(text = "/ $pointsPossible", style = InstUIText.contentImportant)
                            }
                            if (gradeCell.grade.isNotEmpty()) {
                                Text(text = gradeCell.grade, style = InstUIText.content, color = InstUISemanticColors.Text.muted())
                            }
                            if (gradeCell.yourGrade.isNotEmpty()) {
                                Text(text = gradeCell.yourGrade, style = InstUIText.content)
                            }
                            if (gradeCell.latePenalty.isNotEmpty()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Pill(
                                        text = pluralStringResource(R.plurals.ngc_daysLate, gradeCell.daysLate, gradeCell.daysLate),
                                        variant = PillVariant.Error,
                                        icon = InstUIIcons.hourglass,
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text(
                                        text = gradeCell.latePenaltyText,
                                        style = InstUIText.contentImportant,
                                        color = InstUISemanticColors.Text.error(),
                                    )
                                }
                            }
                            if (gradeCell.extraCreditText.isNotEmpty()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Pill(
                                        text = stringResource(R.string.ngc_extraCredit),
                                        variant = PillVariant.Success,
                                        icon = InstUIIcons.star,
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text(
                                        text = gradeCell.extraCreditText,
                                        style = InstUIText.contentImportant,
                                        color = InstUISemanticColors.Text.success(),
                                    )
                                }
                            }
                        }
                    }

                    // VM-provided display value (letter / percentage / GPA / "x pts"); stays correct
                    // even with a late penalty. The box header already says "Final Grade".
                    val finalGradeText = gradeCell.finalGradeText
                        .ifEmpty { gradeCell.grade.ifEmpty { gradeCell.score } }
                    if (finalGradeText.isNotEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(InstUILayoutSizes.BorderRadius.Md.md))
                                .background(InstUISemanticColors.Background.muted())
                                .padding(InstUILayoutSizes.Spacing.SpaceMd.spaceMd),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(InstUILayoutSizes.Spacing.SpaceXs.spaceXs),
                        ) {
                            Text(text = stringResource(R.string.ngc_finalGrade), style = InstUIText.contentSmall, color = InstUISemanticColors.Text.muted())
                            Heading(text = finalGradeText, level = HeadingLevel.H2)
                        }
                    }

                    gradeCell.stats?.let { ClassPerformance(it) }
                }

                GradeCellViewData.State.SUBMITTED,
                GradeCellViewData.State.EMPTY -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(InstUILayoutSizes.Spacing.SpaceMd.spaceMd),
                    ) {
                        ProgressCircle(progress = 0f, meterColor = courseColor, trackColor = InstUISemanticColors.Stroke.muted()) {
                            Text(text = "-", style = InstUIText.contentImportant)
                        }
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(InstUILayoutSizes.Spacing.SpaceSm.spaceSm),
                        ) {
                            if (pointsPossible.isNotEmpty()) {
                                Text(text = "/ $pointsPossible", style = InstUIText.contentImportant)
                            }
                            Pill(text = stringResource(R.string.ngc_notYetGraded), variant = PillVariant.Info, icon = InstUIIcons.hourglass)
                        }
                    }
                }

                GradeCellViewData.State.UPLOADING -> {
                    Text(text = stringResource(R.string.ngc_uploadingSubmission), style = InstUIText.content)
                }

                GradeCellViewData.State.FAILED -> {
                    Text(text = stringResource(R.string.ngc_submissionFailed), style = InstUIText.content, color = InstUISemanticColors.Text.error())
                }
            }
        }
    }
}

@Composable
private fun ClassPerformance(stats: GradeCellViewState.GradeStats) {
    val courseColor = LocalCourseColor.current
    val trackColor = InstUISemanticColors.Background.Divider.base()
    val markerColor = InstUISemanticColors.Text.base()
    // Reveal the graph left-to-right on open (in step with the meter sweep).
    val reveal = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        reveal.animateTo(1f, tween(durationMillis = 900, easing = FastOutSlowInEasing))
    }
    Column(verticalArrangement = Arrangement.spacedBy(InstUILayoutSizes.Spacing.SpaceSm.spaceSm)) {
        Text(text = stringResource(R.string.ngc_classPerformance), style = InstUIText.contentImportant)
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .testTag("ngcAssignmentDetails_classPerformance"),
        ) {
            // Everything is positioned on a fixed 0..outOf axis (not min..max), matching the
            // classic GradeStatistics graph.
            val maxPossible = stats.outOf
            if (maxPossible <= 0.0) return@Canvas
            val bounds = 0.0..maxPossible
            val minScore = (stats.min ?: return@Canvas).coerceIn(bounds)
            val maxScore = (stats.max ?: return@Canvas).coerceIn(bounds)
            val meanScore = (stats.mean ?: return@Canvas).coerceIn(bounds)
            val youScore = stats.score.coerceIn(bounds)

            val sidePadding = 4.dp.toPx()
            val centerY = size.height / 2f
            val usableWidth = size.width - 2 * sidePadding
            fun positionFor(value: Double) = (sidePadding + value / maxPossible * usableWidth).toFloat()

            val lineStroke = 4.dp.toPx()
            val tickHeight = 12.dp.toPx()
            val circleRadius = 7.dp.toPx()

            clipRect(right = size.width * reveal.value) {
                // Full-width track (0..outOf).
                drawLine(trackColor, Offset(sidePadding, centerY), Offset(size.width - sidePadding, centerY), lineStroke, cap = StrokeCap.Round)
                // Class min..max range, in the course color.
                drawLine(courseColor, Offset(positionFor(minScore), centerY), Offset(positionFor(maxScore), centerY), lineStroke, cap = StrokeCap.Round)
                // Class mean tick.
                val meanX = positionFor(meanScore)
                drawLine(markerColor, Offset(meanX, centerY - tickHeight / 2f), Offset(meanX, centerY + tickHeight / 2f), 2.dp.toPx())
                // Your score.
                drawCircle(markerColor, radius = circleRadius, center = Offset(positionFor(youScore), centerY))
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.ngc_classPerformanceLowest, NumberHelper.formatDecimal(stats.min ?: 0.0, 1, true)),
                style = InstUIText.contentSmall,
                color = InstUISemanticColors.Text.muted(),
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = stringResource(R.string.ngc_classPerformanceAverage, NumberHelper.formatDecimal(stats.mean ?: 0.0, 1, true)),
                style = InstUIText.contentSmall,
                color = InstUISemanticColors.Text.muted(),
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = stringResource(R.string.ngc_classPerformanceHighest, NumberHelper.formatDecimal(stats.max ?: 0.0, 1, true)),
                style = InstUIText.contentSmall,
                color = InstUISemanticColors.Text.muted(),
            )
        }
    }
}

@Composable
private fun QuizSettingsSection(quiz: QuizViewViewData) {
    Column(verticalArrangement = Arrangement.spacedBy(InstUILayoutSizes.Spacing.SpaceXs.spaceXs)) {
        Heading(
            text = stringResource(R.string.ngc_quizSettings),
            level = HeadingLevel.H4,
            modifier = Modifier.testTag("ngcAssignmentDetails_quizSettings"),
        )
        Text(text = quiz.questionCount, style = InstUIText.content)
        Text(text = quiz.timeLimit, style = InstUIText.content)
        Text(text = quiz.allowedAttempts, style = InstUIText.content)
    }
}

@Composable
private fun SubmissionSection(
    uiState: AssignmentDetailsUiState,
    previewState: NgcSubmissionPreviewUiState,
    rubric: NgcRubricUiState?,
    onShowMore: () -> Unit,
    onRubricClick: () -> Unit,
    onCommentsClick: () -> Unit,
    onFullScreenMedia: (url: String, contentType: String, displayName: String, thumbnailUrl: String) -> Unit,
) {
    val commentCount = uiState.attempts.getOrNull(uiState.selectedAttemptPosition)
        ?.submission?.submissionComments?.size ?: 0
    val commentsTitle = if (commentCount > 0) {
        stringResource(R.string.ngc_commentsWithCount, commentCount)
    } else {
        stringResource(R.string.ngc_comments)
    }
    Column(verticalArrangement = Arrangement.spacedBy(InstUILayoutSizes.Spacing.SpaceMd.spaceMd)) {
        Heading(text = stringResource(R.string.ngc_submission), level = HeadingLevel.H4)
        AttemptSelector(
            attempts = uiState.attempts,
            selectedIndex = uiState.selectedAttemptPosition,
            onSelect = uiState.actions.onAttemptSelected,
        )
        // A single white card holds the preview, "Show more", and — via the footer — the grade
        // and the Rubric/Comments list cells, separated by dividers like a list (per design).
        NGCSubmissionPreview(
            state = previewState,
            onShowMore = onShowMore,
            onFullScreenMedia = onFullScreenMedia,
        ) {
            val rows = buildList<@Composable () -> Unit> {
                if (uiState.gradeCell != null) {
                    add { AttemptGradeRow(uiState) }
                }
                if (rubric != null) {
                    add {
                        SubmissionListCell(
                            title = stringResource(R.string.ngc_rubric),
                            icon = InstUIIcons.clipboardPenLine,
                            onClick = onRubricClick,
                            modifier = Modifier.testTag("ngcAssignmentDetails_rubricRow"),
                        )
                    }
                }
                add {
                    SubmissionListCell(
                        title = commentsTitle,
                        icon = InstUIIcons.messageCircle,
                        onClick = onCommentsClick,
                        modifier = Modifier.testTag("ngcAssignmentDetails_commentsRow"),
                    )
                }
            }
            rows.forEachIndexed { index, row ->
                if (index > 0) Separator()
                row()
            }
        }
    }
}

/**
 * Grade + status of the selected attempt, shown inside the submission card under "Show more" (the
 * shared ViewModel keeps [AssignmentDetailsUiState.gradeCell] in sync with the chosen attempt).
 * Renders "score/points" in the course color plus the status pill. Renders nothing when there is no
 * grade cell yet.
 */
@Composable
private fun AttemptGradeRow(uiState: AssignmentDetailsUiState) {
    val gradeCell = uiState.gradeCell ?: return
    val courseColor = LocalCourseColor.current
    val gradeText = gradeCell.score.takeIf { it.isNotEmpty() }?.let { score ->
        if (uiState.points.isNotEmpty()) "$score/${uiState.points}" else score
    }
    val statusPill = uiState.submissionStateLabel
        ?.takeIf { uiState.submissionStatusVisible }
        ?.let { mapSubmissionStateToPill(it) }
    if (gradeText == null && statusPill == null) return

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = InstUILayoutSizes.Spacing.SpaceLg.spaceLg,
                vertical = InstUILayoutSizes.Spacing.SpaceSm.spaceSm,
            )
            .testTag("ngcAssignmentDetails_attemptGrade"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(InstUILayoutSizes.Spacing.SpaceMd.spaceMd),
    ) {
        gradeText?.let {
            Text(text = it, style = InstUIText.contentImportant, color = courseColor)
        }
        statusPill?.let { pill ->
            Pill(
                text = pill.text,
                variant = pill.variant,
                icon = if (pill.variant == PillVariant.Success) InstUIIcons.circleCheck else null,
            )
        }
    }
}

/**
 * A submission list cell (rubric / comments), matching the design's list-cell component: a
 * course-colored leading icon, a semibold title, and a trailing chevron. Rendered as a flat row
 * (separators are drawn by the caller), unlike the standalone card-wrapped [RubricRow].
 */
@Composable
private fun SubmissionListCell(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // A plain center-aligned row rather than InstUI's ListItem: ListItem top-aligns its leading /
    // title / trailing (for multi-line items), which leaves this single-line cell's icon, text and
    // chevron looking uncentered.
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(InstUILayoutSizes.Spacing.SpaceMd.spaceMd),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(InstUILayoutSizes.Spacing.SpaceMd.spaceMd),
    ) {
        Icon(imageVector = icon, color = IconColor.CourseColor, size = IconSize.Large)
        Text(text = title, style = InstUIText.contentImportant, modifier = Modifier.weight(1f))
        Icon(imageVector = InstUIIcons.chevronRight, color = IconColor.Muted, size = IconSize.Small)
    }
}

@Composable
private fun AttemptSelector(
    attempts: List<AssignmentDetailsAttemptViewData>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var anchorWidthPx by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current
    // Self-contained against an empty list: render nothing rather than relying on the caller's
    // isNotEmpty() guard.
    val selected = attempts.getOrNull(selectedIndex) ?: attempts.firstOrNull() ?: return

    Box(modifier = Modifier.fillMaxWidth().testTag("ngcAssignmentDetails_attemptSelector")) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .onSizeChanged { anchorWidthPx = it.width }
                .clickable { expanded = true }
                .padding(vertical = InstUILayoutSizes.Spacing.SpaceSm.spaceSm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(InstUILayoutSizes.Spacing.SpaceSm.spaceSm),
        ) {
            Text(text = selected.title, style = InstUIText.contentImportant)
            Spacer(modifier = Modifier.weight(1f))
            if (selected.date.isNotEmpty()) {
                Text(text = selected.date, style = InstUIText.contentSmall, color = InstUISemanticColors.Text.muted())
            }
            Icon(imageVector = InstUIIcons.chevronDown, color = IconColor.Base, size = IconSize.Small)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.width(with(density) { anchorWidthPx.toDp() }),
        ) {
            attempts.forEachIndexed { index, attempt ->
                DropdownMenuItem(
                    text = {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text(text = attempt.title, style = InstUIText.content, modifier = Modifier.weight(1f))
                            if (attempt.date.isNotEmpty()) {
                                Text(text = attempt.date, style = InstUIText.contentSmall, color = InstUISemanticColors.Text.muted())
                            }
                        }
                    },
                    onClick = {
                        onSelect(index)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun ReminderSection(uiState: AssignmentDetailsUiState, dateFormat: DateFormat) {
    val addTag = uiState.reminders.firstOrNull()?.tag
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("ngcAssignmentDetails_reminders"),
        verticalArrangement = Arrangement.spacedBy(InstUILayoutSizes.Spacing.SpaceSm.spaceSm),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Heading(text = stringResource(R.string.ngc_assignmentReminder), level = HeadingLevel.H4, modifier = Modifier.weight(1f))
            IconButton(
                onClick = { uiState.actions.onAddReminderClick(addTag) },
                modifier = Modifier
                    .offset(x = InstUILayoutSizes.Spacing.SpaceMd.spaceMd)
                    .testTag("ngcAssignmentDetails_addReminder"),
            ) {
                Icon(imageVector = InstUIIcons.plus, color = IconColor.Base, size = IconSize.Medium)
            }
        }
        uiState.reminders.forEach { reminder ->
            reminder.reminders.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(InstUILayoutSizes.Spacing.SpaceSm.spaceSm),
                ) {
                    Text(
                        text = dateFormat.format(item.date),
                        style = InstUIText.content,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(
                        onClick = { uiState.actions.onRemoveReminderClick(item.id) },
                        modifier = Modifier.offset(x = InstUILayoutSizes.Spacing.SpaceMd.spaceMd),
                    ) {
                        Icon(imageVector = InstUIIcons.x, color = IconColor.Muted, size = IconSize.Small)
                    }
                }
            }
        }
    }
}

@Composable
private fun CheckpointSection(uiState: AssignmentDetailsUiState) {
    Card(modifier = Modifier.fillMaxWidth().testTag("ngcAssignmentDetails_checkpoints")) {
        Column(modifier = Modifier.fillMaxWidth()) {
            uiState.checkpoints.forEachIndexed { index, checkpoint ->
                if (index > 0) Separator()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = InstUILayoutSizes.Spacing.SpaceSm.spaceSm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(InstUILayoutSizes.Spacing.SpaceXs.spaceXs),
                    ) {
                        Text(text = checkpoint.name, style = InstUIText.content)
                        mapSubmissionStateToPill(checkpoint.stateLabel)?.let { pill ->
                            Pill(text = pill.text, variant = pill.variant)
                        }
                    }
                    if (checkpoint.grade.isNotEmpty()) {
                        Text(
                            text = checkpoint.grade,
                            style = InstUIText.contentImportant,
                            color = Color(checkpoint.courseColor),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DescriptionSection(
    uiState: AssignmentDetailsUiState,
    webViewCallbacks: ComposeWebViewCallbacks,
    embeddedWebViewCallbacks: ComposeEmbeddedWebViewCallbacks,
) {
    if (uiState.fullLocked) return
    var expanded by rememberSaveable { mutableStateOf(true) }
    val htmlColors = ngcHtmlFormatColors()
    val buttonColors = ngcThemeSwitchButtonColors()
    ToggleDetails(
        title = stringResource(R.string.ngc_instructions),
        expanded = expanded,
        onExpandToggle = { expanded = !expanded },
        modifier = Modifier.testTag("ngcAssignmentDetails_instructionsAccordion"),
    ) {
        uiState.discussionHeader?.let { header ->
            DiscussionHeader(header, uiState.actions.onDiscussionAttachmentClick)
        }
        when {
            uiState.noDescriptionVisible -> {
                Text(
                    text = stringResource(R.string.ngc_noAssignmentDescription),
                    style = InstUIText.content,
                    color = InstUISemanticColors.Text.muted(),
                    modifier = Modifier.testTag("ngcAssignmentDetails_noDescription"),
                )
            }

            uiState.description.isNotEmpty() -> {
                ComposeCanvasWebViewWrapper(
                    content = uiState.description,
                    baseUrl = uiState.baseUrl,
                    onLtiButtonPressed = uiState.actions.onLtiButtonPressed,
                    webViewCallbacks = webViewCallbacks,
                    embeddedWebViewCallbacks = embeddedWebViewCallbacks,
                    applyOnWebView = {
                        overrideHtmlFormatColors = htmlColors
                        overrideHtmlFormatColorsSwitched = ngcHtmlFormatColorsLight()
                    },
                    applyOnUpdate = {
                        themeSwitchButtonColors = buttonColors
                        themeSwitchButtonColorsSwitched = ngcThemeSwitchButtonColorsLight()
                    },
                    modifier = Modifier
                        // The HTML wrapper adds its own horizontal padding; cancel it so the
                        // instructions text lines up with the rest of the screen content.
                        .cancelHorizontalPadding(INSTRUCTIONS_HTML_PADDING)
                        .fillMaxWidth()
                        .testTag("ngcAssignmentDetails_description"),
                )
            }
        }
    }
}

@Composable
private fun LockedCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("ngcAssignmentDetails_lockedMessage"),
        borderColor = InstUISemanticColors.Stroke.base(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(InstUILayoutSizes.Spacing.SpaceSm.spaceSm),
        ) {
            Icon(imageVector = InstUIIcons.lock, color = IconColor.Muted, size = IconSize.Small)
            Text(text = message, style = InstUIText.content)
        }
    }
}

@Composable
private fun RubricRow(onClick: () -> Unit) {
    // The row lives in a card (like the grade card, but without elevation), per design.
    // contentPadding = 0 so the cell's own padding is the only inset (not doubled by the card).
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = Elevation.None,
        contentPadding = 0.dp,
    ) {
        SubmissionListCell(
            title = stringResource(R.string.ngc_rubric),
            icon = InstUIIcons.clipboardPenLine,
            onClick = onClick,
            modifier = Modifier.testTag("ngcAssignmentDetails_rubricRow"),
        )
    }
}

@Composable
private fun LabeledRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(InstUILayoutSizes.Spacing.SpaceXs.spaceXs)) {
        Text(text = label, style = InstUIText.contentSmall, color = InstUISemanticColors.Text.muted())
        Text(text = value, style = InstUIText.content)
    }
}

@Composable
private fun DiscussionHeader(header: DiscussionHeaderUiState, onAttachmentClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = InstUILayoutSizes.Spacing.SpaceSm.spaceSm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(InstUILayoutSizes.Spacing.SpaceSm.spaceSm),
    ) {
        UserAvatar(
            imageUrl = header.authorAvatarUrl,
            name = header.authorName,
            modifier = Modifier.size(32.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
            val authorName = if (header.authorPronouns.isNotEmpty()) {
                "${header.authorName} (${header.authorPronouns})"
            } else {
                header.authorName
            }
            Text(text = authorName, style = InstUIText.contentImportant)
            if (header.authoredDate.isNotEmpty()) {
                Text(text = header.authoredDate, style = InstUIText.contentSmall, color = InstUISemanticColors.Text.muted())
            }
        }
        if (header.attachmentIconVisible) {
            Icon(
                imageVector = InstUIIcons.paperclip,
                color = IconColor.Muted,
                size = IconSize.Small,
                modifier = Modifier
                    .clickable { onAttachmentClick() }
                    .testTag("ngcAssignmentDetails_attachmentIcon"),
            )
        }
    }
}

/** Horizontal padding baked into the WebView's HTML wrapper; cancelled so content aligns with the screen. */
private val INSTRUCTIONS_HTML_PADDING = 10.dp

/**
 * Lets a child draw [amount] wider on each side than its slot, cancelling padding applied by an
 * ancestor (here, the HTML wrapper's built-in horizontal padding).
 */
private fun Modifier.cancelHorizontalPadding(amount: Dp) = layout { measurable, constraints ->
    val extra = amount.roundToPx()
    val targetWidth = constraints.maxWidth + extra * 2
    val placeable = measurable.measure(constraints.copy(minWidth = targetWidth, maxWidth = targetWidth))
    layout(constraints.maxWidth, placeable.height) {
        placeable.place(-extra, 0)
    }
}


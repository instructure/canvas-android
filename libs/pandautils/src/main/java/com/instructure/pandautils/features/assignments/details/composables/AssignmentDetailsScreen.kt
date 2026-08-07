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
package com.instructure.pandautils.features.assignments.details.composables

import androidx.annotation.ColorInt
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.CanvasTheme
import com.instructure.pandautils.compose.composables.CanvasDivider
import com.instructure.pandautils.compose.composables.CanvasThemedAppBar
import com.instructure.pandautils.compose.composables.ComposeCanvasWebViewWrapper
import com.instructure.pandautils.compose.composables.ComposeEmbeddedWebViewCallbacks
import com.instructure.pandautils.compose.composables.ComposeWebViewCallbacks
import com.instructure.pandautils.compose.composables.ErrorContent
import com.instructure.pandautils.compose.composables.Loading
import com.instructure.pandautils.compose.composables.OverflowMenu
import com.instructure.pandautils.compose.composables.UserAvatar
import com.instructure.pandautils.features.assignmentdetails.AssignmentDetailsAttemptViewData
import com.instructure.pandautils.features.assignments.details.AssignmentDetailsScreenState
import com.instructure.pandautils.features.assignments.details.AssignmentDetailsUiState
import com.instructure.pandautils.features.assignments.details.DiscussionHeaderUiState
import com.instructure.pandautils.features.assignments.details.MessageFabUiState
import com.instructure.pandautils.features.assignments.details.gradecellview.GradeCellViewData
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.ThemedColor
import com.instructure.pandautils.views.CanvasWebView

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AssignmentDetailsScreen(
    title: String,
    subtitle: String,
    uiState: AssignmentDetailsUiState,
    @ColorInt toolbarColor: Int,
    navigationActionClick: () -> Unit,
    webViewCallbacks: ComposeWebViewCallbacks,
    embeddedWebViewCallbacks: ComposeEmbeddedWebViewCallbacks,
    modifier: Modifier = Modifier,
    applyOnWebView: (CanvasWebView.() -> Unit)? = null,
    onAddBookmark: (() -> Unit)? = null,
    messageFab: MessageFabUiState? = null,
    recordingOverlay: @Composable () -> Unit = {}
) {
    CanvasTheme {
        Scaffold(
            modifier = modifier.testTag("assignmentDetailsPage"),
            containerColor = colorResource(id = R.color.backgroundLightest),
            // The app bar handles the status bar (top) inset. For the bottom: the Scaffold applies
            // this inset to the content only when there is no bottom bar (e.g. Parent, which has no
            // submit button), and uses the bottom bar height when the submit button is present
            // (Student) — so the bottom inset is handled everywhere without switching on behavior.
            contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Bottom),
            topBar = {
                CanvasThemedAppBar(
                    title = title,
                    subtitle = subtitle,
                    navigationActionClick = navigationActionClick,
                    backgroundColor = Color(toolbarColor),
                    contentColor = colorResource(id = R.color.textLightest),
                    actions = { onAddBookmark?.let { BookmarkOverflowMenu(it) } }
                )
            },
            bottomBar = {
                if (uiState.screenState is AssignmentDetailsScreenState.Content && uiState.submitVisible) {
                    SubmitButton(uiState)
                }
            }
        ) { padding ->
            val pullRefreshState = rememberPullRefreshState(
                refreshing = uiState.isRefreshing,
                onRefresh = uiState.actions.onRefresh
            )
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .pullRefresh(pullRefreshState)
            ) {
                when (val screenState = uiState.screenState) {
                    is AssignmentDetailsScreenState.Loading -> Loading(
                        color = Color(toolbarColor),
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("loading")
                    )

                    is AssignmentDetailsScreenState.Error -> ErrorContent(
                        errorMessage = screenState.message,
                        retryClick = uiState.actions.onRefresh,
                        modifier = Modifier.fillMaxSize()
                    )

                    is AssignmentDetailsScreenState.Content -> AssignmentDetailsContent(
                        uiState = uiState,
                        toolbarColor = toolbarColor,
                        webViewCallbacks = webViewCallbacks,
                        embeddedWebViewCallbacks = embeddedWebViewCallbacks,
                        applyOnWebView = applyOnWebView,
                        messageFab = messageFab,
                        recordingOverlay = recordingOverlay
                    )
                }

                PullRefreshIndicator(
                    refreshing = uiState.isRefreshing,
                    state = pullRefreshState,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .testTag("pullRefreshIndicator"),
                    contentColor = Color(toolbarColor)
                )
            }
        }
    }
}

@Composable
private fun AssignmentDetailsContent(
    uiState: AssignmentDetailsUiState,
    @ColorInt toolbarColor: Int,
    webViewCallbacks: ComposeWebViewCallbacks,
    embeddedWebViewCallbacks: ComposeEmbeddedWebViewCallbacks,
    applyOnWebView: (CanvasWebView.() -> Unit)?,
    messageFab: MessageFabUiState?,
    recordingOverlay: @Composable () -> Unit
) {
    val actions = uiState.actions
    val scrollState = rememberScrollState()
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            Text(
                text = uiState.assignmentName,
                fontSize = 22.sp,
                color = colorResource(id = R.color.textDarkest),
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = 24.dp)
                    .semantics { heading() }
                    .testTag("assignmentName")
            )

            SubmissionStatusRow(uiState)

            if (uiState.hasDraft) {
                DraftSection(onDraftClick = actions.onDraftClick)
            }

            CanvasDivider(modifier = Modifier.padding(top = 30.dp))
            Spacer(modifier = Modifier.height(8.dp))

            AttemptSelector(uiState)

            uiState.gradeCell?.let { gradeCell ->
                GradeCell(
                    viewData = gradeCell,
                    onClick = actions.onGradeCellClick,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            DiscussionCheckpointLayout(checkpoints = uiState.checkpoints)

            if (uiState.lockedMessage.isNotEmpty()) {
                LockedSection(uiState.lockedMessage)
            }

            CanvasDivider(modifier = Modifier.padding(top = 16.dp))
            DueDateReminderLayout(
                reminderViewStates = uiState.reminders,
                onAddClick = actions.onAddReminderClick,
                onRemoveClick = actions.onRemoveReminderClick
            )

            if (uiState.fullLocked) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_panda_locked),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 24.dp)
                )
            }

            LabeledSection(R.string.submission_types, uiState.submissionTypes, valueTestTag = "submissionTypes")
            LabeledSection(R.string.allowedFileTypes, uiState.allowedFileTypes)

            uiState.quizDetails?.let { quiz ->
                SectionLabel(stringResource(id = R.string.settings))
                SectionValue(quiz.questionCount)
                SectionValue(quiz.timeLimit)
                SectionValue(quiz.allowedAttempts)
            }

            uiState.attemptsViewData?.let { attempts ->
                SectionLabel(stringResource(id = R.string.attempts))
                SectionValue(attempts.allowedAttempts)
                SectionValue(attempts.usedAttempts)
            }

            if (!uiState.fullLocked) {
                CanvasDivider(modifier = Modifier.padding(top = 16.dp))
                SectionLabel(uiState.descriptionLabelText)

                uiState.discussionHeader?.let { DiscussionHeader(it, actions.onDiscussionAttachmentClick) }

                when {
                    uiState.noDescriptionVisible -> Text(
                        text = stringResource(id = R.string.noAssignmentDescriptionStudent),
                        fontSize = 16.sp,
                        color = colorResource(id = R.color.textDarkest),
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .padding(top = 2.dp, bottom = 16.dp)
                            .testTag("noDescription")
                    )

                    uiState.description.isNotEmpty() -> ComposeCanvasWebViewWrapper(
                        content = uiState.description,
                        title = uiState.assignmentName,
                        baseUrl = uiState.baseUrl,
                        onLtiButtonPressed = { actions.onLtiButtonPressed(it) },
                        applyOnWebView = applyOnWebView,
                        webViewCallbacks = webViewCallbacks,
                        embeddedWebViewCallbacks = embeddedWebViewCallbacks,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                            .testTag("assignmentDescription")
                    )
                }
            }
        }

        if (messageFab != null) {
            MessageFab(toolbarColor = toolbarColor, messageFab = messageFab, scrollState = scrollState)
        }
        recordingOverlay()
    }
}

@Composable
private fun BookmarkOverflowMenu(onAddBookmark: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    OverflowMenu(
        modifier = Modifier.background(colorResource(id = R.color.backgroundLightestElevated)),
        showMenu = expanded,
        iconColor = colorResource(id = R.color.textLightest),
        onDismissRequest = { expanded = !expanded }
    ) {
        DropdownMenuItem(onClick = {
            expanded = false
            onAddBookmark()
        }) {
            Text(
                text = stringResource(id = R.string.addBookmark),
                color = colorResource(id = R.color.textDarkest)
            )
        }
    }
}

@Composable
private fun BoxScope.MessageFab(
    @ColorInt toolbarColor: Int,
    messageFab: MessageFabUiState,
    scrollState: ScrollState
) {
    var previousScrollOffset by remember { mutableIntStateOf(scrollState.value) }
    var fabVisible by remember { mutableStateOf(true) }

    LaunchedEffect(scrollState.value) {
        val currentScrollOffset = scrollState.value
        fabVisible = when {
            currentScrollOffset == 0 -> true
            !scrollState.canScrollForward -> false
            currentScrollOffset < previousScrollOffset -> true
            currentScrollOffset > previousScrollOffset -> false
            else -> fabVisible
        }
        previousScrollOffset = currentScrollOffset
    }

    AnimatedVisibility(
        visible = fabVisible,
        modifier = Modifier.align(Alignment.BottomEnd),
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut()
    ) {
        FloatingActionButton(
            onClick = messageFab.onClick,
            containerColor = Color(toolbarColor),
            contentColor = colorResource(id = R.color.textLightest),
            shape = CircleShape,
            modifier = Modifier
                .padding(16.dp)
                .testTag("messageFab")
        ) {
            Icon(
                painter = painterResource(id = messageFab.iconRes),
                contentDescription = messageFab.contentDescription
            )
        }
    }
}

@Composable
private fun SubmissionStatusRow(uiState: AssignmentDetailsUiState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (uiState.points.isNotEmpty()) {
            Text(
                text = uiState.points,
                fontSize = 16.sp,
                color = colorResource(id = R.color.textDark),
                modifier = Modifier.testTag("points")
            )
        }
        if (uiState.submissionStatusVisible) {
            if (uiState.submissionStatusIcon != 0) {
                // Only separate the icon from the points; with no points (e.g. letter-grade /
                // complete-incomplete assignments) the status aligns with the title.
                if (uiState.points.isNotEmpty()) {
                    Spacer(modifier = Modifier.size(10.dp))
                }
                Icon(
                    painter = painterResource(id = uiState.submissionStatusIcon),
                    contentDescription = null,
                    tint = colorResource(id = uiState.submissionStatusTint),
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.size(6.dp))
            Text(
                text = uiState.submissionStatusText,
                fontSize = 16.sp,
                color = colorResource(id = uiState.submissionStatusTint),
                modifier = Modifier.testTag("submissionStatus")
            )
        }
    }
}

@Composable
private fun DraftSection(onDraftClick: () -> Unit) {
    CanvasDivider(modifier = Modifier
        .padding(top = 30.dp)
        .testTag("draftDivider"))
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onDraftClick() }
            .padding(top = 30.dp)
            .testTag("draftContainer"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = R.string.submissionDraftAvailableTitle),
            fontSize = 20.sp,
            color = colorResource(id = R.color.textDarkest),
            textAlign = TextAlign.Center,
            modifier = Modifier.testTag("draftTitle")
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stringResource(id = R.string.submissionDraftAvailableSubtitle),
            color = colorResource(id = R.color.textInfo),
            textAlign = TextAlign.Center,
            modifier = Modifier.testTag("draftSubtitle")
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun AttemptSelector(uiState: AssignmentDetailsUiState) {
    if (uiState.attempts.isEmpty()) return

    if (uiState.attempts.size == 1) {
        AttemptRow(attempt = uiState.attempts.first())
    } else {
        var expanded by remember { mutableStateOf(false) }
        val selected = uiState.attempts.getOrElse(uiState.selectedAttemptPosition) { uiState.attempts.first() }
        val screenWidth = LocalWindowInfo.current.containerSize.width.dp
        Box {
            AttemptRow(
                attempt = selected,
                showChevron = true,
                modifier = Modifier
                    .clickable { expanded = true }
                    .testTag("attemptSpinner")
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .width(screenWidth)
                    .background(colorResource(id = R.color.backgroundLightestElevated))
            ) {
                uiState.attempts.forEachIndexed { index, attempt ->
                    DropdownMenuItem(
                        onClick = {
                            expanded = false
                            uiState.actions.onAttemptSelected(index)
                        },
                        // Tag the (merging, clickable) menu item itself so the tag isn't absorbed
                        // by semantics merging and stays addressable in the merged tree, alongside
                        // the merged attempt title/date text.
                        modifier = Modifier.testTag("attemptListItem")
                    ) {
                        AttemptRow(
                            attempt = attempt,
                            padded = false,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AttemptRow(
    attempt: AssignmentDetailsAttemptViewData,
    modifier: Modifier = Modifier,
    showChevron: Boolean = false,
    padded: Boolean = true
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(if (padded) Modifier.padding(horizontal = 16.dp, vertical = 8.dp) else Modifier),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = attempt.title,
            fontSize = 14.sp,
            color = colorResource(id = R.color.textDark),
            modifier = Modifier.testTag("attemptTitle")
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = attempt.date,
            fontSize = 14.sp,
            color = colorResource(id = R.color.textDark),
            modifier = Modifier.testTag("attemptDate")
        )
        if (showChevron) {
            Spacer(modifier = Modifier.size(8.dp))
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_down),
                contentDescription = null,
                tint = colorResource(id = R.color.textDark),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun LockedSection(lockedMessage: String) {
    CanvasDivider(modifier = Modifier.padding(top = 30.dp))
    SectionLabel(stringResource(id = R.string.locked))
    Text(
        text = lockedMessage,
        fontSize = 16.sp,
        color = colorResource(id = R.color.textDarkest),
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(top = 2.dp)
            .testTag("lockedMessage")
    )
}

@Composable
private fun LabeledSection(labelRes: Int, value: String, valueTestTag: String? = null) {
    if (value.isEmpty()) return
    SectionLabel(stringResource(id = labelRes))
    SectionValue(value, valueTestTag)
}

@Composable
private fun SectionLabel(label: String) {
    Text(
        text = label,
        fontSize = 14.sp,
        color = colorResource(id = R.color.textDark),
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(top = 24.dp)
            .semantics { heading() }
    )
}

@Composable
private fun SectionValue(value: String, testTag: String? = null) {
    Text(
        text = value,
        fontSize = 16.sp,
        color = colorResource(id = R.color.textDarkest),
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(top = 2.dp)
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
    )
}

@Composable
private fun DiscussionHeader(
    header: DiscussionHeaderUiState,
    onAttachmentClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        UserAvatar(
            imageUrl = header.authorAvatarUrl,
            name = header.authorName,
            modifier = Modifier.size(32.dp)
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp)
        ) {
            Text(
                text = header.authorName,
                fontSize = 14.sp,
                color = colorResource(id = R.color.textDarkest)
            )
            Text(
                text = header.authoredDate,
                fontSize = 12.sp,
                color = colorResource(id = R.color.textDark)
            )
        }
        if (header.attachmentIconVisible) {
            Icon(
                painter = painterResource(id = R.drawable.ic_attachment),
                contentDescription = stringResource(id = R.string.attachment),
                tint = colorResource(id = R.color.textDark),
                modifier = Modifier
                    .padding(end = 16.dp)
                    .size(24.dp)
                    .clickable { onAttachmentClick() }
                    .testTag("attachmentIcon")
            )
        }
    }
}

@Composable
private fun SubmitButton(uiState: AssignmentDetailsUiState) {
    Button(
        onClick = uiState.actions.onSubmitClick,
        enabled = uiState.submitEnabled,
        shape = RectangleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(ThemePrefs.buttonColor),
            contentColor = Color(ThemePrefs.buttonTextColor),
            disabledContainerColor = Color(ThemePrefs.buttonColor),
            disabledContentColor = Color(ThemePrefs.buttonTextColor)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Bottom))
            .height(48.dp)
            .alpha(if (uiState.submitEnabled) 1f else 0.5f)
            .testTag("submitButton")
    ) {
        Text(
            text = uiState.submitButtonText,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal
        )
    }
}

@Composable
private fun PreviewScreen(uiState: AssignmentDetailsUiState) {
    ContextKeeper.appContext = LocalContext.current
    AssignmentDetailsScreen(
        title = stringResource(id = R.string.assignmentDetails),
        subtitle = "Biology 101",
        uiState = uiState,
        toolbarColor = "#9C27B0".toColorInt(),
        navigationActionClick = {},
        webViewCallbacks = ComposeWebViewCallbacks(),
        embeddedWebViewCallbacks = ComposeEmbeddedWebViewCallbacks()
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AssignmentDetailsScreenLoadingPreview() {
    PreviewScreen(AssignmentDetailsUiState(screenState = AssignmentDetailsScreenState.Loading))
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AssignmentDetailsScreenErrorPreview() {
    PreviewScreen(
        AssignmentDetailsUiState(
            screenState = AssignmentDetailsScreenState.Error("There was a problem loading this assignment.")
        )
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AssignmentDetailsScreenGradedPreview() {
    val courseColor = ThemedColor("#9C27B0".toColorInt())
    PreviewScreen(
        AssignmentDetailsUiState(
            screenState = AssignmentDetailsScreenState.Content,
            courseColor = courseColor,
            assignmentName = "Creative Machines and Innovative Instrumentation",
            points = "100 pts",
            submissionStatusText = "Graded",
            submissionStatusIcon = R.drawable.ic_complete_solid,
            submissionStatusTint = R.color.textSuccess,
            submissionStatusVisible = true,
            gradeCell = GradeCellViewData(
                courseColor = courseColor,
                submissionAndRubricLabelColor = "#9C27B0".toColorInt(),
                state = GradeCellViewData.State.GRADED,
                chartPercent = 0.85f,
                showPointsLabel = true,
                score = "85",
                outOf = "Out of 100 pts"
            ),
            submissionTypes = "Text Entry, File Upload",
            descriptionLabelText = "Description",
            description = "This assignment explores the design of creative machines.",
            submitButtonText = "Resubmit Assignment",
            submitEnabled = true,
            submitVisible = true
        )
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AssignmentDetailsScreenLockedPreview() {
    PreviewScreen(
        AssignmentDetailsUiState(
            screenState = AssignmentDetailsScreenState.Content,
            courseColor = ThemedColor("#9C27B0".toColorInt()),
            assignmentName = "Locked Assignment",
            points = "50 pts",
            fullLocked = true,
            lockedMessage = "This assignment is locked until the module requirements are met."
        )
    )
}

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

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.instructure.canvasapi2.models.Assignment
import com.instructure.instui.compose.container.Tray
import com.instructure.instui.compose.indicator.Icon
import com.instructure.instui.compose.indicator.IconColor
import com.instructure.instui.compose.indicator.IconSize
import com.instructure.instui.compose.list.Separator
import com.instructure.instui.compose.text.Heading
import com.instructure.instui.compose.text.HeadingLevel
import com.instructure.instui.compose.text.Text
import com.instructure.instui.token.component.InstUIText
import com.instructure.instui.token.semantic.InstUISemanticColors
import com.instructure.instui.token.icon.InstUIIcons
import com.instructure.instui.token.icon.chevronRight
import com.instructure.instui.token.semantic.InstUILayoutSizes
import com.instructure.ngc.R
import com.instructure.ngc.navigation.NGCWebViewRouterEntryPoint
import com.instructure.pandautils.compose.composables.ComposeEmbeddedWebViewCallbacks
import com.instructure.pandautils.compose.composables.ComposeWebViewCallbacks
import com.instructure.pandautils.features.assignments.details.AssignmentDetailsNavigation
import com.instructure.pandautils.features.assignments.details.AssignmentDetailsViewModel
import dagger.hilt.android.EntryPointAccessors

private const val TAG = "NGCAssignmentDetails"
private const val DEFAULT_REMINDER_COLOR = android.graphics.Color.GRAY

/**
 * NGC host for Assignment Details. Reuses the shared [AssignmentDetailsViewModel] (selected via the
 * NGC nav route's SavedStateHandle args) and renders the InstUI [NGCAssignmentDetailsContent].
 *
 * Submit opens a bottom sheet of the assignment's submission types; NGC-implemented types (Text Entry,
 * File Upload) route to the submission screen with that type preselected, others are logged until built.
 * Reminders reuse the ViewModel's existing dialogs.
 */
@Composable
fun NGCAssignmentDetailsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSubmission: (Assignment.SubmissionType) -> Unit,
    viewModel: AssignmentDetailsViewModel = hiltViewModel(),
    draftHintsViewModel: NGCSubmitDraftHintsViewModel = hiltViewModel(),
    previewViewModel: NgcSubmissionPreviewViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val previewState by previewViewModel.uiState.collectAsState()
    val draftHintsUiState by draftHintsViewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showSubmitSheet by remember { mutableStateOf(false) }
    var showRubricSheet by remember { mutableStateOf(false) }
    // Rubric is read straight off the loaded assignment + the course's restrict-quantitative-data setting,
    // so it's keyed on those inputs (not uiState) to recompute exactly when they change.
    val restrictQuantitativeData = viewModel.currentCourse?.settings?.restrictQuantitativeData == true
    val rubric = remember(viewModel.assignment, restrictQuantitativeData) {
        buildNgcRubric(viewModel.assignment, restrictQuantitativeData)
    }

    // Links in the instructions WebView must never load in-place: route internal Canvas URLs to
    // native screens, and open external URLs in a separate WebView fragment (same as Academic).
    val activity = remember(context) { context.findActivity() }
    val webViewRouter = remember(activity) {
        EntryPointAccessors.fromActivity(activity, NGCWebViewRouterEntryPoint::class.java).webViewRouter()
    }
    val webViewCallbacks = remember(webViewRouter) {
        ComposeWebViewCallbacks(
            openMedia = { _, url, _ -> webViewRouter.openMedia(url) },
            canRouteInternally = { url -> webViewRouter.canRouteInternally(url, false) },
            routeInternally = { url -> webViewRouter.routeInternally(url) },
        )
    }
    val embeddedWebViewCallbacks = remember(webViewRouter) {
        ComposeEmbeddedWebViewCallbacks(
            shouldLaunchInternalWebViewFragment = { true },
            launchInternalWebViewFragment = { url -> webViewRouter.launchInternalWebViewFragment(url, viewModel.currentCourse) },
        )
    }

    LaunchedEffect(uiState.courseColor) {
        uiState.courseColor?.let { viewModel.updateReminderColor(it.color()) }
    }

    // Resolve the inline submission preview whenever the selected attempt changes.
    LaunchedEffect(uiState.attempts, uiState.selectedAttemptPosition) {
        val submission = uiState.attempts.getOrNull(uiState.selectedAttemptPosition)?.submission
        previewViewModel.load(submission, uiState.baseUrl, viewModel.currentCourse?.id ?: 0L)
    }

    LaunchedEffect(uiState.navigation) {
        uiState.navigation?.let { navigation ->
            when (navigation) {
                // Classic quiz: open the dedicated quiz screen (hosted over the NGC content).
                is AssignmentDetailsNavigation.QuizScreen ->
                    webViewRouter.launchInternalQuiz(viewModel.currentCourse, navigation.quiz, navigation.quiz.url.orEmpty())
                // New-quiz LTI: launch the tool via the assignment-LTI flow inside the app.
                is AssignmentDetailsNavigation.LtiLaunchScreen ->
                    webViewRouter.launchAssignmentLtiTool(
                        viewModel.currentCourse,
                        navigation.ltiTool?.url.orEmpty(),
                        navigation.title,
                        navigation.ltiTool,
                        navigation.openInternally,
                    )
                else -> Log.d(TAG, "Navigation requested (Phase 1, not routed): $navigation")
            }
            uiState.onNavigationHandled()
        }
    }

    LaunchedEffect(uiState.toastMessage) {
        uiState.toastMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            uiState.onToastShown()
        }
    }

    LaunchedEffect(uiState.createReminder) {
        uiState.createReminder?.let { reminder ->
            viewModel.showCreateReminderDialog(context, uiState.courseColor?.color() ?: DEFAULT_REMINDER_COLOR, reminder.tag)
            uiState.onCreateReminderShown()
        }
    }

    LaunchedEffect(uiState.deleteReminderId) {
        uiState.deleteReminderId?.let { reminderId ->
            viewModel.showDeleteReminderConfirmationDialog(context, reminderId, uiState.courseColor?.color() ?: DEFAULT_REMINDER_COLOR)
            uiState.onDeleteReminderShown()
        }
    }

    NGCAssignmentDetailsContent(
        courseName = viewModel.currentCourse?.name.orEmpty(),
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onSubmitClick = {
            val assignment = viewModel.assignment
            // For discussions the action is "View Discussion": open the discussion instead of the
            // submit type chooser.
            val discussionUrl = assignment
                ?.takeIf { it.turnInType == Assignment.TurnInType.DISCUSSION }
                ?.let { it.discussionTopicHeader?.htmlUrl ?: it.htmlUrl }
            when {
                discussionUrl != null -> webViewRouter.routeInternally(discussionUrl)
                // Quizzes (and new-quiz LTIs) open the quiz instead of the submit chooser. The
                // ViewModel decides between a quiz route and an LTI launch, mirroring Academic.
                assignment?.isQuiz() == true -> uiState.actions.onSubmitClick()
                else -> {
                    draftHintsViewModel.refresh()
                    showSubmitSheet = true
                }
            }
        },
        webViewCallbacks = webViewCallbacks,
        embeddedWebViewCallbacks = embeddedWebViewCallbacks,
        rubric = rubric,
        onRubricClick = { showRubricSheet = true },
        previewState = previewState,
        // "Show more" reuses the Grade Card's navigation (both open the submission screen).
        // Outward routing is still Phase 1 (logged, not routed).
        onShowMore = uiState.actions.onGradeCellClick,
        onCommentsClick = { Log.d(TAG, "View comments requested (Phase 1, not routed)") },
        onFullScreenMedia = { url, contentType, displayName, thumbnailUrl ->
            webViewRouter.openMediaFullScreen(url, contentType, displayName, thumbnailUrl)
        },
    )

    if (showRubricSheet && rubric != null) {
        RubricBottomSheet(rubric = rubric, onDismiss = { showRubricSheet = false })
    }

    if (showSubmitSheet) {
        val assignment = viewModel.assignment
        // Studio is not part of submissionTypes; it's an extra option available for file-upload
        // assignments when Studio is enabled (mirrors the Academic submit dialog).
        val options = buildList {
            assignment?.getSubmissionTypes()?.forEach { type ->
                add(
                    SubmitOption(
                        submissionType = type,
                        label = Assignment.submissionTypeToPrettyPrintString(type, context, assignment.ltiToolType()).orEmpty(),
                        showDraftHint = when (type) {
                            Assignment.SubmissionType.ONLINE_TEXT_ENTRY -> draftHintsUiState.hasTextDraft
                            Assignment.SubmissionType.ONLINE_UPLOAD -> draftHintsUiState.hasFileDraft
                            Assignment.SubmissionType.ONLINE_URL -> draftHintsUiState.hasUrlDraft
                            else -> false
                        },
                    )
                )
            }
            if (viewModel.isStudioAccepted()) {
                // Studio isn't a real assignment submission type; it launches the academic Studio
                // WebView full-screen over NGC (not the generic submission screen).
                add(SubmitOption(submissionType = null, label = stringResource(R.string.ngc_studio), showDraftHint = false, isStudio = true))
            }
        }
        SubmitTypeBottomSheet(
            options = options,
            onSelect = { option ->
                showSubmitSheet = false
                val type = option.submissionType
                when {
                    // Studio: launch the academic Studio WebView full-screen over NGC (no bottom bar).
                    option.isStudio -> viewModel.assignment?.let { assignment ->
                        webViewRouter.launchStudio(viewModel.currentCourse, assignment, viewModel.getStudioLTITool())
                    }
                    // NGC-implemented submission types route to the submission screen with that type preselected.
                    type == Assignment.SubmissionType.ONLINE_TEXT_ENTRY ||
                        type == Assignment.SubmissionType.ONLINE_UPLOAD ||
                        type == Assignment.SubmissionType.ONLINE_URL -> type?.let { onNavigateToSubmission(it) }
                    else -> Log.d(TAG, "Submission type selected (not yet routed): ${option.label}")
                }
            },
            onDismiss = { showSubmitSheet = false },
        )
    }
}

private data class SubmitOption(
    val submissionType: Assignment.SubmissionType?,
    val label: String,
    val showDraftHint: Boolean,
    val isStudio: Boolean = false,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SubmitTypeBottomSheet(
    options: List<SubmitOption>,
    onSelect: (SubmitOption) -> Unit,
    onDismiss: () -> Unit,
) {
    Tray(onDismiss = onDismiss, modifier = Modifier.testTag("ngcAssignmentDetails_submitSheet")) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(InstUILayoutSizes.Spacing.SpaceLg.spaceLg),
        ) {
            Heading(
                text = stringResource(R.string.ngc_submitAssignment),
                level = HeadingLevel.H3,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = InstUILayoutSizes.Spacing.SpaceSm.spaceSm),
            )
            options.forEach { option ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(option) }
                        .padding(vertical = InstUILayoutSizes.Spacing.SpaceMd.spaceMd),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(InstUILayoutSizes.Spacing.SpaceSm.spaceSm),
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = option.label, style = InstUIText.content)
                        if (option.showDraftHint) {
                            Text(
                                text = stringResource(R.string.ngc_continueDraft),
                                style = InstUIText.contentSmall,
                                color = InstUISemanticColors.Text.muted(),
                            )
                        }
                    }
                    Icon(imageVector = InstUIIcons.chevronRight, color = IconColor.Muted, size = IconSize.Small)
                }
                Separator()
            }
        }
    }
}

private tailrec fun Context.findActivity(): Activity = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> error("No Activity found in the context chain")
}

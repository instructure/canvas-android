/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
@file:OptIn(ExperimentalMaterial3Api::class)

package com.instructure.horizon.features.moduleitemsequence.content.assignment

import android.view.ViewGroup
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.R
import com.instructure.horizon.features.moduleitemsequence.content.assignment.addsubmission.AddSubmissionContent
import com.instructure.horizon.features.moduleitemsequence.content.assignment.addsubmission.AddSubmissionViewModel
import com.instructure.horizon.features.moduleitemsequence.content.assignment.attempts.AttemptSelectorBottomSheet
import com.instructure.horizon.features.moduleitemsequence.content.assignment.comments.CommentsDialog
import com.instructure.horizon.features.moduleitemsequence.content.assignment.comments.CommentsViewModel
import com.instructure.horizon.features.moduleitemsequence.content.assignment.submission.TextSubmissionContent
import com.instructure.horizon.features.moduleitemsequence.content.assignment.submission.file.FileSubmissionContent
import com.instructure.horizon.features.moduleitemsequence.content.assignment.submission.file.FileSubmissionContentViewModel
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonCornerRadius
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.horizon.horizonui.molecules.ActionBottomSheet
import com.instructure.horizon.horizonui.molecules.Badge
import com.instructure.horizon.horizonui.molecules.BadgeContent
import com.instructure.horizon.horizonui.molecules.BadgeType
import com.instructure.horizon.horizonui.molecules.BottomSheetActionState
import com.instructure.horizon.horizonui.molecules.Button
import com.instructure.horizon.horizonui.molecules.ButtonColor
import com.instructure.horizon.horizonui.organisms.Modal
import com.instructure.horizon.horizonui.organisms.ModalDialogState
import com.instructure.horizon.horizonui.organisms.cards.AttemptCard
import com.instructure.horizon.horizonui.platform.LoadingStateWrapper
import com.instructure.pandautils.compose.composables.ComposeCanvasWebView
import com.instructure.pandautils.compose.composables.ComposeCanvasWebViewWrapper
import com.instructure.pandautils.compose.composables.ComposeEmbeddedWebViewCallbacks
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.getActivityOrNull
import com.instructure.pandautils.utils.launchCustomTab
import com.instructure.pandautils.views.JSInterface

@Composable
fun AssignmentDetailsScreen(uiState: AssignmentDetailsUiState, scrollState: ScrollState, modifier: Modifier = Modifier) {
    val activity = LocalContext.current.getActivityOrNull()
    LaunchedEffect(uiState.urlToOpen) {
        uiState.urlToOpen?.let { url ->
            activity?.launchCustomTab(url, ThemePrefs.brandColor)
            uiState.onUrlOpened()
        }
    }

    if (uiState.attemptSelectorUiState.show) {
        AttemptSelectorBottomSheet(uiState.attemptSelectorUiState)
    }

    if (uiState.openCommentsBottomSheetParams != null) {
        val commentsViewModel = hiltViewModel<CommentsViewModel>()
        val commentsUiState by commentsViewModel.uiState.collectAsState()
        val commentBottomSheetParams = uiState.openCommentsBottomSheetParams
        LaunchedEffect(commentBottomSheetParams.assignmentId, uiState.submissionDetailsUiState.currentSubmissionAttempt) {
            commentsViewModel.initWithAttempt(
                commentBottomSheetParams.assignmentId,
                uiState.submissionDetailsUiState.currentSubmissionAttempt.toInt(),
                commentBottomSheetParams.courseId
            )
        }
        CommentsDialog(commentsUiState, onDismiss = uiState.onCommentsBottomSheetDismissed)
    }

    if (uiState.submissionConfirmationUiState.show) {
        Modal(
            dialogState = ModalDialogState(
                title = stringResource(R.string.assignmentDetails_submissionSuccessTitle),
                message = stringResource(R.string.assignmentDetails_submissionSuccessSubtitle),
                primaryButtonTitle = stringResource(R.string.assignmentDetails_submissionSuccessButton),
                primaryButtonClick = uiState.submissionConfirmationUiState.onDismiss
            ),
            headerIcon = { Badge(type = BadgeType.Success, content = BadgeContent.Icon(R.drawable.check, null)) },
            onDismiss = uiState.submissionConfirmationUiState.onDismiss,
            extraBody = {
                uiState.submissionConfirmationUiState.attemptCardState?.let {
                    AttemptCard(it, modifier = Modifier.fillMaxWidth())
                }
            }
        )
    }

    if (uiState.toolsBottomSheetUiState.show) {
        ActionBottomSheet(
            title = stringResource(R.string.assignmentDetails_tools),
            actions = buildList {
                if (uiState.toolsBottomSheetUiState.showAttemptSelector) {
                    add(
                        BottomSheetActionState(
                            stringResource(R.string.assignmentDetails_attemptHistory),
                            R.drawable.history,
                            onClick = uiState.toolsBottomSheetUiState.onAttemptsClick
                        )
                    )
                }
                val commentsIcon = if (uiState.toolsBottomSheetUiState.hasUnreadComments) {
                    R.drawable.mark_unread_chat_alt
                } else {
                    R.drawable.chat
                }
                add(
                    BottomSheetActionState(
                        stringResource(R.string.assignmentDetails_comments),
                        commentsIcon,
                        onClick = uiState.toolsBottomSheetUiState.onCommentsClick
                    )
                )
            },
            sheetState = rememberStandardBottomSheetState(initialValue = SheetValue.Expanded, skipHiddenState = false),
            onDismiss = uiState.toolsBottomSheetUiState.onDismiss
        )
    }

    val snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
    LoadingStateWrapper(loadingState = uiState.loadingState, containerColor = Color.Transparent, snackbarHostState = snackbarHostState) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier
                .fillMaxSize()
                .clip(HorizonCornerRadius.level5)
                .background(HorizonColors.Surface.cardPrimary())
        ) {

            var rceFocused by remember { mutableStateOf(false) }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                if (uiState.viewingAttemptText != null) {
                    Text(
                        uiState.viewingAttemptText.uppercase(),
                        style = HorizonTypography.p2,
                        color = HorizonColors.Surface.institution(),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(color = HorizonColors.Surface.pagePrimary())
                            .padding(vertical = 8.dp)
                    )
                }
                HorizonSpace(SpaceSize.SPACE_24)
                Text(
                    stringResource(R.string.assignmentDetails_instructions),
                    style = HorizonTypography.h3,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                HorizonSpace(SpaceSize.SPACE_8)
                ComposeCanvasWebViewWrapper(
                    content = uiState.instructions,
                    applyOnWebView = {
                        activity?.let { addVideoClient(it) }
                        overrideHtmlFormatColors = HorizonColors.htmlFormatColors
                        if (uiState.ltiButtonPressed != null) {
                            addJavascriptInterface(JSInterface(uiState.ltiButtonPressed), Const.LTI_TOOL)
                        }
                    },
                    embeddedWebViewCallbacks = ComposeEmbeddedWebViewCallbacks(
                        shouldLaunchInternalWebViewFragment = { _ -> true },
                        launchInternalWebViewFragment = { url -> activity?.launchCustomTab(url, ThemePrefs.brandColor) }
                    ),
                    modifier = Modifier.padding(16.dp)
                )
                if (uiState.ltiUrl.isNotEmpty()) {
                    HorizonSpace(SpaceSize.SPACE_24)
                    ComposeCanvasWebView(
                        uiState.ltiUrl,
                        modifier = modifier
                            .height(400.dp)
                            .padding(horizontal = 24.dp),
                        embeddedWebViewCallbacks = ComposeEmbeddedWebViewCallbacks(
                            shouldLaunchInternalWebViewFragment = { _ -> true },
                            launchInternalWebViewFragment = { url -> activity?.launchCustomTab(url, ThemePrefs.brandColor) }
                        ),
                        applyOnWebView = {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            setInitialScale(100)
                            setZoomSettings(false)
                            activity?.let { addVideoClient(it) }
                        })
                    HorizonSpace(SpaceSize.SPACE_24)
                }
                HorizonSpace(SpaceSize.SPACE_40)
                if (uiState.showSubmissionDetails) {
                    SubmissionDetailsContent(uiState.submissionDetailsUiState, modifier = Modifier.padding(24.dp))
                }
                if (uiState.showAddSubmission) {
                    val addSubmissionViewModel = hiltViewModel<AddSubmissionViewModel>()
                    val addSubmissionUiState by addSubmissionViewModel.uiState.collectAsState()
                    addSubmissionViewModel.setOnSubmissionSuccessListener(uiState.onSubmissionSuccess)

                    val assignment by hiltViewModel<AssignmentDetailsViewModel>().assignmentFlow.collectAsState()
                    LaunchedEffect(assignment) {
                        assignment?.let {
                            addSubmissionViewModel.updateAssignment(it)
                        }
                    }
                    AddSubmissionContent(
                        addSubmissionUiState,
                        snackbarHostState = snackbarHostState,
                        onRceFocused = { rceFocused = true },
                        modifier = Modifier.padding(16.dp)
                    )
                }
                HorizonSpace(SpaceSize.SPACE_48)
            }
        }
    }
}

@Composable
private fun SubmissionDetailsContent(uiState: SubmissionDetailsUiState, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        SubmissionContent(uiState.submissions.find { it.submissionAttempt == uiState.currentSubmissionAttempt }
            ?: uiState.submissions.first())
        HorizonSpace(SpaceSize.SPACE_40)
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
            Button(
                label = stringResource(R.string.assignmentDetails_newAttempt),
                color = ButtonColor.Institution,
                onClick = uiState.onNewAttemptClick
            )
        }
    }
}

@Composable
fun SubmissionContent(uiState: SubmissionUiState, modifier: Modifier = Modifier) {
    when (uiState.submissionContent) {
        is SubmissionContent.TextSubmission -> TextSubmissionContent(text = uiState.submissionContent.text, modifier = modifier)
        is SubmissionContent.FileSubmission -> {
            NavHost(
                rememberNavController(),
                startDestination = "fileSubmission",
                modifier = modifier
            ) {
                composable("fileSubmission") {
                    val viewModel = hiltViewModel<FileSubmissionContentViewModel>()
                    LaunchedEffect(uiState.submissionContent.fileItems) {
                        viewModel.setInitialData(uiState.submissionContent.fileItems)
                    }
                    val fileSubmissionContentUiState by viewModel.uiState.collectAsState()
                    FileSubmissionContent(uiState = fileSubmissionContentUiState)
                }
            }
        }
    }
}

@Preview
@Composable
fun AssignmentDetailsScreenPreview() {
    ContextKeeper.appContext = LocalContext.current
    AssignmentDetailsScreen(
        uiState = AssignmentDetailsUiState(
            assignmentId = 1L,
            instructions = "This is a test",
            ltiUrl = "",
            submissionDetailsUiState = SubmissionDetailsUiState(
                submissions = listOf(
                    SubmissionUiState(
                        submissionAttempt = 1L,
                        submissionContent = SubmissionContent.TextSubmission("This is a test"),
                        date = "2023-10-01"
                    )
                ),
                currentSubmissionAttempt = 1L
            ),
            showSubmissionDetails = true
        ),
        scrollState = ScrollState(0)
    )
}
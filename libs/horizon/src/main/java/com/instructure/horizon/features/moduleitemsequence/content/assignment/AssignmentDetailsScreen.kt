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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SheetValue
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.R
import com.instructure.horizon.features.moduleitemsequence.content.assignment.addsubmission.AddFileSubmissionContent
import com.instructure.horizon.features.moduleitemsequence.content.assignment.addsubmission.AddTextSubmissionContent
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
import com.instructure.horizon.horizonui.molecules.ButtonIconPosition
import com.instructure.horizon.horizonui.molecules.SegmentedControl
import com.instructure.horizon.horizonui.molecules.SegmentedControlIconPosition
import com.instructure.horizon.horizonui.molecules.Spinner
import com.instructure.horizon.horizonui.molecules.SpinnerSize
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

    val selectedSubmissionType =
        uiState.addSubmissionUiState.submissionTypes.getOrNull(uiState.addSubmissionUiState.selectedSubmissionTypeIndex)
    if (selectedSubmissionType?.draftUiState?.showDeleteDraftConfirmation == true) {
        Modal(
            dialogState = ModalDialogState(
                title = stringResource(R.string.assignmentDetails_deleteDraftTitle),
                message = stringResource(R.string.assignmentDetails_deleteDraftMessage),
                primaryButtonTitle = stringResource(R.string.assignmentDetails_deleteDraftConfirm),
                secondaryButtonTitle = stringResource(R.string.assignmentDetails_deleteDraftCancel),
                primaryButtonClick = { selectedSubmissionType.draftUiState.onDraftDeleted(selectedSubmissionType.submissionType) },
                secondaryButtonClick = {
                    selectedSubmissionType.draftUiState.onDismissDeleteDraftConfirmation(
                        selectedSubmissionType.submissionType
                    )
                }
            ),
            onDismiss = {
                selectedSubmissionType.draftUiState.onDismissDeleteDraftConfirmation(
                    selectedSubmissionType.submissionType
                )
            }
        )
    }

    if (uiState.addSubmissionUiState.showSubmissionConfirmation) {
        Modal(
            dialogState = ModalDialogState(
                title = stringResource(R.string.assignmentDetails_submissionConfirmationTitle),
                message = stringResource(R.string.assignmentDetails_submissionConfirmationMessage),
                primaryButtonTitle = stringResource(R.string.assignmentDetails_submissionConfirmationConfirm),
                secondaryButtonTitle = stringResource(R.string.assignmentDetails_submissionConfirmationCancel),
                primaryButtonClick = uiState.addSubmissionUiState.onSubmitAssignment,
                secondaryButtonClick = uiState.addSubmissionUiState.onDismissSubmissionConfirmation
            ),
            onDismiss = uiState.addSubmissionUiState.onDismissSubmissionConfirmation
        )
    }

    if (uiState.toolsBottomSheetUiState.show) {
        ActionBottomSheet(
            title = stringResource(R.string.assignmentDetails_tools),
            actions = listOf(
                BottomSheetActionState(
                    stringResource(R.string.assignmentDetails_attemptHistory),
                    R.drawable.history,
                    onClick = uiState.toolsBottomSheetUiState.onAttemptsClick
                ),
                BottomSheetActionState(
                    stringResource(R.string.assignmentDetails_comments),
                    R.drawable.chat,
                    onClick = uiState.toolsBottomSheetUiState.onCommentsClick
                ),
            ),
            sheetState = rememberStandardBottomSheetState(initialValue = SheetValue.Expanded, skipHiddenState = false),
            onDismiss = uiState.toolsBottomSheetUiState.onDismiss
        )
    }

    LoadingStateWrapper(loadingState = uiState.loadingState, containerColor = Color.Transparent) {
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
                    .padding(horizontal = 16.dp)
                    .verticalScroll(scrollState)
            ) {
                HorizonSpace(SpaceSize.SPACE_24)
                Text(
                    stringResource(R.string.assignmentDetails_instructions),
                    style = HorizonTypography.h3,
                    modifier = Modifier.padding(horizontal = 8.dp)
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
                    )
                )
                if (uiState.ltiUrl.isNotEmpty()) {
                    HorizonSpace(SpaceSize.SPACE_24)
                    ComposeCanvasWebView(
                        uiState.ltiUrl,
                        modifier = modifier
                            .height(400.dp)
                            .padding(horizontal = 16.dp),
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
                    SubmissionDetailsContent(uiState.submissionDetailsUiState)
                }
                if (uiState.showAddSubmission) {
                    AddSubmissionContent(uiState.addSubmissionUiState, onRceFocused = { rceFocused = true })
                }
                HorizonSpace(SpaceSize.SPACE_48)
            }
        }
    }
}

@Composable
private fun ColumnScope.SubmissionDetailsContent(uiState: SubmissionDetailsUiState, modifier: Modifier = Modifier) {
    SubmissionContent(uiState.submissions.find { it.submissionAttempt == uiState.currentSubmissionAttempt }
        ?: uiState.submissions.first(), modifier = modifier)
    HorizonSpace(SpaceSize.SPACE_40)
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
        Button(
            label = stringResource(R.string.assignmentDetails_newAttempt),
            color = ButtonColor.Institution,
            onClick = uiState.onNewAttemptClick
        )
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

@Composable
fun ColumnScope.AddSubmissionContent(uiState: AddSubmissionUiState, modifier: Modifier = Modifier, onRceFocused: () -> Unit = {}) {
    if (uiState.submissionTypes.size > 1) {
        Text(stringResource(R.string.assignmentDetails_selectSubmissionType), style = HorizonTypography.h3)
        HorizonSpace(SpaceSize.SPACE_16)
        val options = uiState.submissionTypes.map { stringResource(it.labelRes) }
        SegmentedControl(
            options = options,
            onItemSelected = uiState.onSubmissionTypeSelected,
            selectedIndex = uiState.selectedSubmissionTypeIndex,
            iconPosition = SegmentedControlIconPosition.Start(checkmark = true),
            modifier = modifier
        )
        HorizonSpace(SpaceSize.SPACE_24)
    }
    if (uiState.submissionTypes.isNotEmpty()) {
        val selectedSubmissionType = uiState.submissionTypes[uiState.selectedSubmissionTypeIndex]
        when (selectedSubmissionType) {
            is AddSubmissionTypeUiState.File -> AddFileSubmissionContent(
                uiState = selectedSubmissionType,
                submissionInProgress = uiState.submissionInProgress
            )

            is AddSubmissionTypeUiState.Text -> AddTextSubmissionContent(uiState = selectedSubmissionType, onRceFocused = onRceFocused)
        }
        HorizonSpace(SpaceSize.SPACE_24)
        AnimatedVisibility(visible = uiState.errorMessage != null) {
            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    painterResource(R.drawable.error),
                    tint = HorizonColors.Icon.error(),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                HorizonSpace(SpaceSize.SPACE_6)
                Text(
                    uiState.errorMessage.orEmpty(),
                    style = HorizonTypography.p1,
                    color = HorizonColors.Text.error()
                )
            }
        }
        AnimatedVisibility(visible = selectedSubmissionType.draftUiState.draftDateString.isNotEmpty() && !uiState.submissionInProgress) {
            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    selectedSubmissionType.draftUiState.draftDateString,
                    style = HorizonTypography.p1,
                    color = HorizonColors.Text.timestamp()
                )
                Button(
                    label = stringResource(R.string.assignmentDetails_deleteDraft),
                    color = ButtonColor.Custom(
                        backgroundColor = HorizonColors.Surface.pageSecondary(),
                        contentColor = HorizonColors.Text.error()
                    ),
                    onClick = { selectedSubmissionType.draftUiState.onDeleteDraftClicked(selectedSubmissionType.submissionType) },
                    iconPosition = ButtonIconPosition.Start(R.drawable.delete),
                )
            }
        }
        HorizonSpace(SpaceSize.SPACE_16)
        Box(contentAlignment = Alignment.CenterEnd, modifier = Modifier.fillMaxWidth()) {
            val alpha = if (selectedSubmissionType.submitEnabled || uiState.submissionInProgress) 1f else 0.5f
            Box(
                contentAlignment = Alignment.CenterEnd,
                modifier = Modifier
                    .background(color = HorizonColors.Surface.institution().copy(alpha = alpha), shape = HorizonCornerRadius.level6)
                    .animateContentSize()
            ) {
                if (uiState.submissionInProgress) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .background(color = HorizonColors.Surface.institution(), shape = HorizonCornerRadius.level6)
                    ) {
                        Spinner(
                            size = SpinnerSize.EXTRA_SMALL,
                            color = HorizonColors.Surface.cardPrimary(),
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(horizontal = 22.dp, vertical = 10.dp),
                        )
                    }
                } else {
                    Button(
                        label = stringResource(R.string.assignmentDetails_submitAssignment),
                        color = ButtonColor.Custom(backgroundColor = Color.Transparent, contentColor = HorizonColors.Text.surfaceColored()),
                        onClick = uiState.onSubmissionButtonClicked,
                        enabled = selectedSubmissionType.submitEnabled
                    )
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

@Preview
@Composable
fun AssignmentDetailsScreenAddSubmissionPreview() {
    ContextKeeper.appContext = LocalContext.current
    AssignmentDetailsScreen(
        uiState = AssignmentDetailsUiState(
            instructions = "This is a test",
            ltiUrl = "",
            addSubmissionUiState = AddSubmissionUiState(
                submissionTypes = listOf(
                    AddSubmissionTypeUiState.File(draftUiState = DraftUiState(draftDateString = "Saved at 2023-10-01"))
                ),
                errorMessage = "Error occurred while submitting.",
            ),
            showAddSubmission = true
        ),
        scrollState = ScrollState(0)
    )
}
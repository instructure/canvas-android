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
package com.instructure.horizon.features.moduleitemsequence.content.assignment.addsubmission

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonCornerRadius
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.horizon.horizonui.molecules.Button
import com.instructure.horizon.horizonui.molecules.ButtonColor
import com.instructure.horizon.horizonui.molecules.IconButton
import com.instructure.horizon.horizonui.molecules.IconButtonColor
import com.instructure.horizon.horizonui.molecules.SegmentedControl
import com.instructure.horizon.horizonui.molecules.SegmentedControlIconPosition
import com.instructure.horizon.horizonui.molecules.Spinner
import com.instructure.horizon.horizonui.molecules.SpinnerSize
import com.instructure.horizon.horizonui.organisms.Modal
import com.instructure.horizon.horizonui.organisms.ModalDialogState
import com.instructure.pandautils.utils.toPx

@Composable
fun AddSubmissionContent(
    uiState: AddSubmissionUiState,
    snackbarHostState: SnackbarHostState,
    scrollState: ScrollState,
    moduleHeaderHeight: Dp,
    modifier: Modifier = Modifier,
    onRceFocused: () -> Unit = {}
) {
    var rceYPositionInRoot by remember { mutableIntStateOf(0) }
    var cursorYPosition by remember { mutableIntStateOf(0) }
    var scrollContent: Int? by remember { mutableStateOf(null) }
    var viewportHeight by remember { mutableIntStateOf(0) }
    LaunchedEffect(scrollState.viewportSize, moduleHeaderHeight) {
        viewportHeight = scrollState.viewportSize + moduleHeaderHeight.value.toInt().toPx
    }
    LaunchedEffect(viewportHeight, cursorYPosition) {
        if (rceYPositionInRoot + cursorYPosition + 32.toPx > viewportHeight) {
            //Cursor is under the viewport
            scrollContent = rceYPositionInRoot + cursorYPosition + 32.toPx - viewportHeight
        }
        if (rceYPositionInRoot + cursorYPosition < moduleHeaderHeight.value.toInt().toPx) {
            //Cursor is under the module header
            scrollContent = (rceYPositionInRoot + cursorYPosition) - moduleHeaderHeight.value.toInt().toPx
        }
    }
    LaunchedEffect(scrollContent) {
        scrollState.scrollBy(scrollContent?.toFloat() ?: 0f)
    }
    LaunchedEffect(uiState.snackbarMessage) {
        if (uiState.snackbarMessage != null) {
            val result = snackbarHostState.showSnackbar(uiState.snackbarMessage)
            if (result == SnackbarResult.Dismissed) {
                uiState.onSnackbarDismiss()
            }
        }
    }

    if (uiState.submissionTypes.isNotEmpty()) {
        val selectedSubmissionType = uiState.submissionTypes[uiState.selectedSubmissionTypeIndex]
        if (selectedSubmissionType.draftUiState.showDeleteDraftConfirmation) {
            Modal(
                dialogState = ModalDialogState(
                    title = stringResource(R.string.assignmentDetails_deleteDraftTitle),
                    message = stringResource(R.string.assignmentDetails_deleteDraftMessage),
                    primaryButtonTitle = stringResource(R.string.assignmentDetails_deleteDraftConfirm),
                    secondaryButtonTitle = stringResource(R.string.assignmentDetails_deleteDraftCancel),
                    primaryButtonClick = selectedSubmissionType.draftUiState.onDraftDeleted,
                    secondaryButtonClick = selectedSubmissionType.draftUiState.onDismissDeleteDraftConfirmation
                ),
                onDismiss = selectedSubmissionType.draftUiState.onDismissDeleteDraftConfirmation
            )
        }

        if (uiState.showSubmissionConfirmation) {
            Modal(
                dialogState = ModalDialogState(
                    title = stringResource(R.string.assignmentDetails_submissionConfirmationTitle),
                    message = stringResource(R.string.assignmentDetails_submissionConfirmationMessage),
                    primaryButtonTitle = stringResource(R.string.assignmentDetails_submissionConfirmationConfirm),
                    secondaryButtonTitle = stringResource(R.string.assignmentDetails_submissionConfirmationCancel),
                    primaryButtonClick = uiState.onSubmitAssignment,
                    secondaryButtonClick = uiState.onDismissSubmissionConfirmation
                ),
                onDismiss = uiState.onDismissSubmissionConfirmation
            )
        }

        Column(modifier = modifier) {
            if (uiState.submissionTypes.size > 1) {
                Text(stringResource(R.string.assignmentDetails_selectSubmissionType), style = HorizonTypography.h3)
                HorizonSpace(SpaceSize.SPACE_16)
                val options = uiState.submissionTypes.map { stringResource(it.labelRes) }
                SegmentedControl(
                    options = options,
                    onItemSelected = uiState.onSubmissionTypeSelected,
                    selectedIndex = uiState.selectedSubmissionTypeIndex,
                    iconPosition = SegmentedControlIconPosition.Start(checkmark = true)
                )
                HorizonSpace(SpaceSize.SPACE_24)
            }

            when (selectedSubmissionType) {
                is AddSubmissionTypeUiState.File -> AddFileSubmissionContent(
                    uiState = selectedSubmissionType,
                    submissionInProgress = uiState.submissionInProgress
                )

                is AddSubmissionTypeUiState.Text ->
                    AddTextSubmissionContent(
                        uiState = selectedSubmissionType,
                        onRceFocused = onRceFocused,
                        onCursorYCoordinateChanged = {
                            cursorYPosition = it.toInt().toPx
                        },
                        modifier = Modifier.onGloballyPositioned { coordinates ->
                            rceYPositionInRoot = coordinates.positionInRoot().y.toInt()
                        }
                    )
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
                    IconButton(
                        color = IconButtonColor.InverseDanger,
                        onClick = selectedSubmissionType.draftUiState.onDeleteDraftClicked,
                        iconRes = R.drawable.delete,
                        contentDescription = stringResource(R.string.assignmentDetails_deleteDraft)
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
                            color = ButtonColor.Custom(
                                backgroundColor = Color.Transparent,
                                contentColor = HorizonColors.Text.surfaceColored()
                            ),
                            onClick = uiState.onSubmissionButtonClicked,
                            enabled = selectedSubmissionType.submitEnabled
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AssignmentDetailsScreenAddSubmissionPreview() {
    ContextKeeper.appContext = LocalContext.current
    Column {
        AddSubmissionContent(
            uiState = AddSubmissionUiState(
                submissionTypes = listOf(
                    AddSubmissionTypeUiState.File(draftUiState = DraftUiState(draftDateString = "Saved at 2023-10-01"))
                ),
                errorMessage = "Error occurred while submitting.",
            ),
            snackbarHostState = SnackbarHostState(),
            scrollState = rememberScrollState(),
            moduleHeaderHeight = 0.dp
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AssignmentDetailsScreenAddSubmissionSubmitEnabledPreview() {
    ContextKeeper.appContext = LocalContext.current
    Column {
        AddSubmissionContent(
            uiState = AddSubmissionUiState(
                submissionTypes = listOf(
                    AddSubmissionTypeUiState.File(
                        draftUiState = DraftUiState(draftDateString = "Saved at 2023-10-01"),
                        submitEnabled = true
                    )
                ),
                errorMessage = "Error occurred while submitting.",
            ),
            snackbarHostState = SnackbarHostState(),
            scrollState = rememberScrollState(),
            moduleHeaderHeight = 0.dp
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AssignmentDetailsScreenAddSubmissionNoErrorPreview() {
    ContextKeeper.appContext = LocalContext.current
    Column {
        AddSubmissionContent(
            uiState = AddSubmissionUiState(
                submissionTypes = listOf(
                    AddSubmissionTypeUiState.File(
                        draftUiState = DraftUiState(draftDateString = "Saved at 2023-10-01"),
                        submitEnabled = true
                    )
                )
            ),
            snackbarHostState = SnackbarHostState(),
            scrollState = rememberScrollState(),
            moduleHeaderHeight = 0.dp
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AssignmentDetailsScreenAddSubmissionNoDraftPreview() {
    ContextKeeper.appContext = LocalContext.current
    Column {
        AddSubmissionContent(
            uiState = AddSubmissionUiState(
                submissionTypes = listOf(
                    AddSubmissionTypeUiState.File(submitEnabled = true)
                )
            ),
            snackbarHostState = SnackbarHostState(),
            scrollState = rememberScrollState(),
            moduleHeaderHeight = 0.dp
        )
    }
}
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

package com.instructure.ngc.features.submission

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.navigation.NavigableSupportingPaneScaffold
import androidx.compose.material3.adaptive.navigation.rememberSupportingPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.window.core.layout.WindowWidthSizeClass
import com.instructure.instui.compose.InstUITheme
import com.instructure.instui.compose.LocalCourseColor
import com.instructure.instui.compose.indicator.Icon
import com.instructure.instui.compose.indicator.IconColor
import com.instructure.instui.compose.indicator.IconSize
import com.instructure.instui.compose.input.Button
import com.instructure.instui.compose.input.TextButton
import com.instructure.instui.compose.navigation.TopBar
import com.instructure.instui.compose.text.Heading
import com.instructure.instui.compose.text.HeadingLevel
import com.instructure.instui.compose.text.Text
import com.instructure.instui.token.component.InstUIText
import com.instructure.instui.token.icon.InstUIIcons
import com.instructure.instui.token.icon.arrowLeft
import com.instructure.instui.token.icon.chevronDown
import com.instructure.instui.token.icon.chevronUp
import com.instructure.instui.token.icon.trash2
import com.instructure.instui.token.semantic.InstUILayoutSizes
import com.instructure.instui.token.semantic.InstUISemanticColors
import com.instructure.ngc.R
import com.instructure.ngc.features.submission.supportingpane.SubmissionSupportingPane
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun NGCSubmissionScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NGCSubmissionViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val courseColor = LocalCourseColor.current
    val widthSizeClass = currentWindowAdaptiveInfo().windowSizeClass.windowWidthSizeClass
    val twoPane = widthSizeClass != WindowWidthSizeClass.COMPACT
    var selectedSegment by rememberSaveable { mutableIntStateOf(0) }

    // Navigate back on successful submission
    LaunchedEffect(uiState.submissionSuccess) {
        if (uiState.submissionSuccess) {
            onNavigateBack()
            uiState.onSubmissionSuccessHandled()
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        TopBar(
            title = stringResource(R.string.ngc_submission_submitAssignment),
            subtitle = uiState.courseName.ifBlank { null },
            containerColor = courseColor,
            navigationIcon = {
                Icon(
                    imageVector = InstUIIcons.arrowLeft,
                    contentDescription = stringResource(R.string.ngc_back),
                    tint = InstUISemanticColors.Text.inverse(),
                    modifier = Modifier
                        .clickable(onClick = onNavigateBack)
                        .padding(InstUILayoutSizes.Spacing.SpaceMd.spaceMd),
                )
            },
        )
        if (twoPane) {
            // Expanded width (e.g. landscape / tablet): show the supporting content as a side pane.
            val navigator = rememberSupportingPaneScaffoldNavigator()
            NavigableSupportingPaneScaffold(
                navigator = navigator,
                mainPane = {
                    AnimatedPane {
                        MainPaneContent(
                            uiState = uiState,
                            onTextChanged = viewModel::onTextChanged,
                            onDeleteDraft = viewModel::onDeleteDraft,
                            onSubmit = viewModel::onSubmit,
                        )
                    }
                },
                supportingPane = {
                    AnimatedPane {
                        SubmissionSupportingPane(
                            selectedIndex = selectedSegment,
                            onSegmentSelected = { selectedSegment = it },
                        )
                    }
                },
                modifier = Modifier.weight(1f),
            )
        } else {
            // Compact width (e.g. portrait phone): surface the supporting content as a bottom sheet.
            val scaffoldState = rememberBottomSheetScaffoldState()
            val scope = rememberCoroutineScope()
            BottomSheetScaffold(
                scaffoldState = scaffoldState,
                sheetPeekHeight = 96.dp,
                sheetContent = {
                    SubmissionSupportingPane(
                        selectedIndex = selectedSegment,
                        onSegmentSelected = { selectedSegment = it },
                        headerTrailing = {
                            val isExpanded = scaffoldState.bottomSheetState.currentValue == SheetValue.Expanded
                            val chevronIcon = if (isExpanded) InstUIIcons.chevronDown else InstUIIcons.chevronUp
                            val chevronContentDescription = stringResource(
                                if (isExpanded) R.string.a11y_submissionSupportingPaneCollapse
                                else R.string.a11y_submissionSupportingPaneExpand
                            )
                            Icon(
                                imageVector = chevronIcon,
                                contentDescription = chevronContentDescription,
                                modifier = Modifier.clickable {
                                    scope.launch {
                                        if (isExpanded) {
                                            scaffoldState.bottomSheetState.partialExpand()
                                        } else {
                                            scaffoldState.bottomSheetState.expand()
                                        }
                                    }
                                },
                            )
                        },
                    )
                },
                modifier = Modifier.weight(1f),
            ) { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding)) {
                    MainPaneContent(
                        uiState = uiState,
                        onTextChanged = viewModel::onTextChanged,
                        onDeleteDraft = viewModel::onDeleteDraft,
                        onSubmit = viewModel::onSubmit,
                    )
                }
            }
        }
    }
}

@Composable
private fun MainPaneContent(
    uiState: NGCSubmissionUiState,
    onTextChanged: (String) -> Unit,
    onDeleteDraft: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val courseColor = LocalCourseColor.current
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(InstUISemanticColors.Background.base())
            // Protect content from the display cutout on notched devices in landscape.
            // Horizontal only — the top/bottom cutout insets are already handled by the
            // top bar / scaffold, so including them here would add a gap below the toolbar.
            .windowInsetsPadding(WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal)),
    ) {
        when {
            uiState.loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = courseColor)
                }
            }
            uiState.loadError != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(InstUILayoutSizes.Spacing.SpaceLg.spaceLg),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = uiState.loadError,
                            color = InstUISemanticColors.Text.error(),
                        )
                        Spacer(Modifier.height(InstUILayoutSizes.Spacing.SpaceMd.spaceMd))
                        TextButton(
                            text = stringResource(R.string.ngc_submission_retry),
                            onClick = uiState.onRetry,
                        )
                    }
                }
            }
            else -> {
                SubmissionFormContent(
                    uiState = uiState,
                    onTextChanged = onTextChanged,
                    onDeleteDraft = onDeleteDraft,
                    onSubmit = onSubmit,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@Composable
private fun SubmissionFormContent(
    uiState: NGCSubmissionUiState,
    onTextChanged: (String) -> Unit,
    onDeleteDraft: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            containerColor = InstUISemanticColors.Background.base(),
            title = {
                Text(
                    text = stringResource(R.string.ngc_submission_deleteDraftDialogTitle),
                    style = InstUIText.contentImportant,
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.ngc_submission_deleteDraftDialogMessage),
                    style = InstUIText.content,
                )
            },
            confirmButton = {
                TextButton(
                    text = stringResource(R.string.ngc_submission_deleteDraftDialogConfirm),
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDeleteDraft()
                    },
                    contentColor = InstUISemanticColors.Text.error(),
                )
            },
            dismissButton = {
                TextButton(
                    text = stringResource(R.string.ngc_submission_deleteDraftDialogCancel),
                    onClick = { showDeleteConfirmDialog = false },
                )
            },
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            // Ensure the Submit button scrolls above the keyboard on short screens.
            .imePadding()
            .padding(InstUILayoutSizes.Spacing.SpaceLg.spaceLg),
        verticalArrangement = Arrangement.spacedBy(InstUILayoutSizes.Spacing.SpaceMd.spaceMd),
    ) {
        // Assignment title
        Heading(
            text = uiState.assignmentName,
            level = HeadingLevel.H2,
        )

        // Due date (hidden when blank)
        if (uiState.dueDateText.isNotBlank()) {
            Text(
                text = uiState.dueDateText,
                style = InstUIText.content,
                color = InstUISemanticColors.Text.muted(),
            )
        }

        // "New Submission" label + type selector row (non-interactive)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.ngc_submission_newSubmission),
                style = InstUIText.contentImportant,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(InstUILayoutSizes.Spacing.SpaceXs.spaceXs),
            ) {
                Text(
                    text = uiState.submissionTypeLabel,
                    style = InstUIText.content,
                )
                Icon(
                    imageVector = InstUIIcons.chevronDown,
                    contentDescription = null,
                    size = IconSize.Small,
                    color = IconColor.Muted,
                )
            }
        }

        // Bordered RCE text entry field
        SubmissionTextEntryField(
            html = uiState.draftHtml,
            contentResetKey = uiState.contentResetKey,
            onTextChanged = onTextChanged,
            modifier = Modifier.fillMaxWidth(),
        )

        // Saved-at + Delete draft row — only shown when a draft exists
        if (uiState.savedAtText.isNotBlank()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = uiState.savedAtText,
                    style = InstUIText.contentSmall,
                    color = InstUISemanticColors.Text.muted(),
                )
                // Icon + label act as a single clickable button.
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(InstUILayoutSizes.Spacing.SpaceXs.spaceXs),
                    modifier = Modifier
                        .clickable(role = Role.Button) { showDeleteConfirmDialog = true }
                        .padding(InstUILayoutSizes.Spacing.SpaceXs.spaceXs),
                ) {
                    Icon(
                        imageVector = InstUIIcons.trash2,
                        tint = InstUISemanticColors.Text.error(),
                        size = IconSize.Small,
                        contentDescription = null,
                    )
                    Text(
                        text = stringResource(R.string.ngc_submission_deleteDraft),
                        style = InstUIText.content,
                        color = InstUISemanticColors.Text.error(),
                    )
                }
            }
        }

        // Submit button
        Button(
            text = stringResource(R.string.ngc_submission_submit),
            onClick = onSubmit,
            enabled = uiState.submitEnabled,
            loading = uiState.submitting,
            modifier = Modifier.fillMaxWidth(),
        )

        // Inline error message — shown until the next submit attempt clears it
        if (uiState.errorMessage != null) {
            Text(
                text = uiState.errorMessage,
                style = InstUIText.content,
                color = InstUISemanticColors.Text.error(),
            )
        }
    }
}

// region Previews

@Preview(name = "Submission Main Pane — Loading", showBackground = true)
@Composable
private fun MainPaneLoadingPreview() {
    InstUITheme {
        MainPaneContent(
            uiState = NGCSubmissionUiState(loading = true),
            onTextChanged = {},
            onDeleteDraft = {},
            onSubmit = {},
        )
    }
}

@Preview(name = "Submission Main Pane — Loaded with draft", showBackground = true)
@Composable
private fun MainPaneLoadedPreview() {
    InstUITheme {
        MainPaneContent(
            uiState = NGCSubmissionUiState(
                loading = false,
                courseName = "Intro to Space Stations",
                assignmentName = "Write an essay about the International Space Station",
                dueDateText = "Due 06/25, 11:59 PM",
                submissionTypeLabel = "Text Entry",
                draftHtml = "<p>The ISS is a marvel of modern engineering...</p>",
                savedAtText = "Saved 06/19, 10:30 AM",
                submitEnabled = true,
            ),
            onTextChanged = {},
            onDeleteDraft = {},
            onSubmit = {},
        )
    }
}

@Preview(name = "Submission Main Pane — Load Error", showBackground = true)
@Composable
private fun MainPaneErrorPreview() {
    InstUITheme {
        MainPaneContent(
            uiState = NGCSubmissionUiState(
                loading = false,
                loadError = "Failed to load assignment",
            ),
            onTextChanged = {},
            onDeleteDraft = {},
            onSubmit = {},
        )
    }
}

@Preview(
    name = "Submission Main Pane — Loaded (Dark)",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun MainPaneLoadedDarkPreview() {
    InstUITheme {
        MainPaneContent(
            uiState = NGCSubmissionUiState(
                loading = false,
                courseName = "Intro to Space Stations",
                assignmentName = "Write an essay about the ISS",
                dueDateText = "Due 06/25, 11:59 PM",
                submissionTypeLabel = "Text Entry",
                draftHtml = "",
                savedAtText = "",
                submitEnabled = false,
            ),
            onTextChanged = {},
            onDeleteDraft = {},
            onSubmit = {},
        )
    }
}

// endregion

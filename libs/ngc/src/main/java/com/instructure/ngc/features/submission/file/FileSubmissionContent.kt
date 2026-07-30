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

package com.instructure.ngc.features.submission.file

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.instructure.instui.compose.InstUITheme
import com.instructure.instui.token.semantic.InstUILayoutSizes
import com.instructure.instui.token.semantic.InstUISemanticColors
import com.instructure.ngc.R
import com.instructure.ngc.features.submission.SubmissionFileState
import com.instructure.ngc.features.submission.SubmissionFileUiState

/**
 * Main-pane body for the ONLINE_UPLOAD submission type.
 *
 * When [files] is empty shows [SubmissionFileEmptyState]; once files are attached
 * renders the list of [SubmissionFileRow]s followed by an "Add another file"
 * affordance. Both the empty-state button and the "Add another file" affordance
 * invoke [onAddFile] to surface the source sheet.
 *
 * File-row delete/retry callbacks pass through to the ViewModel handlers.
 * Source-sheet wiring (launchers) is added in Phase 4; passing no-ops here is fine.
 *
 * When [enabled] is `false` all interactive affordances (add file, empty-state card,
 * per-file delete/retry) are visually muted and non-interactive.
 */
@Composable
fun FileSubmissionContent(
    files: List<SubmissionFileUiState>,
    onAddFile: () -> Unit,
    onFileRemoved: (id: Long) -> Unit,
    onRetryFile: (id: Long) -> Unit,
    modifier: Modifier = Modifier,
    allowedExtensions: List<String> = emptyList(),
    enabled: Boolean = true,
) {
    if (files.isEmpty()) {
        SubmissionFileEmptyState(
            onAddFile = onAddFile,
            allowedExtensions = allowedExtensions,
            enabled = enabled,
            modifier = modifier,
        )
    } else {
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(InstUILayoutSizes.Spacing.SpaceSm.spaceSm),
        ) {
            AllowedFileTypesText(allowedExtensions)

            // Maintain a display map so removed items can play their exit animation before
            // being dropped from the composition. Each entry maps file.id → (file, transitionState).
            val displayMap = remember {
                mutableStateMapOf<Long, Pair<SubmissionFileUiState, MutableTransitionState<Boolean>>>()
            }

            val currentIds = files.map { it.id }.toSet()
            SideEffect {
                files.forEach { file ->
                    if (!displayMap.containsKey(file.id)) {
                        // New file: start invisible and animate to visible (enter animation).
                        displayMap[file.id] = file to MutableTransitionState(false).also { it.targetState = true }
                    } else {
                        // Existing file: update data (state/progress may have changed).
                        displayMap[file.id] = file to displayMap[file.id]!!.second
                    }
                }
                // Removed files: trigger exit animation.
                displayMap.keys.filter { it !in currentIds }.forEach { removedId ->
                    displayMap[removedId]?.second?.targetState = false
                }
            }

            displayMap.entries.toList().forEach { (id, pair) ->
                val (file, transitionState) = pair
                key(id) {
                    // When exit animation has completed, remove from display map.
                    if (transitionState.isIdle && !transitionState.currentState) {
                        SideEffect { displayMap.remove(id) }
                    }
                    AnimatedVisibility(
                        visibleState = transitionState,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically(),
                    ) {
                        SubmissionFileRow(
                            file = file,
                            onDelete = onFileRemoved,
                            onRetry = onRetryFile,
                            enabled = enabled,
                        )
                    }
                }
            }

            AddFileButton(
                label = stringResource(R.string.ngc_submission_addAnotherFile),
                onClick = onAddFile,
                enabled = enabled,
            )
        }
    }
}

// region Previews

@Preview(name = "FileSubmissionContent — empty", showBackground = true)
@Composable
private fun FileSubmissionContentEmptyPreview() {
    InstUITheme {
        Column(
            modifier = Modifier
                .background(InstUISemanticColors.Background.base())
                .padding(InstUILayoutSizes.Spacing.SpaceLg.spaceLg),
        ) {
            FileSubmissionContent(
                files = emptyList(),
                onAddFile = {},
                onFileRemoved = {},
                onRetryFile = {},
            )
        }
    }
}

@Preview(name = "FileSubmissionContent — with files", showBackground = true)
@Composable
private fun FileSubmissionContentWithFilesPreview() {
    InstUITheme {
        Column(
            modifier = Modifier
                .background(InstUISemanticColors.Background.base())
                .padding(InstUILayoutSizes.Spacing.SpaceLg.spaceLg),
        ) {
            FileSubmissionContent(
                files = listOf(
                    SubmissionFileUiState(
                        id = 1,
                        name = "biology_essay.pdf",
                        size = 2_340_000,
                        contentType = "application/pdf",
                        fullPath = null,
                        state = SubmissionFileState.Ready,
                    ),
                    SubmissionFileUiState(
                        id = 2,
                        name = "diagram.png",
                        size = 512_000,
                        contentType = "image/png",
                        fullPath = null,
                        state = SubmissionFileState.InProgress(progress = 0.6f),
                    ),
                ),
                onAddFile = {},
                onFileRemoved = {},
                onRetryFile = {},
                allowedExtensions = listOf("pdf", "png"),
            )
        }
    }
}

@Preview(
    name = "FileSubmissionContent — with files (Dark)",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun FileSubmissionContentWithFilesDarkPreview() {
    InstUITheme {
        Column(
            modifier = Modifier
                .background(InstUISemanticColors.Background.base())
                .padding(InstUILayoutSizes.Spacing.SpaceLg.spaceLg),
        ) {
            FileSubmissionContent(
                files = listOf(
                    SubmissionFileUiState(
                        id = 1,
                        name = "notes.txt",
                        size = 4_096,
                        contentType = "text/plain",
                        fullPath = null,
                        state = SubmissionFileState.Error("Upload failed. Tap to retry."),
                    ),
                ),
                onAddFile = {},
                onFileRemoved = {},
                onRetryFile = {},
            )
        }
    }
}

// endregion

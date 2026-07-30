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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.instructure.instui.compose.container.Tray
import com.instructure.instui.compose.text.Heading
import com.instructure.instui.compose.text.HeadingLevel
import com.instructure.instui.token.semantic.InstUILayoutSizes
import com.instructure.ngc.R
import com.instructure.ngc.features.comments.SubmissionCommentsComponent

/**
 * Submission comments bottom sheet for the NGC Assignment Details screen. Mirrors [RubricBottomSheet]:
 * an InstUI [Tray] with a centered title, hosting the reusable [SubmissionCommentsComponent] which loads
 * and renders the comments and the comment input.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubmissionCommentsBottomSheet(
    courseId: Long,
    assignmentId: Long,
    studentId: Long,
    attemptId: Long?,
    onOpenFile: (url: String, contentType: String, fileName: String) -> Unit,
    onOpenMedia: (url: String, contentType: String, fileName: String, thumbnailUrl: String?) -> Unit,
    onDismiss: () -> Unit,
) {
    // The sheet is hard-capped below the status bar (see modifier below), so it can never animate
    // under it. Content fills that capped space, keeping visible height equal to content height.
    // Open fully expanded so the whole list is laid out immediately — otherwise the initial
    // auto-scroll to the latest comment lands in the clipped (below-the-fold) area.
    Tray(
        onDismiss = onDismiss,
        modifier = Modifier
            .testTag("ngcAssignmentDetails_commentsSheet")
            .windowInsetsPadding(WindowInsets.statusBars),
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        contentWindowInsets = { WindowInsets.navigationBars.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom) },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
        ) {
            Heading(
                text = stringResource(R.string.ngc_comments),
                level = HeadingLevel.H3,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(InstUILayoutSizes.Spacing.SpaceLg.spaceLg),
            )
            SubmissionCommentsComponent(
                courseId = courseId,
                assignmentId = assignmentId,
                studentId = studentId,
                attemptId = attemptId,
                modifier = Modifier.weight(1f),
                onOpenFile = onOpenFile,
                onOpenMedia = onOpenMedia,
            )
        }
    }
}

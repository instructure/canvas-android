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

package com.instructure.ngc.features.submission.supportingpane

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.instructure.ngc.features.comments.SubmissionCommentsComponent

/**
 * Comments pane of the NGC submission screen. Delegates to the reusable comments component so it shares the
 * same UI, ViewModel and data loading as the assignment-details comments bottom sheet.
 */
@Composable
fun SubmissionCommentsScreen(
    courseId: Long,
    assignmentId: Long,
    studentId: Long,
    attemptId: Long?,
    modifier: Modifier = Modifier,
    onOpenFile: (url: String, contentType: String, fileName: String) -> Unit = { _, _, _ -> },
    onOpenMedia: (url: String, contentType: String, fileName: String, thumbnailUrl: String?) -> Unit = { _, _, _, _ -> },
) {
    SubmissionCommentsComponent(
        courseId = courseId,
        assignmentId = assignmentId,
        studentId = studentId,
        attemptId = attemptId,
        modifier = modifier.fillMaxSize(),
        onOpenFile = onOpenFile,
        onOpenMedia = onOpenMedia,
    )
}

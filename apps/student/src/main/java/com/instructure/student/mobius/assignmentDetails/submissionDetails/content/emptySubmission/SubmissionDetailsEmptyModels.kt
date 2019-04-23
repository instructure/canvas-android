/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
package com.instructure.student.mobius.assignmentDetails.submissionDetails.content.emptySubmission

import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Course

sealed class SubmissionDetailsEmptyContentEvent {
    object SubmitAssignmentClicked : SubmissionDetailsEmptyContentEvent()
}

sealed class SubmissionDetailsEmptyContentEffect {
    data class ShowSubmitDialogView(val assignment: Assignment, val course: Course, val isArcEnabled: Boolean) : SubmissionDetailsEmptyContentEffect()
    data class ShowCreateSubmissionView(val submissionType: Assignment.SubmissionType, val courseId: Long, val assignment: Assignment) : SubmissionDetailsEmptyContentEffect()
}

data class SubmissionDetailsEmptyContentModel(
    val assignment: Assignment,
    val course: Course,
    val isArcEnabled: Boolean
)
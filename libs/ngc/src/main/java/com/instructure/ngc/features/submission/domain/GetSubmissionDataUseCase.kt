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
package com.instructure.ngc.features.submission.domain

import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.pandautils.data.repository.assignment.AssignmentRepository
import com.instructure.pandautils.data.repository.course.CourseRepository
import com.instructure.pandautils.features.submission.SubmissionUploadHelper
import javax.inject.Inject

class GetSubmissionDataUseCase @Inject constructor(
    private val assignmentRepository: AssignmentRepository,
    private val courseRepository: CourseRepository,
    private val submissionUploadHelper: SubmissionUploadHelper
) {
    suspend operator fun invoke(courseId: Long, assignmentId: Long): SubmissionData {
        val canvasContext = CanvasContext.emptyCourseContext(id = courseId)
        val assignment = assignmentRepository.getAssignmentWithHistory(courseId, assignmentId, false).dataOrThrow
        val courseName = courseRepository.getCourse(courseId, false).dataOrThrow.name.orEmpty()
        val draft = submissionUploadHelper.loadDraft(canvasContext, assignmentId, Assignment.SubmissionType.ONLINE_TEXT_ENTRY.apiString)

        val allowsTextEntry = assignment.getSubmissionTypes().contains(Assignment.SubmissionType.ONLINE_TEXT_ENTRY)
        val lastAttempt = assignment.lastGradedOrSubmittedSubmission?.attempt?.takeIf { it > 0 }

        return SubmissionData(
            assignmentName = assignment.name.orEmpty(),
            courseName = courseName,
            dueDate = assignment.dueDate,
            allowsTextEntry = allowsTextEntry,
            lastAttempt = lastAttempt,
            draftHtml = draft?.submissionEntry,
            draftSavedAt = draft?.lastActivityDate
        )
    }
}

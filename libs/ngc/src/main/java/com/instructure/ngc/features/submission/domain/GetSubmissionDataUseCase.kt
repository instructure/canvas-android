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
import com.instructure.ngc.features.rubric.buildNgcRubric
import com.instructure.pandautils.data.repository.assignment.AssignmentRepository
import com.instructure.pandautils.data.repository.course.CourseRepository
import com.instructure.pandautils.domain.usecase.BaseUseCase
import com.instructure.pandautils.features.submission.SubmissionUploadHelper
import com.instructure.pandautils.room.studentdb.entities.daos.CreateFileSubmissionDao
import java.util.Date
import javax.inject.Inject

class GetSubmissionDataUseCase @Inject constructor(
    private val assignmentRepository: AssignmentRepository,
    private val courseRepository: CourseRepository,
    private val submissionUploadHelper: SubmissionUploadHelper,
    private val createFileSubmissionDao: CreateFileSubmissionDao
) : BaseUseCase<GetSubmissionDataUseCase.Params, SubmissionData>() {

    data class Params(
        val courseId: Long,
        val assignmentId: Long,
        val submissionType: Assignment.SubmissionType,
    )

    private val ngcImplementedTypes = listOf(
        Assignment.SubmissionType.ONLINE_TEXT_ENTRY,
        Assignment.SubmissionType.ONLINE_UPLOAD,
        Assignment.SubmissionType.ONLINE_URL,
    )

    override suspend fun execute(params: Params): SubmissionData {
        val (courseId, assignmentId, submissionType) = params
        val canvasContext = CanvasContext.emptyCourseContext(id = courseId)
        val assignment = assignmentRepository.getAssignmentWithHistory(courseId, assignmentId, false).dataOrThrow
        val course = courseRepository.getCourseWithGrade(courseId, false).dataOrThrow
        val courseName = course.name.orEmpty()
        val rubric = buildNgcRubric(assignment, course.settings?.restrictQuantitativeData == true)

        val assignmentTypes = assignment.getSubmissionTypes()
        val availableTypes = assignmentTypes.filter { it in ngcImplementedTypes }

        val allowsTextEntry = assignmentTypes.contains(Assignment.SubmissionType.ONLINE_TEXT_ENTRY)
        val lastAttempt = assignment.lastGradedOrSubmittedSubmission?.attempt?.takeIf { it > 0 }

        val payload = when (submissionType) {
            Assignment.SubmissionType.ONLINE_UPLOAD -> {
                val fileDraft = submissionUploadHelper.loadDraft(
                    canvasContext, assignmentId, Assignment.SubmissionType.ONLINE_UPLOAD.apiString
                )
                val files = if (fileDraft != null) {
                    createFileSubmissionDao.findFilesForSubmissionId(fileDraft.id).map { it.toSubmissionFileData() }
                } else {
                    emptyList()
                }
                DraftPayload(draftFiles = files, fileDraftSavedAt = fileDraft?.lastActivityDate)
            }
            Assignment.SubmissionType.ONLINE_URL -> {
                val urlDraft = submissionUploadHelper.loadDraft(
                    canvasContext, assignmentId, Assignment.SubmissionType.ONLINE_URL.apiString
                )
                DraftPayload(draftUrl = urlDraft?.submissionEntry, urlDraftSavedAt = urlDraft?.lastActivityDate)
            }
            else -> {
                val textDraft = submissionUploadHelper.loadDraft(
                    canvasContext, assignmentId, Assignment.SubmissionType.ONLINE_TEXT_ENTRY.apiString
                )
                DraftPayload(draftHtml = textDraft?.submissionEntry, draftSavedAt = textDraft?.lastActivityDate)
            }
        }

        return SubmissionData(
            assignmentName = assignment.name.orEmpty(),
            courseName = courseName,
            dueDate = assignment.dueDate,
            allowsTextEntry = allowsTextEntry,
            lastAttempt = lastAttempt,
            draftHtml = payload.draftHtml,
            draftSavedAt = payload.draftSavedAt,
            availableTypes = availableTypes,
            draftFiles = payload.draftFiles,
            fileDraftSavedAt = payload.fileDraftSavedAt,
            allowedExtensions = assignment.allowedExtensions,
            instructionsHtml = assignment.description,
            draftUrl = payload.draftUrl,
            urlDraftSavedAt = payload.urlDraftSavedAt,
            rubricUiState = rubric,
        )
    }

    private data class DraftPayload(
        val draftHtml: String? = null,
        val draftSavedAt: Date? = null,
        val draftFiles: List<SubmissionFileData> = emptyList(),
        val fileDraftSavedAt: Date? = null,
        val draftUrl: String? = null,
        val urlDraftSavedAt: Date? = null,
    )
}

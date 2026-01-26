/*
 * Copyright (C) 2019 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.student.mobius.assignmentDetails.submissionDetails

import android.content.Context
import com.instructure.student.R
import com.instructure.student.mobius.assignmentDetails.getFormattedAttemptDate
import com.instructure.student.mobius.assignmentDetails.submissionDetails.ui.SubmissionDetailsTabData
import com.instructure.student.mobius.assignmentDetails.submissionDetails.ui.SubmissionDetailsViewState
import com.instructure.student.mobius.common.ui.Presenter


object SubmissionDetailsPresenter : Presenter<SubmissionDetailsModel, SubmissionDetailsViewState> {
    override fun present(model: SubmissionDetailsModel, context: Context): SubmissionDetailsViewState {
        if (model.isLoading) return SubmissionDetailsViewState.Loading
        if (model.assignmentResult?.isSuccess != true || model.rootSubmissionResult?.isSuccess != true) return SubmissionDetailsViewState.Error

        val rootSubmission = model.rootSubmissionResult.dataOrThrow
        val assignment = model.assignmentResult.dataOrThrow

        model.submissionComments?.let {
            rootSubmission.submissionComments = it
        }

        val validSubmissions = rootSubmission.submissionHistory
            .filterNotNull()
            .sortedByDescending { it.submittedAt }

        // Check if any attempt number is missing or invalid
        val hasAnyMissingAttemptNumber = validSubmissions.any { it.attempt == 0L }

        // Create submission to attempt number mapping
        val submissionToAttemptMap = if (hasAnyMissingAttemptNumber) {
            // Re-index from newest (highest) to 1
            validSubmissions.mapIndexed { index, submission ->
                submission to (validSubmissions.size - index).toLong()
            }.toMap()
        } else {
            // Use original attempt numbers
            validSubmissions.associateWith { it.attempt }
        }

        val selectedSubmission = if (hasAnyMissingAttemptNumber) {
            // Find by position in the sorted list
            val attemptNumber = model.selectedSubmissionAttempt
            validSubmissions.firstOrNull { submissionToAttemptMap[it] == attemptNumber }
        } else {
            validSubmissions.firstOrNull { it.attempt == model.selectedSubmissionAttempt }
        }

        val submissionVersions: List<Pair<Long, String>> = validSubmissions.map { submission ->
            val attemptNumber = submissionToAttemptMap[submission] ?: submission.attempt
            attemptNumber to submission.submittedAt?.let { getFormattedAttemptDate(it) }.orEmpty()
        }

        val selectedVersionIdx = submissionVersions
            .indexOfFirst { it.first == model.selectedSubmissionAttempt }
            .coerceAtLeast(0)

        val tabData = mutableListOf<SubmissionDetailsTabData>()

        // Comments tab
        tabData += SubmissionDetailsTabData.CommentData(
            name = context.getString(R.string.comments),
            assignment = assignment,
            submission = rootSubmission,
            attemptId = model.selectedSubmissionAttempt,
            assignmentEnhancementsEnabled = model.assignmentEnhancementsEnabled
        )

        // Files tab
        with (selectedSubmission?.attachments ?: ArrayList()) {
            val name = if (isEmpty()) {
                context.getString(R.string.files)
            } else {
                context.getString(R.string.submissionDetailsFileTabName, size)
            }
            tabData += SubmissionDetailsTabData.FileData(
                name = name,
                files = this,
                selectedFileId = model.selectedAttachmentId ?: getOrNull(0)?.id ?: 0,
                canvasContext = model.canvasContext
            )
        }

        // Rubric tab
        tabData += SubmissionDetailsTabData.RubricData(
            name = context.getString(R.string.rubric),
            assignment = assignment,
            submission = rootSubmission,
            restrictQuantitativeData = model.restrictQuantitativeData
        )

        return SubmissionDetailsViewState.Loaded(
            showVersionsSpinner = submissionVersions.size > 1,
            selectedVersionSpinnerIndex = selectedVersionIdx,
            submissionVersions = submissionVersions,
            tabData = tabData
        )
    }
}

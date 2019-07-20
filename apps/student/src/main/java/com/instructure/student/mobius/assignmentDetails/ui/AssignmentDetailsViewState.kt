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
package com.instructure.student.mobius.assignmentDetails.ui

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import com.instructure.canvasapi2.models.Assignment
import com.instructure.student.mobius.assignmentDetails.SubmissionUploadStatus
import com.instructure.student.mobius.assignmentDetails.ui.gradeCell.GradeCellViewState
import java.util.*

sealed class AssignmentDetailsViewState(val visibilities: AssignmentDetailsVisibilities) {
    object Loading : AssignmentDetailsViewState(AssignmentDetailsVisibilities(loading = true))

    object Error : AssignmentDetailsViewState(AssignmentDetailsVisibilities(errorMessage = true))

    data class Loaded(
        val assignmentName: String,
        val assignmentPoints: String,
        val assignmentPointsA11yText: String,
        val submittedStateLabel: String,
        @ColorInt val submittedStateColor: Int,
        @DrawableRes val submittedStateIcon: Int,
        val submissionUploadStatus: SubmissionUploadStatus = SubmissionUploadStatus.Empty,
        val dueDate: String = "",
        val lockMessage: String = "",
        val submissionTypes: String = "",
        val fileTypes: String = "",
        val description: String = "",
        val descriptionLabel: String = "",
        val submitButtonText: String = "",
        val gradeState: GradeCellViewState = GradeCellViewState.Empty,
        val assignmentDetailsVisibilities: AssignmentDetailsVisibilities,
        val isExternalToolSubmission: Boolean = false,
        val quizDescriptionViewState: QuizDescriptionViewState? = null,
        val discussionHeaderViewState: DiscussionHeaderViewState? = null
    ) : AssignmentDetailsViewState(assignmentDetailsVisibilities)
}

data class AssignmentDetailsVisibilities (
    var loading: Boolean = false,
    var errorMessage: Boolean = false,
    var title: Boolean = false,
    var dueDate: Boolean = false,
    var submissionTypes: Boolean = false,
    var fileTypes: Boolean = false,
    var submissionAndRubricButton: Boolean = false,
    var grade: Boolean = false,
    var lockedMessage: Boolean = false,
    var lockedImage: Boolean = false,
    var noDescriptionLabel: Boolean = false,
    var description: Boolean = false,
    var submitButton: Boolean = false,
    var submissionUploadStatus: Boolean = false,
    var quizDetails: Boolean = false,
    var discussionTopicHeader: Boolean = false
)

data class SubmissionTypesVisibilities(
    var textEntry: Boolean = false,
    var urlEntry: Boolean = false,
    var fileUpload: Boolean = false,
    var mediaRecording: Boolean = false,
    var arcUpload: Boolean = false
)

data class QuizDescriptionViewState(
    val questionCount: String,
    val timeLimit: String,
    val allowedAttempts: String
)

data class DiscussionHeaderViewState(
    val authorAvatarUrl: String? = null,
    val authorName: String,
    val authoredDate: String,
    val attachmentIconVisibility: Boolean
)

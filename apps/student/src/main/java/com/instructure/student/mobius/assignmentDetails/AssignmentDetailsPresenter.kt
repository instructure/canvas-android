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
package com.instructure.student.mobius.assignmentDetails

import android.content.Context
import androidx.core.content.ContextCompat
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.canvasapi2.utils.NumberHelper
import com.instructure.canvasapi2.utils.isRtl
import com.instructure.canvasapi2.utils.isValid
import com.instructure.student.R
import com.instructure.student.mobius.assignmentDetails.ui.AssignmentDetailsViewState
import com.instructure.student.mobius.assignmentDetails.ui.AssignmentDetailsVisibilities
import com.instructure.student.mobius.assignmentDetails.ui.SubmissionTypesVisibilities
import com.instructure.student.mobius.assignmentDetails.ui.gradeCell.GradeCellViewState
import com.instructure.student.mobius.common.ui.Presenter
import java.text.DateFormat
import java.util.*

object AssignmentDetailsPresenter : Presenter<AssignmentDetailsModel, AssignmentDetailsViewState> {
    override fun present(model: AssignmentDetailsModel, context: Context): AssignmentDetailsViewState {
        // Loading state
        if (model.isLoading) return AssignmentDetailsViewState.Loading

        // Failed state
        if (model.assignmentResult == null || model.assignmentResult.isFail) {
            return AssignmentDetailsViewState.Error
        }

        val assignment = model.assignmentResult.dataOrNull!!

        // Loaded state
        return presentLoadedState(assignment, context)
    }

    private fun presentLoadedState(
        assignment: Assignment,
        context: Context
    ): AssignmentDetailsViewState.Loaded {
        val visibilities = AssignmentDetailsVisibilities()

        // Assignment name and points
        visibilities.title = true
        val points = context.resources.getQuantityString(
            R.plurals.quantityPointsAbbreviated,
            assignment.pointsPossible.toInt(),
            NumberHelper.formatDecimal(assignment.pointsPossible, 1, true)
        )
        val pointsA11y = context.resources.getQuantityString(
            R.plurals.quantityPointsFull,
            assignment.pointsPossible.toInt(),
            NumberHelper.formatDecimal(assignment.pointsPossible, 1, true)
        )

        // Submission state
        val submitted = assignment.isSubmitted
        val (submittedLabelRes, submittedColorRes, submittedIconRes) = if (submitted) {
            Triple(R.string.submitted, R.color.alertGreen, R.drawable.vd_submitted)
        } else {
            Triple(R.string.notSubmitted, R.color.defaultTextGray, R.drawable.vd_unsubmitted)
        }
        val submittedLabel = context.getString(submittedLabelRes)
        val submittedColor = ContextCompat.getColor(context, submittedColorRes)

        // Fully locked state (hide most details)
        if (assignment.isLocked) {
            return makeLockedState(
                visibilities,
                assignment,
                context,
                points,
                pointsA11y,
                submittedLabel,
                submittedColor,
                submittedIconRes
            )
        }

        // Partial locked state (availability date has passed; show details and the lock explanation)
        val lockMessage =
            assignment.lockExplanation.takeIf { it.isValid() && assignment.lockDate?.before(Date()) == true }
        visibilities.lockedMessage = lockMessage.isValid()

        // Due date
        visibilities.dueDate = true
        val dueDate = if (assignment.dueDate == null) {
            context.getString(R.string.noDueDate)
        } else {
            DateHelper.getMonthDayTimeMaybeMinutesMaybeYear(context, assignment.dueDate, R.string.at)!!
        }

        // Submission/Rubric button
        visibilities.submissionAndRubricButton = submitted

        // Description
        val description = if (assignment.description.isValid()) {
            visibilities.description = true
            if (Locale.getDefault().isRtl) {
                "<body dir=\"rtl\">${assignment.description}</body>"
            } else {
                assignment.description
            }
        } else {
            visibilities.noDescriptionLabel = true
            ""
        }

        // Submission types
        visibilities.submissionTypes = true
        val submissionTypes = assignment.getSubmissionTypes()
            .map { Assignment.submissionTypeToPrettyPrintString(it, context) }
            .joinToString(", ")

        // File types
        visibilities.fileTypes = assignment.allowedExtensions.isNotEmpty()
        val fileTypes = assignment.allowedExtensions.joinToString(", ")


        // SubmitButton TODO - Check logic around enabling this
        visibilities.submitButton = assignment.isAllowedToSubmit
        val submitButtonText = context.getString(
            if (submitted) R.string.resubmitAssignment else R.string.submitAssignment
        )

        val gradeState = GradeCellViewState.fromSubmission(context, assignment, assignment.submission)
        visibilities.grade = gradeState != GradeCellViewState.Empty

        return AssignmentDetailsViewState.Loaded(
            assignmentName = assignment.name.orEmpty(),
            assignmentPoints = points,
            assignmentPointsA11yText = pointsA11y,
            submittedStateLabel = submittedLabel,
            submittedStateColor = submittedColor,
            submittedStateIcon = submittedIconRes,
            lockMessage = lockMessage.orEmpty(),
            dueDate = dueDate,
            submissionTypes = submissionTypes,
            fileTypes = fileTypes,
            description = description.orEmpty(),
            submitButtonText = submitButtonText,
            gradeState = gradeState,
            assignmentDetailsVisibilities = visibilities,
            submissionTypesVisibilities = getSubmissionTypesVisibilities(assignment)
        )
    }

    private fun getSubmissionTypesVisibilities(assignment: Assignment) : SubmissionTypesVisibilities {
        val visibilities = SubmissionTypesVisibilities()

        val submissionTypes = assignment.getSubmissionTypes()

        for (submissionType in submissionTypes) {
            when(submissionType) {
                Assignment.SubmissionType.ONLINE_UPLOAD -> visibilities.fileUpload = true
                Assignment.SubmissionType.ONLINE_TEXT_ENTRY -> visibilities.textEntry = true
                Assignment.SubmissionType.ONLINE_URL -> visibilities.urlEntry = true
                Assignment.SubmissionType.MEDIA_RECORDING -> visibilities.mediaRecording = true
            }
        }

        return visibilities
    }

    private fun makeLockedState(
        visibilities: AssignmentDetailsVisibilities,
        assignment: Assignment,
        context: Context,
        points: String,
        pointsA11y: String,
        submittedLabel: String,
        submittedColor: Int,
        submittedIconRes: Int
    ): AssignmentDetailsViewState.Loaded {
        visibilities.lockedMessage = true
        visibilities.lockedImage = true
        val unlockDate = assignment.unlockDate
        val lockMessage = if (unlockDate != null) {
            val dateString = DateFormat.getDateInstance().format(unlockDate)
            val timeString = DateFormat.getTimeInstance(DateFormat.SHORT).format(unlockDate)
            context.getString(R.string.lockedSubtext, dateString, timeString)
        } else {
            val name = assignment.lockInfo?.lockedModuleName
            context.getString(R.string.lockedModule, name)
        }
        return AssignmentDetailsViewState.Loaded(
            assignmentName = assignment.name.orEmpty(),
            assignmentPoints = points,
            assignmentPointsA11yText = pointsA11y,
            submittedStateLabel = submittedLabel,
            submittedStateColor = submittedColor,
            submittedStateIcon = submittedIconRes,
            lockMessage = lockMessage,
            assignmentDetailsVisibilities = visibilities
        )
    }

}

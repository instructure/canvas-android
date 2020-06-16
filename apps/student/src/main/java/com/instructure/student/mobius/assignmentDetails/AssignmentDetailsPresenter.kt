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
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.Quiz
import com.instructure.canvasapi2.models.isDiscussionAuthorNull
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.canvasapi2.utils.NumberHelper
import com.instructure.canvasapi2.utils.isRtl
import com.instructure.canvasapi2.utils.isValid
import com.instructure.pandautils.discussions.DiscussionUtils
import com.instructure.pandautils.utils.AssignmentUtils2
import com.instructure.student.R
import com.instructure.student.Submission
import com.instructure.student.mobius.assignmentDetails.ui.AssignmentDetailsViewState
import com.instructure.student.mobius.assignmentDetails.ui.AssignmentDetailsVisibilities
import com.instructure.student.mobius.assignmentDetails.ui.DiscussionHeaderViewState
import com.instructure.student.mobius.assignmentDetails.ui.QuizDescriptionViewState
import com.instructure.student.mobius.assignmentDetails.ui.gradeCell.GradeCellViewState
import com.instructure.student.mobius.common.ui.Presenter
import java.text.DateFormat
import java.util.Date
import java.util.Locale

object AssignmentDetailsPresenter : Presenter<AssignmentDetailsModel, AssignmentDetailsViewState> {
    override fun present(model: AssignmentDetailsModel, context: Context): AssignmentDetailsViewState {
        // Loading state
        if (model.isLoading) return AssignmentDetailsViewState.Loading

        // Failed state
        if (model.assignmentResult == null || model.assignmentResult.isFail){
            return AssignmentDetailsViewState.Error
        }

        // Failed Quiz state
        if(model.assignmentResult.dataOrNull?.turnInType == Assignment.TurnInType.QUIZ
                && (model.quizResult == null || model.quizResult.isFail)) {
            return AssignmentDetailsViewState.Error
        }

        val assignment = model.assignmentResult.dataOrNull!!
        assignment.isStudioEnabled = model.isStudioEnabled

        val quiz = model.quizResult?.dataOrNull

        // Loaded state
        return presentLoadedState(assignment, quiz, model.databaseSubmission, context, model.isObserver)
    }

    private fun presentLoadedState(
        assignment: Assignment,
        quiz: Quiz?,
        databaseSubmission: Submission?,
        context: Context,
        isObserver: Boolean
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

        // Submission state - Some of this may be hidden by the visibility above
        val assignmentState = AssignmentUtils2.getAssignmentState(assignment, assignment.submission, false)
        val (submittedLabelRes, submittedColorRes, submittedIconRes) = if (assignment.isSubmitted) {
            Triple(
                if (assignmentState == AssignmentUtils2.ASSIGNMENT_STATE_GRADED) R.string.gradedSubmissionLabel else R.string.submitted,
                R.color.alertGreen,
                R.drawable.vd_submitted
            )
        } else {
            // Don't mark LTI assignments as missing when overdue as they usually won't have a real submission for it
            val isMissingFromDueDate = assignment.turnInType != Assignment.TurnInType.EXTERNAL_TOOL
                && assignment.dueAt != null
                && assignmentState == AssignmentUtils2.ASSIGNMENT_STATE_MISSING

            if (assignment.submission?.missing == true || isMissingFromDueDate) {
                // Mark it missing if the teacher marked it missing or if it's past due
                Triple(R.string.missingSubmissionLabel, R.color.submissionStatusColorMissing, R.drawable.vd_unsubmitted)
            } else {
                Triple(R.string.notSubmitted, R.color.defaultTextGray, R.drawable.vd_unsubmitted)
            }
        }

        // Submission Status under title - We only show Graded or nothing at all for PAPER/NONE
        visibilities.submissionStatus = if (assignmentState == AssignmentUtils2.ASSIGNMENT_STATE_GRADED) {
            true
        } else {
            assignment.turnInType != Assignment.TurnInType.ON_PAPER && assignment.turnInType != Assignment.TurnInType.NONE
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
        val lockMessage = assignment.lockExplanation.takeIf { it.isValid() && assignment.lockDate?.before(Date()) == true }
        visibilities.lockedMessage = lockMessage.isValid()

        // Due date
        visibilities.dueDate = true
        val dueDate = if (assignment.dueDate == null) {
            context.getString(R.string.toDoNoDueDate)
        } else {
            DateHelper.getMonthDayTimeMaybeMinutesMaybeYear(context, assignment.dueDate, R.string.at)!!
        }

        // Submission/Rubric button
        visibilities.submissionAndRubricButton = true // Always show the submission and rubric button

        // Description
        val description = if (assignment.turnInType == Assignment.TurnInType.DISCUSSION && getDiscussionText(assignment.discussionTopicHeader!!).isNotEmpty()) {
            visibilities.description = true
            getDiscussionText(assignment.discussionTopicHeader!!)
        } else if (assignment.description.isValid()) {
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
        visibilities.submissionTypes = when(assignment.turnInType) {
            Assignment.TurnInType.ONLINE -> true
            else -> false // Discussions / On Paper / Quizzes
        }

        val submissionTypes = assignment.getSubmissionTypes()
            .map { Assignment.submissionTypeToPrettyPrintString(it, context) }
            .joinToString(", ")

        val isExternalToolSubmission = assignment.turnInType == Assignment.TurnInType.EXTERNAL_TOOL

        // File types
        visibilities.fileTypes = assignment.allowedExtensions.isNotEmpty() && assignment.getSubmissionTypes().contains(Assignment.SubmissionType.ONLINE_UPLOAD)
        val fileTypes = assignment.allowedExtensions.joinToString(", ")

        // Handle attempt limits (only show attempt details if it's not unlimited, disable the submit button if they're out of attempts)
        visibilities.allowedAttempts = assignment.allowedAttempts != -1L
        visibilities.submitButtonEnabled = assignment.allowedAttempts == -1L || (assignment.submission?.attempt?.let{ it < assignment.allowedAttempts } ?: true)

        if (isObserver) {
            // Observers shouldn't see the submit button
            visibilities.submitButton = false
        } else {
            //Configure stickied submit button visibility state,
            visibilities.submitButton = when(assignment.turnInType) {
                // We always show the button for quizzes and discussions, so the users can always route
                Assignment.TurnInType.QUIZ, Assignment.TurnInType.DISCUSSION -> true
                Assignment.TurnInType.ONLINE, Assignment.TurnInType.EXTERNAL_TOOL -> assignment.isAllowedToSubmit
                else -> false // On Paper / etc
            }
        }

        // Configure stickied submit button
        val submitButtonText = getSubmitButtonText(context, isExternalToolSubmission, assignment.isSubmitted, assignment.turnInType, visibilities.submitButtonEnabled)

        // Configure description label
        val descriptionLabel = when(assignment.turnInType) {
            Assignment.TurnInType.QUIZ -> context.getString(R.string.instructions)
            else -> context.getString(R.string.description)
        }

        // Configure quiz specific details
        visibilities.quizDetails = assignment.turnInType == Assignment.TurnInType.QUIZ
        val quizDescriptionViewState = if (assignment.turnInType == Assignment.TurnInType.QUIZ) {
            getQuizDescriptionViewState(context, quiz!!)
        } else null

        // Configure discussion specific description/topic header view
        visibilities.discussionTopicHeader = assignment.turnInType == Assignment.TurnInType.DISCUSSION
        val discussionHeaderViewState = if (assignment.turnInType == Assignment.TurnInType.DISCUSSION) {
            getDiscussionHeaderViewState(context, assignment.discussionTopicHeader!!)
        } else null

        // Show the grade cell if there's not a database submission and the grade state isn't empty
        val gradeState = GradeCellViewState.fromSubmission(context, assignment, assignment.submission)
        if (databaseSubmission == null) {
            visibilities.grade = gradeState != GradeCellViewState.Empty
        } else {
            visibilities.grade = gradeState is GradeCellViewState.GradeData
            visibilities.submissionUploadStatusInProgress = !databaseSubmission.errorFlag
            visibilities.submissionUploadStatusFailed = databaseSubmission.errorFlag
        }

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
            descriptionLabel = descriptionLabel,
            submitButtonText = submitButtonText,
            gradeState = gradeState,
            assignmentDetailsVisibilities = visibilities,
            isExternalToolSubmission = isExternalToolSubmission,
            quizDescriptionViewState = quizDescriptionViewState,
            discussionHeaderViewState = discussionHeaderViewState,
            allowedAttempts = assignment.allowedAttempts,
            usedAttempts = assignment.submission?.attempt ?: 0
        )
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
        visibilities.submissionAndRubricButton = true
        val unlockDate = assignment.unlockDate
        val lockMessage = if (assignment.lockInfo?.contextModule != null) {
            val name = assignment.lockInfo?.lockedModuleName
            context.getString(R.string.lockedModule, name)
        } else {
            val dateString = DateFormat.getDateInstance().format(unlockDate)
            val timeString = DateFormat.getTimeInstance(DateFormat.SHORT).format(unlockDate)
            context.getString(R.string.lockedSubtext, dateString, timeString)
        }
        return AssignmentDetailsViewState.Loaded(
            assignmentName = assignment.name.orEmpty(),
            assignmentPoints = points,
            assignmentPointsA11yText = pointsA11y,
            submittedStateLabel = submittedLabel,
            submittedStateColor = submittedColor,
            submittedStateIcon = submittedIconRes,
            lockMessage = lockMessage,
            assignmentDetailsVisibilities = visibilities,
            allowedAttempts = assignment.allowedAttempts,
            usedAttempts = assignment.submission?.attempt ?: 0
        )
    }

    private fun getQuizDescriptionViewState(context: Context, quiz: Quiz): QuizDescriptionViewState {
        val questionCount = NumberHelper.formatInt(quiz.questionCount.toLong())

        val timeLimit = if (quiz.timeLimit != 0) {
            context.getString(R.string.timeLimit)
            NumberHelper.formatInt(quiz.timeLimit.toLong())
        } else {
            context.getString(R.string.quizNoTimeLimit)
        }

        val allowedAttempts = if (quiz.allowedAttempts == -1) {
            context.getString(R.string.unlimited)
        } else {
            NumberHelper.formatInt(quiz.allowedAttempts.toLong())
        }

        return QuizDescriptionViewState(questionCount, timeLimit, allowedAttempts)
    }

    private fun getDiscussionHeaderViewState(context: Context, discussionTopicHeader: DiscussionTopicHeader): DiscussionHeaderViewState {
        return if (discussionTopicHeader.author.isDiscussionAuthorNull()) {
            DiscussionHeaderViewState.NoAuthor
        } else {
            val authorAvatarUrl = discussionTopicHeader.author?.avatarImageUrl
            // Can't have a discussion topic header with a null author or date
            val authorName = discussionTopicHeader.author?.displayName ?: context.getString(R.string.discussions_unknown_author)
            val authorPronouns = discussionTopicHeader.author?.pronouns
            val authoredDate = DateHelper.getMonthDayAtTime(context, discussionTopicHeader.postedDate, context.getString(R.string.at)) ?: context.getString(R.string.discussions_unknown_date)
            val attachmentIconVisibility = discussionTopicHeader.attachments.isNotEmpty()

            DiscussionHeaderViewState.Loaded(
                authorAvatarUrl,
                authorName,
                authorPronouns,
                authoredDate,
                attachmentIconVisibility
            )
        }
    }

    private fun getDiscussionText(discussionTopicHeader: DiscussionTopicHeader): String {
        return when {
            discussionTopicHeader.message.isValid() -> discussionTopicHeader.message!!
            discussionTopicHeader.title.isValid() -> discussionTopicHeader.title!!
            else -> ""
        }
    }

    private fun getSubmitButtonText(context: Context, isExternalToolSubmission: Boolean, submitted: Boolean, turnInType: Assignment.TurnInType, submitEnabled: Boolean): String {
        return context.getString(
                when {
                    !submitEnabled -> R.string.noAttemptsLeft
                    turnInType == Assignment.TurnInType.QUIZ -> R.string.viewQuiz
                    turnInType == Assignment.TurnInType.DISCUSSION -> R.string.viewDiscussion
                    isExternalToolSubmission -> R.string.launchExternalTool
                    submitted -> R.string.resubmitAssignment
                    else -> R.string.submitAssignment
                }
        )
    }

}

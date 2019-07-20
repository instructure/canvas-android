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
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.canvasapi2.utils.NumberHelper
import com.instructure.canvasapi2.utils.isRtl
import com.instructure.canvasapi2.utils.isValid
import com.instructure.pandautils.discussions.DiscussionUtils
import com.instructure.student.R
import com.instructure.student.Submission
import com.instructure.student.mobius.assignmentDetails.ui.AssignmentDetailsViewState
import com.instructure.student.mobius.assignmentDetails.ui.AssignmentDetailsVisibilities
import com.instructure.student.mobius.assignmentDetails.ui.DiscussionHeaderViewState
import com.instructure.student.mobius.assignmentDetails.ui.QuizDescriptionViewState
import com.instructure.student.mobius.assignmentDetails.ui.gradeCell.GradeCellViewState
import com.instructure.student.mobius.common.ui.Presenter
import java.text.DateFormat
import java.util.*

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
        assignment.isArcEnabled = model.isArcEnabled

        val quiz = model.quizResult?.dataOrNull

        // Loaded state
        return presentLoadedState(assignment, quiz, model.databaseSubmission, context)
    }

    private fun presentLoadedState(
        assignment: Assignment,
        quiz: Quiz?,
        databaseSubmission: Submission?,
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
        val lockMessage = assignment.lockExplanation.takeIf { it.isValid() && assignment.lockDate?.before(Date()) == true }
        visibilities.lockedMessage = lockMessage.isValid()

        // Due date
        visibilities.dueDate = true
        val dueDate = if (assignment.dueDate == null) {
            context.getString(R.string.noDueDate)
        } else {
            DateHelper.getMonthDayTimeMaybeMinutesMaybeYear(context, assignment.dueDate, R.string.at)!!
        }

        // Submission/Rubric button
        visibilities.submissionAndRubricButton = true // Always show the submission and rubric button

        // Description
        val description = if (assignment.turnInType == Assignment.TurnInType.DISCUSSION && getDiscussionText(assignment.discussionTopicHeader!!).isNotEmpty()) {
            visibilities.description = true
            DiscussionUtils.createDiscussionTopicHeaderHtml(
                context,
                context.resources.getBoolean(R.bool.isDeviceTablet),
                getDiscussionText(assignment.discussionTopicHeader!!),
                null
            )
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

        val isExternalToolSubmission = assignment.getSubmissionTypes()
            .any { it == Assignment.SubmissionType.EXTERNAL_TOOL || it == Assignment.SubmissionType.BASIC_LTI_LAUNCH }

        // File types
        visibilities.fileTypes = assignment.allowedExtensions.isNotEmpty() && assignment.getSubmissionTypes().contains(Assignment.SubmissionType.ONLINE_UPLOAD)
        val fileTypes = assignment.allowedExtensions.joinToString(", ")


        //Configure stickied submit button visibility state,
        visibilities.submitButton = when(assignment.turnInType) {
            // We always show the button for quizzes and discussions, so the users can always route
            Assignment.TurnInType.QUIZ -> true
            Assignment.TurnInType.DISCUSSION -> true
            Assignment.TurnInType.ONLINE -> assignment.isAllowedToSubmit
            else -> false // On Paper / etc
        }

        // Configure stickied submit button
        val submitButtonText = when(assignment.turnInType) {
            Assignment.TurnInType.QUIZ -> context.getString(R.string.viewQuiz)
            Assignment.TurnInType.ONLINE -> getAssignmentSubmitButtonText(context, isExternalToolSubmission, submitted)
            else -> context.getString(R.string.viewDiscussion)
        }

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
            discussionHeaderViewState = discussionHeaderViewState
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
        val authorAvatarUrl = discussionTopicHeader.author?.avatarImageUrl
        // Can't have a discussion topic header with a null author or date
        val authorName = discussionTopicHeader.author!!.displayName!!
        val authoredDate = DateHelper.getMonthDayAtTime(context, discussionTopicHeader.postedDate, context.getString(R.string.at))!!
        val attachmentIconVisibility = discussionTopicHeader.attachments.isNotEmpty()

        return DiscussionHeaderViewState(authorAvatarUrl, authorName, authoredDate, attachmentIconVisibility)
    }

    private fun getDiscussionText(discussionTopicHeader: DiscussionTopicHeader): String {
        return if (discussionTopicHeader.message.isNullOrEmpty()) {
            discussionTopicHeader.title ?: ""
        } else ""
    }

    private fun getAssignmentSubmitButtonText(context: Context, isExternalToolSubmission: Boolean, submitted: Boolean): String {
        return context.getString(
                when {
                    isExternalToolSubmission -> R.string.launchExternalTool
                    submitted -> R.string.resubmitAssignment
                    else -> R.string.submitAssignment
                }
        )
    }

}

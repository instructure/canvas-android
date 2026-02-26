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

import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.utils.APIHelper
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.correctAttemptNumbers
import com.instructure.canvasapi2.utils.exhaustive
import com.instructure.pandautils.utils.orDefault
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.comments.SubmissionCommentsSharedEvent
import com.instructure.student.mobius.assignmentDetails.submissionDetails.ui.SubmissionDetailsView
import com.instructure.student.mobius.common.FlowSource
import com.instructure.student.mobius.common.trySend
import com.instructure.student.mobius.common.ui.EffectHandler
import com.instructure.student.util.getStudioLTITool
import kotlinx.coroutines.launch
import java.io.File

class SubmissionDetailsEffectHandler(
    private val repository: SubmissionDetailsRepository,
    private val apiPrefs: ApiPrefs
) : EffectHandler<SubmissionDetailsView, SubmissionDetailsEvent, SubmissionDetailsEffect>() {

    override fun accept(effect: SubmissionDetailsEffect) {
        when (effect) {
            is SubmissionDetailsEffect.LoadData -> loadData(effect)
            is SubmissionDetailsEffect.ShowSubmissionContentType -> {
                view?.showSubmissionContent(effect.submissionContentType, repository.isOnline())
            }
            is SubmissionDetailsEffect.ShowAudioRecordingView -> {
                view?.showAudioRecordingView()
            }
            is SubmissionDetailsEffect.ShowVideoRecordingView -> {
                view?.showVideoRecordingView()
            }
            is SubmissionDetailsEffect.ShowVideoRecordingPlayback -> {
                view?.showVideoRecordingPlayback(effect.file)
            }
            is SubmissionDetailsEffect.ShowVideoRecordingPlaybackError -> {
                view?.showVideoRecordingPlaybackError()
            }
            is SubmissionDetailsEffect.ShowMediaCommentError -> {
                view?.showMediaCommentError()
            }
            is SubmissionDetailsEffect.UploadMediaComment -> {
                uploadMediaComment(effect.file)
            }
            is SubmissionDetailsEffect.MediaCommentDialogClosed -> {
                mediaDialogClosed()
            }
        }.exhaustive
    }

    private fun loadData(effect: SubmissionDetailsEffect.LoadData) {
        launch {
            // If the user is an observer, get the id of the first observee that comes back, otherwise use the user's id
            val enrollments = repository.getObserveeEnrollments(true).dataOrNull.orEmpty()
            val observeeId = enrollments.firstOrNull { it.isObserver && it.courseId == effect.courseId }?.associatedUserId
            val userId = observeeId ?: apiPrefs.user!!.id

            val finalUserId = APIHelper.getUserIdForCourse(
                effect.courseId,
                userId,
                apiPrefs.shardIds,
                apiPrefs.accessToken
            )

            val submissionResult = repository.getSingleSubmission(effect.courseId, effect.assignmentId, finalUserId, true)

            // Correct attempt numbers in submission history if needed (for new quizzes that don't provide them)
            val correctedSubmissionResult = submissionResult.dataOrNull?.let { submission ->
                val correctedHistory = submission.submissionHistory.correctAttemptNumbers()
                DataResult.Success(submission.copy(submissionHistory = correctedHistory))
            } ?: submissionResult

            val assignmentResult = repository.getAssignment(effect.assignmentId, effect.courseId, true)

            val studioLTIToolResult = if (repository.isOnline() && assignmentResult.containsSubmissionType(Assignment.SubmissionType.ONLINE_UPLOAD)) {
                effect.courseId.getStudioLTITool()
            } else {
                DataResult.Fail(null)
            }

            // For empty submissions - We need to know if they can make submissions through Studio, only used for file uploads
            val isStudioEnabled = studioLTIToolResult.dataOrNull != null

            // Determine if we need to retrieve an authenticated LTI URL based on whether this assignment accepts external tool submissions
            val ltiToolId = assignmentResult.dataOrNull?.externalToolAttributes?.contentId
            val ltiToolResponse = if (ltiToolId != null && ltiToolId != 0L) {
                // Use this to create a proper fetch url for the external tool
                repository.getExternalToolLaunchUrl(
                    assignmentResult.dataOrNull?.courseId!!,
                    ltiToolId, assignmentResult.dataOrNull?.id!!,
                    true
                )
            } else {
                val assignmentUrl = assignmentResult.dataOrNull?.url
                if (assignmentUrl != null && assignmentResult.containsSubmissionType(Assignment.SubmissionType.EXTERNAL_TOOL)) {
                    repository.getLtiFromAuthenticationUrl(assignmentUrl, true)
                } else {
                    DataResult.Fail(null)
                }
            }

            val ltiTool = if (ltiToolResponse.dataOrNull != null) {
                DataResult.Success(
                    ltiToolResponse.dataOrThrow.copy(
                        assignmentId = assignmentResult.dataOrNull?.id!!,
                        courseId = assignmentResult.dataOrNull?.courseId!!
                    )
                )
            } else {
                ltiToolResponse
            }

            // We need to get the quiz for the empty submission page
            val quizResult = if (assignmentResult.dataOrNull?.turnInType == Assignment.TurnInType.QUIZ
                && assignmentResult.dataOrNull?.quizId != 0L
            ) {
                repository.getQuiz(effect.courseId, assignmentResult.dataOrNull?.quizId!!, true)
            } else {
                null
            }

            val featureFlags = repository.getCourseFeatures(effect.courseId, true).dataOrNull
            val assignmentEnhancementsEnabled = featureFlags?.contains("assignments_2_student").orDefault()

            val restrictQuantitativeData = repository.loadCourseSettings(effect.courseId, true)?.restrictQuantitativeData.orDefault()

            consumer.accept(
                SubmissionDetailsEvent.DataLoaded(
                    assignmentResult,
                    correctedSubmissionResult,
                    ltiTool,
                    isStudioEnabled,
                    quizResult,
                    studioLTIToolResult,
                    effect.isObserver,
                    assignmentEnhancementsEnabled,
                    restrictQuantitativeData
                )
            )
        }
    }

    private fun DataResult<Assignment>.containsSubmissionType(type: Assignment.SubmissionType): Boolean {
        return dataOrNull?.getSubmissionTypes()?.contains(type).orDefault()
    }

    private fun uploadMediaComment(file: File) {
        FlowSource.getFlow<SubmissionCommentsSharedEvent>().trySend(
            SubmissionCommentsSharedEvent.SendMediaCommentClicked(file)
        )
    }

    private fun mediaDialogClosed() {
        FlowSource.getFlow<SubmissionCommentsSharedEvent>().trySend(
            SubmissionCommentsSharedEvent.MediaCommentDialogClosed
        )
    }
}

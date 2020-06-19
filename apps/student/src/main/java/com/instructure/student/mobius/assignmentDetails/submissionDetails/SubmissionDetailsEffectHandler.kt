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

import com.instructure.canvasapi2.managers.AssignmentManager
import com.instructure.canvasapi2.managers.EnrollmentManager
import com.instructure.canvasapi2.managers.QuizManager
import com.instructure.canvasapi2.managers.SubmissionManager
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.models.LTITool
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.Failure
import com.instructure.canvasapi2.utils.exhaustive
import com.instructure.canvasapi2.utils.weave.StatusCallbackError
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.comments.SubmissionCommentsSharedEvent
import com.instructure.student.mobius.assignmentDetails.submissionDetails.ui.SubmissionDetailsView
import com.instructure.student.mobius.common.ChannelSource
import com.instructure.student.mobius.common.ui.EffectHandler
import com.instructure.student.util.getStudioLTITool
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import java.io.File

class SubmissionDetailsEffectHandler : EffectHandler<SubmissionDetailsView, SubmissionDetailsEvent, SubmissionDetailsEffect>() {
    @ExperimentalCoroutinesApi
    override fun accept(effect: SubmissionDetailsEffect) {
        when (effect) {
            is SubmissionDetailsEffect.LoadData -> loadData(effect)
            is SubmissionDetailsEffect.ShowSubmissionContentType -> {
                view?.showSubmissionContent(effect.submissionContentType)
            }
            is SubmissionDetailsEffect.ShowAudioRecordingView -> {
                view?.showAudioRecordingView()
            }
            is SubmissionDetailsEffect.ShowVideoRecordingView-> {
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
            val enrollmentsResult = EnrollmentManager.getObserveeEnrollmentsAsync(true).await()
            val observeeId = enrollmentsResult.dataOrNull?.firstOrNull { it.isObserver && it.courseId == effect.courseId }?.associatedUserId
            val userId = observeeId ?: ApiPrefs.user!!.id

            val submissionResult = SubmissionManager.getSingleSubmissionAsync(effect.courseId, effect.assignmentId, userId, true).await()
            val assignmentResult = AssignmentManager.getAssignmentAsync(effect.assignmentId, effect.courseId, true).await()

            val studioLTIToolResult: DataResult<LTITool> = if (assignmentResult.isSuccess && assignmentResult.dataOrThrow.getSubmissionTypes().contains(Assignment.SubmissionType.ONLINE_UPLOAD)) {
                effect.courseId.getStudioLTITool()
            } else DataResult.Fail(null)

            // For empty submissions - We need to know if they can make submissions through Studio, only used for file uploads
            val isStudioEnabled = studioLTIToolResult.dataOrNull != null

            // Determine if we need to retrieve an authenticated LTI URL based on whether this assignment accepts external tool submissions
            val assignmentUrl = assignmentResult.dataOrNull?.url
            val ltiUrl = if (assignmentUrl != null && assignmentResult.dataOrNull?.getSubmissionTypes()?.contains(Assignment.SubmissionType.EXTERNAL_TOOL) == true)
                 SubmissionManager.getLtiFromAuthenticationUrlAsync(assignmentUrl, true).await()
            else DataResult.Fail(null)

            // We need to get the quiz for the empty submission page
            val quizResult = if (assignmentResult.dataOrNull?.turnInType == (Assignment.TurnInType.QUIZ) && assignmentResult.dataOrNull?.quizId != 0L) {
                try {
                    QuizManager.getQuizAsync(effect.courseId, assignmentResult.dataOrNull?.quizId!!, true).await()
                } catch (e: StatusCallbackError) {
                    if (e.response?.code() == 401) {
                        DataResult.Fail(Failure.Authorization(e.response?.message()))
                    } else {
                        DataResult.Fail(Failure.Network(e.response?.message()))
                    }
                }
            } else null

            consumer.accept(SubmissionDetailsEvent.DataLoaded(assignmentResult, submissionResult, ltiUrl, isStudioEnabled, quizResult, studioLTIToolResult, effect.isObserver))
        }
    }

    @ExperimentalCoroutinesApi
    private fun uploadMediaComment(file: File) {
        ChannelSource.getChannel<SubmissionCommentsSharedEvent>().offer(
                SubmissionCommentsSharedEvent.SendMediaCommentClicked(file)
        )
    }

    @ExperimentalCoroutinesApi
    private fun mediaDialogClosed() {
        ChannelSource.getChannel<SubmissionCommentsSharedEvent>().offer(
                SubmissionCommentsSharedEvent.MediaCommentDialogClosed
        )
    }
}

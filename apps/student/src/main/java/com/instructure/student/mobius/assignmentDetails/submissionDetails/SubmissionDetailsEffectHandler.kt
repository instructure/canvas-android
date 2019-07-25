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
import com.instructure.canvasapi2.managers.SubmissionManager
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.exhaustive
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.comments.SubmissionCommentsSharedEvent
import com.instructure.student.mobius.assignmentDetails.submissionDetails.ui.SubmissionDetailsView
import com.instructure.student.mobius.common.ChannelSource
import com.instructure.student.mobius.common.ui.EffectHandler
import com.instructure.student.util.isStudioEnabled
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
            val submission = SubmissionManager.getSingleSubmissionAsync(effect.courseId, effect.assignmentId, ApiPrefs.user!!.id, true).await()
            val assignment = AssignmentManager.getAssignmentAsync(effect.assignmentId, effect.courseId, true).await()


            // We need to know if they can make submissions through Studio, only for file uploads - This is for empty submissions
            val isArcEnabled = if (assignment.isSuccess && assignment.dataOrThrow.getSubmissionTypes().contains(Assignment.SubmissionType.ONLINE_UPLOAD)) {
                effect.courseId.isStudioEnabled()
            } else false

            // Determine if we need to retrieve an authenticated LTI URL based on whether this assignment accepts external tool submissions
            val assignmentUrl = assignment.dataOrNull?.url
            val ltiUrl = if (assignmentUrl != null && assignment.dataOrNull?.getSubmissionTypes()?.contains(Assignment.SubmissionType.EXTERNAL_TOOL) == true)
                 SubmissionManager.getLtiFromAuthenticationUrlAsync(assignmentUrl, true).await()
            else DataResult.Fail(null)

            consumer.accept(SubmissionDetailsEvent.DataLoaded(assignment, submission, ltiUrl, isArcEnabled))
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

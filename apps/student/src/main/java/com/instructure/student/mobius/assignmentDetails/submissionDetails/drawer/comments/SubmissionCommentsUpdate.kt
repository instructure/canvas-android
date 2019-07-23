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
package com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.comments

import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.student.mobius.common.ui.UpdateInit
import com.spotify.mobius.First
import com.spotify.mobius.Next

class SubmissionCommentsUpdate : UpdateInit<SubmissionCommentsModel, SubmissionCommentsEvent, SubmissionCommentsEffect>() {
    override fun performInit(model: SubmissionCommentsModel): First<SubmissionCommentsModel, SubmissionCommentsEffect> {
        return First.first(model)
    }

    override fun update(
            model: SubmissionCommentsModel,
            event: SubmissionCommentsEvent
    ): Next<SubmissionCommentsModel, SubmissionCommentsEffect> {
        return when (event) {
            is SubmissionCommentsEvent.AddMediaCommentClicked -> {
                Next.next(model.copy(isMediaCommentEnabled = false), setOf(SubmissionCommentsEffect.ShowMediaCommentDialog))
            }
            is SubmissionCommentsEvent.AddAudioCommentClicked -> {
                Next.dispatch(setOf(SubmissionCommentsEffect.ShowAudioRecordingView))
            }
            is SubmissionCommentsEvent.AddVideoCommentClicked -> {
                Next.dispatch(setOf(SubmissionCommentsEffect.ShowVideoRecordingView))
            }
            is SubmissionCommentsEvent.SendMediaCommentClicked -> {
                val effect = SubmissionCommentsEffect.UploadMediaComment(
                    event.file,
                    model.assignment.id,
                    model.assignment.name.orEmpty(),
                    model.assignment.courseId,
                    model.assignment.groupCategoryId > 0
                )
                Next.next(
                    model.copy(isMediaCommentEnabled = true),
                    setOf(effect)
                )
            }
            is SubmissionCommentsEvent.MediaCommentDialogClosed -> {
                Next.next(model.copy(isMediaCommentEnabled = true))
            }
            is SubmissionCommentsEvent.SendTextCommentClicked -> {
                val effect = SubmissionCommentsEffect.SendTextComment(
                    event.message,
                    model.assignment.id,
                    model.assignment.name.orEmpty(),
                    model.assignment.courseId,
                    model.assignment.groupCategoryId > 0
                )
                Next.dispatch(setOf(effect, SubmissionCommentsEffect.ClearTextInput))
            }
            is SubmissionCommentsEvent.SubmissionCommentAdded -> {
                Next.next(model.copy(comments = model.comments + event.comment))
            }
            SubmissionCommentsEvent.UploadFilesClicked -> {
                val effect = SubmissionCommentsEffect.ShowFilePicker(
                    CanvasContext.emptyCourseContext(model.assignment.courseId),
                    model.assignment
                )
                Next.dispatch(setOf(effect))
            }
            is SubmissionCommentsEvent.PendingSubmissionsUpdated -> {
                if (event.ids == model.pendingCommentIds) {
                    Next.noChange()
                } else {
                    Next.next(model.copy(pendingCommentIds = event.ids))
                }
            }
            is SubmissionCommentsEvent.RetryCommentUploadClicked -> {
                Next.dispatch(setOf(SubmissionCommentsEffect.RetryCommentUpload(event.commentId)))
            }
        }
    }

}

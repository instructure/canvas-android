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

class SubmissionCommentsUpdate :
    UpdateInit<SubmissionCommentsModel, SubmissionCommentsEvent, SubmissionCommentsEffect>() {
    override fun performInit(model: SubmissionCommentsModel): First<SubmissionCommentsModel, SubmissionCommentsEffect> {
        return First.first(model)
    }

    override fun update(
        model: SubmissionCommentsModel,
        event: SubmissionCommentsEvent
    ): Next<SubmissionCommentsModel, SubmissionCommentsEffect> {
        return when (event) {
            is SubmissionCommentsEvent.AddFilesClicked -> {
                Next.next(
                    model.copy(isFileButtonEnabled = false),
                    setOf(SubmissionCommentsEffect.ShowMediaCommentDialog)
                )
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
                    model.assignment.groupCategoryId > 0,
                    model.attemptId.takeIf { model.assignmentEnhancementsEnabled }
                )
                Next.next(
                    model.copy(isFileButtonEnabled = true),
                    setOf(effect)
                )
            }
            is SubmissionCommentsEvent.AddFilesDialogClosed -> {
                Next.next(model.copy(isFileButtonEnabled = true))
            }
            is SubmissionCommentsEvent.SendTextCommentClicked -> {
                val effect = SubmissionCommentsEffect.SendTextComment(
                    event.message,
                    model.assignment.id,
                    model.assignment.name.orEmpty(),
                    model.assignment.courseId,
                    model.assignment.groupCategoryId > 0,
                    model.attemptId.takeIf { model.assignmentEnhancementsEnabled }
                )
                Next.dispatch(setOf(effect, SubmissionCommentsEffect.ClearTextInput))
            }
            is SubmissionCommentsEvent.SubmissionCommentAdded -> {
                Next.next(
                    model.copy(comments = model.comments + event.comment),
                    setOf(SubmissionCommentsEffect.ScrollToBottom)
                )
            }
            SubmissionCommentsEvent.UploadFilesClicked -> {
                val effect = SubmissionCommentsEffect.ShowFilePicker(
                    CanvasContext.emptyCourseContext(model.assignment.courseId),
                    model.assignment,
                    model.attemptId.takeIf { model.assignmentEnhancementsEnabled }
                )
                Next.dispatch(setOf(effect))
            }
            is SubmissionCommentsEvent.PendingSubmissionsUpdated -> {
                when {
                    event.ids == model.pendingCommentIds -> Next.noChange()
                    event.ids.size > model.pendingCommentIds.size -> {
                        Next.next<SubmissionCommentsModel, SubmissionCommentsEffect>(
                            model.copy(pendingCommentIds = event.ids),
                            setOf(SubmissionCommentsEffect.ScrollToBottom)
                        )
                    }
                    else -> Next.next(model.copy(pendingCommentIds = event.ids))
                }
            }
            is SubmissionCommentsEvent.RetryCommentUploadClicked -> {
                Next.dispatch(setOf(SubmissionCommentsEffect.RetryCommentUpload(event.commentId)))
            }
            is SubmissionCommentsEvent.DeletePendingCommentClicked -> {
                Next.dispatch(setOf(SubmissionCommentsEffect.DeletePendingComment(event.commentId)))
            }
            is SubmissionCommentsEvent.SubmissionClicked -> {
                Next.dispatch(setOf(SubmissionCommentsEffect.BroadcastSubmissionSelected(event.submission)))
            }
            is SubmissionCommentsEvent.SubmissionAttachmentClicked -> {
                Next.dispatch(
                    setOf(
                        SubmissionCommentsEffect.BroadcastSubmissionAttachmentSelected(
                            event.submission,
                            event.attachment
                        )
                    )
                )
            }
            is SubmissionCommentsEvent.CommentAttachmentClicked -> {
                Next.dispatch(
                    setOf(
                        SubmissionCommentsEffect.OpenMedia(
                            CanvasContext.emptyCourseContext(model.assignment.courseId),
                            event.attachment.contentType.orEmpty(),
                            event.attachment.url.orEmpty(),
                            event.attachment.filename.orEmpty()
                        )
                    )
                )
            }
        }
    }

}

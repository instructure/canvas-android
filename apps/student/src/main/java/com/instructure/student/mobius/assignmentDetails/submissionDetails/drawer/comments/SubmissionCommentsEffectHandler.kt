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
@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.comments

import android.app.Activity
import android.content.Context
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.utils.AnalyticsEventConstants
import com.instructure.canvasapi2.utils.exhaustive
import com.instructure.pandautils.utils.PermissionUtils
import com.instructure.pandautils.utils.requestPermissions
import com.instructure.student.mobius.assignmentDetails.submissionDetails.SubmissionDetailsSharedEvent
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.comments.ui.SubmissionCommentsView
import com.instructure.student.mobius.common.ChannelSource
import com.instructure.student.mobius.common.ui.EffectHandler
import com.instructure.student.mobius.common.ui.SubmissionService

class SubmissionCommentsEffectHandler(val context: Context) : EffectHandler<SubmissionCommentsView, SubmissionCommentsEvent, SubmissionCommentsEffect>() {
    override fun accept(effect: SubmissionCommentsEffect) {
        when (effect) {
            is SubmissionCommentsEffect.ShowMediaCommentDialog -> {
                view?.showMediaCommentDialog()
            }
            is SubmissionCommentsEffect.ShowAudioRecordingView -> {
                launchAudio()
            }
            is SubmissionCommentsEffect.ShowVideoRecordingView -> {
                launchVideo()
            }
            is SubmissionCommentsEffect.UploadMediaComment -> {
                logEvent(AnalyticsEventConstants.SUBMISSION_COMMENTS_MEDIA_REPLY)
                SubmissionService.startMediaCommentUpload(
                    context = context,
                    canvasContext = CanvasContext.emptyCourseContext(effect.courseId),
                    assignmentId = effect.assignmentId,
                    assignmentName = effect.assignmentName,
                    mediaFile = effect.file,
                    isGroupMessage = effect.isGroupMessage
                )
            }
            is SubmissionCommentsEffect.ShowFilePicker -> {
                view?.showFilePicker(effect.canvasContext, effect.assignment)
            }
            SubmissionCommentsEffect.ClearTextInput -> {
                view?.clearTextInput()
            }
            is SubmissionCommentsEffect.SendTextComment -> {
                logEvent(AnalyticsEventConstants.SUBMISSION_COMMENTS_TEXT_REPLY)
                SubmissionService.startCommentUpload(
                    context,
                    CanvasContext.emptyCourseContext(effect.courseId),
                    effect.assignmentId,
                    effect.assignmentName,
                    effect.message,
                    emptyList(),
                    effect.isGroupMessage
                )
            }
            is SubmissionCommentsEffect.RetryCommentUpload -> {
                SubmissionService.retryCommentUpload(context, effect.commentId)
            }
            is SubmissionCommentsEffect.DeletePendingComment -> {
                SubmissionService.deletePendingComment(context, effect.commentId)
            }
            SubmissionCommentsEffect.ScrollToBottom -> view?.scrollToBottom()
            is SubmissionCommentsEffect.BroadcastSubmissionSelected -> {
                ChannelSource.getChannel<SubmissionDetailsSharedEvent>().trySend(
                    SubmissionDetailsSharedEvent.SubmissionClicked(effect.submission)
                )
                Unit
            }
            is SubmissionCommentsEffect.BroadcastSubmissionAttachmentSelected -> {
                ChannelSource.getChannel<SubmissionDetailsSharedEvent>().trySend(
                    SubmissionDetailsSharedEvent.SubmissionAttachmentClicked(
                        effect.submission,
                        effect.attachment
                    )
                )
                Unit
            }
            is SubmissionCommentsEffect.OpenMedia -> {
                view?.openMedia(effect.canvasContext, effect.contentType, effect.url, effect.fileName)
            }
        }.exhaustive
    }

    private fun launchAudio() {
        if(needsPermissions(::launchAudio, PermissionUtils.RECORD_AUDIO)) return
        showAudioCommentDialog()
    }

    private fun launchVideo() {
        if(needsPermissions(::launchVideo, PermissionUtils.CAMERA, PermissionUtils.RECORD_AUDIO)) return
        showVideoCommentDialog()
    }

    private fun needsPermissions(successCallback: () -> Unit, vararg permissions: String): Boolean {
        if (PermissionUtils.hasPermissions(context as Activity, *permissions)) {
            return false
        }

        context.requestPermissions(setOf(*permissions)) { results ->
            if (results.isNotEmpty() && results.all { it.value }) {
                // If permissions list is not empty and all are granted, retry camera
                successCallback()
            } else {
                view?.showPermissionDeniedToast()
            }
        }
        return true
    }

    private fun showVideoCommentDialog() {
        ChannelSource.getChannel<SubmissionDetailsSharedEvent>().trySend(
            SubmissionDetailsSharedEvent.VideoRecordingViewLaunched
        )
    }

    private fun showAudioCommentDialog() {
        ChannelSource.getChannel<SubmissionDetailsSharedEvent>().trySend(
            SubmissionDetailsSharedEvent.AudioRecordingViewLaunched
        )
    }

}

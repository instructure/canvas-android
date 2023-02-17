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
package com.instructure.student.test.assignment.details.submissionDetails.commentTab

import com.instructure.canvasapi2.models.*
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.comments.SubmissionCommentsEffect
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.comments.SubmissionCommentsEvent
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.comments.SubmissionCommentsModel
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.comments.SubmissionCommentsUpdate
import com.instructure.student.test.util.matchesEffects
import com.spotify.mobius.test.FirstMatchers
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import com.spotify.mobius.test.NextMatchers
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.io.File

class SubmissionCommentsUpdateTest : Assert() {

    private val initSpec = InitSpec(SubmissionCommentsUpdate()::init)
    private val updateSpec = UpdateSpec(SubmissionCommentsUpdate()::update)

    private lateinit var initModel: SubmissionCommentsModel

    @Before
    fun setup() {
        initModel = SubmissionCommentsModel(
            comments = arrayListOf(),
            submissionHistory = emptyList(),
            assignment = Assignment(id = 123L, name = "Test Assignment", courseId = 456L),
            attemptId = 1
        )
    }

    @Test
    fun `Initializes without model changes and without effects`() {
        val expectedModel = initModel.copy()
        initSpec
            .whenInit(initModel)
            .then(
                assertThatFirst(
                    FirstMatchers.hasModel(expectedModel),
                    FirstMatchers.hasNoEffects()
                )
            )
    }

    @Test
    fun `AddMediaCommentClicked results in ShowMediaCommentDialog effect and model change`() {
        val expectedModel = initModel.copy(isFileButtonEnabled = false)
        updateSpec
            .given(initModel)
            .whenEvent(SubmissionCommentsEvent.AddFilesClicked)
            .then(
                assertThatNext(
                    hasModel(expectedModel),
                    matchesEffects<SubmissionCommentsModel, SubmissionCommentsEffect>(SubmissionCommentsEffect.ShowMediaCommentDialog)
                )
            )
    }

    @Test
    fun `AddAudioCommentClicked results in ShowAudioRecordingView effect`() {
        updateSpec
            .given(initModel)
            .whenEvent(SubmissionCommentsEvent.AddAudioCommentClicked)
            .then(
                assertThatNext(
                    matchesEffects<SubmissionCommentsModel, SubmissionCommentsEffect>(SubmissionCommentsEffect.ShowAudioRecordingView)
                )
            )
    }

    @Test
    fun `AddVideoCommentClicked results in ShowVideoRecordingView effect`() {
        updateSpec
            .given(initModel)
            .whenEvent(SubmissionCommentsEvent.AddVideoCommentClicked)
            .then(
                assertThatNext(
                    matchesEffects<SubmissionCommentsModel, SubmissionCommentsEffect>(SubmissionCommentsEffect.ShowVideoRecordingView)
                )
            )
    }

    @Test
    fun `SendMediaCommentClicked results in UploadMediaComment effect`() {
        val file = File("test")
        val expectedEffect = SubmissionCommentsEffect.UploadMediaComment(
            file = file,
            assignmentId = 123L,
            assignmentName = "Test Assignment",
            courseId = 456L,
            isGroupMessage = false,
            attemptId = 1
        )
        updateSpec
            .given(initModel)
            .whenEvent(SubmissionCommentsEvent.SendMediaCommentClicked(file))
            .then(
                assertThatNext(
                    matchesEffects<SubmissionCommentsModel, SubmissionCommentsEffect>(expectedEffect)
                )
            )
    }

    @Test
    fun `SendTextCommentClicked results in SendTextComment effect and ClearTextInput effect`() {
        val message = "Test message"
        val expectedEffect = SubmissionCommentsEffect.SendTextComment(
            assignmentId = 123L,
            assignmentName = "Test Assignment",
            courseId = 456L,
            isGroupMessage = false,
            message = message,
            attemptId = 1
        )
        updateSpec
            .given(initModel)
            .whenEvent(SubmissionCommentsEvent.SendTextCommentClicked(message))
            .then(
                assertThatNext<SubmissionCommentsModel, SubmissionCommentsEffect>(
                    matchesEffects(expectedEffect, SubmissionCommentsEffect.ClearTextInput)
                )
            )
    }

    @Test
    fun `MediaCommentDialogClosed results in no effects and a model change`() {
        val givenModel = initModel.copy(isFileButtonEnabled = false)
        val expectedModel = initModel.copy(isFileButtonEnabled = true)
        updateSpec
            .given(givenModel)
            .whenEvent(SubmissionCommentsEvent.AddFilesDialogClosed)
            .then(
                assertThatNext(
                    hasModel(expectedModel),
                    NextMatchers.hasNoEffects()
                )
            )
    }

    @Test
    fun `SubmissionCommentAdded results a model change and ScrollToBottom effect`() {
        val existingComment = SubmissionComment(comment = "Existing comment")
        val newComment = SubmissionComment(comment = "New comment")
        val givenModel = initModel.copy(comments = listOf(existingComment))
        val expectedModel = initModel.copy(comments = listOf(existingComment, newComment))
        updateSpec
            .given(givenModel)
            .whenEvent(SubmissionCommentsEvent.SubmissionCommentAdded(newComment))
            .then(
                assertThatNext<SubmissionCommentsModel, SubmissionCommentsEffect>(
                    hasModel(expectedModel),
                    matchesEffects(SubmissionCommentsEffect.ScrollToBottom)
                )
            )
    }

    @Test
    fun `PendingSubmissionsUpdated results in nothing if IDs have changed`() {
        val model = initModel.copy(pendingCommentIds = listOf(1L, 2L))
        updateSpec
            .given(model)
            .whenEvent(SubmissionCommentsEvent.PendingSubmissionsUpdated(listOf(1L, 2L)))
            .then(
                assertThatNext(
                    NextMatchers.hasNothing()
                )
            )
    }

    @Test
    fun `PendingSubmissionsUpdated results in model change if IDs have changed`() {
        val model = initModel.copy(pendingCommentIds = listOf(1L, 2L))
        val expectedModel = initModel.copy(pendingCommentIds = listOf(1L))
        updateSpec
            .given(model)
            .whenEvent(SubmissionCommentsEvent.PendingSubmissionsUpdated(listOf(1L)))
            .then(
                assertThatNext(
                    hasModel(expectedModel)
                )
            )
    }

    @Test
    fun `PendingSubmissionsUpdated results in model change and ScrollToBottom effect if ID count has increased`() {
        val model = initModel.copy(pendingCommentIds = listOf(1L))
        val expectedModel = initModel.copy(pendingCommentIds = listOf(1L, 2L))
        val expectedEffect = SubmissionCommentsEffect.ScrollToBottom
        updateSpec
            .given(model)
            .whenEvent(SubmissionCommentsEvent.PendingSubmissionsUpdated(listOf(1L, 2L)))
            .then(
                assertThatNext<SubmissionCommentsModel, SubmissionCommentsEffect>(
                    hasModel(expectedModel),
                    matchesEffects(expectedEffect)
                )
            )
    }

    @Test
    fun `UploadFilesClicked results in ShowFilePicker effect`() {
        val expectedEffect = SubmissionCommentsEffect.ShowFilePicker(
            canvasContext = Course(456L),
            assignment = initModel.assignment,
            attemptId = 1
        )
        updateSpec
            .given(initModel)
            .whenEvent(SubmissionCommentsEvent.UploadFilesClicked)
            .then(
                assertThatNext<SubmissionCommentsModel, SubmissionCommentsEffect>(
                    NextMatchers.hasNoModel(),
                    matchesEffects(expectedEffect)
                )
            )
    }

    @Test
    fun `RetryCommentUploadClicked results in RetryCommentUpload effect`() {
        val event = SubmissionCommentsEvent.RetryCommentUploadClicked(123L)
        val effect = SubmissionCommentsEffect.RetryCommentUpload(123L)
        updateSpec
            .given(initModel)
            .whenEvent(event)
            .then(
                assertThatNext<SubmissionCommentsModel, SubmissionCommentsEffect>(
                    matchesEffects(effect)
                )
            )
    }

    @Test
    fun `DeletePendingCommentClicked results in DeletePendingComment effect`() {
        val event = SubmissionCommentsEvent.DeletePendingCommentClicked(123L)
        val effect = SubmissionCommentsEffect.DeletePendingComment(123L)
        updateSpec
            .given(initModel)
            .whenEvent(event)
            .then(
                assertThatNext<SubmissionCommentsModel, SubmissionCommentsEffect>(
                    matchesEffects(effect)
                )
            )
    }

    @Test
    fun `SubmissionClicked results in BroadcastSubmissionSelected effect`() {
        val submission = Submission(123L)
        val event = SubmissionCommentsEvent.SubmissionClicked(submission)
        val effect = SubmissionCommentsEffect.BroadcastSubmissionSelected(submission)
        updateSpec
            .given(initModel)
            .whenEvent(event)
            .then(
                assertThatNext<SubmissionCommentsModel, SubmissionCommentsEffect>(
                    matchesEffects(effect)
                )
            )
    }

    @Test
    fun `SubmissionAttachmentClicked results in BroadcastSubmissionAttachmentSelected effect`() {
        val submission = Submission(123L)
        val attachment = Attachment(456L)
        val event = SubmissionCommentsEvent.SubmissionAttachmentClicked(submission, attachment)
        val effect = SubmissionCommentsEffect.BroadcastSubmissionAttachmentSelected(submission, attachment)
        updateSpec
            .given(initModel)
            .whenEvent(event)
            .then(
                assertThatNext<SubmissionCommentsModel, SubmissionCommentsEffect>(
                    matchesEffects(effect)
                )
            )
    }

    @Test
    fun `CommentAttachmentClicked event results in OpenMedia effect`() {
        val attachment = Attachment(
            contentType = "contentType",
            url = "url",
            filename = "fileName"
        )
        val expectedEffect = SubmissionCommentsEffect.OpenMedia(
            canvasContext = Course(initModel.assignment.courseId),
            contentType = "contentType",
            url = "url",
            fileName = "fileName"
        )
        updateSpec
            .given(initModel)
            .whenEvent(SubmissionCommentsEvent.CommentAttachmentClicked(attachment))
            .then {
                assertThatNext<SubmissionCommentsModel, SubmissionCommentsEffect>(
                    hasNoModel(),
                    matchesEffects(expectedEffect)
                )
            }
    }

}

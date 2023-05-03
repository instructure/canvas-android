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

package com.emeritus.student.test.assignment.details.submissionDetails.commentTab

import android.app.Activity
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Attachment
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.utils.Analytics
import com.instructure.canvasapi2.utils.AnalyticsEventConstants
import com.instructure.pandautils.utils.PermissionUtils
import com.instructure.pandautils.utils.requestPermissions
import com.emeritus.student.mobius.assignmentDetails.submissionDetails.SubmissionDetailsSharedEvent
import com.emeritus.student.mobius.assignmentDetails.submissionDetails.drawer.comments.SubmissionCommentsEffect
import com.emeritus.student.mobius.assignmentDetails.submissionDetails.drawer.comments.SubmissionCommentsEffectHandler
import com.emeritus.student.mobius.assignmentDetails.submissionDetails.drawer.comments.SubmissionCommentsEvent
import com.emeritus.student.mobius.assignmentDetails.submissionDetails.drawer.comments.ui.SubmissionCommentsView
import com.emeritus.student.mobius.common.ChannelSource
import com.emeritus.student.mobius.common.ui.SubmissionService
import com.emeritus.student.test.util.receiveOnce
import com.spotify.mobius.functions.Consumer
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.test.setMain
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.io.File
import java.util.concurrent.Executors

class SubmissionCommentsEffectHandlerTest : Assert(){

    private val mockView: SubmissionCommentsView = mockk(relaxed = true)
    private val context: Activity = mockk(relaxed = true)
    private val effectHandler = SubmissionCommentsEffectHandler(context).apply { view = mockView }
    private val eventConsumer: Consumer<SubmissionCommentsEvent> = mockk(relaxed = true)
    private val connection = effectHandler.connect(eventConsumer)

    @Before
    fun setup() {
        Dispatchers.setMain(Executors.newSingleThreadExecutor().asCoroutineDispatcher())
    }

    @Test
    fun `ShowMediaCommentDialog effect results in view calling showMediaCommentDialog`() {
        connection.accept(SubmissionCommentsEffect.ShowMediaCommentDialog)

        verify(timeout = 100) {
            mockView.showMediaCommentDialog()
        }

        confirmVerified(mockView)
    }

    @Test
    fun `UploadMediaComment effect results in calling SubmissionService startMediaCommentUpload`() {
        val effect = SubmissionCommentsEffect.UploadMediaComment(
            file = File("test"),
            assignmentId = 123L,
            assignmentName = "Test Assignment",
            courseId = 123L,
            isGroupMessage = false
        )
        mockkObject(SubmissionService.Companion)
        every {
            SubmissionService.startMediaCommentUpload(any(), any(), any(), any(), any(), any())
        } returns Unit

        connection.accept(effect)

        verify(timeout = 100) {
            SubmissionService.startMediaCommentUpload(
                context,
                Course(123L),
                123L,
                "Test Assignment",
                File("test"),
                false
            )
        }

        confirmVerified(SubmissionService)
    }

    @Test
    fun `ShowAudioRecordingView effect with no permission results in view calling showPermissionDeniedToast`() {
        mockPermissions(false)

        connection.accept(SubmissionCommentsEffect.ShowAudioRecordingView)

        verify(timeout = 100) {
            mockView.showPermissionDeniedToast()
        }
    }

    @Test
    fun `ShowVideoRecordingView effect with no permission results in view calling showPermissionDeniedToast`() {
        mockPermissions(false)

        connection.accept(SubmissionCommentsEffect.ShowVideoRecordingView)

        verify(timeout = 100) {
            mockView.showPermissionDeniedToast()
        }
    }

    @Test
    fun `ShowAudioRecordingView effect with permission results in AudioRecordingViewLaunched shared event`() {
        mockPermissions(true)

        val channel = ChannelSource.getChannel<SubmissionDetailsSharedEvent>()
        val expectedEvent = SubmissionDetailsSharedEvent.AudioRecordingViewLaunched
        val actualEvent = channel.receiveOnce {
            connection.accept(SubmissionCommentsEffect.ShowAudioRecordingView)
        }
        assertEquals(expectedEvent, actualEvent)
    }

    @Test
    fun `ShowVideoRecordingView effect with permission results in VideoRecordingViewLaunched shared event`() {
        mockPermissions(true)

        val channel = ChannelSource.getChannel<SubmissionDetailsSharedEvent>()
        val expectedEvent = SubmissionDetailsSharedEvent.VideoRecordingViewLaunched
        val actualEvent = channel.receiveOnce {
            connection.accept(SubmissionCommentsEffect.ShowVideoRecordingView)
        }
        assertEquals(expectedEvent, actualEvent)
    }

    @Test
    fun `ShowAudioRecordingView effect without permissions results in AudioRecordingViewLaunched shared event`() {
        mockPermissions(hasPermission = true, permissionGranted = true)

        val channel = ChannelSource.getChannel<SubmissionDetailsSharedEvent>()
        val expectedEvent = SubmissionDetailsSharedEvent.AudioRecordingViewLaunched
        val actualEvent = channel.receiveOnce {
            connection.accept(SubmissionCommentsEffect.ShowAudioRecordingView)
        }
        assertEquals(expectedEvent, actualEvent)
    }


    @Test
    fun `ShowVideoRecordingView effect without permission results in VideoRecordingViewLaunched shared event`() {
        mockPermissions(hasPermission = true, permissionGranted = true)

        val channel = ChannelSource.getChannel<SubmissionDetailsSharedEvent>()
        val expectedEvent = SubmissionDetailsSharedEvent.VideoRecordingViewLaunched
        val actualEvent = channel.receiveOnce {
            connection.accept(SubmissionCommentsEffect.ShowVideoRecordingView)
        }
        assertEquals(expectedEvent, actualEvent)
    }

    @Test
    fun `ShowAudioRecordingView effect with permission check results in AudioRecordingViewLaunched shared event`() {
        mockPermissions(hasPermission = false, permissionGranted = true)

        val channel = ChannelSource.getChannel<SubmissionDetailsSharedEvent>()
        val expectedEvent = SubmissionDetailsSharedEvent.AudioRecordingViewLaunched
        val actualEvent = channel.receiveOnce {
            connection.accept(SubmissionCommentsEffect.ShowAudioRecordingView)
        }
        assertEquals(expectedEvent, actualEvent)
    }

    @Test
    fun `ShowVideoRecordingView effect with permission check results in VideoRecordingViewLaunched shared event`() {
        mockPermissions(hasPermission = false, permissionGranted = true)

        val channel = ChannelSource.getChannel<SubmissionDetailsSharedEvent>()
        val expectedEvent = SubmissionDetailsSharedEvent.VideoRecordingViewLaunched
        val actualEvent = channel.receiveOnce {
            connection.accept(SubmissionCommentsEffect.ShowVideoRecordingView)
        }
        assertEquals(expectedEvent, actualEvent)
    }

    @Test
    fun `SendTextComment effect results in calling SubmissionService startCommentUpload`() {
        val effect = SubmissionCommentsEffect.SendTextComment(
            message = "Test message",
            assignmentId = 123L,
            assignmentName = "Test Assignment",
            courseId = 456L,
            isGroupMessage = false
        )

        mockkObject(SubmissionService.Companion)
        every {
            SubmissionService.startCommentUpload(any(), any(), any(), any(), any(), any(), any())
        } returns Unit

        connection.accept(effect)

        verify(timeout = 100) {
            SubmissionService.startCommentUpload(
                context = context,
                canvasContext = Course(456L),
                assignmentId = 123L,
                assignmentName = "Test Assignment",
                message = "Test message",
                attachments = emptyList(),
                isGroupMessage = false
            )
        }

        confirmVerified(SubmissionService)
    }

    @Test
    fun `ShowFilePicker effect results in view calling showFilePicker`() {
        val course = Course(name = "Test Course")
        val assignment = Assignment(name = "Test Assignment")
        val effect = SubmissionCommentsEffect.ShowFilePicker(course, assignment)
        connection.accept(effect)

        verify(timeout = 100) {
            mockView.showFilePicker(course, assignment)
        }

        confirmVerified(mockView)
    }

    @Test
    fun `ClearTextInput effect results in view calling clearTextInput`() {
        connection.accept(SubmissionCommentsEffect.ClearTextInput)

        verify(timeout = 100) {
            mockView.clearTextInput()
        }

        confirmVerified(mockView)
    }

    @Test
    fun `ScrollToBottom effect results in view calling scrollToBottom`() {
        connection.accept(SubmissionCommentsEffect.ScrollToBottom)

        verify(timeout = 100) {
            mockView.scrollToBottom()
        }

        confirmVerified(mockView)
    }

    @Test
    fun `RetryCommentUpload effect results in calling SubmissionService retryCommentUpload`() {
        val effect = SubmissionCommentsEffect.RetryCommentUpload(123L)
        mockkObject(SubmissionService.Companion)
        every {
            SubmissionService.retryCommentUpload(any(), any())
        } returns Unit

        connection.accept(effect)

        verify(timeout = 100) {
            SubmissionService.retryCommentUpload(context, 123L)
        }

        confirmVerified(SubmissionService)
    }

    @Test
    fun ` DeleteCommentEffect effect results in calling SubmissionService deletePendingComment`() {
        val effect = SubmissionCommentsEffect.DeletePendingComment(123L)
        mockkObject(SubmissionService.Companion)
        every {
            SubmissionService.deletePendingComment(any(), any())
        } returns Unit

        connection.accept(effect)

        verify(timeout = 100) {
            SubmissionService.deletePendingComment(context, 123L)
        }

        confirmVerified(SubmissionService)
    }

    private fun mockPermissions(hasPermission: Boolean, permissionGranted: Boolean = false) {
        // Mock both so we can mockk the class and the extensions in the same file
        mockkObject(PermissionUtils)
        mockkStatic("${PermissionUtils::class.java.canonicalName}Kt")
        every { PermissionUtils.hasPermissions(context, *anyVararg()) } returns hasPermission andThen permissionGranted

        val block = slot<(Map<String, Boolean>) -> Unit>()

        every { context.requestPermissions(any(), capture(block)) } answers {
            block.invoke(mapOf(Pair("any", permissionGranted)))
        }
    }

    @Test
    fun `BroadcastSubmissionSelected effect sends SubmissionClicked shared event`() {
        val channel = ChannelSource.getChannel<SubmissionDetailsSharedEvent>()
        val submission = Submission(123L)
        val expectedEvent = SubmissionDetailsSharedEvent.SubmissionClicked(submission)
        val actualEvent = channel.receiveOnce {
            connection.accept(SubmissionCommentsEffect.BroadcastSubmissionSelected(submission))
        }
        assertEquals(expectedEvent, actualEvent)
    }

    @Test
    fun `BroadcastSubmissionAttachmentSelected effect sends SubmissionAttachmentClicked shared event`() {
        val channel = ChannelSource.getChannel<SubmissionDetailsSharedEvent>()
        val submission = Submission(123L)
        val attachment = Attachment(id = 456L, contentType = "test/data")
        val expectedEvent = SubmissionDetailsSharedEvent.SubmissionAttachmentClicked(submission, attachment)
        val actualEvent = channel.receiveOnce {
            connection.accept(SubmissionCommentsEffect.BroadcastSubmissionAttachmentSelected(submission, attachment))
        }
        assertEquals(expectedEvent, actualEvent)
    }

    @Test
    fun `OpenMedia effect results in view calling openMedia`() {
        val effect = SubmissionCommentsEffect.OpenMedia(
            Course(123L),
            "contentType",
            "url",
            "fileName"
        )
        connection.accept(effect)

        verify(timeout = 100) {
            mockView.openMedia(
                Course(123L),
                "contentType",
                "url",
                "fileName"
            )
        }

        confirmVerified(mockView)
    }

}

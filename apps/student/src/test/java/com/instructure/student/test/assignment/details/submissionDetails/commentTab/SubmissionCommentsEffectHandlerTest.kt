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

package com.instructure.student.test.assignment.details.submissionDetails.commentTab

import androidx.fragment.app.FragmentActivity
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Attachment
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.utils.Analytics
import com.instructure.pandautils.utils.PermissionUtils
import com.instructure.pandautils.utils.requestPermissions
import com.instructure.student.mobius.assignmentDetails.submissionDetails.SubmissionDetailsSharedEvent
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.comments.SubmissionCommentsEffect
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.comments.SubmissionCommentsEffectHandler
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.comments.SubmissionCommentsEvent
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.comments.ui.SubmissionCommentsView
import com.instructure.student.mobius.common.FlowSource
import com.instructure.student.mobius.common.ui.SubmissionHelper
import com.spotify.mobius.functions.Consumer
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.invoke
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.runs
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class SubmissionCommentsEffectHandlerTest : Assert(){

    private val mockView: SubmissionCommentsView = mockk(relaxed = true)
    private val context: FragmentActivity = mockk(relaxed = true)
    private val submissionHelper: SubmissionHelper = mockk(relaxed = true)
    private val effectHandler = SubmissionCommentsEffectHandler(context, submissionHelper).apply { view = mockView }
    private val eventConsumer: Consumer<SubmissionCommentsEvent> = mockk(relaxed = true)
    private val connection = effectHandler.connect(eventConsumer)
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        mockkObject(Analytics)
        every { Analytics.logEvent(any()) } just runs
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
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
    fun `UploadMediaComment effect results in calling SubmissionHelper startMediaCommentUpload`() {
        val effect = SubmissionCommentsEffect.UploadMediaComment(
            file = File("test"),
            assignmentId = 123L,
            assignmentName = "Test Assignment",
            courseId = 123L,
            isGroupMessage = false,
            attemptId = 1
        )
        every {
            submissionHelper.startMediaCommentUpload(any(), any(), any(), any(), any(), any())
        } returns Unit

        connection.accept(effect)

        verify(timeout = 100) {
            submissionHelper.startMediaCommentUpload(
                Course(123L),
                123L,
                "Test Assignment",
                File("test"),
                false,
                1
            )
        }

        confirmVerified(submissionHelper)
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
    fun `ShowAudioRecordingView effect with permission results in AudioRecordingViewLaunched shared event`() = runTest(testDispatcher) {
        mockPermissions(true)

        val flow = FlowSource.getFlow<SubmissionDetailsSharedEvent>()
        val expectedEvent = SubmissionDetailsSharedEvent.AudioRecordingViewLaunched
        val deferred = async {
            flow.first()
        }
        connection.accept(SubmissionCommentsEffect.ShowAudioRecordingView)
        assertEquals(expectedEvent, deferred.await())
    }

    @Test
    fun `ShowVideoRecordingView effect with permission results in VideoRecordingViewLaunched shared event`() = runTest(testDispatcher) {
        mockPermissions(true)

        val flow = FlowSource.getFlow<SubmissionDetailsSharedEvent>()
        val expectedEvent = SubmissionDetailsSharedEvent.VideoRecordingViewLaunched
        val deferred = async {
            flow.first()
        }
        connection.accept(SubmissionCommentsEffect.ShowVideoRecordingView)
        assertEquals(expectedEvent, deferred.await())
    }

    @Test
    fun `ShowAudioRecordingView effect without permissions results in AudioRecordingViewLaunched shared event`() = runTest(testDispatcher) {
        mockPermissions(hasPermission = true, permissionGranted = true)

        val flow = FlowSource.getFlow<SubmissionDetailsSharedEvent>()
        val expectedEvent = SubmissionDetailsSharedEvent.AudioRecordingViewLaunched
        val deferred = async {
            flow.first()
        }
        connection.accept(SubmissionCommentsEffect.ShowAudioRecordingView)
        assertEquals(expectedEvent, deferred.await())
    }


    @Test
    fun `ShowVideoRecordingView effect without permission results in VideoRecordingViewLaunched shared event`() = runTest(testDispatcher) {
        mockPermissions(hasPermission = true, permissionGranted = true)

        val flow = FlowSource.getFlow<SubmissionDetailsSharedEvent>()
        val expectedEvent = SubmissionDetailsSharedEvent.VideoRecordingViewLaunched
        val deferred = async {
            flow.first()
        }
        connection.accept(SubmissionCommentsEffect.ShowVideoRecordingView)
        assertEquals(expectedEvent, deferred.await())
    }

    @Test
    fun `ShowAudioRecordingView effect with permission check results in AudioRecordingViewLaunched shared event`() = runTest(testDispatcher) {
        mockPermissions(hasPermission = false, permissionGranted = true)

        val flow = FlowSource.getFlow<SubmissionDetailsSharedEvent>()
        val expectedEvent = SubmissionDetailsSharedEvent.AudioRecordingViewLaunched
        val deferred = async {
            flow.first()
        }
        connection.accept(SubmissionCommentsEffect.ShowAudioRecordingView)
        assertEquals(expectedEvent, deferred.await())
    }

    @Test
    fun `ShowVideoRecordingView effect with permission check results in VideoRecordingViewLaunched shared event`() = runTest(testDispatcher) {
        mockPermissions(hasPermission = false, permissionGranted = true)

        val flow = FlowSource.getFlow<SubmissionDetailsSharedEvent>()
        val expectedEvent = SubmissionDetailsSharedEvent.VideoRecordingViewLaunched
        val deferred = async {
            flow.first()
        }
        connection.accept(SubmissionCommentsEffect.ShowVideoRecordingView)
        assertEquals(expectedEvent, deferred.await())
    }

    @Test
    fun `SendTextComment effect results in calling SubmissionHelper startCommentUpload`() {
        val effect = SubmissionCommentsEffect.SendTextComment(
            message = "Test message",
            assignmentId = 123L,
            assignmentName = "Test Assignment",
            courseId = 456L,
            isGroupMessage = false,
            attemptId = 1
        )

        connection.accept(effect)

        verify(timeout = 100) {
            submissionHelper.startCommentUpload(
                canvasContext = Course(456L),
                assignmentId = 123L,
                assignmentName = "Test Assignment",
                message = "Test message",
                attachments = emptyList(),
                isGroupMessage = false,
                attemptId = 1
            )
        }

        confirmVerified(submissionHelper)
    }

    @Test
    fun `ShowFilePicker effect results in view calling showFilePicker`() {
        val course = Course(name = "Test Course")
        val assignment = Assignment(name = "Test Assignment")
        val effect = SubmissionCommentsEffect.ShowFilePicker(course, assignment, 1)
        connection.accept(effect)

        verify(timeout = 100) {
            mockView.showFilePicker(course, assignment, 1)
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
    fun `RetryCommentUpload effect results in calling SubmissionHelper retryCommentUpload`() {
        val effect = SubmissionCommentsEffect.RetryCommentUpload(123L)
        connection.accept(effect)

        verify(timeout = 100) {
            submissionHelper.retryCommentUpload(123L)
        }
    }

    @Test
    fun ` DeleteCommentEffect effect results in calling SubmissionHelper deletePendingComment`() {
        val effect = SubmissionCommentsEffect.DeletePendingComment(123L)
        connection.accept(effect)

        verify(timeout = 100) {
            submissionHelper.deletePendingComment(123L)
        }
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
    fun `BroadcastSubmissionSelected effect sends SubmissionClicked shared event`() = runTest(testDispatcher) {
        val flow = FlowSource.getFlow<SubmissionDetailsSharedEvent>()
        val submission = Submission(123L)
        val expectedEvent = SubmissionDetailsSharedEvent.SubmissionClicked(submission)
        val deferred = async {
            flow.first()
        }
        connection.accept(SubmissionCommentsEffect.BroadcastSubmissionSelected(submission))
        assertEquals(expectedEvent, deferred.await())
    }

    @Test
    fun `BroadcastSubmissionAttachmentSelected effect sends SubmissionAttachmentClicked shared event`() = runTest(testDispatcher) {
        val flow = FlowSource.getFlow<SubmissionDetailsSharedEvent>()
        val submission = Submission(123L)
        val attachment = Attachment(id = 456L, contentType = "test/data")
        val expectedEvent = SubmissionDetailsSharedEvent.SubmissionAttachmentClicked(submission, attachment)
        val deferred = async {
            flow.first()
        }
        connection.accept(SubmissionCommentsEffect.BroadcastSubmissionAttachmentSelected(submission, attachment))
        assertEquals(expectedEvent, deferred.await())
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

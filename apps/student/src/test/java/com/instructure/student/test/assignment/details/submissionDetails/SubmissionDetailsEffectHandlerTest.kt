/*
 * Copyright (C) 2019 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package com.instructure.student.test.assignment.details.submissionDetails

import com.instructure.canvasapi2.managers.AssignmentManager
import com.instructure.canvasapi2.managers.SubmissionManager
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.LTITool
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.Failure
import com.instructure.student.mobius.assignmentDetails.submissionDetails.SubmissionDetailsContentType
import com.instructure.student.mobius.assignmentDetails.submissionDetails.SubmissionDetailsEffect
import com.instructure.student.mobius.assignmentDetails.submissionDetails.SubmissionDetailsEffectHandler
import com.instructure.student.mobius.assignmentDetails.submissionDetails.SubmissionDetailsEvent
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.comments.SubmissionCommentsSharedEvent
import com.instructure.student.mobius.assignmentDetails.submissionDetails.ui.SubmissionDetailsView
import com.instructure.student.mobius.common.ChannelSource
import com.instructure.student.test.util.receiveOnce
import com.spotify.mobius.functions.Consumer
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.test.setMain
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.io.File
import java.util.concurrent.Executors

class SubmissionDetailsEffectHandlerTest : Assert() {
    private val view: SubmissionDetailsView = mockk(relaxed = true)
    private val effectHandler =
            SubmissionDetailsEffectHandler().apply { view = this@SubmissionDetailsEffectHandlerTest.view }
    private val eventConsumer: Consumer<SubmissionDetailsEvent> = mockk(relaxed = true)
    private val connection = effectHandler.connect(eventConsumer)

    @ExperimentalCoroutinesApi
    @Before
    fun setup() {
        Dispatchers.setMain(Executors.newSingleThreadExecutor().asCoroutineDispatcher())
    }

    @Test
    fun `Failed LoadData results in fail DataLoaded`() {
        val user = User()
        val courseId = 1L
        val assignmentId = 1L
        val errorMessage = "Error"

        mockkObject(AssignmentManager)
        mockkObject(SubmissionManager)

        every { AssignmentManager.getAssignmentAsync(any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Fail(Failure.Network(errorMessage))
        }

        every { SubmissionManager.getSingleSubmissionAsync(any(), any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Fail(Failure.Network(errorMessage))
        }

        mockkStatic(ApiPrefs::class)
        every { ApiPrefs.user } returns user

        connection.accept(SubmissionDetailsEffect.LoadData(assignmentId, courseId))

        verify(timeout = 100) {
            eventConsumer.accept(
                SubmissionDetailsEvent.DataLoaded(
                    DataResult.Fail(Failure.Network(errorMessage)),
                    DataResult.Fail(Failure.Network(errorMessage)),
                    DataResult.Fail(null),
                    false
                )
            )
        }

        confirmVerified(eventConsumer)
    }

    @Test
    fun `Failed auth LoadData results in fail DataLoaded`() {
        val user = User()
        val courseId = 1L
        val assignmentId = 1L
        val errorMessage = "Error"

        mockkObject(AssignmentManager)
        mockkObject(SubmissionManager)

        every { AssignmentManager.getAssignmentAsync(any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Fail(Failure.Authorization(errorMessage))
        }

        every { SubmissionManager.getSingleSubmissionAsync(any(), any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Fail(Failure.Authorization(errorMessage))
        }

        mockkStatic(ApiPrefs::class)
        every { ApiPrefs.user } returns user

        connection.accept(SubmissionDetailsEffect.LoadData(assignmentId, courseId))

        verify(timeout = 100) {
            eventConsumer.accept(
                SubmissionDetailsEvent.DataLoaded(
                    DataResult.Fail(Failure.Authorization(errorMessage)),
                    DataResult.Fail(Failure.Authorization(errorMessage)),
                    DataResult.Fail(null),
                    false
                )
            )
        }

        confirmVerified(eventConsumer)
    }

    /**
     * Can't test loading data successfully as the `awaitApis` function is inline.
     * No other tool is available in the app to do parallel network operations, besides `inParallel`,
     * which is also unmockkable as all data is passed around through callbacks.
     *
     * We either need to make an `awaitApis` that is not inline or have a new repository pattern that
     * is testable with our mobius loops
     *
     * wontfix from mockk: https://github.com/mockk/mockk/issues/27
     */
    @Test
    fun `LoadData results in DataLoaded`() {
        val courseId = 1L
        val assignment = Assignment().copy(submissionTypesRaw = listOf(Assignment.SubmissionType.EXTERNAL_TOOL.apiString), url="https://www.instructure.com")
        val submission = Submission()
        val user = User()
        val ltiTool = LTITool(url = "https://www.instructure.com")

        mockkObject(AssignmentManager)
        mockkObject(SubmissionManager)

        every { AssignmentManager.getAssignmentAsync(any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(assignment)
        }

        every { SubmissionManager.getSingleSubmissionAsync(any(), any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(submission)
        }

        every { SubmissionManager.getLtiFromAuthenticationUrlAsync(any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(ltiTool)
        }

        mockkStatic(ApiPrefs::class)
        every { ApiPrefs.user } returns user

        connection.accept(SubmissionDetailsEffect.LoadData(assignment.id, courseId))

        verify(timeout = 100) {
            eventConsumer.accept(
                SubmissionDetailsEvent.DataLoaded(
                    DataResult.Success(assignment),
                    DataResult.Success(submission),
                    DataResult.Success(ltiTool),
                    false
                )
            )
        }

        confirmVerified(eventConsumer)
    }

    @Test
    fun `ShowSubmissionContentType results in view calling showSubmissionContent`() {
        val contentType = SubmissionDetailsContentType.NoneContent

        connection.accept(SubmissionDetailsEffect.ShowSubmissionContentType(contentType))

        verify(timeout = 100) {
            view.showSubmissionContent(contentType)
        }

        confirmVerified(view)
    }

    @Test
    fun `ShowAudioRecordingView results in view calling showAudioRecordingView`() {
        connection.accept(SubmissionDetailsEffect.ShowAudioRecordingView)

        verify(timeout = 100) {
            view.showAudioRecordingView()
        }

        confirmVerified(view)
    }

    @Test
    fun `ShowVideoRecordingView results in view calling showVideoRecordingView`() {
        connection.accept(SubmissionDetailsEffect.ShowVideoRecordingView)

        verify(timeout = 100) {
            view.showVideoRecordingView()
        }

        confirmVerified(view)
    }

    @Test
    fun `ShowVideoRecordingPlayback results in view calling showVideoRecordingPlayback`() {
        val file = File("test")
        connection.accept(SubmissionDetailsEffect.ShowVideoRecordingPlayback(file))

        verify(timeout = 100) {
            view.showVideoRecordingPlayback(file)
        }

        confirmVerified(view)
    }

    @Test
    fun `ShowVideoRecordingPlaybackError results in view calling showVideoRecordingPlaybackError`() {
        connection.accept(SubmissionDetailsEffect.ShowVideoRecordingPlaybackError)

        verify(timeout = 100) {
            view.showVideoRecordingPlaybackError()
        }

        confirmVerified(view)
    }

    @Test
    fun `ShowMediaCommentError results in view calling showMediaCommentError`() {
        connection.accept(SubmissionDetailsEffect.ShowMediaCommentError)

        verify(timeout = 100) {
            view.showMediaCommentError()
        }

        confirmVerified(view)
    }

    @Test
    fun `UploadMediaComment results in SendMediaCommentClicked shared event`() {
        val file = File("test")
        val channel = ChannelSource.getChannel<SubmissionCommentsSharedEvent>()
        val expectedEvent = SubmissionCommentsSharedEvent.SendMediaCommentClicked(file)
        val actualEvent = channel.receiveOnce {
            connection.accept(SubmissionDetailsEffect.UploadMediaComment(file))
        }

        assertEquals(expectedEvent, actualEvent)
    }

    @Test
    fun `MediaCommentDialogClosed results in MediaCommentDialogClosed shared event`() {
        val channel = ChannelSource.getChannel<SubmissionCommentsSharedEvent>()
        val expectedEvent = SubmissionCommentsSharedEvent.MediaCommentDialogClosed
        val actualEvent = channel.receiveOnce {
            connection.accept(SubmissionDetailsEffect.MediaCommentDialogClosed)
        }

        assertEquals(expectedEvent, actualEvent)
    }

}

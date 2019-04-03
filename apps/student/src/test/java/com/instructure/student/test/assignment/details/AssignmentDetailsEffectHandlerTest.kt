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
package com.instructure.student.test.assignment.details

import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.weave.StatusCallbackError
import com.instructure.canvasapi2.utils.weave.awaitApiResponse
import com.instructure.student.mobius.assignmentDetails.AssignmentDetailsEffect
import com.instructure.student.mobius.assignmentDetails.AssignmentDetailsEffectHandler
import com.instructure.student.mobius.assignmentDetails.AssignmentDetailsEvent
import com.instructure.student.mobius.assignmentDetails.SubmissionUploadStatus
import com.instructure.student.mobius.assignmentDetails.ui.AssignmentDetailsView
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.Failure
import com.spotify.mobius.functions.Consumer
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.test.setMain
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.ResponseBody
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import java.util.concurrent.Executors

class AssignmentDetailsEffectHandlerTest : Assert() {
    private val view: AssignmentDetailsView = mockk(relaxed = true)
    private val effectHandler =
            AssignmentDetailsEffectHandler().apply { view = this@AssignmentDetailsEffectHandlerTest.view }
    private val eventConsumer: Consumer<AssignmentDetailsEvent> = mockk(relaxed = true)
    private val connection = effectHandler.connect(eventConsumer)

    @ExperimentalCoroutinesApi
    @Before
    fun setup() {
        Dispatchers.setMain(Executors.newSingleThreadExecutor().asCoroutineDispatcher())
    }

    @Test
    fun `Failed LoadData results in fail DataLoaded`() {
        val courseId = 1L
        val assignmentId = 1L
        val errorMessage = "Error"
        val expectedEvent = AssignmentDetailsEvent.DataLoaded(
                DataResult.Fail(Failure.Network(errorMessage))
        )

        mockkStatic("com.instructure.canvasapi2.utils.weave.AwaitApiKt")
        coEvery { awaitApiResponse<Assignment>(any()) } throws createError<Assignment>(errorMessage)

        connection.accept(AssignmentDetailsEffect.LoadData(assignmentId, courseId, false))

        verify(timeout = 100) {
            eventConsumer.accept(expectedEvent)
        }

        confirmVerified(eventConsumer)
    }

    @Test
    fun `Auth failed LoadData results in fail DataLoaded`() {
        val courseId = 1L
        val assignmentId = 1L
        val errorMessage = "Error"
        val expectedEvent = AssignmentDetailsEvent.DataLoaded(
                DataResult.Fail(Failure.Authorization(errorMessage))
        )

        mockkStatic("com.instructure.canvasapi2.utils.weave.AwaitApiKt")
        coEvery { awaitApiResponse<Assignment>(any()) } throws createError<Assignment>(errorMessage, 401)

        connection.accept(AssignmentDetailsEffect.LoadData(assignmentId, courseId, false))

        verify(timeout = 100) {
            eventConsumer.accept(expectedEvent)
        }

        confirmVerified(eventConsumer)
    }

    @Test
    fun `Successful LoadData results in DataLoaded`() {
        val courseId = 1L
        val assignment = Assignment()
        val expectedEvent = AssignmentDetailsEvent.DataLoaded(
                DataResult.Success(assignment)
        )

        mockkStatic("com.instructure.canvasapi2.utils.weave.AwaitApiKt")
        coEvery { awaitApiResponse<Assignment>(any()) } returns Response.success(assignment)

        connection.accept(AssignmentDetailsEffect.LoadData(assignment.id, courseId, false))

        verify(timeout = 100) {
            eventConsumer.accept(expectedEvent)
        }

        confirmVerified(eventConsumer)
    }

    @Test
    fun `ShowSubmitAssignmentView calls ShowSubmitDialogView on the view`() {
        val course = Course()
        val assignmentId = 1L

        connection.accept(AssignmentDetailsEffect.ShowSubmitDialogView(assignmentId, course))

        verify(timeout = 100) {
            view.showSubmitDialogView(assignmentId, course)
        }

        confirmVerified(view)
    }

    @Test
    fun `ShowSubmissionView calls showSubmissionView on the view`() {
        val course = Course()
        val assignmentId = 1L

        connection.accept(AssignmentDetailsEffect.ShowSubmissionView(assignmentId, course))

        verify(timeout = 100) {
            view.showSubmissionView(assignmentId, course)
        }

        confirmVerified(view)
    }

    @Test
    fun `ShowUploadStatusView calls showUploadStatusView on the view`() {
        val course = Course()
        val assignmentId = 1L

        connection.accept(AssignmentDetailsEffect.ShowUploadStatusView(assignmentId, course))

        verify(timeout = 100) {
            view.showUploadStatusView(assignmentId, course)
        }

        confirmVerified(view)
    }

    // TODO: Finish submission status events once implemented

    @Test
    fun `ObserveSubmissionStatus results in SubmissionStatusUpdated event`() {
        val assignmentId = 1L
        val expectedEvent = AssignmentDetailsEvent.SubmissionStatusUpdated(SubmissionUploadStatus.Empty)

        connection.accept(AssignmentDetailsEffect.ObserveSubmissionStatus(assignmentId))

        verify(timeout = 100) {
            eventConsumer.accept(expectedEvent)
        }

        confirmVerified(eventConsumer)
    }

    @Test
    fun `ShowCreateSubmissionView with quiz submissionType calls showQuizOrDiscussionView`() {
        val courseId = 1234L
        val quizId = 1234L
        val assignmentId = 1234L
        val assignment = Assignment(id = assignmentId, quizId = quizId, courseId = courseId)
        val domain = "mobiledev.instructure.com/api/v1"
        val protocol = "https"
        val submissionType = Assignment.SubmissionType.ONLINE_QUIZ

        mockkStatic(ApiPrefs::class)
        every { ApiPrefs.protocol } returns protocol
        every { ApiPrefs.domain } returns domain

        connection.accept(AssignmentDetailsEffect.ShowCreateSubmissionView(submissionType, courseId, assignment))


        val url = "$protocol://$domain/courses/$courseId/quizzes/$quizId"

        verify(timeout = 100) {
            view.showQuizOrDiscussionView(url)
        }
        confirmVerified(view)
    }

    @Test
    fun `ShowCreateSubmissionView with discussion submissionType calls showQuizOrDiscussionView`() {
        val courseId = 1234L
        val discussionTopicId = 1234L
        val assignmentId = 1234L
        val assignment = Assignment(id = assignmentId, courseId = courseId, discussionTopicHeader = DiscussionTopicHeader(id = discussionTopicId))
        val domain = "mobiledev.instructure.com/api/v1"
        val protocol = "https"
        val submissionType = Assignment.SubmissionType.DISCUSSION_TOPIC

        mockkStatic(ApiPrefs::class)
        every { ApiPrefs.protocol } returns protocol
        every { ApiPrefs.domain } returns domain

        connection.accept(AssignmentDetailsEffect.ShowCreateSubmissionView(submissionType, courseId, assignment))


        val url = "$protocol://$domain/courses/$courseId/discussion_topics/$discussionTopicId"

        verify(timeout = 100) {
            view.showQuizOrDiscussionView(url)
        }
        confirmVerified(view)
    }

    @Test
    fun `ShowCreateSubmissionView with fileUpload submissionType calls showFileUploadView`() {
        val courseId = 1234L
        val assignmentId = 1234L
        val assignment = Assignment(id = assignmentId, courseId = courseId)
        val submissionType = Assignment.SubmissionType.ONLINE_UPLOAD

        connection.accept(AssignmentDetailsEffect.ShowCreateSubmissionView(submissionType, courseId, assignment))

        verify(timeout = 100) {
            view.showFileUploadView(assignment, courseId)
        }
        confirmVerified(view)
    }

    @Test
    fun `ShowCreateSubmissionView with textEntry submissionType calls showOnlineTextEntryView`() {
        val courseId = 1234L
        val assignmentId = 1234L
        val assignment = Assignment(id = assignmentId, courseId = courseId)
        val submissionType = Assignment.SubmissionType.ONLINE_TEXT_ENTRY

        connection.accept(AssignmentDetailsEffect.ShowCreateSubmissionView(submissionType, courseId, assignment))

        verify(timeout = 100) {
            view.showOnlineTextEntryView(assignmentId, courseId)
        }
        confirmVerified(view)
    }

    @Test
    fun `ShowCreateSubmissionView with urlEntry submissionType calls showOnlineUrlEntryView`() {
        val courseId = 1234L
        val assignmentId = 1234L
        val assignment = Assignment(id = assignmentId, courseId = courseId)
        val submissionType = Assignment.SubmissionType.ONLINE_URL

        connection.accept(AssignmentDetailsEffect.ShowCreateSubmissionView(submissionType, courseId, assignment))

        verify(timeout = 100) {
            view.showOnlineUrlEntryView(assignmentId, courseId)
        }
        confirmVerified(view)
    }

    @Test
    fun `ShowCreateSubmissionView with mediaRecording submissionType calls showMediaRecordingView`() {
        val courseId = 1234L
        val assignmentId = 1234L
        val assignment = Assignment(id = assignmentId, courseId = courseId)
        val submissionType = Assignment.SubmissionType.MEDIA_RECORDING

        connection.accept(AssignmentDetailsEffect.ShowCreateSubmissionView(submissionType, courseId, assignment))

        verify(timeout = 100) {
            view.showMediaRecordingView(assignment, courseId)
        }
        confirmVerified(view)
    }

    private fun <T> createError(message: String = "Error", code: Int = 400) = StatusCallbackError(
        null,
        null,
        Response.error<T>(
            ResponseBody.create(null, ""),
            okhttp3.Response.Builder()
                .protocol(Protocol.HTTP_1_1)
                .message(message)
                .code(code)
                .request(Request.Builder().url("http://localhost/").build())
                .build()
        )
    )
}

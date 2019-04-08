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
import com.instructure.student.mobius.assignmentDetails.ui.AssignmentDetailsView
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.Failure
import com.instructure.student.mobius.assignmentDetails.*
import com.instructure.student.mobius.assignmentDetails.ui.AssignmentDetailsViewState
import com.instructure.student.mobius.assignmentDetails.ui.SubmissionTypesVisibilities
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

    lateinit var assignment: Assignment

    @ExperimentalCoroutinesApi
    @Before
    fun setup() {
        Dispatchers.setMain(Executors.newSingleThreadExecutor().asCoroutineDispatcher())
        assignment = Assignment(id = 2468, courseId = 8642)
    }

    @Test
    fun `Failed LoadData results in fail DataLoaded`() {
        val courseId = 1L
        val errorMessage = "Error"
        val expectedEvent = AssignmentDetailsEvent.DataLoaded(
                DataResult.Fail(Failure.Network(errorMessage))
        )

        mockkStatic("com.instructure.canvasapi2.utils.weave.AwaitApiKt")
        coEvery { awaitApiResponse<Assignment>(any()) } throws createError<Assignment>(errorMessage)

        connection.accept(AssignmentDetailsEffect.LoadData(assignment.id, courseId, false))

        verify(timeout = 100) {
            eventConsumer.accept(expectedEvent)
        }

        confirmVerified(eventConsumer)
    }

    @Test
    fun `Auth failed LoadData results in fail DataLoaded`() {
        val courseId = 1L
        val errorMessage = "Error"
        val expectedEvent = AssignmentDetailsEvent.DataLoaded(
                DataResult.Fail(Failure.Authorization(errorMessage))
        )

        mockkStatic("com.instructure.canvasapi2.utils.weave.AwaitApiKt")
        coEvery { awaitApiResponse<Assignment>(any()) } throws createError<Assignment>(errorMessage, 401)

        connection.accept(AssignmentDetailsEffect.LoadData(assignment.id, courseId, false))

        verify(timeout = 100) {
            eventConsumer.accept(expectedEvent)
        }

        confirmVerified(eventConsumer)
    }

    @Test
    fun `Successful LoadData results in DataLoaded`() {
        val courseId = 1L
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

        connection.accept(AssignmentDetailsEffect.ShowSubmitDialogView(assignment, course))

        verify(timeout = 100) {
            view.showSubmitDialogView(assignment, course.id, SubmissionTypesVisibilities())
        }

        confirmVerified(view)
    }

    @Test
    fun `ShowSubmissionView calls showSubmissionView on the view`() {
        val course = Course()

        connection.accept(AssignmentDetailsEffect.ShowSubmissionView(assignment.id, course))

        verify(timeout = 100) {
            view.showSubmissionView(assignment.id, course)
        }

        confirmVerified(view)
    }

    @Test
    fun `ShowUploadStatusView calls showUploadStatusView on the view`() {
        val course = Course()

        connection.accept(AssignmentDetailsEffect.ShowUploadStatusView(assignment.id, course))

        verify(timeout = 100) {
            view.showUploadStatusView(assignment.id, course)
        }

        confirmVerified(view)
    }

    // TODO: Finish submission status events once implemented

    @Test
    fun `ObserveSubmissionStatus results in SubmissionStatusUpdated event`() {
        val expectedEvent = AssignmentDetailsEvent.SubmissionStatusUpdated(SubmissionUploadStatus.Empty)

        connection.accept(AssignmentDetailsEffect.ObserveSubmissionStatus(assignment.id))

        verify(timeout = 100) {
            eventConsumer.accept(expectedEvent)
        }

        confirmVerified(eventConsumer)
    }

    @Test
    fun `ShowCreateSubmissionView with quiz submissionType calls showQuizOrDiscussionView`() {
        val quizId = 1234L
        val domain = "mobiledev.instructure.com/api/v1"
        val protocol = "https"
        val submissionType = Assignment.SubmissionType.ONLINE_QUIZ
        val assignment = assignment.copy(quizId = quizId)

        mockkStatic(ApiPrefs::class)
        every { ApiPrefs.protocol } returns protocol
        every { ApiPrefs.domain } returns domain

        connection.accept(AssignmentDetailsEffect.ShowCreateSubmissionView(submissionType, assignment.courseId, assignment))


        val url = "$protocol://$domain/courses/${assignment.courseId}/quizzes/$quizId"

        verify(timeout = 100) {
            view.showQuizOrDiscussionView(url)
        }
        confirmVerified(view)
    }

    @Test
    fun `ShowCreateSubmissionView with discussion submissionType calls showQuizOrDiscussionView`() {
        val discussionTopicId = 1234L
        val domain = "mobiledev.instructure.com/api/v1"
        val protocol = "https"
        val submissionType = Assignment.SubmissionType.DISCUSSION_TOPIC
        val assignment = assignment.copy(discussionTopicHeader = DiscussionTopicHeader(id = discussionTopicId))

        mockkStatic(ApiPrefs::class)
        every { ApiPrefs.protocol } returns protocol
        every { ApiPrefs.domain } returns domain

        connection.accept(AssignmentDetailsEffect.ShowCreateSubmissionView(submissionType, assignment.courseId, assignment))


        val url = "$protocol://$domain/courses/${assignment.courseId}/discussion_topics/$discussionTopicId"

        verify(timeout = 100) {
            view.showQuizOrDiscussionView(url)
        }
        confirmVerified(view)
    }

    @Test
    fun `ShowCreateSubmissionView with fileUpload submissionType calls showFileUploadView`() {
        val submissionType = Assignment.SubmissionType.ONLINE_UPLOAD

        connection.accept(AssignmentDetailsEffect.ShowCreateSubmissionView(submissionType, assignment.courseId, assignment))

        verify(timeout = 100) {
            view.showFileUploadView(assignment, assignment.courseId)
        }
        confirmVerified(view)
    }

    @Test
    fun `ShowCreateSubmissionView with textEntry submissionType calls showOnlineTextEntryView`() {
        val submissionType = Assignment.SubmissionType.ONLINE_TEXT_ENTRY

        connection.accept(AssignmentDetailsEffect.ShowCreateSubmissionView(submissionType, assignment.courseId, assignment))

        verify(timeout = 100) {
            view.showOnlineTextEntryView(assignment.id, assignment.courseId)
        }
        confirmVerified(view)
    }

    @Test
    fun `ShowCreateSubmissionView with urlEntry submissionType calls showOnlineUrlEntryView`() {
        val submissionType = Assignment.SubmissionType.ONLINE_URL

        connection.accept(AssignmentDetailsEffect.ShowCreateSubmissionView(submissionType, assignment.courseId, assignment))

        verify(timeout = 100) {
            view.showOnlineUrlEntryView(assignment.id, assignment.courseId)
        }
        confirmVerified(view)
    }

    @Test
    fun `ShowCreateSubmissionView with mediaRecording submissionType calls showMediaRecordingView`() {
        val courseId = 1234L
        val submissionType = Assignment.SubmissionType.MEDIA_RECORDING

        connection.accept(AssignmentDetailsEffect.ShowCreateSubmissionView(submissionType, courseId, assignment))

        verify(timeout = 100) {
            view.showMediaRecordingView(assignment, courseId)
        }
        confirmVerified(view)
    }


    @Test
    fun `Displays fileUpload when submission type is fileUpload`() {
        val course = Course()
        val assignment = assignment.copy(
                submissionTypesRaw = listOf("online_upload")
        )

        connection.accept(AssignmentDetailsEffect.ShowSubmitDialogView(assignment, course))

        verify(timeout = 100) {
            view.showSubmitDialogView(assignment, course.id, SubmissionTypesVisibilities(fileUpload = true))
        }

        confirmVerified(view)
    }

    @Test
    fun `Displays textEntry when submission type is textEntry`() {
        val course = Course()
        val assignment = assignment.copy(
                submissionTypesRaw = listOf("online_text_entry")
        )

        connection.accept(AssignmentDetailsEffect.ShowSubmitDialogView(assignment, course))

        verify(timeout = 100) {
            view.showSubmitDialogView(assignment, course.id, SubmissionTypesVisibilities(textEntry = true))
        }

        confirmVerified(view)
    }

    @Test
    fun `Displays onlineUrl when submission type is onlineUrl`() {
        val course = Course()
        val assignment = assignment.copy(
                submissionTypesRaw = listOf("online_url")
        )

        connection.accept(AssignmentDetailsEffect.ShowSubmitDialogView(assignment, course))

        verify(timeout = 100) {
            view.showSubmitDialogView(assignment, course.id, SubmissionTypesVisibilities(urlEntry = true))
        }

        confirmVerified(view)
    }

    @Test
    fun `Displays mediaRecording when submission type is mediaRecording`() {
        val course = Course()
        val assignment = assignment.copy(
                submissionTypesRaw = listOf("media_recording")
        )

        connection.accept(AssignmentDetailsEffect.ShowSubmitDialogView(assignment, course))

        verify(timeout = 100) {
            view.showSubmitDialogView(assignment, course.id, SubmissionTypesVisibilities(mediaRecording = true))
        }

        confirmVerified(view)
    }

    @Test
    fun `Displays all submission types when all are present`() {
        val course = Course()
        val assignment = assignment.copy(
                submissionTypesRaw = listOf("media_recording", "online_url", "online_text_entry", "online_upload")
        )

        connection.accept(AssignmentDetailsEffect.ShowSubmitDialogView(assignment, course))

        verify(timeout = 100) {
            view.showSubmitDialogView(assignment, course.id, SubmissionTypesVisibilities(true, true, true, true))
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

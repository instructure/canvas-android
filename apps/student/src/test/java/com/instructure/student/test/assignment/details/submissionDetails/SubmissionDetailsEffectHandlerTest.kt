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

import com.instructure.canvasapi2.managers.*
import com.instructure.canvasapi2.models.*
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
    private val effectHandler = SubmissionDetailsEffectHandler().apply { view = this@SubmissionDetailsEffectHandlerTest.view }
    private val eventConsumer: Consumer<SubmissionDetailsEvent> = mockk(relaxed = true)
    private val connection = effectHandler.connect(eventConsumer)

    @ExperimentalCoroutinesApi
    @Before
    fun setup() {
        Dispatchers.setMain(Executors.newSingleThreadExecutor().asCoroutineDispatcher())
        mockkObject(FeaturesManager)
        every { FeaturesManager.getEnabledFeaturesForCourseAsync(any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(listOf("assignments_2_student"))
        }
    }

    @Test
    fun `Failed loadData results in fail DataLoaded`() {
        val user = User()
        val courseId = 1L
        val assignmentId = 1L
        val errorMessage = "Error"

        mockkObject(AssignmentManager)
        mockkObject(SubmissionManager)
        mockkObject(EnrollmentManager)

        every { EnrollmentManager.getObserveeEnrollmentsAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Fail(Failure.Network(errorMessage))
        }

        every { AssignmentManager.getAssignmentAsync(any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Fail(Failure.Network(errorMessage))
        }

        every { SubmissionManager.getSingleSubmissionAsync(any(), any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Fail(Failure.Network(errorMessage))
        }

        mockkObject(ApiPrefs)
        every { ApiPrefs.user } returns user

        connection.accept(SubmissionDetailsEffect.LoadData(assignmentId, courseId))

        verify(timeout = 100) {
            eventConsumer.accept(
                SubmissionDetailsEvent.DataLoaded(
                    assignment = DataResult.Fail(Failure.Network(errorMessage)),
                    rootSubmissionResult = DataResult.Fail(Failure.Network(errorMessage)),
                    ltiUrlResult = DataResult.Fail(null),
                    isStudioEnabled = false,
                    quizResult = null,
                    studioLTIToolResult = DataResult.Fail(null),
                    assignmentEnhancementsEnabled = true
                )
            )
        }

        confirmVerified(eventConsumer)
    }

    @Test
    fun `Failed auth loadData results in fail DataLoaded`() {
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

        mockkObject(ApiPrefs)
        every { ApiPrefs.user } returns user

        connection.accept(SubmissionDetailsEffect.LoadData(assignmentId, courseId))

        verify(timeout = 100) {
            eventConsumer.accept(
                SubmissionDetailsEvent.DataLoaded(
                    assignment = DataResult.Fail(Failure.Authorization(errorMessage)),
                    rootSubmissionResult = DataResult.Fail(Failure.Authorization(errorMessage)),
                    ltiUrlResult = DataResult.Fail(null),
                    isStudioEnabled = false,
                    quizResult = null,
                    studioLTIToolResult = DataResult.Fail(null),
                    assignmentEnhancementsEnabled = true
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
    fun `loadData results in DataLoaded`() {
        val courseId = 1L
        val assignment = Assignment().copy(submissionTypesRaw = listOf(Assignment.SubmissionType.EXTERNAL_TOOL.apiString), url="https://www.instructure.com")
        val submission = Submission()
        val user = User()
        val ltiTool = LTITool(url = "https://www.instructure.com")

        mockkObject(AssignmentManager)
        mockkObject(SubmissionManager)
        mockkObject(EnrollmentManager)

       every { EnrollmentManager.getObserveeEnrollmentsAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(listOf())
        }

        every { AssignmentManager.getAssignmentAsync(any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(assignment)
        }

        every { SubmissionManager.getSingleSubmissionAsync(any(), any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(submission)
        }

        every { SubmissionManager.getLtiFromAuthenticationUrlAsync(any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(ltiTool)
        }

        mockkObject(ApiPrefs)
        every { ApiPrefs.user } returns user

        connection.accept(SubmissionDetailsEffect.LoadData(courseId, assignment.id))

        verify(timeout = 100) {
            eventConsumer.accept(
                SubmissionDetailsEvent.DataLoaded(
                    assignment = DataResult.Success(assignment),
                    rootSubmissionResult = DataResult.Success(submission),
                    ltiUrlResult = DataResult.Success(ltiTool),
                    isStudioEnabled = false,
                    quizResult = null,
                    studioLTIToolResult = DataResult.Fail(null),
                    assignmentEnhancementsEnabled = true
                )
            )
        }

        confirmVerified(eventConsumer)
    }

    @Test
    fun `loadData with external tag attributes results in DataLoaded`() {
        val courseId = 1L
        val assignmentId = 1L
        val ltiUrl = "https://www.instructure.com"
        val ltiId = 123L
        val ltiTool = LTITool(id = ltiId, url = ltiUrl, assignmentId = assignmentId, courseId = courseId)
        val externalToolAttributes = ExternalToolAttributes(url = ltiUrl, contentId = ltiId)
        val assignment = Assignment().copy(
            id = assignmentId,
            courseId = courseId,
            externalToolAttributes = externalToolAttributes,
            submissionTypesRaw = listOf(Assignment.SubmissionType.EXTERNAL_TOOL.apiString)
        )
        val submission = Submission()
        val user = User()

        mockkObject(AssignmentManager)
        mockkObject(SubmissionManager)
        mockkObject(EnrollmentManager)

        every { EnrollmentManager.getObserveeEnrollmentsAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(listOf())
        }

        every { AssignmentManager.getAssignmentAsync(any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(assignment)
        }

        every { SubmissionManager.getSingleSubmissionAsync(any(), any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(submission)
        }

        every { AssignmentManager.getExternalToolLaunchUrlAsync(any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(ltiTool)
        }

        mockkObject(ApiPrefs)
        every { ApiPrefs.user } returns user

        connection.accept(SubmissionDetailsEffect.LoadData(courseId, assignment.id))

        verify(timeout = 100) {
            eventConsumer.accept(
                    SubmissionDetailsEvent.DataLoaded(
                            assignment = DataResult.Success(assignment),
                            rootSubmissionResult = DataResult.Success(submission),
                            ltiUrlResult = DataResult.Success(ltiTool),
                            isStudioEnabled = false,
                            quizResult = null,
                            studioLTIToolResult = DataResult.Fail(null),
                            assignmentEnhancementsEnabled = true
                    )
            )
        }

        confirmVerified(eventConsumer)
    }

    @Test
    fun `loadData for observer results in DataLoaded`() {
        val courseId = 1L
        val observerId = 9000L
        val assignment = Assignment().copy(submissionTypesRaw = listOf(Assignment.SubmissionType.EXTERNAL_TOOL.apiString), url="https://www.instructure.com")
        val submission = Submission()
        val observeeEnrollment = Enrollment().copy(role = Enrollment.EnrollmentType.Observer, courseId = courseId, associatedUserId = observerId)
        val user = User()
        val ltiTool = LTITool(url = "https://www.instructure.com")

        mockkObject(AssignmentManager)
        mockkObject(SubmissionManager)
        mockkObject(EnrollmentManager)

        every { EnrollmentManager.getObserveeEnrollmentsAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(listOf(observeeEnrollment))
        }

        every { AssignmentManager.getAssignmentAsync(any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(assignment)
        }

        every { SubmissionManager.getSingleSubmissionAsync(any(), any(), observerId, any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(submission)
        }

        every { SubmissionManager.getLtiFromAuthenticationUrlAsync(any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(ltiTool)
        }

        mockkObject(ApiPrefs)
        every { ApiPrefs.user } returns user

        connection.accept(SubmissionDetailsEffect.LoadData(courseId, assignment.id))

        verify(timeout = 100) {
            eventConsumer.accept(
                SubmissionDetailsEvent.DataLoaded(
                    assignment = DataResult.Success(assignment),
                    rootSubmissionResult = DataResult.Success(submission),
                    ltiUrlResult = DataResult.Success(ltiTool),
                    isStudioEnabled = false,
                    quizResult = null,
                    studioLTIToolResult = DataResult.Fail(null),
                    assignmentEnhancementsEnabled = true
                )
            )
        }

        confirmVerified(eventConsumer)
    }



    @Test
    fun `loadData gets quiz if assignment is a quiz`() {
        val courseId = 1L
        val quizId = 1234L
        val assignment = Assignment().copy(submissionTypesRaw = listOf(Assignment.SubmissionType.ONLINE_QUIZ.apiString), quizId = quizId)
        val submission = Submission()
        val quiz = Quiz(quizId)
        val user = User()

        mockkObject(AssignmentManager)
        mockkObject(SubmissionManager)
        mockkObject(QuizManager)

        every { AssignmentManager.getAssignmentAsync(any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(assignment)
        }

        every { SubmissionManager.getSingleSubmissionAsync(any(), any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(submission)
        }

        every { SubmissionManager.getLtiFromAuthenticationUrlAsync(any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Fail(null)
        }

        every { QuizManager.getQuizAsync(any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(quiz)
        }

        mockkObject(ApiPrefs)
        every { ApiPrefs.user } returns user
        every { ApiPrefs.fullDomain} returns "https://www.instructure.com"

        connection.accept(SubmissionDetailsEffect.LoadData(courseId, assignment.id))

        verify(timeout = 100) {
            eventConsumer.accept(
                SubmissionDetailsEvent.DataLoaded(
                    assignment = DataResult.Success(assignment),
                    rootSubmissionResult = DataResult.Success(submission),
                    ltiUrlResult = DataResult.Fail(null),
                    isStudioEnabled = false,
                    quizResult = DataResult.Success(quiz),
                    studioLTIToolResult = DataResult.Fail(null),
                    assignmentEnhancementsEnabled = true
                )
            )
        }

        confirmVerified(eventConsumer)
    }

    @Test
    fun `loadData gets Studio LTI tool if it is enabled`() {
        val courseId = 1L
        val assignment = Assignment().copy(submissionTypesRaw = listOf(Assignment.SubmissionType.ONLINE_UPLOAD.apiString))
        val submission = Submission()
        val user = User()
        val studioLTITool = LTITool(url = "instructuremedia.com/lti/launch")

        mockkObject(AssignmentManager)
        mockkObject(SubmissionManager)
        mockkObject(ExternalToolManager)

        every { AssignmentManager.getAssignmentAsync(any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(assignment)
        }

        every { SubmissionManager.getSingleSubmissionAsync(any(), any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(submission)
        }

        every { SubmissionManager.getLtiFromAuthenticationUrlAsync(any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Fail(null)
        }

        every { ExternalToolManager.getExternalToolsForCanvasContextAsync(any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(listOf(studioLTITool))
        }

        mockkObject(ApiPrefs)
        every { ApiPrefs.user } returns user

        connection.accept(SubmissionDetailsEffect.LoadData(courseId, assignment.id))

        verify(timeout = 100000) {
            eventConsumer.accept(
                SubmissionDetailsEvent.DataLoaded(
                    assignment = DataResult.Success(assignment),
                    rootSubmissionResult = DataResult.Success(submission),
                    ltiUrlResult = DataResult.Fail(null),
                    isStudioEnabled = true,
                    quizResult = null,
                    studioLTIToolResult = DataResult.Success(studioLTITool),
                    assignmentEnhancementsEnabled = true
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

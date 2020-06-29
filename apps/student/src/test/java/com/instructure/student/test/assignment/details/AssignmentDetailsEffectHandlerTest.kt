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

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.google.firebase.analytics.FirebaseAnalytics
import com.instructure.canvasapi2.managers.CourseManager
import com.instructure.canvasapi2.managers.ExternalToolManager
import com.instructure.canvasapi2.managers.QuizManager
import com.instructure.canvasapi2.managers.SubmissionManager
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.utils.*
import com.instructure.canvasapi2.utils.weave.StatusCallbackError
import com.instructure.canvasapi2.utils.weave.awaitApiResponse
import com.instructure.pandautils.utils.FilePrefs
import com.instructure.pandautils.utils.FileUploadUtils
import com.instructure.pandautils.utils.PermissionUtils
import com.instructure.pandautils.utils.requestPermissions
import com.instructure.student.Submission
import com.instructure.student.db.Db
import com.instructure.student.db.StudentDb
import com.instructure.student.db.getInstance
import com.instructure.student.mobius.assignmentDetails.*
import com.instructure.student.mobius.assignmentDetails.ui.AssignmentDetailsFragment
import com.instructure.student.mobius.assignmentDetails.ui.AssignmentDetailsView
import com.instructure.student.mobius.assignmentDetails.ui.SubmissionTypesVisibilities
import com.instructure.student.mobius.common.ui.SubmissionService
import com.spotify.mobius.Connection
import com.spotify.mobius.functions.Consumer
import com.squareup.sqldelight.Query
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
import org.threeten.bp.OffsetDateTime
import retrofit2.Response
import java.io.File
import java.util.*
import java.util.concurrent.Executors

class AssignmentDetailsEffectHandlerTest : Assert() {
    private val assignmentId = 2468L
    private val view: AssignmentDetailsView = mockk(relaxed = true)
    private val context: Activity = mockk(relaxed = true)
    private val firebase: FirebaseAnalytics = mockk(relaxed = true)
    private var effectHandler =
        AssignmentDetailsEffectHandler(context, assignmentId).apply { view = this@AssignmentDetailsEffectHandlerTest.view }
    private val eventConsumer: Consumer<AssignmentDetailsEvent> = mockk(relaxed = true)
    private lateinit var connection: Connection<AssignmentDetailsEffect>

    lateinit var assignment: Assignment
    lateinit var quiz: Quiz
    lateinit var course: Course
    lateinit var queryMockk: Query<Submission>
    private var userId: Long = 0

    @ExperimentalCoroutinesApi
    @Before
    fun setup() {
        Dispatchers.setMain(Executors.newSingleThreadExecutor().asCoroutineDispatcher())
        assignment = Assignment(id = assignmentId, courseId = 8642)
        quiz = Quiz(id = 12345L)
        course = Course()
        userId = 6789L

        mockkStatic(ApiPrefs::class)
        every { ApiPrefs.user } returns User(id = userId)

        mockkStatic("com.instructure.student.db.ExtensionsKt")

        queryMockk = mockk(relaxed = true)
        val db: StudentDb = mockk {
            every {
                submissionQueries.getSubmissionsByAssignmentId(assignment.id, userId)
            } returns queryMockk
        }

        every { Db.getInstance(context) } returns db

        // Connect after mocks, so database is setup properly
        connection = effectHandler.connect(eventConsumer)

        Analytics.firebase = firebase

        mockkObject(CourseManager)
        every { CourseManager.getCourseWithGradeAsync(any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(Course())
        }
    }

    private fun mockkDatabase(data: List<Submission> = emptyList()) {
        every { queryMockk.executeAsList() } returns data
    }

    private fun mockkSubmission(
        submissionId: Long,
        daysAgo: Long = 0,
        failed: Boolean = false,
        submissionType: String = "online_text_entry"
    ): Submission {
        return Submission.Impl(
            id = submissionId,
            submissionEntry = "Entry text",
            lastActivityDate = OffsetDateTime.now().minusDays(daysAgo),
            assignmentName = "Assignment Name",
            assignmentId = assignment.id,
            canvasContext = course,
            submissionType = submissionType,
            errorFlag = failed,
            assignmentGroupCategoryId = null,
            userId = userId,
            currentFile = 0,
            fileCount = 0,
            progress = null
        )
    }

    @Test
    fun `Failed LoadData results in fail DataLoaded`() {
        val courseId = 1L
        val errorMessage = "Error"
        val expectedEvent = AssignmentDetailsEvent.DataLoaded(
            DataResult.Fail(Failure.Network(errorMessage)),
            false,
            DataResult.Fail(null),
            DataResult.Fail(null),
            null,
            null
        )

        mockkStatic("com.instructure.canvasapi2.utils.weave.AwaitApiKt")
        coEvery { awaitApiResponse<Assignment>(any()) } throws createError<Assignment>(errorMessage)

        mockkDatabase()

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
            DataResult.Fail(Failure.Authorization(errorMessage)),
            false,
            DataResult.Fail(null),
            DataResult.Fail(null),
            null,
            null
        )

        mockkStatic("com.instructure.canvasapi2.utils.weave.AwaitApiKt")
        coEvery { awaitApiResponse<Assignment>(any()) } throws createError<Assignment>(errorMessage, 401)

        mockkDatabase()

        connection.accept(AssignmentDetailsEffect.LoadData(assignment.id, courseId, false))

        verify(timeout = 100) {
            eventConsumer.accept(expectedEvent)
        }

        confirmVerified(eventConsumer)
    }

    @Test
    fun `Successful LoadData results in DataLoaded`() {
        val courseId = 1L
        val submission = mockkSubmission(9876L)
        val ltiTool = LTITool(url = "https://www.instructure.com")
        assignment = assignment.copy(submissionTypesRaw = listOf(Assignment.SubmissionType.EXTERNAL_TOOL.apiString), url = "https://www.instructure.com")
        val expectedEvent = AssignmentDetailsEvent.DataLoaded(
            DataResult.Success(assignment),
            false,
            DataResult.Fail(null),
            DataResult.Success(ltiTool),
            submission,
            null
        )

        mockkStatic("com.instructure.canvasapi2.utils.weave.AwaitApiKt")
        coEvery { awaitApiResponse<Assignment>(any()) } returns Response.success(assignment)

        mockkObject(SubmissionManager)
        every { SubmissionManager.getLtiFromAuthenticationUrlAsync(any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(ltiTool)
        }

        mockkDatabase(listOf(submission))

        connection.accept(AssignmentDetailsEffect.LoadData(assignment.id, courseId, false))

        verify(timeout = 100) {
            eventConsumer.accept(expectedEvent)
        }

        verify {
            firebase.logEvent(AnalyticsEventConstants.ASSIGNMENT_DETAIL_ASSIGNMENT, null)
        }

        confirmVerified(firebase)
        confirmVerified(eventConsumer)
    }

    @Test
    fun `Successful LoadData for observer results in DataLoaded`() {
        val courseId = 1L
        val submission = mockkSubmission(9876L)
        val ltiTool = LTITool(url = "https://www.instructure.com")
        val observerAssignment = ObserveeAssignment(
            id= assignmentId,
            courseId = courseId,
            submissionList = listOf(Submission(id = 9876L)),
            submissionTypesRaw = listOf(Assignment.SubmissionType.EXTERNAL_TOOL.apiString),
            url = "https://www.instructure.com"
        )
        val expectedEvent = AssignmentDetailsEvent.DataLoaded(
                DataResult.Success(observerAssignment.toAssignmentForObservee()!!),
                false,
                DataResult.Fail(null),
                DataResult.Success(ltiTool),
                submission,
                null,
                true
        )
        val observerEnrollment = Enrollment(id = 1, role = Enrollment.EnrollmentType.Observer)
        val course = Course(id = courseId, enrollments = mutableListOf(observerEnrollment))

        mockkStatic("com.instructure.canvasapi2.utils.weave.AwaitApiKt")
        coEvery { awaitApiResponse<ObserveeAssignment>(any()) } returns Response.success(observerAssignment)

        mockkObject(CourseManager)
        every { CourseManager.getCourseWithGradeAsync(any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(course)
        }

        mockkObject(SubmissionManager)
        every { SubmissionManager.getLtiFromAuthenticationUrlAsync(any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(ltiTool)
        }

        mockkDatabase(listOf(submission))

        connection.accept(AssignmentDetailsEffect.LoadData(assignment.id, courseId, false))

        verify(timeout = 100) {
            eventConsumer.accept(expectedEvent)
        }

        verify {
            firebase.logEvent(AnalyticsEventConstants.ASSIGNMENT_DETAIL_ASSIGNMENT, null)
        }

        confirmVerified(firebase)
        confirmVerified(eventConsumer)
    }

    @Test
    fun `Successful LoadData with discussion results in DataLoaded`() {
        val courseId = 1L
        val discussionTopicId = 1234L
        val submission = mockkSubmission(9876L)
        assignment = assignment.copy(
                submissionTypesRaw = listOf(Assignment.SubmissionType.DISCUSSION_TOPIC.apiString),
                discussionTopicHeader = DiscussionTopicHeader(id = discussionTopicId)
        )
        val expectedEvent = AssignmentDetailsEvent.DataLoaded(
                DataResult.Success(assignment),
                false,
                DataResult.Fail(null),
                DataResult.Fail(null),
                submission,
                null
        )

        mockkStatic("com.instructure.canvasapi2.utils.weave.AwaitApiKt")
        coEvery { awaitApiResponse<Assignment>(any()) } returns Response.success(assignment)

        mockkDatabase(listOf(submission))

        connection.accept(AssignmentDetailsEffect.LoadData(assignment.id, courseId, false))

        verify(timeout = 100) {
            eventConsumer.accept(expectedEvent)
        }

        verify {
            firebase.logEvent(AnalyticsEventConstants.ASSIGNMENT_DETAIL_DISCUSSION, null)
        }

        confirmVerified(firebase)
        confirmVerified(eventConsumer)
    }

    @Test
    fun `Successful LoadData results in DataLoaded with submissionId if an older submission exists from API`() {
        val courseId = 1L
        val submission = mockkSubmission(9876L, daysAgo = -1, failed = true)
        val ltiTool = LTITool(url = "https://www.instructure.com")
        assignment = assignment.copy(
            submissionTypesRaw = listOf(Assignment.SubmissionType.EXTERNAL_TOOL.apiString),
            url = "https://www.instructure.com",
            submission = Submission(submittedAt = Date())
        )
        val expectedEvent = AssignmentDetailsEvent.DataLoaded(
            DataResult.Success(assignment),
            false,
            DataResult.Fail(null),
            DataResult.Success(ltiTool),
            submission,
            null
        )

        mockkStatic("com.instructure.canvasapi2.utils.weave.AwaitApiKt")
        coEvery { awaitApiResponse<Assignment>(any()) } returns Response.success(assignment)

        mockkObject(SubmissionManager)
        every { SubmissionManager.getLtiFromAuthenticationUrlAsync(any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(ltiTool)
        }

        mockkDatabase(listOf(submission))

        connection.accept(AssignmentDetailsEffect.LoadData(assignment.id, courseId, false))

        verify(timeout = 100) {
            eventConsumer.accept(expectedEvent)
        }

        confirmVerified(eventConsumer)
    }

    @Test
    fun `Successful LoadData results in DataLoaded with submissionId if submittedAt on submission is null from API`() {
        val courseId = 1L
        val submission = mockkSubmission(9876L)
        val ltiTool = LTITool(url = "https://www.instructure.com")
        assignment = assignment.copy(
            submissionTypesRaw = listOf(Assignment.SubmissionType.EXTERNAL_TOOL.apiString),
            url = "https://www.instructure.com",
            submission = Submission(submittedAt = null)
        )
        val expectedEvent = AssignmentDetailsEvent.DataLoaded(
            DataResult.Success(assignment),
            false,
            DataResult.Fail(null),
            DataResult.Success(ltiTool),
            submission,
            null
        )

        mockkStatic("com.instructure.canvasapi2.utils.weave.AwaitApiKt")
        coEvery { awaitApiResponse<Assignment>(any()) } returns Response.success(assignment)

        mockkObject(SubmissionManager)
        every { SubmissionManager.getLtiFromAuthenticationUrlAsync(any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(ltiTool)
        }

        mockkDatabase(listOf(submission))

        connection.accept(AssignmentDetailsEffect.LoadData(assignment.id, courseId, false))

        verify(timeout = 100) {
            eventConsumer.accept(expectedEvent)
        }

        confirmVerified(eventConsumer)
    }

    @Test
    fun `Successful LoadData with ONLINE_UPLOAD submissionType with Studio enabled results in DataLoaded`() {
        val courseId = 1L
        val submission = mockkSubmission(9876L)
        val assignment = assignment.copy(submissionTypesRaw = listOf("online_upload"))
        val ltiTool = LTITool(url = "instructuremedia.com/lti/launch")
        val expectedEvent = AssignmentDetailsEvent.DataLoaded(
            DataResult.Success(assignment),
            true,
            DataResult.Success(ltiTool),
            DataResult.Fail(null),
            submission,
            null
        )

        mockkStatic("com.instructure.canvasapi2.utils.weave.AwaitApiKt")
        coEvery { awaitApiResponse<Assignment>(any()) } returns Response.success(assignment)

        mockkObject(ExternalToolManager)
        every { ExternalToolManager.getExternalToolsForCanvasContextAsync(any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(listOf(LTITool(url = "instructuremedia.com/lti/launch")))
        }

        mockkDatabase(listOf(submission))

        connection.accept(AssignmentDetailsEffect.LoadData(assignment.id, courseId, false))

        verify(timeout = 100) {
            eventConsumer.accept(expectedEvent)
        }

        confirmVerified(eventConsumer)
    }

    @Test
    fun `Successful LoadData with ONLINE_UPLOAD submissionType without Studio enabled results in DataLoaded`() {
        val courseId = 1L
        val assignment = assignment.copy(submissionTypesRaw = listOf("online_upload"))
        val expectedEvent = AssignmentDetailsEvent.DataLoaded(
            DataResult.Success(assignment),
            false,
            DataResult.Fail(null),
            DataResult.Fail(null),
            null,
            null
        )

        mockkStatic("com.instructure.canvasapi2.utils.weave.AwaitApiKt")
        coEvery { awaitApiResponse<Assignment>(any()) } returns Response.success(assignment)

        mockkObject(ExternalToolManager)
        every { ExternalToolManager.getExternalToolsForCanvasContextAsync(any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(listOf(LTITool(url = "bad_test")))
        }

        mockkDatabase()

        connection.accept(AssignmentDetailsEffect.LoadData(assignment.id, courseId, false))

        verify(timeout = 100) {
            eventConsumer.accept(expectedEvent)
        }

        confirmVerified(eventConsumer)
    }

    @Test
    fun `Successful LoadData with ONLINE_UPLOAD submissionType with null url in LTITool results in DataLoaded`() {
        val courseId = 1L
        val assignment = assignment.copy(submissionTypesRaw = listOf("online_upload"))
        val expectedEvent = AssignmentDetailsEvent.DataLoaded(
            DataResult.Success(assignment),
            false,
            DataResult.Fail(null),
            DataResult.Fail(null),
            null,
            null
        )

        mockkStatic("com.instructure.canvasapi2.utils.weave.AwaitApiKt")
        coEvery { awaitApiResponse<Assignment>(any()) } returns Response.success(assignment)

        mockkObject(ExternalToolManager)
        every { ExternalToolManager.getExternalToolsForCanvasContextAsync(any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(listOf(LTITool(url = null)))
        }

        mockkDatabase()

        connection.accept(AssignmentDetailsEffect.LoadData(assignment.id, courseId, false))

        verify(timeout = 100) {
            eventConsumer.accept(expectedEvent)
        }

        confirmVerified(eventConsumer)
    }

    @Test
    fun `Successful LoadData with ONLINE_UPLOAD submissionType with failed LTITool results in DataLoaded`() {
        val courseId = 1L
        val assignment = assignment.copy(submissionTypesRaw = listOf("online_upload"))
        val expectedEvent = AssignmentDetailsEvent.DataLoaded(
            DataResult.Success(assignment),
            false,
            DataResult.Fail(),
            DataResult.Fail(),
            null,
            null
        )

        mockkStatic("com.instructure.canvasapi2.utils.weave.AwaitApiKt")
        coEvery { awaitApiResponse<Assignment>(any()) } returns Response.success(assignment)

        mockkObject(ExternalToolManager)
        every { ExternalToolManager.getExternalToolsForCanvasContextAsync(any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Fail()
        }

        mockkDatabase()

        connection.accept(AssignmentDetailsEffect.LoadData(assignment.id, courseId, false))

        verify(timeout = 100) {
            eventConsumer.accept(expectedEvent)
        }

        confirmVerified(eventConsumer)
    }

    @Test
    fun `Successful LoadData with QUIZ results in DataLoaded`() {
        val courseId = 1L
        val submission = mockkSubmission(9876L)
        assignment = assignment.copy(submissionTypesRaw = listOf("online_quiz"), quizId = quiz.id)
        val expectedEvent = AssignmentDetailsEvent.DataLoaded(
                DataResult.Success(assignment),
                false,
                DataResult.Fail(null),
                DataResult.Fail(null),
                submission,
                DataResult.Success(quiz)
        )

        mockkStatic("com.instructure.canvasapi2.utils.weave.AwaitApiKt")
        coEvery { awaitApiResponse<Assignment>(any()) } returns Response.success(assignment)

        mockkObject(QuizManager)
        every { QuizManager.getQuizAsync(any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(quiz)
        }

        mockkDatabase(listOf(submission))

        connection.accept(AssignmentDetailsEffect.LoadData(assignment.id, courseId, false))

        verify(timeout = 100) {
            eventConsumer.accept(expectedEvent)
        }

        verify {
            firebase.logEvent(AnalyticsEventConstants.ASSIGNMENT_DETAIL_QUIZ, null)
        }

        confirmVerified(firebase)
        confirmVerified(eventConsumer)
    }

    @Test
    fun `Successful LoadData with QUIZ failure results in DataLoaded`() {
        val courseId = 1L
        val submission = mockkSubmission(9876L)
        assignment = assignment.copy(submissionTypesRaw = listOf("online_quiz"), quizId = quiz.id)
        val expectedEvent = AssignmentDetailsEvent.DataLoaded(
                DataResult.Success(assignment),
                false,
                DataResult.Fail(null),
                DataResult.Fail(null),
                submission,
                DataResult.Fail(null)
        )

        mockkStatic("com.instructure.canvasapi2.utils.weave.AwaitApiKt")
        coEvery { awaitApiResponse<Assignment>(any()) } returns Response.success(assignment)

        mockkObject(QuizManager)
        every { QuizManager.getQuizAsync(any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Fail(null)
        }

        mockkDatabase(listOf(submission))

        connection.accept(AssignmentDetailsEffect.LoadData(assignment.id, courseId, false))

        verify(timeout = 100) {
            eventConsumer.accept(expectedEvent)
        }

        confirmVerified(eventConsumer)
    }

    @Test
    fun `Successful LoadData with QUIZ authFail results in DataLoaded`() {
        val courseId = 1L
        val submission = mockkSubmission(9876L)
        val errorMessage = "Error"
        val authFailure = Failure.Authorization(errorMessage)
        assignment = assignment.copy(submissionTypesRaw = listOf("online_quiz"), quizId = quiz.id)
        val expectedEvent = AssignmentDetailsEvent.DataLoaded(
                DataResult.Success(assignment),
                false,
                DataResult.Fail(null),
                DataResult.Fail(null),
                submission,
                DataResult.Fail(authFailure)
        )

        mockkStatic("com.instructure.canvasapi2.utils.weave.AwaitApiKt")
        coEvery { awaitApiResponse<Assignment>(any()) } returns Response.success(assignment)

        mockkObject(QuizManager)
        every { QuizManager.getQuizAsync(any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Fail(authFailure)
        }

        mockkDatabase(listOf(submission))

        connection.accept(AssignmentDetailsEffect.LoadData(assignment.id, courseId, false))

        verify(timeout = 100) {
            eventConsumer.accept(expectedEvent)
        }

        confirmVerified(eventConsumer)
    }

    @Test
    fun `ShowSubmitAssignmentView calls ShowSubmitDialogView on the view`() {
        val course = Course()

        connection.accept(AssignmentDetailsEffect.ShowSubmitDialogView(assignment, course, false))

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
    fun `ShowUploadStatusView calls showUploadStatusView on the view for online uploads`() {
        val submission = mockkSubmission(9876L, submissionType = Assignment.SubmissionType.ONLINE_UPLOAD.apiString)
        connection.accept(AssignmentDetailsEffect.ShowUploadStatusView(submission))

        verify(timeout = 100) {
            view.showUploadStatusView(submission.id)
        }

        confirmVerified(view)
    }

    @Test
    fun `ShowUploadStatusView calls showUploadStatusView on the view for media uploads`() {
        val submission = mockkSubmission(9876L, submissionType = Assignment.SubmissionType.MEDIA_RECORDING.apiString)
        connection.accept(AssignmentDetailsEffect.ShowUploadStatusView(submission))

        verify(timeout = 100) {
            view.showUploadStatusView(submission.id)
        }

        confirmVerified(view)
    }

    @Test
    fun `ShowUploadStatusView calls showOnlineTextEntryView on the view for text submissions`() {
        val submission = mockkSubmission(9876L, submissionType = Assignment.SubmissionType.ONLINE_TEXT_ENTRY.apiString)
        connection.accept(AssignmentDetailsEffect.ShowUploadStatusView(submission))

        verify(timeout = 100) {
            view.showOnlineTextEntryView(submission.assignmentId, submission.assignmentName, submission.submissionEntry)
        }

        confirmVerified(view)
    }

    @Test
    fun `ShowUploadStatusView calls showOnlineUrlEntryView on the view for url submissions`() {
        val submission = mockkSubmission(9876L, submissionType = Assignment.SubmissionType.ONLINE_URL.apiString)
        connection.accept(AssignmentDetailsEffect.ShowUploadStatusView(submission))

        verify(timeout = 100) {
            view.showOnlineUrlEntryView(submission.assignmentId, submission.assignmentName, course, submission.submissionEntry)
        }

        confirmVerified(view)
    }

    @Test
    fun `queryResultsChanged calls from the database send SubmissionStatusUpdated events`() {
        val submission = mockkSubmission(9876L)

        mockkDatabase(listOf(submission))
        effectHandler.queryResultsChanged()

        verify(timeout = 100) {
            eventConsumer.accept(AssignmentDetailsEvent.SubmissionStatusUpdated(submission))
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

        connection.accept(AssignmentDetailsEffect.ShowCreateSubmissionView(submissionType, course, assignment))


        val url = "$protocol://$domain/courses/${course.id}/quizzes/$quizId"

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
        val course = Course()

        mockkStatic(ApiPrefs::class)
        every { ApiPrefs.protocol } returns protocol
        every { ApiPrefs.domain } returns domain

        connection.accept(AssignmentDetailsEffect.ShowCreateSubmissionView(submissionType, course, assignment))


        val url = "$protocol://$domain/courses/${assignment.courseId}/discussion_topics/$discussionTopicId"

        verify(timeout = 100) {
            view.showQuizOrDiscussionView(url)
        }
        confirmVerified(view)
    }

    @Test
    fun `ShowCreateSubmissionView with fileUpload submissionType calls showFileUploadView`() {
        val submissionType = Assignment.SubmissionType.ONLINE_UPLOAD

        connection.accept(AssignmentDetailsEffect.ShowCreateSubmissionView(submissionType, course, assignment))

        verify(timeout = 100) {
            view.showFileUploadView(assignment)
        }
        confirmVerified(view)
    }

    @Test
    fun `ShowCreateSubmissionView with textEntry submissionType calls showOnlineTextEntryView`() {
        val submissionType = Assignment.SubmissionType.ONLINE_TEXT_ENTRY

        connection.accept(AssignmentDetailsEffect.ShowCreateSubmissionView(submissionType, course, assignment))

        verify(timeout = 100) {
            view.showOnlineTextEntryView(assignment.id, assignment.name)
        }
        confirmVerified(view)
    }

    @Test
    fun `ShowCreateSubmissionView with urlEntry submissionType calls showOnlineUrlEntryView`() {
        val submissionType = Assignment.SubmissionType.ONLINE_URL

        connection.accept(AssignmentDetailsEffect.ShowCreateSubmissionView(submissionType, course, assignment))

        verify(timeout = 100) {
            view.showOnlineUrlEntryView(assignment.id, assignment.name, course)
        }
        confirmVerified(view)
    }

    @Test
    fun `ShowCreateSubmissionView with mediaRecording submissionType calls showMediaRecordingView`() {
        val submissionType = Assignment.SubmissionType.MEDIA_RECORDING

        connection.accept(AssignmentDetailsEffect.ShowCreateSubmissionView(submissionType, course, assignment))

        verify(timeout = 100) {
            view.showMediaRecordingView(assignment)
        }
        confirmVerified(view)
    }

    @Test
    fun `RouteInternally effect calls routeInternally on View`() {
        val url = "url"
        val domain = "domain"
        val course = Course()
        val assignment = Assignment()
        val effect = AssignmentDetailsEffect.RouteInternally(url, course, assignment)

        mockkStatic(ApiPrefs::class)
        every { ApiPrefs.domain } returns domain

        connection.accept(effect)
        verify(timeout = 100) {
            view.routeInternally(url, domain, course, assignment)
        }
        confirmVerified(view)
        unmockkStatic(ApiPrefs::class)
    }

    @Test
    fun `Displays Studio when submission type is fileUpload and Studio is enabled`() {
        val course = Course()
        val studioLTITool = LTITool(url = "instructuremedia.com/lti/launch")
        val domain = "domain.com"
        val ltiUrl = "domain.com//courses/0/external_tools/0/resource_selection?launch_type=homework_submission&assignment_id=2468"

        mockkStatic(ApiPrefs::class)
        every { ApiPrefs.fullDomain } returns domain

        val assignment = assignment.copy(submissionTypesRaw = listOf("online_upload"))
        connection.accept(AssignmentDetailsEffect.ShowSubmitDialogView(assignment, course, true, studioLTITool))

        verify(timeout = 100) {
            view.showSubmitDialogView(
                assignment,
                course.id,
                SubmissionTypesVisibilities(fileUpload = true, studioUpload = true),
                ltiUrl,
                studioLTITool.name
            )
        }

        confirmVerified(view)
        unmockkStatic(ApiPrefs::class)
    }

    @Test
    fun `Displays fileUpload when submission type is fileUpload`() {
        val course = Course()
        val assignment = assignment.copy(
            submissionTypesRaw = listOf("online_upload")
        )

        connection.accept(AssignmentDetailsEffect.ShowSubmitDialogView(assignment, course, false))

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

        connection.accept(AssignmentDetailsEffect.ShowSubmitDialogView(assignment, course, false))

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

        connection.accept(AssignmentDetailsEffect.ShowSubmitDialogView(assignment, course, false))

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

        connection.accept(AssignmentDetailsEffect.ShowSubmitDialogView(assignment, course, false))

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

        connection.accept(AssignmentDetailsEffect.ShowSubmitDialogView(assignment, course, false))

        verify(timeout = 100) {
            view.showSubmitDialogView(assignment, course.id, SubmissionTypesVisibilities(true, true, true, true))
        }

        confirmVerified(view)
    }

    @Test
    fun `ShowAudioRecordingView with permission results in view calling showAudioRecordingView`() {
        mockPermissions(hasPermission = true)

        connection.accept(AssignmentDetailsEffect.ShowAudioRecordingView)

        verify(timeout = 100) {
            view.showAudioRecordingView()
        }

        confirmVerified(view)
    }

    @Test
    fun `ShowQuizStartView calls showQuizStartView on the view`() {
        val quiz = Quiz(id = 123L)
        val course = Course(id = 123L)

        connection.accept(AssignmentDetailsEffect.ShowQuizStartView(quiz, course))

        verify(timeout = 100) {
            view.showQuizStartView(course, quiz)
        }

        confirmVerified(view)
    }

    @Test
    fun `ShowAudioRecordingView without permission results in view calling showPermissionDeniedToast`() {
        mockPermissions(hasPermission = false)

        connection.accept(AssignmentDetailsEffect.ShowAudioRecordingView)

        verify(timeout = 100) {
            view.showPermissionDeniedToast()
        }

        confirmVerified(view)
    }
    @Test
    fun `ShowDiscussionDetailView calls showDiscussionDetailView on the view`() {
        val discussionTopicHeaderId = 112233L
        val course = Course(id = 123L)

        connection.accept(AssignmentDetailsEffect.ShowDiscussionDetailView(discussionTopicHeaderId, course))

        verify(timeout = 100) {
            view.showDiscussionDetailView(course, discussionTopicHeaderId)
        }

        confirmVerified(view)
    }

    @Test
    fun `ShowAudioRecordingError results in view calling showAudioRecordingError`() {
        connection.accept(AssignmentDetailsEffect.ShowAudioRecordingError)

        verify(timeout = 100) {
            view.showAudioRecordingError()
        }

        confirmVerified(view)
    }

    @Test
    fun `ShowBookmarkDialog results in view calling showBookmarkDialog`() {
        connection.accept(AssignmentDetailsEffect.ShowBookmarkDialog)

        verify(timeout = 100) {
            view.showBookmarkDialog()
        }

        confirmVerified(view)
    }

    @Test
    fun `ShowVideoRecordingError results in view calling showAudioRecordingError`() {
        connection.accept(AssignmentDetailsEffect.ShowVideoRecordingError)

        verify(timeout = 100) {
            view.showVideoRecordingError()
        }

        confirmVerified(view)
    }

    @Test
    fun `UploadAudioSubmission results in starting submission service`() {
        val file: File = mockk()
        every { file.path } returns "Path"

        mockkObject(SubmissionService)
        every {
            SubmissionService.startMediaSubmission(
                context,
                course,
                assignment.id,
                assignment.name,
                assignment.groupCategoryId,
                "Path"
            )
        } returns Unit


        connection.accept(AssignmentDetailsEffect.UploadAudioSubmission(file, course, assignment))
        verify(timeout = 100) {
            SubmissionService.startMediaSubmission(
                context,
                course,
                assignment.id,
                assignment.name,
                assignment.groupCategoryId,
                "Path"
            )
        }

        confirmVerified(SubmissionService)
    }

    @Test
    fun `UploadVideoSubmission results in view calling launchFilePickerView`() {
        val uri = mockk<Uri>(relaxed = true)
        val course = Course()
        val assignment = Assignment()

        connection.accept(AssignmentDetailsEffect.UploadVideoSubmission(uri, course, assignment))

        verify(timeout = 100) {
            view.launchFilePickerView(uri, course, assignment)
        }

        confirmVerified(view)
    }

    @Test
    fun `ShowDiscussionAttachment calls showDiscussionAttachment on the view`() {
        val attachment = Attachment(id = 123L)
        val course = Course(id = 123L)

        connection.accept(AssignmentDetailsEffect.ShowDiscussionAttachment(attachment, course))

        verify(timeout = 100) {
            view.showDiscussionAttachment(course, attachment)
        }

        confirmVerified(view)
    }

    @Test
    fun `ShowCreateSubmissionView with type external tool calls showLTIView`() {
        val ltiUrl = "https://www.instructure.com"
        val assignmentName = "hodor"
        val assignmentCopy = assignment.copy(name = assignmentName)
        connection.accept(AssignmentDetailsEffect.ShowCreateSubmissionView(Assignment.SubmissionType.EXTERNAL_TOOL, course, assignmentCopy, ltiUrl))

        verify(timeout = 100) {
            view.showLTIView(course, ltiUrl, assignmentName)
        }

        confirmVerified(view)
    }

    @Test
    fun `ShowCreateSubmissionView with type basic lti launch calls showLTIView`() {
        val ltiUrl = "https://www.instructure.com"
        val assignmentName = "hodor"
        val assignmentCopy = assignment.copy(name = assignmentName)
        connection.accept(AssignmentDetailsEffect.ShowCreateSubmissionView(Assignment.SubmissionType.BASIC_LTI_LAUNCH, course, assignmentCopy, ltiUrl))

        verify(timeout = 100) {
            view.showLTIView(course, ltiUrl, assignmentName)
        }

        confirmVerified(view)
    }

    @Test
    fun `ShowVideoRecordingView without permission will request permission and show an error message when denied`() {
        mockPermissions(false, permissionGranted = false)

        connection.accept(AssignmentDetailsEffect.ShowVideoRecordingView)

        verify(timeout = 100) {
            view.showPermissionDeniedToast()
        }

        confirmVerified(view)
    }

    @Test
    fun `ShowVideoRecordingView without permission will request permission and results in launching intent`() {
        mockPermissions(false, permissionGranted = true)
        testVideo()
    }

    @Test
    fun `ShowVideoRecordingView with permission results in launching intent`() {
        mockPermissions(true)
        testVideo()
    }

    @Test
    fun `ShowMediaPickerView results in startActivityForResult with choose media request code`() {
        testMediaPicker()
    }

    @Test
    fun `ShowMediaPickingError results in view calling showMediaPickingError`() {
        connection.accept(AssignmentDetailsEffect.ShowMediaPickingError)

        verify(timeout = 100) {
            view.showMediaPickingError()
        }

        confirmVerified(view)
    }

    @Test
    fun `UploadMediaFileSubmission results in view calling launchFilePickerView`() {
        val uri = mockk<Uri>(relaxed = true)
        val course = Course()
        val assignment = Assignment()

        connection.accept(AssignmentDetailsEffect.UploadMediaFileSubmission(uri, course, assignment))

        verify(timeout = 100) {
            view.launchFilePickerView(uri, course, assignment)
        }

        confirmVerified(view)
    }

    private fun testVideo() {
        val uri = mockk<Uri>()
        val intent = mockk<Intent>()
        every { intent.action } returns ""
        every { context.packageManager.queryIntentActivities(any(), any()).size } returns 1

        mockkStatic(FileUploadUtils::class)
        every { FileUploadUtils.getExternalCacheDir(context) } returns File("")

        mockkStatic(FileProvider::class)
        every { FileProvider.getUriForFile(any(), any(), any()) } returns uri

        mockkStatic(FilePrefs::class)
        every { FilePrefs.tempCaptureUri = any() }

        mockkStatic("com.instructure.student.mobius.assignmentDetails.SubmissionUtilsKt")
        every { any<Uri>().getVideoIntent() } returns intent


        excludeRecords {
            context.packageName
            context.packageManager
        }

        connection.accept(AssignmentDetailsEffect.ShowVideoRecordingView)

        verify(timeout = 100) {
            eventConsumer.accept(AssignmentDetailsEvent.StoreVideoUri(uri))
            context.startActivityForResult(intent, AssignmentDetailsFragment.VIDEO_REQUEST_CODE)
        }

        confirmVerified(eventConsumer, context)
    }

    private fun testMediaPicker() {
        val intent = mockk<Intent>()
        every { intent.action } returns ""
        every { context.packageManager.queryIntentActivities(any(), any()).size } returns 1

        mockkStatic("com.instructure.student.mobius.assignmentDetails.SubmissionUtilsKt")
        every { chooseMediaIntent } returns intent

        excludeRecords {
            context.packageName
            context.packageManager
        }

        connection.accept(AssignmentDetailsEffect.ShowMediaPickerView)

        verify(timeout = 100) {
            context.startActivityForResult(intent, AssignmentDetailsFragment.CHOOSE_MEDIA_REQUEST_CODE)
        }

        confirmVerified(context)
    }

    private fun mockPermissions(hasPermission: Boolean, permissionGranted: Boolean = false) {
        // Mock both so we can mockk the class and the extensions in the same file
        mockkStatic(PermissionUtils::class)
        mockkStatic("${PermissionUtils::class.java.canonicalName}Kt")
        every { PermissionUtils.hasPermissions(context, *anyVararg()) } returns hasPermission andThen permissionGranted

        val block = slot<(Map<String, Boolean>) -> Unit>()

        every { context.requestPermissions(any(), capture(block)) } answers {
            block.invoke(mapOf(Pair("any", permissionGranted)))
        }
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

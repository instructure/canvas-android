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

import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.student.Submission
import com.instructure.student.mobius.assignmentDetails.AssignmentDetailsEffect
import com.instructure.student.mobius.assignmentDetails.AssignmentDetailsEvent
import com.instructure.student.mobius.assignmentDetails.AssignmentDetailsModel
import com.instructure.student.mobius.assignmentDetails.AssignmentDetailsUpdate
import com.instructure.student.test.util.matchesEffects
import com.instructure.student.test.util.matchesFirstEffects
import com.spotify.mobius.test.FirstMatchers
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import com.spotify.mobius.test.NextMatchers
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.threeten.bp.OffsetDateTime
import java.io.File
import java.util.*

class AssignmentDetailsUpdateTest : Assert() {
    private val initSpec = InitSpec(AssignmentDetailsUpdate()::init)
    private val updateSpec = UpdateSpec(AssignmentDetailsUpdate()::update)

    private lateinit var initModel: AssignmentDetailsModel
    private lateinit var course: Course
    private lateinit var assignment: Assignment
    private lateinit var quiz: Quiz
    private var submissionId: Long = 0
    private var assignmentId: Long = 0
    private var courseId: Long = 0
    private var userId: Long = 0
    private var quizId: Long = 0

    @Before
    fun setup() {
        submissionId = 2468L
        assignmentId = 4321L
        courseId = 1234L
        userId = 4321L
        quizId = 1337L
        course = Course(id = courseId)
        assignment = Assignment(id = assignmentId)
        quiz = Quiz(id = quizId)
        initModel = AssignmentDetailsModel(assignmentId = assignmentId, course = course)
    }

    private fun mockkSubmission(submissionId: Long = this.submissionId, daysAgo: Long = 0): Submission {
        return Submission.Impl(
            submissionId,
            null,
            OffsetDateTime.now().minusDays(daysAgo),
            null,
            assignment.id,
            course,
            null,
            false,
            null,
            userId
        )
    }

    @Test
    fun `Initializes into a loading state`() {
        val expectedModel = initModel.copy(isLoading = true)
        initSpec
            .whenInit(initModel)
            .then(
                assertThatFirst(
                    FirstMatchers.hasModel(expectedModel),
                    matchesFirstEffects<AssignmentDetailsModel, AssignmentDetailsEffect>(
                        AssignmentDetailsEffect.LoadData(
                            assignmentId,
                            course.id,
                            false
                        )
                    )
                )
            )
    }

    @Test
    fun `PullToRefresh event forces network reload of assignment`() {
        val expectedModel = initModel.copy(isLoading = true)
        updateSpec
            .given(initModel)
            .whenEvent(AssignmentDetailsEvent.PullToRefresh)
            .then(
                assertThatNext(
                    NextMatchers.hasModel(expectedModel),
                    matchesEffects<AssignmentDetailsModel, AssignmentDetailsEffect>(
                        AssignmentDetailsEffect.LoadData(
                            assignmentId,
                            course.id,
                            true
                        )
                    )
                )
            )
    }

    @Test
    fun `SubmitAssignmentClicked event with multiple submission types results in ShowSubmitDialogView effect`() {
        val submissionTypes = listOf("online_upload", "online_text_entry", "media_recording")
        val assignmentCopy = assignment.copy(submissionTypesRaw = submissionTypes)
        val givenModel = initModel.copy(assignmentResult = DataResult.Success(assignmentCopy))
        updateSpec
            .given(givenModel)
            .whenEvent(AssignmentDetailsEvent.SubmitAssignmentClicked)
            .then(
                assertThatNext(
                    matchesEffects<AssignmentDetailsModel, AssignmentDetailsEffect>(
                        AssignmentDetailsEffect.ShowSubmitDialogView(
                            assignmentCopy,
                            course,
                            false
                        )
                    )
                )
            )
    }

    @Test
    fun `SubmitAssignmentClicked event with only ONLINE_UPLOAD submission type and having Studio enabled results in ShowSubmitDialogView effect`() {
        val submissionTypes = listOf("online_upload")
        val assignmentCopy = assignment.copy(submissionTypesRaw = submissionTypes)
        val studioLTITool = LTITool(url = "instructuremedia.com/lti/launch")
        val givenModel = initModel.copy(assignmentResult = DataResult.Success(assignmentCopy), isStudioEnabled = true, studioLTIToolResult = DataResult.Success(studioLTITool))

        updateSpec
            .given(givenModel)
            .whenEvent(AssignmentDetailsEvent.SubmitAssignmentClicked)
            .then(
                assertThatNext(
                    matchesEffects<AssignmentDetailsModel, AssignmentDetailsEffect>(
                        AssignmentDetailsEffect.ShowSubmitDialogView(
                            assignmentCopy,
                            course,
                            true,
                            studioLTITool
                        )
                    )
                )
            )
    }

    @Test
    fun `SubmitAssignmentClicked event with only ONLINE_UPLOAD submission type and without Studio enabled results in ShowCreateSubmissionView effect`() {
        val submissionType = Assignment.SubmissionType.ONLINE_UPLOAD
        val submissionTypes = listOf("online_upload")
        val assignmentCopy = assignment.copy(submissionTypesRaw = submissionTypes)
        val givenModel = initModel.copy(assignmentResult = DataResult.Success(assignmentCopy))
        updateSpec
            .given(givenModel)
            .whenEvent(AssignmentDetailsEvent.SubmitAssignmentClicked)
            .then(
                assertThatNext(
                    matchesEffects<AssignmentDetailsModel, AssignmentDetailsEffect>(
                        AssignmentDetailsEffect.ShowCreateSubmissionView(
                            submissionType,
                            course,
                            assignmentCopy
                        )
                    )
                )
            )
    }

    @Test
    fun `SubmitAssignmentClicked event with one submission type results in ShowCreateSubmissionView effect`() {
        val submissionType = Assignment.SubmissionType.ONLINE_TEXT_ENTRY
        val submissionTypes = listOf("online_text_entry")
        val assignmentCopy = assignment.copy(submissionTypesRaw = submissionTypes)
        val givenModel = initModel.copy(assignmentResult = DataResult.Success(assignmentCopy))
        updateSpec
            .given(givenModel)
            .whenEvent(AssignmentDetailsEvent.SubmitAssignmentClicked)
            .then(
                assertThatNext(
                    matchesEffects<AssignmentDetailsModel, AssignmentDetailsEffect>(
                        AssignmentDetailsEffect.ShowCreateSubmissionView(
                            submissionType,
                            course,
                            assignmentCopy
                        )
                    )
                )
            )
    }

    @Test
    fun `SubmitAssignmentClicked event with quiz submission type results in ShowQuizStartView effect`() {
        val submissionTypes = listOf("online_quiz")
        val assignmentCopy = assignment.copy(submissionTypesRaw = submissionTypes, quizId = quizId)
        val givenModel = initModel.copy(
            assignmentResult = DataResult.Success(assignmentCopy),
            quizResult = DataResult.Success(quiz)
        )
        updateSpec
            .given(givenModel)
            .whenEvent(AssignmentDetailsEvent.SubmitAssignmentClicked)
            .then(
                assertThatNext(
                    matchesEffects<AssignmentDetailsModel, AssignmentDetailsEffect>(
                        AssignmentDetailsEffect.ShowQuizStartView(
                            quiz,
                            course
                        )
                    )
                )
            )
    }

    @Test
    fun `SubmitAssignmentClicked event with discussion submission type results in ShowQuizStartView effect`() {
        val discussionTopicHeader = DiscussionTopicHeader(id = 123L)
        val submissionTypes = listOf("discussion_topic")
        val assignmentCopy = assignment.copy(
            submissionTypesRaw = submissionTypes,
            discussionTopicHeader = discussionTopicHeader
        )
        val givenModel = initModel.copy(assignmentResult = DataResult.Success(assignmentCopy))
        updateSpec
            .given(givenModel)
            .whenEvent(AssignmentDetailsEvent.SubmitAssignmentClicked)
            .then(
                assertThatNext(
                    matchesEffects<AssignmentDetailsModel, AssignmentDetailsEffect>(
                        AssignmentDetailsEffect.ShowDiscussionDetailView(
                            discussionTopicHeader.id,
                            course
                        )
                    )
                )
            )
    }

    @Test
    fun `ViewSubmissionClicked event results in ShowSubmissionView effect`() {
        updateSpec
            .given(initModel)
            .whenEvent(AssignmentDetailsEvent.ViewSubmissionClicked)
            .then(
                assertThatNext(
                    matchesEffects<AssignmentDetailsModel, AssignmentDetailsEffect>(
                        AssignmentDetailsEffect.ShowSubmissionView(
                            assignmentId,
                            course
                        )
                    )
                )
            )
    }

    @Test
    fun `ViewDiscussionAttachmentClicked event results in ShowDiscussionAttachment effect`() {
        val attachmentId = 12345L
        val remoteFiles = mutableListOf(RemoteFile(id = attachmentId))
        val discussionTopicHeader = DiscussionTopicHeader(id = 123L, attachments = remoteFiles)
        val assignment = assignment.copy()
        val submissionTypes = listOf("discussion_topic")
        val assignmentCopy = assignment.copy(
            submissionTypesRaw = submissionTypes,
            discussionTopicHeader = discussionTopicHeader
        )
        val givenModel = initModel.copy(assignmentResult = DataResult.Success(assignmentCopy))
        updateSpec
            .given(givenModel)
            .whenEvent(AssignmentDetailsEvent.DiscussionAttachmentClicked)
            .then(
                assertThatNext(
                    matchesEffects<AssignmentDetailsModel, AssignmentDetailsEffect>(
                        AssignmentDetailsEffect.ShowDiscussionAttachment(
                            Attachment(id = attachmentId),
                            course
                        )
                    )
                )
            )
    }

    @Test
    fun `ViewUploadStatusClicked event results in ShowUploadStatusView effect`() {
        val submission = mockkSubmission()
        updateSpec
            .given(initModel.copy(databaseSubmission = submission))
            .whenEvent(AssignmentDetailsEvent.ViewUploadStatusClicked)
            .then(
                assertThatNext(
                    matchesEffects<AssignmentDetailsModel, AssignmentDetailsEffect>(
                        AssignmentDetailsEffect.ShowUploadStatusView(
                            submission
                        )
                    )
                )
            )
    }

    @Test
    fun `DataLoaded event updates the model`() {
        val assignment = Assignment(id = assignmentId)
        val submission = mockkSubmission()
        val startModel = initModel
        val expectedModel = initModel.copy(
            isLoading = false,
            assignmentResult = DataResult.Success(assignment),
            isStudioEnabled = true,
            ltiTool = DataResult.Fail(null),
            databaseSubmission = submission
        )
        updateSpec
            .given(startModel)
            .whenEvent(
                AssignmentDetailsEvent.DataLoaded(
                    assignmentResult = expectedModel.assignmentResult,
                    isStudioEnabled = true,
                    studioLTITool = null,
                    ltiTool = expectedModel.ltiTool,
                    submission = submission,
                    quizResult = null
                )
            )
            .then(assertThatNext(NextMatchers.hasModel(expectedModel)))
    }

    @Test
    fun `DataLoaded event with assignment load failure updates the model`() {
        val submission = mockkSubmission()
        val startModel = initModel
        val expectedModel = initModel.copy(
            isLoading = false,
            assignmentResult = DataResult.Fail(),
            ltiTool = DataResult.Fail(),
            databaseSubmission = submission
        )
        updateSpec
            .given(startModel)
            .whenEvent(
                AssignmentDetailsEvent.DataLoaded(
                    assignmentResult = expectedModel.assignmentResult,
                    isStudioEnabled = false,
                    studioLTITool = null,
                    ltiTool = expectedModel.ltiTool,
                    submission = submission,
                    quizResult = null
                )
            )
            .then(assertThatNext(NextMatchers.hasModel(expectedModel)))
    }

    @Test
    fun `DataLoaded event with a null assignment updates the model`() {
        val startModel = initModel
        val expectedModel = initModel.copy(
            isLoading = false,
            assignmentResult = null,
            ltiTool = null,
            databaseSubmission = null
        )
        updateSpec
            .given(startModel)
            .whenEvent(
                AssignmentDetailsEvent.DataLoaded(
                    assignmentResult = expectedModel.assignmentResult,
                    isStudioEnabled = false,
                    studioLTITool = null,
                    ltiTool = expectedModel.ltiTool,
                    submission = null,
                    quizResult = null
                )
            )
            .then(assertThatNext(NextMatchers.hasModel(expectedModel)))
    }

    @Test
    fun `DataLoaded event ignores database submission if a newer submission exists from API`() {
        val assignment = Assignment(
            id = assignmentId,
            submission = Submission(submittedAt = Date())
        )
        val submission = mockkSubmission(daysAgo = 1)
        val startModel = initModel
        val expectedModel = initModel.copy(
            isLoading = false,
            assignmentResult = DataResult.Success(assignment),
            isStudioEnabled = true,
            ltiTool = DataResult.Fail(null),
            databaseSubmission = null
        )
        updateSpec
            .given(startModel)
            .whenEvent(
                AssignmentDetailsEvent.DataLoaded(
                    assignmentResult = expectedModel.assignmentResult,
                    isStudioEnabled = true,
                    studioLTITool = null,
                    ltiTool = expectedModel.ltiTool,
                    submission = submission,
                    quizResult = null
                )
            )
            .then(assertThatNext(NextMatchers.hasModel(expectedModel)))
    }

    @Test
    fun `DataLoaded event with a quiz updates the model`() {
        val submissionTypes = listOf("online_quiz")
        val assignment = Assignment(id = assignmentId, quizId = quizId, submissionTypesRaw = submissionTypes)
        val submission = mockkSubmission()
        val startModel = initModel
        val expectedModel = initModel.copy(
            isLoading = false,
            assignmentResult = DataResult.Success(assignment),
            isStudioEnabled = false,
            ltiTool = DataResult.Fail(null),
            databaseSubmission = submission,
            quizResult = DataResult.Success(quiz),
            studioLTIToolResult = DataResult.Fail(null)
        )
        updateSpec
            .given(startModel)
            .whenEvent(
                AssignmentDetailsEvent.DataLoaded(
                    assignmentResult = expectedModel.assignmentResult,
                    isStudioEnabled = false,
                    ltiTool = expectedModel.ltiTool,
                    submission = submission,
                    quizResult = DataResult.Success(quiz),
                    studioLTITool = DataResult.Fail(null)
                )
            )
            .then(assertThatNext(NextMatchers.hasModel(expectedModel)))
    }

    @Test
    fun `DataLoaded event with a quiz load failure updates the model`() {
        val submissionTypes = listOf("online_quiz")
        val assignment =
            Assignment(id = assignmentId, quizId = quizId, submissionTypesRaw = submissionTypes)
        val submission = mockkSubmission()
        val startModel = initModel
        val expectedModel = initModel.copy(
            isLoading = false,
            assignmentResult = DataResult.Success(assignment),
            isStudioEnabled = true,
            ltiTool = DataResult.Fail(null),
            databaseSubmission = submission,
            quizResult = DataResult.Fail(null),
            studioLTIToolResult = DataResult.Fail(null)
        )
        updateSpec
            .given(startModel)
            .whenEvent(
                AssignmentDetailsEvent.DataLoaded(
                    assignmentResult = expectedModel.assignmentResult,
                    isStudioEnabled = true,
                    ltiTool = expectedModel.ltiTool,
                    submission = submission,
                    quizResult = DataResult.Fail(null),
                    studioLTITool = DataResult.Fail(null)
                )
            )
            .then(assertThatNext(NextMatchers.hasModel(expectedModel)))
    }

    @Test
    fun `SubmissionStatusUpdated event updates the model`() {
        val submission = mockkSubmission()
        val startModel = initModel
        val expectedModel = initModel.copy(
            databaseSubmission = submission
        )
        updateSpec
            .given(startModel)
            .whenEvent(AssignmentDetailsEvent.SubmissionStatusUpdated(submission = submission))
            .then(assertThatNext(NextMatchers.hasModel(expectedModel)))
    }

    @Test
    fun `SubmissionTypeClicked event results in ShowCreateSubmissionView effect`() {
        val submissionType = Assignment.SubmissionType.ONLINE_UPLOAD
        val submissionTypes = listOf("online_upload")
        val assignmentCopy = assignment.copy(submissionTypesRaw = submissionTypes)
        val givenModel = initModel.copy(assignmentResult = DataResult.Success(assignmentCopy))
        updateSpec
            .given(givenModel)
            .whenEvent(AssignmentDetailsEvent.SubmissionTypeClicked(submissionType))
            .then(
                assertThatNext(
                    matchesEffects<AssignmentDetailsModel, AssignmentDetailsEffect>(
                        AssignmentDetailsEffect.ShowCreateSubmissionView(
                            submissionType,
                            course,
                            assignmentCopy
                        )
                    )
                )
            )
    }

    @Test
    fun `InternalRouteRequested event results in RouteInternally effect`() {
        val url = "www.instructure.com"
        val model = initModel.copy(assignmentResult = DataResult.Success(assignment))
        val event = AssignmentDetailsEvent.InternalRouteRequested(url)
        val expectedEffect = AssignmentDetailsEffect.RouteInternally(
            url = url,
            course = model.course,
            assignment = model.assignmentResult!!.dataOrThrow
        )
        updateSpec
            .given(model)
            .whenEvent(event)
            .then(
                assertThatNext(
                    matchesEffects<AssignmentDetailsModel, AssignmentDetailsEffect>(expectedEffect)
                )
            )
    }

    @Test
    fun `SendAudioRecordingClicked with valid file results in UploadMediaSubmission effect`() {
        val file = File("hodorpath")
        val model = initModel.copy(assignmentResult = DataResult.Success(assignment))
        updateSpec
            .given(model)
            .whenEvent(AssignmentDetailsEvent.SendAudioRecordingClicked(file))
            .then(
                assertThatNext(
                    matchesEffects<AssignmentDetailsModel, AssignmentDetailsEffect>(AssignmentDetailsEffect.UploadMediaSubmission(file, course, assignment))
                )
            )
    }

    @Test
    fun `SendAudioRecordingClicked with invalid file results in UploadMediaSubmission effect`() {
        val model = initModel.copy(assignmentResult = DataResult.Success(assignment))
        updateSpec
            .given(model)
            .whenEvent(AssignmentDetailsEvent.SendAudioRecordingClicked(null))
            .then(
                assertThatNext(
                    matchesEffects<AssignmentDetailsModel, AssignmentDetailsEffect>(AssignmentDetailsEffect.ShowAudioRecordingError)
                )
            )
    }

    @Test
    fun `AudioRecordingClicked results in ShowAudioRecordingView effect`() {
        updateSpec
            .given(initModel)
            .whenEvent(AssignmentDetailsEvent.AudioRecordingClicked)
            .then(
                assertThatNext(
                    matchesEffects<AssignmentDetailsModel, AssignmentDetailsEffect>(AssignmentDetailsEffect.ShowAudioRecordingView)
                )
            )
    }
}

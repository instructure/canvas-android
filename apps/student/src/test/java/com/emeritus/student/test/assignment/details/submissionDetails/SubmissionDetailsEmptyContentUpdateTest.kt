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
package com.emeritus.student.test.assignment.details.submissionDetails

import android.net.Uri
import com.instructure.canvasapi2.models.*
import com.emeritus.student.mobius.assignmentDetails.submissionDetails.content.emptySubmission.SubmissionDetailsEmptyContentEffect
import com.emeritus.student.mobius.assignmentDetails.submissionDetails.content.emptySubmission.SubmissionDetailsEmptyContentEvent
import com.emeritus.student.mobius.assignmentDetails.submissionDetails.content.emptySubmission.SubmissionDetailsEmptyContentModel
import com.emeritus.student.mobius.assignmentDetails.submissionDetails.content.emptySubmission.SubmissionDetailsEmptyContentUpdate
import com.instructure.student.test.util.matchesEffects
import com.spotify.mobius.test.FirstMatchers
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.NextMatchers
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import io.mockk.mockk
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.io.File


class SubmissionDetailsEmptyContentUpdateTest : Assert() {
    private val initSpec = InitSpec(SubmissionDetailsEmptyContentUpdate()::init)
    private val updateSpec = UpdateSpec(SubmissionDetailsEmptyContentUpdate()::update)

    private lateinit var assignment: Assignment
    private lateinit var course: Course
    private lateinit var initModel: SubmissionDetailsEmptyContentModel
    private var isStudioEnabled = false
    private lateinit var quiz: Quiz
    private lateinit var studioLTITool: LTITool
    private lateinit var videoFileUri: Uri
    private var quizId: Long = 0

    @Before
    fun setup() {
        quizId = 1337L
        assignment = Assignment(id = 1234L)
        quiz = Quiz(id = quizId)
        course = Course(id = 91011L)
        studioLTITool = LTITool(url = "https://www.instructure.com")
        videoFileUri = mockk()

        initModel = SubmissionDetailsEmptyContentModel(
            assignment = assignment,
            course = course,
            isStudioEnabled = isStudioEnabled,
            quiz = quiz,
            studioLTITool = studioLTITool,
            videoFileUri = videoFileUri
        )
    }

    @Test
    fun `Initializes with a basic model and an empty event set`() {
        val expectedModel = initModel
        initSpec
            .whenInit(initModel)
            .then(
                InitSpec.assertThatFirst(FirstMatchers.hasModel(expectedModel))
            )
    }

    @Test
    fun `SubmitAssignmentClicked event with multiple submission types results in ShowSubmitDialogView effect`() {
        val submissionTypes = listOf("online_upload", "online_text_entry", "media_recording")
        val assignmentCopy = assignment.copy(submissionTypesRaw = submissionTypes)
        val givenModel = initModel.copy(assignment = assignmentCopy, studioLTITool = null)
        updateSpec
            .given(givenModel)
            .whenEvent(SubmissionDetailsEmptyContentEvent.SubmitAssignmentClicked)
            .then(
                assertThatNext(
                    matchesEffects<SubmissionDetailsEmptyContentModel, SubmissionDetailsEmptyContentEffect>(
                        SubmissionDetailsEmptyContentEffect.ShowSubmitDialogView(
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
        val givenModel = initModel.copy(assignment = assignmentCopy, isStudioEnabled = true, studioLTITool = studioLTITool)

        updateSpec
            .given(givenModel)
            .whenEvent(SubmissionDetailsEmptyContentEvent.SubmitAssignmentClicked)
            .then(
                assertThatNext(
                    matchesEffects<SubmissionDetailsEmptyContentModel, SubmissionDetailsEmptyContentEffect>(
                        SubmissionDetailsEmptyContentEffect.ShowSubmitDialogView(
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
        val givenModel = initModel.copy(assignment = assignmentCopy)
        updateSpec
            .given(givenModel)
            .whenEvent(SubmissionDetailsEmptyContentEvent.SubmitAssignmentClicked)
            .then(
                assertThatNext(
                    matchesEffects<SubmissionDetailsEmptyContentModel, SubmissionDetailsEmptyContentEffect>(
                        SubmissionDetailsEmptyContentEffect.ShowCreateSubmissionView(
                            submissionType,
                            course,
                            assignmentCopy
                        )
                    )
                )
            )
    }

    @Test
    fun `SubmitAssignmentClicked event with only EXTERNAL_TOOL submission type and without Studio enabled results in ShowCreateSubmissionView effect`() {
        val submissionType = Assignment.SubmissionType.EXTERNAL_TOOL
        val submissionTypes = listOf("external_tool")
        val ltiTool = LTITool(123L, url = "https://hodor.instructure.com")
        val assignmentCopy = assignment.copy(submissionTypesRaw = submissionTypes)
        val givenModel = initModel.copy(assignment = assignmentCopy, ltiTool = ltiTool)
        updateSpec
                .given(givenModel)
                .whenEvent(SubmissionDetailsEmptyContentEvent.SubmitAssignmentClicked)
                .then(
                        assertThatNext(
                                matchesEffects<SubmissionDetailsEmptyContentModel, SubmissionDetailsEmptyContentEffect>(
                                        SubmissionDetailsEmptyContentEffect.ShowCreateSubmissionView(
                                                submissionType,
                                                course,
                                                assignmentCopy,
                                                ltiTool
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
        val givenModel = initModel.copy(assignment = assignmentCopy)
        updateSpec
            .given(givenModel)
            .whenEvent(SubmissionDetailsEmptyContentEvent.SubmitAssignmentClicked)
            .then(
                assertThatNext(
                    matchesEffects<SubmissionDetailsEmptyContentModel, SubmissionDetailsEmptyContentEffect>(
                        SubmissionDetailsEmptyContentEffect.ShowCreateSubmissionView(
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
            assignment = assignmentCopy,
            quiz = quiz
        )
        updateSpec
            .given(givenModel)
            .whenEvent(SubmissionDetailsEmptyContentEvent.SubmitAssignmentClicked)
            .then(
                assertThatNext(
                    matchesEffects<SubmissionDetailsEmptyContentModel, SubmissionDetailsEmptyContentEffect>(
                        SubmissionDetailsEmptyContentEffect.ShowQuizStartView(
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
        val givenModel = initModel.copy(assignment = assignmentCopy)
        updateSpec
            .given(givenModel)
            .whenEvent(SubmissionDetailsEmptyContentEvent.SubmitAssignmentClicked)
            .then(
                assertThatNext(
                    matchesEffects<SubmissionDetailsEmptyContentModel, SubmissionDetailsEmptyContentEffect>(
                        SubmissionDetailsEmptyContentEffect.ShowDiscussionDetailView(
                            discussionTopicHeader.id,
                            course
                        )
                    )
                )
            )
    }

    @Test
    fun `SendAudioRecordingClicked with valid file results in UploadMediaSubmission effect`() {
        val file = File("Instructure")
        val model = initModel.copy(assignment = assignment)
        updateSpec
            .given(model)
            .whenEvent(SubmissionDetailsEmptyContentEvent.SendAudioRecordingClicked(file))
            .then(
                assertThatNext(
                    matchesEffects<SubmissionDetailsEmptyContentModel, SubmissionDetailsEmptyContentEffect>(SubmissionDetailsEmptyContentEffect.UploadAudioSubmission(file, course, assignment))
                )
            )
    }

    @Test
    fun `SendAudioRecordingClicked with invalid file results in UploadMediaSubmission effect`() {
        val model = initModel.copy(assignment = assignment)
        updateSpec
            .given(model)
            .whenEvent(SubmissionDetailsEmptyContentEvent.SendAudioRecordingClicked(null))
            .then(
                assertThatNext(
                    matchesEffects<SubmissionDetailsEmptyContentModel, SubmissionDetailsEmptyContentEffect>(SubmissionDetailsEmptyContentEffect.ShowAudioRecordingError)
                )
            )
    }

    @Test
    fun `SendVideoRecording with valid file results in UploadMediaSubmission effect`() {
        val uri = mockk<Uri>()
        val model = initModel.copy(assignment = assignment, videoFileUri = uri)
        updateSpec
            .given(model)
            .whenEvent(SubmissionDetailsEmptyContentEvent.SendVideoRecording)
            .then(
                assertThatNext(
                    matchesEffects<SubmissionDetailsEmptyContentModel, SubmissionDetailsEmptyContentEffect>(SubmissionDetailsEmptyContentEffect.UploadVideoSubmission(uri, course, assignment))
                )
            )
    }

    @Test
    fun `SendVideoRecording with invalid file results in UploadMediaSubmission effect`() {
        val model = initModel.copy(videoFileUri = null)
        updateSpec
            .given(model)
            .whenEvent(SubmissionDetailsEmptyContentEvent.SendVideoRecording)
            .then(
                assertThatNext(
                    matchesEffects<SubmissionDetailsEmptyContentModel, SubmissionDetailsEmptyContentEffect>(SubmissionDetailsEmptyContentEffect.ShowVideoRecordingError)
                )
            )
    }

    @Test
    fun `OnVideoRecordingError with invalid file results in UploadMediaSubmission effect`() {
        val model = initModel
        updateSpec
            .given(model)
            .whenEvent(SubmissionDetailsEmptyContentEvent.OnVideoRecordingError)
            .then(
                assertThatNext(
                    matchesEffects<SubmissionDetailsEmptyContentModel, SubmissionDetailsEmptyContentEffect>(SubmissionDetailsEmptyContentEffect.ShowVideoRecordingError)
                )
            )
    }

    @Test
    fun `AudioRecordingClicked results in ShowAudioRecordingView effect`() {
        updateSpec
            .given(initModel)
            .whenEvent(SubmissionDetailsEmptyContentEvent.AudioRecordingClicked)
            .then(
                assertThatNext(
                    matchesEffects<SubmissionDetailsEmptyContentModel, SubmissionDetailsEmptyContentEffect>(SubmissionDetailsEmptyContentEffect.ShowAudioRecordingView)
                )
            )
    }

    @Test
    fun `VideoRecordingClicked results in ShowAudioRecordingView effect`() {
        updateSpec
            .given(initModel)
            .whenEvent(SubmissionDetailsEmptyContentEvent.VideoRecordingClicked)
            .then(
                assertThatNext(
                    matchesEffects<SubmissionDetailsEmptyContentModel, SubmissionDetailsEmptyContentEffect>(SubmissionDetailsEmptyContentEffect.ShowVideoRecordingView)
                )
            )
    }

    @Test
    fun `StoreVideoUri results in model change`() {
        val uri = mockk<Uri>()
        updateSpec
            .given(initModel)
            .whenEvent(SubmissionDetailsEmptyContentEvent.StoreVideoUri(uri))
            .then(
                assertThatNext(
                    NextMatchers.hasModel(initModel.copy(videoFileUri = uri)),
                    NextMatchers.hasNoEffects()
                )
            )
    }

    @Test
    fun `ChooseMediaClicked results in ShowMediaPickerView effect`() {
        updateSpec
            .given(initModel)
            .whenEvent(SubmissionDetailsEmptyContentEvent.ChooseMediaClicked)
            .then(
                assertThatNext(
                    matchesEffects<SubmissionDetailsEmptyContentModel, SubmissionDetailsEmptyContentEffect>(SubmissionDetailsEmptyContentEffect.ShowMediaPickerView)
                )
            )
    }

    @Test
    fun `OnMediaPickingError results in ShowMediaPickingError effect`() {
        updateSpec
            .given(initModel)
            .whenEvent(SubmissionDetailsEmptyContentEvent.OnMediaPickingError)
            .then(
                assertThatNext(
                    matchesEffects<SubmissionDetailsEmptyContentModel, SubmissionDetailsEmptyContentEffect>(SubmissionDetailsEmptyContentEffect.ShowMediaPickingError)
                )
            )
    }

    @Test
    fun `SendMediaFile results in UploadMediaFileSubmission effect`() {
        val uri = mockk<Uri>()
        val model = initModel.copy(assignment = assignment)
        updateSpec
            .given(model)
            .whenEvent(SubmissionDetailsEmptyContentEvent.SendMediaFile(uri))
            .then(
                assertThatNext(
                    matchesEffects<SubmissionDetailsEmptyContentModel, SubmissionDetailsEmptyContentEffect>(SubmissionDetailsEmptyContentEffect.UploadMediaFileSubmission(uri, course, assignment))
                )
            )
    }

    @Test
    fun `SubmissionStarted event results in SubmissionStarted effect`() {
        updateSpec
            .given(initModel)
            .whenEvent(SubmissionDetailsEmptyContentEvent.SubmissionStarted)
            .then(
                assertThatNext(
                    matchesEffects<SubmissionDetailsEmptyContentModel, SubmissionDetailsEmptyContentEffect>(SubmissionDetailsEmptyContentEffect.SubmissionStarted)
                )
            )
    }

}
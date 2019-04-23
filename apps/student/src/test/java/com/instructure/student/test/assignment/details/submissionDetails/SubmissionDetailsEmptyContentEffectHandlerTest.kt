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

import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.student.mobius.assignmentDetails.submissionDetails.content.emptySubmission.SubmissionDetailsEmptyContentEffect
import com.instructure.student.mobius.assignmentDetails.submissionDetails.content.emptySubmission.SubmissionDetailsEmptyContentEffectHandler
import com.instructure.student.mobius.assignmentDetails.submissionDetails.content.emptySubmission.SubmissionDetailsEmptyContentEvent
import com.instructure.student.mobius.assignmentDetails.submissionDetails.content.emptySubmission.ui.SubmissionDetailsEmptyContentView
import com.instructure.student.mobius.assignmentDetails.ui.SubmissionTypesVisibilities
import com.spotify.mobius.functions.Consumer
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.test.setMain
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.util.concurrent.Executors


class SubmissionDetailsEmptyContentEffectHandlerTest : Assert() {
    private val view: SubmissionDetailsEmptyContentView = mockk(relaxed = true)
    private val effectHandler =
            SubmissionDetailsEmptyContentEffectHandler().apply { view = this@SubmissionDetailsEmptyContentEffectHandlerTest.view }
    private val eventConsumer: Consumer<SubmissionDetailsEmptyContentEvent> = mockk(relaxed = true)
    private val connection = effectHandler.connect(eventConsumer)

    lateinit var assignment: Assignment

    @ExperimentalCoroutinesApi
    @Before
    fun setup() {
        Dispatchers.setMain(Executors.newSingleThreadExecutor().asCoroutineDispatcher())
        assignment = Assignment(id = 2468, courseId = 8642)
    }

    @Test
    fun `ShowSubmitAssignmentView calls ShowSubmitDialogView on the view`() {
        val course = Course()

        connection.accept(SubmissionDetailsEmptyContentEffect.ShowSubmitDialogView(assignment, course, false))

        verify(timeout = 100) {
            view.showSubmitDialogView(assignment, course.id, SubmissionTypesVisibilities())
        }

        confirmVerified(view)
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

        connection.accept(SubmissionDetailsEmptyContentEffect.ShowCreateSubmissionView(submissionType, assignment.courseId, assignment))


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

        connection.accept(SubmissionDetailsEmptyContentEffect.ShowCreateSubmissionView(submissionType, assignment.courseId, assignment))


        val url = "$protocol://$domain/courses/${assignment.courseId}/discussion_topics/$discussionTopicId"

        verify(timeout = 100) {
            view.showQuizOrDiscussionView(url)
        }
        confirmVerified(view)
    }

    @Test
    fun `ShowCreateSubmissionView with fileUpload submissionType calls showFileUploadView`() {
        val submissionType = Assignment.SubmissionType.ONLINE_UPLOAD

        connection.accept(SubmissionDetailsEmptyContentEffect.ShowCreateSubmissionView(submissionType, assignment.courseId, assignment))

        verify(timeout = 100) {
            view.showFileUploadView(assignment, assignment.courseId)
        }
        confirmVerified(view)
    }

    @Test
    fun `ShowCreateSubmissionView with textEntry submissionType calls showOnlineTextEntryView`() {
        val submissionType = Assignment.SubmissionType.ONLINE_TEXT_ENTRY

        connection.accept(SubmissionDetailsEmptyContentEffect.ShowCreateSubmissionView(submissionType, assignment.courseId, assignment))

        verify(timeout = 100) {
            view.showOnlineTextEntryView(assignment.id, assignment.courseId)
        }
        confirmVerified(view)
    }

    @Test
    fun `ShowCreateSubmissionView with urlEntry submissionType calls showOnlineUrlEntryView`() {
        val submissionType = Assignment.SubmissionType.ONLINE_URL

        connection.accept(SubmissionDetailsEmptyContentEffect.ShowCreateSubmissionView(submissionType, assignment.courseId, assignment))

        verify(timeout = 100) {
            view.showOnlineUrlEntryView(assignment.id, assignment.courseId)
        }
        confirmVerified(view)
    }

    @Test
    fun `ShowCreateSubmissionView with mediaRecording submissionType calls showMediaRecordingView`() {
        val courseId = 1234L
        val submissionType = Assignment.SubmissionType.MEDIA_RECORDING

        connection.accept(SubmissionDetailsEmptyContentEffect.ShowCreateSubmissionView(submissionType, courseId, assignment))

        verify(timeout = 100) {
            view.showMediaRecordingView(assignment, courseId)
        }
        confirmVerified(view)
    }


    @Test
    fun `Displays arc when submission type is fileUpload and arc is enabled`() {
        val course = Course()

        val assignment = assignment.copy(submissionTypesRaw = listOf("online_upload"))
        connection.accept(SubmissionDetailsEmptyContentEffect.ShowSubmitDialogView(assignment, course, true))

        verify(timeout = 100) {
            view.showSubmitDialogView(assignment, course.id, SubmissionTypesVisibilities(fileUpload = true, arcUpload = true))
        }

        confirmVerified(view)
    }

    @Test
    fun `Displays fileUpload when submission type is fileUpload`() {
        val course = Course()
        val assignment = assignment.copy(
                submissionTypesRaw = listOf("online_upload")
        )

        connection.accept(SubmissionDetailsEmptyContentEffect.ShowSubmitDialogView(assignment, course, false))

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

        connection.accept(SubmissionDetailsEmptyContentEffect.ShowSubmitDialogView(assignment, course, false))

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

        connection.accept(SubmissionDetailsEmptyContentEffect.ShowSubmitDialogView(assignment, course, false))

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

        connection.accept(SubmissionDetailsEmptyContentEffect.ShowSubmitDialogView(assignment, course, false))

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

        connection.accept(SubmissionDetailsEmptyContentEffect.ShowSubmitDialogView(assignment, course, false))

        verify(timeout = 100) {
            view.showSubmitDialogView(assignment, course.id, SubmissionTypesVisibilities(true, true, true, true))
        }

        confirmVerified(view)
    }
}
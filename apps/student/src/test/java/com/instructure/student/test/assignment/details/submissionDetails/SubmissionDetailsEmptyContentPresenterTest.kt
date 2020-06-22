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

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvasapi2.models.*
import com.instructure.student.mobius.assignmentDetails.submissionDetails.content.emptySubmission.SubmissionDetailsEmptyContentModel
import com.instructure.student.mobius.assignmentDetails.submissionDetails.content.emptySubmission.SubmissionDetailsEmptyContentPresenter
import com.instructure.student.mobius.assignmentDetails.submissionDetails.content.emptySubmission.ui.SubmissionDetailsEmptyContentViewState.Loaded
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.threeten.bp.Month
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.format.DateTimeFormatter
import java.util.*

@RunWith(AndroidJUnit4::class)
class SubmissionDetailsEmptyContentPresenterTest : Assert() {

    private lateinit var baseModel: SubmissionDetailsEmptyContentModel
    private lateinit var baseAssignment: Assignment
    private lateinit var baseCourse: Course
    private lateinit var baseQuiz: Quiz
    private lateinit var baseDiscussion: DiscussionTopicHeader
    private lateinit var context: Context
    private var isStudioEnabled = false

    private val defaultLoadedState = Loaded(submitButtonText = "Submit Assignment")

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        baseCourse = Course()
        baseAssignment = Assignment(
            dueAt = OffsetDateTime.now().withHour(23).withMinute(59).format(DateTimeFormatter.ISO_DATE_TIME),
            submissionTypesRaw = listOf("online_upload"),
            lockedForUser = false
        )
        baseQuiz = Quiz(
            id = 123L
        )
        baseDiscussion = DiscussionTopicHeader(
            id = 123L,
            message = "discussion message",
            author = DiscussionParticipant(
                displayName = "Hodor",
                avatarImageUrl = "pretty-hodor.com"
            ),
            postedDate = Date()
        )

        baseModel = SubmissionDetailsEmptyContentModel(baseAssignment, baseCourse, isStudioEnabled)
    }

    @Test
    fun `Sets Assignment due tomorrow text`() {
        baseAssignment = baseAssignment.copy(
            dueAt = OffsetDateTime.now().plusDays(1L).withHour(13).withMinute(59).format(
                DateTimeFormatter.ISO_DATE_TIME
            )
        )

        baseModel = baseModel.copy(assignment = baseAssignment)

        val expectedState = defaultLoadedState.copy(
            isAllowedToSubmit = true,
            dueDateText = "Due tomorrow at 1:59 pm"
        )

        val actualState = SubmissionDetailsEmptyContentPresenter.present(baseModel, context)

        assertEquals(expectedState, actualState)
    }

    @Test
    fun `Sets Assignment due today`() {
        baseAssignment = baseAssignment.copy(
            dueAt = OffsetDateTime.now().withHour(13).withMinute(59).format(DateTimeFormatter.ISO_DATE_TIME)
        )

        baseModel = baseModel.copy(assignment = baseAssignment)

        val expectedState = defaultLoadedState.copy(
            isAllowedToSubmit = true,
            dueDateText = "Due today at 1:59 pm"
        )

        val actualState = SubmissionDetailsEmptyContentPresenter.present(baseModel, context)

        assertEquals(expectedState, actualState)
    }

    @Test
    fun `Sets Assignment due yesterday`() {
        baseAssignment = baseAssignment.copy(
            dueAt = OffsetDateTime.now().minusDays(1L).withHour(13).withMinute(59).format(
                DateTimeFormatter.ISO_DATE_TIME
            )
        )

        baseModel = baseModel.copy(assignment = baseAssignment)

        val expectedState = defaultLoadedState.copy(
            isAllowedToSubmit = true,
            dueDateText = "Due yesterday at 1:59 pm"
        )

        val actualState = SubmissionDetailsEmptyContentPresenter.present(baseModel, context)

        assertEquals(expectedState, actualState)
    }

    @Test
    fun `Sets Assignment due on arbitrary date`() {
        var expectedDueDate = "Due Apr 2 at 1:59 pm"
        var expectedMonth = 4
        if(OffsetDateTime.now().month == Month.APRIL) {
            // If it is April 1-3, the above setting will result in failure.
            // Move the expected due date to June.
            expectedDueDate = "Due Jun 2 at 1:59 pm"
            expectedMonth = 6
        }
        baseAssignment = baseAssignment.copy(
            dueAt = OffsetDateTime.now().withMonth(expectedMonth).withDayOfMonth(2).withHour(13).withMinute(59).format(
                DateTimeFormatter.ISO_DATE_TIME
            )
        )

        baseModel = baseModel.copy(assignment = baseAssignment)

        val expectedState = defaultLoadedState.copy(
            isAllowedToSubmit = true,
            dueDateText = expectedDueDate
        )

        val actualState = SubmissionDetailsEmptyContentPresenter.present(baseModel, context)

        assertEquals(expectedState, actualState)

    }

    @Test
    fun `Sets Assignment has no due date`() {
        baseAssignment = baseAssignment.copy(
            dueAt = null
        )
        baseModel = baseModel.copy(assignment = baseAssignment)

        val expectedState = defaultLoadedState.copy(
            isAllowedToSubmit = true,
            dueDateText = "Your assignment has no due date"
        )

        val actualState = SubmissionDetailsEmptyContentPresenter.present(baseModel, context)

        assertEquals(expectedState, actualState)
    }

    @Test
    fun `Sets Assignment locked`() {
        baseAssignment = baseAssignment.copy(
            lockedForUser = true,
            lockAt = OffsetDateTime.now().withYear(2016).withMonth(4).withDayOfMonth(2).withHour(13).withMinute(
                59
            ).format(DateTimeFormatter.ISO_DATE_TIME)
        )
        baseModel = baseModel.copy(assignment = baseAssignment)

        val expectedState = defaultLoadedState.copy(
            isAllowedToSubmit = false,
            dueDateText = "Your assignment was locked on Apr 2, 2016 at 1:59 pm"
        )

        val actualState = SubmissionDetailsEmptyContentPresenter.present(baseModel, context)

        assertEquals(expectedState, actualState)
    }

    @Test
    fun `Sets Assignment will unlock`() {
        baseAssignment = baseAssignment.copy(
            lockedForUser = true,
            unlockAt = OffsetDateTime.now().withYear(2067).withMonth(4).withDayOfMonth(2).withHour(
                13
            ).withMinute(59).format(DateTimeFormatter.ISO_DATE_TIME)
        )
        baseModel = baseModel.copy(assignment = baseAssignment)

        val expectedState = defaultLoadedState.copy(
            isAllowedToSubmit = false,
            dueDateText = "Your assignment will unlock on Apr 2, 2067 at 1:59 pm"
        )

        val actualState = SubmissionDetailsEmptyContentPresenter.present(baseModel, context)

        assertEquals(expectedState, actualState)
    }

    @Test
    fun `Sets Assignment locked by module`() {
        baseAssignment = baseAssignment.copy(
            lockedForUser = true,
            lockInfo = LockInfo(
                contextModule = LockedModule(
                    name = "Test Module"
                )
            )
        )
        baseModel = baseModel.copy(assignment = baseAssignment)

        val expectedState = defaultLoadedState.copy(
            isAllowedToSubmit = false,
            dueDateText = "Your assignment is locked by module \"Test Module\""
        )

        val actualState = SubmissionDetailsEmptyContentPresenter.present(baseModel, context)

        assertEquals(expectedState, actualState)
    }

    @Test
    fun `Sets Assignment locked by module prereq`() {
        baseAssignment = baseAssignment.copy(
            lockedForUser = true,
            lockInfo = LockInfo(
                modulePrerequisiteNames = arrayListOf("must_view")
            )
        )
        baseModel = baseModel.copy(assignment = baseAssignment)

        val expectedState = defaultLoadedState.copy(
            isAllowedToSubmit = false,
            dueDateText = "Your assignment is locked by a module requirement"
        )

        val actualState = SubmissionDetailsEmptyContentPresenter.present(baseModel, context)

        assertEquals(expectedState, actualState)
    }

    @Test
    fun `Adds year to due date when not this year`() {
        baseAssignment = baseAssignment.copy(
            dueAt = OffsetDateTime.now()
                .withYear(2018)
                .withMonth(4)
                .withDayOfMonth(2)
                .withHour(13)
                .withMinute(59)
                .format(DateTimeFormatter.ISO_DATE_TIME)
        )

        baseModel = baseModel.copy(assignment = baseAssignment)

        val expectedState = defaultLoadedState.copy(
            isAllowedToSubmit = true,
            dueDateText = "Due Apr 2, 2018 at 1:59 pm"
        )

        val actualState = SubmissionDetailsEmptyContentPresenter.present(baseModel, context)

        assertEquals(expectedState, actualState)
    }

    @Test
    fun `Sets enable button visible state when Assignment is not locked`() {
        val actualState = SubmissionDetailsEmptyContentPresenter.present(baseModel, context)

        assertTrue((actualState as Loaded).isAllowedToSubmit)
    }

    @Test
    fun `Sets isObserver visible state when isObserver`() {
        baseModel = baseModel.copy(isObserver = true)
        val actualState = SubmissionDetailsEmptyContentPresenter.present(baseModel, context)

        assertTrue((actualState as Loaded).isObserver)
    }


    @Test
    fun `Submit button disabled if not allowed to submit when assignment is online upload`() {
        baseModel = baseModel.copy(Assignment(lockedForUser = true, submissionTypesRaw = listOf("online_upload")))
        val actualState = SubmissionDetailsEmptyContentPresenter.present(baseModel, context)

        assertFalse((actualState as Loaded).isAllowedToSubmit)
    }

    @Test
    fun `Submit button disabled if not allowed to submit when assignment is external tool`() {
        baseModel = baseModel.copy(Assignment(lockedForUser = true, submissionTypesRaw = listOf("external_tool")))
        val actualState = SubmissionDetailsEmptyContentPresenter.present(baseModel, context)

        assertFalse((actualState as Loaded).isAllowedToSubmit)
    }

    @Test
    fun `Submit button enabled if assignment is allowed to submit`() {
        baseAssignment = baseAssignment.copy(
            dueAt = OffsetDateTime.now().withHour(13).withMinute(59).format(DateTimeFormatter.ISO_DATE_TIME)
        )

        baseModel = baseModel.copy(assignment = baseAssignment)

        val expectedState = defaultLoadedState.copy(
            isAllowedToSubmit = true,
            dueDateText = "Due today at 1:59 pm",
            submitButtonText = "Submit Assignment"
        )

        val actualState = SubmissionDetailsEmptyContentPresenter.present(baseModel, context)

        assertEquals(expectedState, actualState)
    }

    @Test
    fun `Submit button reads "View Quiz" if its a quiz`() {
        val assignment = baseAssignment.copy(
            submissionTypesRaw = listOf("online_quiz"),
            quizId = baseQuiz.id
        )
        val model = baseModel.copy(assignment = assignment, quiz = baseQuiz)
        val state = SubmissionDetailsEmptyContentPresenter.present(model, context) as Loaded
        assertEquals("View Quiz", state.submitButtonText)
    }

    @Test
    fun `Submit button reads "View Discussion" if its a discussion`() {
        val assignment = baseAssignment.copy(
            submissionTypesRaw = listOf("discussion_topic"),
            discussionTopicHeader = baseDiscussion
        )
        val model = baseModel.copy(assignment = assignment)
        val state = SubmissionDetailsEmptyContentPresenter.present(model, context) as Loaded
        assertEquals("View Discussion", state.submitButtonText)
    }

    @Test
    fun `Submit button reads "Launch External Tool" if submission type is external tool`() {
        val assignment = baseAssignment.copy(
            submissionTypesRaw = listOf(Assignment.SubmissionType.EXTERNAL_TOOL.apiString)
        )
        val model = baseModel.copy(assignment = assignment)
        val state = SubmissionDetailsEmptyContentPresenter.present(model, context) as Loaded
        assertEquals("Launch External Tool", state.submitButtonText)
    }
}
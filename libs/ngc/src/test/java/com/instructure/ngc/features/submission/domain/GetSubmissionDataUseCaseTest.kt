/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
package com.instructure.ngc.features.submission.domain

import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.data.repository.assignment.AssignmentRepository
import com.instructure.pandautils.data.repository.course.CourseRepository
import com.instructure.pandautils.features.submission.SubmissionUploadHelper
import com.instructure.pandautils.room.studentdb.entities.CreateSubmissionEntity
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class GetSubmissionDataUseCaseTest {

    private val assignmentRepository: AssignmentRepository = mockk(relaxed = true)
    private val courseRepository: CourseRepository = mockk(relaxed = true)
    private val submissionUploadHelper: SubmissionUploadHelper = mockk(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()

    private val useCase = GetSubmissionDataUseCase(assignmentRepository, courseRepository, submissionUploadHelper)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `Draft present - domain model carries draftHtml, draftSavedAt, and all assignment data`() = runTest {
        val courseId = 1L
        val assignmentId = 42L
        val draftDate = Date(1_700_000_000_000L)

        val submission = Submission(attempt = 3L, workflowState = "submitted")
        val assignment = Assignment(
            id = assignmentId,
            name = "Essay Assignment",
            dueAt = "2024-03-10T00:00:00Z",
            submissionTypesRaw = listOf("online_text_entry"),
            submission = submission
        )
        val course = Course(id = courseId, name = "Biology 101")

        val draftEntity = CreateSubmissionEntity(
            id = 1L,
            assignmentId = assignmentId,
            canvasContext = CanvasContext.emptyCourseContext(id = courseId),
            submissionType = Assignment.SubmissionType.ONLINE_TEXT_ENTRY.apiString,
            userId = 99L,
            submissionEntry = "<p>My draft text</p>",
            lastActivityDate = draftDate,
            isDraft = true
        )

        coEvery { assignmentRepository.getAssignmentWithHistory(courseId, assignmentId, false) } returns DataResult.Success(assignment)
        coEvery { courseRepository.getCourse(courseId, false) } returns DataResult.Success(course)
        coEvery { submissionUploadHelper.loadDraft(any(), assignmentId, Assignment.SubmissionType.ONLINE_TEXT_ENTRY.apiString) } returns draftEntity

        val result = useCase(courseId, assignmentId)

        assertEquals("Essay Assignment", result.assignmentName)
        assertEquals("Biology 101", result.courseName)
        assertTrue(result.allowsTextEntry)
        assertEquals(3L, result.lastAttempt)
        assertEquals("<p>My draft text</p>", result.draftHtml)
        assertEquals(draftDate, result.draftSavedAt)
    }

    @Test
    fun `No draft - draftHtml is null and draftSavedAt is null`() = runTest {
        val courseId = 2L
        val assignmentId = 55L

        val assignment = Assignment(
            id = assignmentId,
            name = "Text Submission",
            dueAt = null,
            submissionTypesRaw = listOf("online_text_entry"),
            submission = null
        )
        val course = Course(id = courseId, name = "History 202")

        coEvery { assignmentRepository.getAssignmentWithHistory(courseId, assignmentId, false) } returns DataResult.Success(assignment)
        coEvery { courseRepository.getCourse(courseId, false) } returns DataResult.Success(course)
        coEvery { submissionUploadHelper.loadDraft(any(), assignmentId, Assignment.SubmissionType.ONLINE_TEXT_ENTRY.apiString) } returns null

        val result = useCase(courseId, assignmentId)

        assertEquals("Text Submission", result.assignmentName)
        assertEquals("History 202", result.courseName)
        assertTrue(result.allowsTextEntry)
        assertNull(result.dueDate)
        assertNull(result.lastAttempt)
        assertNull(result.draftHtml)
        assertNull(result.draftSavedAt)
    }

    @Test
    fun `Assignment without text entry - allowsTextEntry is false`() = runTest {
        val courseId = 3L
        val assignmentId = 77L

        val assignment = Assignment(
            id = assignmentId,
            name = "File Upload Only",
            submissionTypesRaw = listOf("online_upload"),
            submission = null
        )
        val course = Course(id = courseId, name = "Art 101")

        coEvery { assignmentRepository.getAssignmentWithHistory(courseId, assignmentId, false) } returns DataResult.Success(assignment)
        coEvery { courseRepository.getCourse(courseId, false) } returns DataResult.Success(course)
        coEvery { submissionUploadHelper.loadDraft(any(), assignmentId, Assignment.SubmissionType.ONLINE_TEXT_ENTRY.apiString) } returns null

        val result = useCase(courseId, assignmentId)

        assertFalse(result.allowsTextEntry)
        assertNull(result.draftHtml)
    }

    @Test
    fun `Last attempt is null when submission attempt is zero`() = runTest {
        val courseId = 4L
        val assignmentId = 88L

        val submission = Submission(attempt = 0L, workflowState = "submitted")
        val assignment = Assignment(
            id = assignmentId,
            name = "Empty Attempt",
            submissionTypesRaw = listOf("online_text_entry"),
            submission = submission
        )
        val course = Course(id = courseId, name = "Math 303")

        coEvery { assignmentRepository.getAssignmentWithHistory(courseId, assignmentId, false) } returns DataResult.Success(assignment)
        coEvery { courseRepository.getCourse(courseId, false) } returns DataResult.Success(course)
        coEvery { submissionUploadHelper.loadDraft(any(), assignmentId, Assignment.SubmissionType.ONLINE_TEXT_ENTRY.apiString) } returns null

        val result = useCase(courseId, assignmentId)

        assertNull(result.lastAttempt)
    }

    @Test
    fun `Due date is populated when assignment has dueAt`() = runTest {
        val courseId = 5L
        val assignmentId = 99L

        val assignment = Assignment(
            id = assignmentId,
            name = "Dated Assignment",
            dueAt = "2025-06-15T23:59:00Z",
            submissionTypesRaw = listOf("online_text_entry"),
            submission = null
        )
        val course = Course(id = courseId, name = "Physics 404")

        coEvery { assignmentRepository.getAssignmentWithHistory(courseId, assignmentId, false) } returns DataResult.Success(assignment)
        coEvery { courseRepository.getCourse(courseId, false) } returns DataResult.Success(course)
        coEvery { submissionUploadHelper.loadDraft(any(), assignmentId, Assignment.SubmissionType.ONLINE_TEXT_ENTRY.apiString) } returns null

        val result = useCase(courseId, assignmentId)

        assertNotNull(result.dueDate)
    }
}

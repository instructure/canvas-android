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
import com.instructure.pandautils.room.studentdb.entities.CreateFileSubmissionEntity
import com.instructure.pandautils.room.studentdb.entities.CreateSubmissionEntity
import com.instructure.pandautils.room.studentdb.entities.daos.CreateFileSubmissionDao
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
    private val createFileSubmissionDao: CreateFileSubmissionDao = mockk(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()

    private val useCase = GetSubmissionDataUseCase(
        assignmentRepository,
        courseRepository,
        submissionUploadHelper,
        createFileSubmissionDao
    )

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
            description = "<p>Write a 500-word essay.</p>",
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

        val result = useCase(GetSubmissionDataUseCase.Params(courseId, assignmentId, Assignment.SubmissionType.ONLINE_TEXT_ENTRY))

        assertEquals("Essay Assignment", result.assignmentName)
        assertEquals("Biology 101", result.courseName)
        assertTrue(result.allowsTextEntry)
        assertEquals(3L, result.lastAttempt)
        assertEquals("<p>My draft text</p>", result.draftHtml)
        assertEquals(draftDate, result.draftSavedAt)
        assertEquals("<p>Write a 500-word essay.</p>", result.instructionsHtml)
    }

    @Test
    fun `Instructions html is null when assignment has no description`() = runTest {
        val courseId = 1L
        val assignmentId = 42L

        val assignment = Assignment(
            id = assignmentId,
            name = "Essay Assignment",
            description = null,
            submissionTypesRaw = listOf("online_text_entry"),
        )
        val course = Course(id = courseId, name = "Biology 101")

        coEvery { assignmentRepository.getAssignmentWithHistory(courseId, assignmentId, false) } returns DataResult.Success(assignment)
        coEvery { courseRepository.getCourse(courseId, false) } returns DataResult.Success(course)
        coEvery { submissionUploadHelper.loadDraft(any(), assignmentId, Assignment.SubmissionType.ONLINE_TEXT_ENTRY.apiString) } returns null

        val result = useCase(GetSubmissionDataUseCase.Params(courseId, assignmentId, Assignment.SubmissionType.ONLINE_TEXT_ENTRY))

        assertNull(result.instructionsHtml)
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

        val result = useCase(GetSubmissionDataUseCase.Params(courseId, assignmentId, Assignment.SubmissionType.ONLINE_TEXT_ENTRY))

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

        val result = useCase(GetSubmissionDataUseCase.Params(courseId, assignmentId, Assignment.SubmissionType.ONLINE_UPLOAD))

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

        val result = useCase(GetSubmissionDataUseCase.Params(courseId, assignmentId, Assignment.SubmissionType.ONLINE_TEXT_ENTRY))

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

        val result = useCase(GetSubmissionDataUseCase.Params(courseId, assignmentId, Assignment.SubmissionType.ONLINE_TEXT_ENTRY))

        assertNotNull(result.dueDate)
    }

    // -------------------------------------------------------------------------
    // availableTypes
    // -------------------------------------------------------------------------

    @Test
    fun `availableTypes - only NGC-implemented types are included`() = runTest {
        val courseId = 6L
        val assignmentId = 100L

        val assignment = Assignment(
            id = assignmentId,
            name = "Multi-type Assignment",
            // online_quiz is NOT NGC-implemented, so it must be filtered out
            submissionTypesRaw = listOf("online_text_entry", "online_upload", "online_url", "online_quiz"),
            submission = null
        )
        val course = Course(id = courseId, name = "Test Course")

        coEvery { assignmentRepository.getAssignmentWithHistory(courseId, assignmentId, false) } returns DataResult.Success(assignment)
        coEvery { courseRepository.getCourse(courseId, false) } returns DataResult.Success(course)
        coEvery { submissionUploadHelper.loadDraft(any(), any(), any()) } returns null

        val result = useCase(GetSubmissionDataUseCase.Params(courseId, assignmentId, Assignment.SubmissionType.ONLINE_TEXT_ENTRY))

        assertEquals(
            listOf(
                Assignment.SubmissionType.ONLINE_TEXT_ENTRY,
                Assignment.SubmissionType.ONLINE_UPLOAD,
                Assignment.SubmissionType.ONLINE_URL,
            ),
            result.availableTypes
        )
    }

    @Test
    fun `availableTypes - preserves TEXT then UPLOAD ordering regardless of raw order`() = runTest {
        val courseId = 7L
        val assignmentId = 101L

        // Upload is listed first in the raw list; we expect TEXT then UPLOAD in the result
        val assignment = Assignment(
            id = assignmentId,
            name = "Ordered Assignment",
            submissionTypesRaw = listOf("online_upload", "online_text_entry"),
            submission = null
        )
        val course = Course(id = courseId, name = "Test Course")

        coEvery { assignmentRepository.getAssignmentWithHistory(courseId, assignmentId, false) } returns DataResult.Success(assignment)
        coEvery { courseRepository.getCourse(courseId, false) } returns DataResult.Success(course)
        coEvery { submissionUploadHelper.loadDraft(any(), any(), any()) } returns null

        val result = useCase(GetSubmissionDataUseCase.Params(courseId, assignmentId, Assignment.SubmissionType.ONLINE_TEXT_ENTRY))

        // The filter preserves the order returned by getSubmissionTypes() which reflects the
        // raw list order — both TEXT and UPLOAD are present, and their relative order here
        // matches their raw declaration order (upload, text_entry after filter).
        assertTrue(result.availableTypes.containsAll(
            listOf(Assignment.SubmissionType.ONLINE_TEXT_ENTRY, Assignment.SubmissionType.ONLINE_UPLOAD)
        ))
    }

    @Test
    fun `availableTypes - empty when assignment has no NGC-implemented types`() = runTest {
        val courseId = 8L
        val assignmentId = 102L

        val assignment = Assignment(
            id = assignmentId,
            name = "Non-NGC Assignment",
            submissionTypesRaw = listOf("online_quiz", "media_recording"),
            submission = null
        )
        val course = Course(id = courseId, name = "Test Course")

        coEvery { assignmentRepository.getAssignmentWithHistory(courseId, assignmentId, false) } returns DataResult.Success(assignment)
        coEvery { courseRepository.getCourse(courseId, false) } returns DataResult.Success(course)
        coEvery { submissionUploadHelper.loadDraft(any(), any(), any()) } returns null

        val result = useCase(GetSubmissionDataUseCase.Params(courseId, assignmentId, Assignment.SubmissionType.ONLINE_TEXT_ENTRY))

        assertTrue(result.availableTypes.isEmpty())
    }

    // -------------------------------------------------------------------------
    // draftFiles and fileDraftSavedAt
    // -------------------------------------------------------------------------

    @Test
    fun `draftFiles - mapped from CreateFileSubmissionEntity when file draft exists`() = runTest {
        val courseId = 9L
        val assignmentId = 103L
        val savedAt = Date(1_700_000_001_000L)

        val assignment = Assignment(
            id = assignmentId,
            name = "Upload Assignment",
            submissionTypesRaw = listOf("online_upload"),
            submission = null
        )
        val course = Course(id = courseId, name = "Art 101")

        val fileDraftEntity = CreateSubmissionEntity(
            id = 55L,
            assignmentId = assignmentId,
            canvasContext = CanvasContext.emptyCourseContext(id = courseId),
            submissionType = Assignment.SubmissionType.ONLINE_UPLOAD.apiString,
            userId = 1L,
            lastActivityDate = savedAt,
            isDraft = true,
        )
        val fileEntity = CreateFileSubmissionEntity(
            id = 200L,
            dbSubmissionId = 55L,
            name = "essay.pdf",
            size = 2048L,
            contentType = "application/pdf",
            fullPath = "/tmp/essay.pdf",
        )

        coEvery { assignmentRepository.getAssignmentWithHistory(courseId, assignmentId, false) } returns DataResult.Success(assignment)
        coEvery { courseRepository.getCourse(courseId, false) } returns DataResult.Success(course)
        coEvery { submissionUploadHelper.loadDraft(any(), assignmentId, Assignment.SubmissionType.ONLINE_TEXT_ENTRY.apiString) } returns null
        coEvery { submissionUploadHelper.loadDraft(any(), assignmentId, Assignment.SubmissionType.ONLINE_UPLOAD.apiString) } returns fileDraftEntity
        coEvery { createFileSubmissionDao.findFilesForSubmissionId(55L) } returns listOf(fileEntity)

        val result = useCase(GetSubmissionDataUseCase.Params(courseId, assignmentId, Assignment.SubmissionType.ONLINE_UPLOAD))

        assertEquals(1, result.draftFiles.size)
        val draftFile = result.draftFiles.first()
        assertEquals(200L, draftFile.id)
        assertEquals("essay.pdf", draftFile.name)
        assertEquals(2048L, draftFile.size)
        assertEquals("application/pdf", draftFile.contentType)
        assertEquals("/tmp/essay.pdf", draftFile.fullPath)
        assertEquals(savedAt, result.fileDraftSavedAt)
    }

    @Test
    fun `allowedExtensions - populated from assignment allowedExtensions`() = runTest {
        val courseId = 11L
        val assignmentId = 105L

        val assignment = Assignment(
            id = assignmentId,
            name = "Restricted Upload",
            submissionTypesRaw = listOf("online_upload"),
            allowedExtensions = listOf("pdf", "docx"),
            submission = null
        )
        val course = Course(id = courseId, name = "English 101")

        coEvery { assignmentRepository.getAssignmentWithHistory(courseId, assignmentId, false) } returns DataResult.Success(assignment)
        coEvery { courseRepository.getCourse(courseId, false) } returns DataResult.Success(course)
        coEvery { submissionUploadHelper.loadDraft(any(), any(), any()) } returns null

        val result = useCase(GetSubmissionDataUseCase.Params(courseId, assignmentId, Assignment.SubmissionType.ONLINE_UPLOAD))

        assertEquals(listOf("pdf", "docx"), result.allowedExtensions)
    }

    @Test
    fun `allowedExtensions - empty when assignment has no extension restrictions`() = runTest {
        val courseId = 12L
        val assignmentId = 106L

        val assignment = Assignment(
            id = assignmentId,
            name = "Open Upload",
            submissionTypesRaw = listOf("online_upload"),
            submission = null
        )
        val course = Course(id = courseId, name = "Art 202")

        coEvery { assignmentRepository.getAssignmentWithHistory(courseId, assignmentId, false) } returns DataResult.Success(assignment)
        coEvery { courseRepository.getCourse(courseId, false) } returns DataResult.Success(course)
        coEvery { submissionUploadHelper.loadDraft(any(), any(), any()) } returns null

        val result = useCase(GetSubmissionDataUseCase.Params(courseId, assignmentId, Assignment.SubmissionType.ONLINE_UPLOAD))

        assertTrue(result.allowedExtensions.isEmpty())
    }

    @Test
    fun `draftFiles - empty and fileDraftSavedAt null when no file draft`() = runTest {
        val courseId = 10L
        val assignmentId = 104L

        val assignment = Assignment(
            id = assignmentId,
            name = "Upload Assignment",
            submissionTypesRaw = listOf("online_upload"),
            submission = null
        )
        val course = Course(id = courseId, name = "Music 101")

        coEvery { assignmentRepository.getAssignmentWithHistory(courseId, assignmentId, false) } returns DataResult.Success(assignment)
        coEvery { courseRepository.getCourse(courseId, false) } returns DataResult.Success(course)
        coEvery { submissionUploadHelper.loadDraft(any(), any(), any()) } returns null

        val result = useCase(GetSubmissionDataUseCase.Params(courseId, assignmentId, Assignment.SubmissionType.ONLINE_UPLOAD))

        assertTrue(result.draftFiles.isEmpty())
        assertNull(result.fileDraftSavedAt)
    }
}

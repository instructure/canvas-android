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
package com.instructure.pandautils.domain.usecase.notes

import com.instructure.canvasapi2.managers.graphql.StudyNoteManager
import com.instructure.canvasapi2.models.CourseTaggedStudyNote
import com.instructure.canvasapi2.models.LearningObjectType
import com.instructure.canvasapi2.models.StudyNote
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetGlobalStudyNotesUseCaseTest {

    private val studyNoteManager: StudyNoteManager = mockk(relaxed = true)
    private lateinit var useCase: GetGlobalStudyNotesUseCase

    @Before
    fun setup() {
        useCase = GetGlobalStudyNotesUseCase(studyNoteManager)
    }

    @After
    fun teardown() {
        unmockkAll()
    }

    private fun taggedNote(id: String, courseId: Long) = CourseTaggedStudyNote(
        courseId = courseId,
        note = StudyNote(
            id = id,
            userText = "note",
            reactions = emptyList(),
            highlightData = null,
            learningObjectId = "page-1",
            learningObjectType = LearningObjectType.WikiPage,
            createdAt = null,
            updatedAt = null
        )
    )

    @Test
    fun `execute delegates to the study note manager with the given course ids and returns its result`() = runTest {
        val courseIds = listOf(1L, 2L)
        val taggedNotes = listOf(taggedNote("1", 1L), taggedNote("2", 2L))
        coEvery { studyNoteManager.getGlobalStudyNotes(any(), any()) } returns taggedNotes

        val result = useCase(GetGlobalStudyNotesUseCase.Params(courseIds))

        assertEquals(taggedNotes, result)
        coVerify { studyNoteManager.getGlobalStudyNotes(courseIds, forceNetwork = true) }
    }

    @Test
    fun `execute returns an empty list when the manager has no notes`() = runTest {
        coEvery { studyNoteManager.getGlobalStudyNotes(any(), any()) } returns emptyList()

        val result = useCase(GetGlobalStudyNotesUseCase.Params(listOf(1L)))

        assertEquals(emptyList<CourseTaggedStudyNote>(), result)
    }

    @Test
    fun `execute passes forceNetwork = false through to the study note manager when requested`() = runTest {
        coEvery { studyNoteManager.getGlobalStudyNotes(any(), any()) } returns emptyList()

        useCase(GetGlobalStudyNotesUseCase.Params(listOf(1L), forceNetwork = false))

        coVerify { studyNoteManager.getGlobalStudyNotes(listOf(1L), forceNetwork = false) }
    }
}

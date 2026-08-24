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

class GetCourseStudyNotesUseCaseTest {

    private val studyNoteManager: StudyNoteManager = mockk(relaxed = true)
    private lateinit var useCase: GetCourseStudyNotesUseCase

    @Before
    fun setup() {
        useCase = GetCourseStudyNotesUseCase(studyNoteManager)
    }

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `execute delegates to the study note manager and returns its result`() = runTest {
        val courseId = 1L
        val studyNotes = listOf(
            StudyNote(
                id = "note-1",
                userText = "Some note",
                reactions = emptyList(),
                highlightData = null,
                learningObjectId = "page-1",
                learningObjectType = LearningObjectType.WikiPage,
                createdAt = null,
                updatedAt = null
            )
        )
        coEvery { studyNoteManager.getCourseStudyNotes(any(), any()) } returns studyNotes

        val result = useCase(GetCourseStudyNotesUseCase.Params(courseId))

        assertEquals(studyNotes, result)
        coVerify { studyNoteManager.getCourseStudyNotes(courseId, forceNetwork = true) }
    }

    @Test
    fun `execute returns an empty list when the manager has no notes`() = runTest {
        coEvery { studyNoteManager.getCourseStudyNotes(any(), any()) } returns emptyList()

        val result = useCase(GetCourseStudyNotesUseCase.Params(2L))

        assertEquals(emptyList<StudyNote>(), result)
    }

    @Test
    fun `execute passes forceNetwork = false through to the study note manager when requested`() = runTest {
        coEvery { studyNoteManager.getCourseStudyNotes(any(), any()) } returns emptyList()

        useCase(GetCourseStudyNotesUseCase.Params(3L, forceNetwork = false))

        coVerify { studyNoteManager.getCourseStudyNotes(3L, forceNetwork = false) }
    }
}

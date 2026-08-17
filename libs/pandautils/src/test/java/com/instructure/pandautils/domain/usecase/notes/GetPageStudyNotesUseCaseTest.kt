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

class GetPageStudyNotesUseCaseTest {

    private val studyNoteManager: StudyNoteManager = mockk(relaxed = true)
    private lateinit var useCase: GetPageStudyNotesUseCase

    @Before
    fun setup() {
        useCase = GetPageStudyNotesUseCase(studyNoteManager)
    }

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `execute delegates to the study note manager and returns its result`() = runTest {
        val courseId = 1L
        val pageId = "page-1"
        val studyNotes = listOf(
            StudyNote(
                id = "note-1",
                userText = "Some note",
                reactions = emptyList(),
                highlightData = null,
                learningObjectId = pageId,
                learningObjectType = LearningObjectType.WikiPage,
                createdAt = null,
                updatedAt = null
            )
        )
        coEvery { studyNoteManager.getPageStudyNotes(any(), any(), any()) } returns studyNotes

        val result = useCase(GetPageStudyNotesUseCase.Params(courseId, pageId))

        assertEquals(studyNotes, result)
        coVerify { studyNoteManager.getPageStudyNotes(courseId, pageId, forceNetwork = true) }
    }

    @Test
    fun `execute returns an empty list when the manager has no notes`() = runTest {
        val courseId = 2L
        val pageId = "page-2"
        coEvery { studyNoteManager.getPageStudyNotes(any(), any(), any()) } returns emptyList()

        val result = useCase(GetPageStudyNotesUseCase.Params(courseId, pageId))

        assertEquals(emptyList<StudyNote>(), result)
        coVerify { studyNoteManager.getPageStudyNotes(courseId, pageId, forceNetwork = true) }
    }

    @Test
    fun `execute passes forceNetwork = false through to the study note manager when requested`() = runTest {
        val courseId = 3L
        val pageId = "page-3"
        coEvery { studyNoteManager.getPageStudyNotes(any(), any(), any()) } returns emptyList()

        useCase(GetPageStudyNotesUseCase.Params(courseId, pageId, forceNetwork = false))

        coVerify { studyNoteManager.getPageStudyNotes(courseId, pageId, forceNetwork = false) }
    }
}

/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
 *
 */

package com.instructure.pandautils.features.speedgrader.details.studentnotes

import androidx.lifecycle.SavedStateHandle
import com.instructure.canvasapi2.models.ColumnDatum
import com.instructure.canvasapi2.models.CustomColumn
import com.instructure.pandautils.features.speedgrader.content.SpeedGraderContentViewModel.Companion.STUDENT_ID_KEY
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.ScreenState
import com.instructure.testutils.ViewModelTestRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StudentNotesViewModelTest {

    @get:Rule
    val viewModelTestRule = ViewModelTestRule()

    private val savedStateHandle: SavedStateHandle = mockk(relaxed = true)
    private val repository: StudentNotesRepository = mockk(relaxed = true)

    private lateinit var viewModel: StudentNotesViewModel

    @Before
    fun setup() {
        every { savedStateHandle.get<Long>(Const.COURSE_ID) } returns 1L
        every { savedStateHandle.get<Long>(STUDENT_ID_KEY) } returns 1L
    }

    private fun createViewModel(): StudentNotesViewModel {
        return StudentNotesViewModel(repository, savedStateHandle)
    }

    @Test
    fun `Loads student notes then filters and maps correctly`() {
        coEvery { repository.getCustomGradeBookColumns(1, false) } returns listOf(
            CustomColumn(id = 1, "Notes", position = 1, teacherNotes = true, hidden = false),
            CustomColumn(id = 2, "Notes", position = 2, teacherNotes = false, hidden = false),
            CustomColumn(id = 3, "Notes", position = 3, teacherNotes = true, hidden = true)
        )
        coEvery {
            repository.getCustomGradeBookColumnsEntries(1, 1, false)
        } returns listOf(
            ColumnDatum(userId = 1, content = "Note 1"),
            ColumnDatum(userId = 2, content = "Note 2")
        )
        coEvery { repository.getCustomGradeBookColumnsEntries(1, 2, true) } returns emptyList()
        coEvery { repository.getCustomGradeBookColumnsEntries(1, 3, true) } returns emptyList()

        viewModel = createViewModel()

        val expected = StudentNotesUiState(
            state = ScreenState.Content,
            studentNotes = listOf(StudentNote("Notes", "Note 1"))
        )

        assertEquals(expected, viewModel.uiState.value)
    }

    @Test
    fun `Loads student notes with empty list`() {
        coEvery { repository.getCustomGradeBookColumns(1, false) } returns emptyList()

        viewModel = createViewModel()

        val expected = StudentNotesUiState(
            state = ScreenState.Empty,
            studentNotes = emptyList()
        )

        assertEquals(expected, viewModel.uiState.value)
    }

    @Test
    fun `Error loading student notes`() {
        coEvery { repository.getCustomGradeBookColumns(1, false) } throws Exception("Network error")

        viewModel = createViewModel()

        assertEquals(ScreenState.Error, viewModel.uiState.value.state)

        coEvery { repository.getCustomGradeBookColumns(1, true) } returns emptyList()

        viewModel.uiState.value.onRefresh()

        assertEquals(ScreenState.Empty, viewModel.uiState.value.state)
    }
}

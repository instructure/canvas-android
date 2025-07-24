/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.instructure.pandautils.features.speedgrader.grade.comments.commentlibrary

import androidx.compose.ui.text.input.TextFieldValue
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.utils.Normalizer
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test


@OptIn(ExperimentalCoroutinesApi::class)
class SpeedGraderCommentLibraryViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val repository: SpeedGraderCommentLibraryRepository = mockk(relaxed = true)
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)

    private lateinit var viewModel: SpeedGraderCommentLibraryViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockkObject(Normalizer)
        every { Normalizer.normalize(any()) } answers { firstArg() }
        every { apiPrefs.user?.id } returns 123L
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `Loads comment library items successfully`() = runTest {
        coEvery { repository.getCommentLibraryItems(123L) } returns listOf("Great job", "Well done")

        viewModel = SpeedGraderCommentLibraryViewModel(repository, apiPrefs)

        val uiState = viewModel.uiState.value
        assertFalse(uiState.isLoading)
        assertEquals(listOf("Great job", "Well done"), uiState.items)
    }

    @Test
    fun `Filters comment library items based on query`() = runTest {
        coEvery { repository.getCommentLibraryItems(123L) } returns listOf("Great job", "Well done", "Try again")

        viewModel = SpeedGraderCommentLibraryViewModel(repository, apiPrefs)

        viewModel.uiState.value.onCommentValueChanged(TextFieldValue("great"))

        val filteredItems = viewModel.uiState.value.items
        assertEquals(listOf("Great job"), filteredItems)
    }

    @Test
    fun `Error loading comment library items`() = runTest {
        every { apiPrefs.user?.id } returns 123L
        coEvery { repository.getCommentLibraryItems(123L) } throws Exception("Network error")

        viewModel = SpeedGraderCommentLibraryViewModel(repository, apiPrefs)

        val uiState = viewModel.uiState.value
        assertFalse(uiState.isLoading)
        assertTrue(uiState.items.isEmpty())
    }
}

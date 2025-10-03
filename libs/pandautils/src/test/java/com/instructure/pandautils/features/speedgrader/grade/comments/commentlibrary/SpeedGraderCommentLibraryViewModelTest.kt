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

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.utils.Normalizer
import com.instructure.testutils.ViewModelTestRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SpeedGraderCommentLibraryViewModelTest {

    @get:Rule
    val viewModelTestRule = ViewModelTestRule()

    private val repository: SpeedGraderCommentLibraryRepository = mockk(relaxed = true)
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)
    private val savedStateHandle: SavedStateHandle = mockk(relaxed = true)

    private lateinit var viewModel: SpeedGraderCommentLibraryViewModel

    @Before
    fun setup() {
        mockkObject(Normalizer)
        every { Normalizer.normalize(any()) } answers { firstArg() }
        mockkStatic(Uri::class)
        every { Uri.decode(any()) } answers { firstArg<String>() }
        every { apiPrefs.user?.id } returns 123L
        every { savedStateHandle.get<String>(COMMENT_LIBRARY_INITIAL_COMMENT_VALUE_ROUTE_PARAM) } returns null
    }

    @Test
    fun `Initializes with comment value from saved state handle`() = runTest {
        every { savedStateHandle.get<String>(COMMENT_LIBRARY_INITIAL_COMMENT_VALUE_ROUTE_PARAM) } returns "Initial comment"

        viewModel = SpeedGraderCommentLibraryViewModel(savedStateHandle, repository, apiPrefs)

        val uiState = viewModel.uiState.value
        assertEquals("Initial comment", uiState.commentValue)
    }

    @Test
    fun `Loads comment library items successfully`() = runTest {
        coEvery { repository.getCommentLibraryItems(123L) } returns listOf("Great job", "Well done")

        viewModel = SpeedGraderCommentLibraryViewModel(savedStateHandle, repository, apiPrefs)

        val uiState = viewModel.uiState.value
        assertFalse(uiState.isLoading)
        assertEquals(listOf("Great job", "Well done"), uiState.items)
    }

    @Test
    fun `Filters comment library items based on query`() = runTest {
        coEvery { repository.getCommentLibraryItems(123L) } returns listOf("Great job", "Well done", "Try again")

        viewModel = SpeedGraderCommentLibraryViewModel(savedStateHandle, repository, apiPrefs)

        viewModel.uiState.value.onCommentValueChanged("great")

        val filteredItems = viewModel.uiState.value.items
        assertEquals(listOf("Great job"), filteredItems)
    }

    @Test
    fun `Error loading comment library items`() = runTest {
        every { apiPrefs.user?.id } returns 123L
        coEvery { repository.getCommentLibraryItems(123L) } throws Exception("Network error")

        viewModel = SpeedGraderCommentLibraryViewModel(savedStateHandle, repository, apiPrefs)

        val uiState = viewModel.uiState.value
        assertFalse(uiState.isLoading)
        assertTrue(uiState.items.isEmpty())
    }
}

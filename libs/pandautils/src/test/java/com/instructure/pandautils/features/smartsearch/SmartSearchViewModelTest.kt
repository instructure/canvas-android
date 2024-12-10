/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
package com.instructure.pandautils.features.smartsearch

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.SavedStateHandle
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.SmartSearchContentType
import com.instructure.canvasapi2.models.SmartSearchFilter
import com.instructure.canvasapi2.models.SmartSearchResult
import com.instructure.pandautils.utils.Const
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SmartSearchViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    private val lifecycleOwner: LifecycleOwner = mockk(relaxed = true)
    private val lifecycleRegistry = LifecycleRegistry(lifecycleOwner)

    private val repository: SmartSearchRepository = mockk(relaxed = true)
    private val savedStateHandle: SavedStateHandle = mockk(relaxed = true)

    @Before
    fun setUp() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        Dispatchers.setMain(testDispatcher)

        every { savedStateHandle.get<String>(QUERY) } returns "query"
        every { savedStateHandle.get<CanvasContext>(Const.CANVAS_CONTEXT) } returns Course(name = "Course")
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `Smart Search results mapped correctly`() {
        val result = listOf(
            SmartSearchResult(
                contentId = 1L,
                contentType = SmartSearchContentType.ASSIGNMENT,
                title = "Assignment 1",
                htmlUrl = "https://www.instructure.com",
                relevance = 85,
                distance = 0.256,
                body = "This is the body of the assignment"
            ),
            SmartSearchResult(
                contentId = 2L,
                contentType = SmartSearchContentType.DISCUSSION_TOPIC,
                title = "Discussion 1",
                htmlUrl = "https://www.instructure.com",
                relevance = 75,
                distance = 0.256,
                body = "This is the body of the discussion"
            ),
            SmartSearchResult(
                contentId = 3L,
                contentType = SmartSearchContentType.ANNOUNCEMENT,
                title = "File 1",
                htmlUrl = "https://www.instructure.com",
                relevance = 65,
                distance = 0.256,
                body = "This is the body of the announcement"
            )
        )
        coEvery { repository.smartSearch(any(), any(), any()) } returns result

        val expected = result.map {
            SmartSearchResultUiState(
                title = it.title,
                body = it.body,
                relevance = it.relevance,
                url = it.htmlUrl,
                type = it.contentType
            )
        }

        val viewModel = createViewModel()

        val uiState = viewModel.uiState.value
        assertEquals(expected, uiState.results)
    }

    @Test
    fun `Smart search query from saved state`() {
        val query = "query"
        every { savedStateHandle.get<String>(QUERY) } returns query

        val viewModel = createViewModel()

        val uiState = viewModel.uiState.value
        assertEquals(query, uiState.query)
    }

    @Test
    fun `CanvasContext from saved state`() {
        val canvasContext = Course(name = "Course")
        every { savedStateHandle.get<CanvasContext>(Const.CANVAS_CONTEXT) } returns canvasContext

        val viewModel = createViewModel()

        val uiState = viewModel.uiState.value
        assertEquals(canvasContext, uiState.canvasContext)
    }

    @Test
    fun `Error if api call fails`() {
        coEvery { repository.smartSearch(any(), any(), any()) } throws Exception()

        val viewModel = createViewModel()

        val uiState = viewModel.uiState.value
        assertEquals(true, uiState.error)
    }

    @Test
    fun `Search action`() {
        val result = listOf(
            SmartSearchResult(
                contentId = 1L,
                contentType = SmartSearchContentType.ASSIGNMENT,
                title = "Assignment 1",
                htmlUrl = "https://www.instructure.com",
                relevance = 85,
                distance = 0.256,
                body = "This is the body of the assignment"
            ),
            SmartSearchResult(
                contentId = 2L,
                contentType = SmartSearchContentType.DISCUSSION_TOPIC,
                title = "Discussion 1",
                htmlUrl = "https://www.instructure.com",
                relevance = 75,
                distance = 0.256,
                body = "This is the body of the discussion"
            ),
            SmartSearchResult(
                contentId = 3L,
                contentType = SmartSearchContentType.ANNOUNCEMENT,
                title = "File 1",
                htmlUrl = "https://www.instructure.com",
                relevance = 65,
                distance = 0.256,
                body = "This is the body of the announcement"
            )
        )
        val expected = result.map {
            SmartSearchResultUiState(
                title = it.title,
                body = it.body,
                relevance = it.relevance,
                url = it.htmlUrl,
                type = it.contentType,
            )
        }
        val query = "test"
        coEvery { repository.smartSearch(any(), "query", any()) } returns emptyList()
        coEvery { repository.smartSearch(any(), "test", any()) } returns result

        val viewModel = createViewModel()

        viewModel.uiState.value.actionHandler(SmartSearchAction.Search(query))

        val uiState = viewModel.uiState.value
        assertEquals(query, uiState.query)
        assertEquals(expected, uiState.results)
    }

    @Test
    fun `Route action`() = runTest {
        val url = "https://www.instructure.com"
        val viewModel = createViewModel()

        viewModel.uiState.value.actionHandler(SmartSearchAction.Route(url))

        val events = mutableListOf<SmartSearchViewModelAction>()
        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
            assertEquals(SmartSearchViewModelAction.Route(url), events.last())
        }
    }

    private fun createViewModel(): SmartSearchViewModel {
        return SmartSearchViewModel(repository, savedStateHandle)
    }

    @Test
    fun `Apply filters`() {
        coEvery { repository.smartSearch(any(), any(), any()) } returns emptyList()
        val viewModel = createViewModel()

        val filters = listOf(SmartSearchFilter.ASSIGNMENTS, SmartSearchFilter.PAGES)

        viewModel.uiState.value.actionHandler(SmartSearchAction.Filter(filters))

        coVerify {
            repository.smartSearch(any(), any(), filters)
        }

        val uiState = viewModel.uiState.value

        assertEquals(filters, uiState.filters)
    }
}
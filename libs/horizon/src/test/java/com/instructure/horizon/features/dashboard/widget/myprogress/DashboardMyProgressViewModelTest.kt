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
package com.instructure.horizon.features.dashboard.widget.myprogress

import com.instructure.canvasapi2.managers.graphql.horizon.journey.LearningStatusWidgetData
import com.instructure.horizon.features.dashboard.DashboardItemState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.unmockkAll
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardMyProgressViewModelTest {

    private val repository: DashboardMyProgressRepository = mockk(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var viewModel: DashboardMyProgressViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `init loads learning status data successfully`() = runTest {
        val learningStatusData = LearningStatusWidgetData(
            lastModifiedDate = Date(),
            data = listOf(mapOf("module_count_completed" to 5))
        )

        coEvery { repository.getLearningStatusData(courseId = null, forceNetwork = false) } returns learningStatusData

        viewModel = DashboardMyProgressViewModel(repository)

        val state = viewModel.uiState.value
        assertEquals(DashboardItemState.SUCCESS, state.state)
        assertEquals(5, state.cardState.moduleCountCompleted)
    }

    @Test
    fun `init handles error gracefully`() = runTest {
        coEvery { repository.getLearningStatusData(courseId = null, forceNetwork = false) } throws Exception("Network error")

        viewModel = DashboardMyProgressViewModel(repository)

        val state = viewModel.uiState.value
        assertEquals(DashboardItemState.ERROR, state.state)
    }

    @Test
    fun `parseModuleCountCompletedFromData handles module_count_completed field`() = runTest {
        val learningStatusData = LearningStatusWidgetData(
            lastModifiedDate = Date(),
            data = listOf(mapOf("module_count_completed" to 12))
        )

        coEvery { repository.getLearningStatusData(courseId = null, forceNetwork = false) } returns learningStatusData

        viewModel = DashboardMyProgressViewModel(repository)

        val state = viewModel.uiState.value
        assertEquals(12, state.cardState.moduleCountCompleted)
    }

    @Test
    fun `parseModuleCountCompletedFromData handles empty data`() = runTest {
        val learningStatusData = LearningStatusWidgetData(
            lastModifiedDate = Date(),
            data = emptyList()
        )

        coEvery { repository.getLearningStatusData(courseId = null, forceNetwork = false) } returns learningStatusData

        viewModel = DashboardMyProgressViewModel(repository)

        val state = viewModel.uiState.value
        assertEquals(0, state.cardState.moduleCountCompleted)
    }

    @Test
    fun `refresh calls repository with forceNetwork true`() = runTest {
        val learningStatusData = LearningStatusWidgetData(
            lastModifiedDate = Date(),
            data = listOf(mapOf("module_count_completed" to 10))
        )

        coEvery { repository.getLearningStatusData(courseId = null, forceNetwork = any()) } returns learningStatusData

        viewModel = DashboardMyProgressViewModel(repository)

        var refreshCompleted = false
        viewModel.uiState.value.onRefresh {
            refreshCompleted = true
        }

        coVerify { repository.getLearningStatusData(forceNetwork = true) }
        assertEquals(true, refreshCompleted)
    }

    @Test
    fun `refresh handles error and completes`() = runTest {
        val learningStatusData = LearningStatusWidgetData(
            lastModifiedDate = Date(),
            data = listOf(mapOf("module_count_completed" to 10))
        )

        coEvery { repository.getLearningStatusData(courseId = null, forceNetwork = false) } returns learningStatusData
        coEvery { repository.getLearningStatusData(courseId = null, forceNetwork = true) } throws Exception("Network error")

        viewModel = DashboardMyProgressViewModel(repository)

        var refreshCompleted = false
        viewModel.uiState.value.onRefresh {
            refreshCompleted = true
        }

        val state = viewModel.uiState.value
        assertEquals(DashboardItemState.ERROR, state.state)
        assertEquals(true, refreshCompleted)
    }

    @Test
    fun `refresh updates state to loading then success`() = runTest {
        val learningStatusData = LearningStatusWidgetData(
            lastModifiedDate = Date(),
            data = listOf(mapOf("module_count_completed" to 10))
        )

        coEvery { repository.getLearningStatusData(courseId = null, forceNetwork = any()) } returns learningStatusData

        viewModel = DashboardMyProgressViewModel(repository)

        viewModel.uiState.value.onRefresh {}

        val state = viewModel.uiState.value
        assertEquals(DashboardItemState.SUCCESS, state.state)
    }
}

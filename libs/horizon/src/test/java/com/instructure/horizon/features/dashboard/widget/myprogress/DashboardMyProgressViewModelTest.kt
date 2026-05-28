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

import com.instructure.canvasapi2.managers.graphql.horizon.CourseWithProgress
import com.instructure.horizon.domain.usecase.GetCoursesWithProgressUseCase
import com.instructure.horizon.domain.usecase.GetLearningStatusDataUseCase
import com.instructure.horizon.features.dashboard.DashboardItemState
import com.instructure.horizon.model.MyProgressWidgetData
import com.instructure.horizon.model.MyProgressWidgetDataEntry
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

    private val getCoursesWithProgressUseCase: GetCoursesWithProgressUseCase = mockk(relaxed = true)
    private val getLearningStatusDataUseCase: GetLearningStatusDataUseCase = mockk(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var viewModel: DashboardMyProgressViewModel

    @Before
    fun setup() {
        coEvery { getCoursesWithProgressUseCase(any()) } returns listOf(
            CourseWithProgress(
                courseId = 1L,
                courseName = "Course 1",
                courseImageUrl = null,
                courseSyllabus = null,
                progress = 1.0
            ),
            CourseWithProgress(
                courseId = 2L,
                courseName = "Course 2",
                courseImageUrl = null,
                courseSyllabus = null,
                progress = 1.0
            )
        )
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    private fun createViewModel() = DashboardMyProgressViewModel(getCoursesWithProgressUseCase, getLearningStatusDataUseCase)

    @Test
    fun `init loads learning status data successfully`() = runTest {
        val myProgressData = MyProgressWidgetData(
            lastModifiedDate = Date(),
            data = listOf(
                MyProgressWidgetDataEntry(
                    courseId = 1L,
                    courseName = "Course 1",
                    userId = 1L,
                    userUUID = "uuid-1",
                    userName = "User 1",
                    userAvatarUrl = null,
                    userEmail = "user1@example.com",
                    moduleCountCompleted = 5,
                    moduleCountStarted = 2,
                    moduleCountLocked = 1,
                    moduleCountTotal = 8
                )
            )
        )

        coEvery { getLearningStatusDataUseCase(any()) } returns myProgressData

        viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertEquals(DashboardItemState.SUCCESS, state.state)
        assertEquals(5, state.cardState.moduleCountCompleted)
    }

    @Test
    fun `init handles error gracefully`() = runTest {
        coEvery { getLearningStatusDataUseCase(any()) } throws Exception("Network error")

        viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertEquals(DashboardItemState.ERROR, state.state)
    }

    @Test
    fun `init handles module_count_completed from multiple entries`() = runTest {
        val myProgressData = MyProgressWidgetData(
            lastModifiedDate = Date(),
            data = listOf(
                MyProgressWidgetDataEntry(
                    courseId = 1L,
                    courseName = "Course 1",
                    userId = 1L,
                    userUUID = "uuid-1",
                    userName = "User 1",
                    userAvatarUrl = null,
                    userEmail = "user1@example.com",
                    moduleCountCompleted = 5,
                    moduleCountStarted = 2,
                    moduleCountLocked = 1,
                    moduleCountTotal = 8
                ),
                MyProgressWidgetDataEntry(
                    courseId = 2L,
                    courseName = "Course 2",
                    userId = 1L,
                    userUUID = "uuid-1",
                    userName = "User 1",
                    userAvatarUrl = null,
                    userEmail = "user1@example.com",
                    moduleCountCompleted = 7,
                    moduleCountStarted = 3,
                    moduleCountLocked = 0,
                    moduleCountTotal = 10
                )
            )
        )

        coEvery { getLearningStatusDataUseCase(any()) } returns myProgressData

        viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertEquals(12, state.cardState.moduleCountCompleted)
    }

    @Test
    fun `init handles empty data`() = runTest {
        val myProgressData = MyProgressWidgetData(
            lastModifiedDate = Date(),
            data = emptyList()
        )

        coEvery { getLearningStatusDataUseCase(any()) } returns myProgressData

        viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertEquals(0, state.cardState.moduleCountCompleted)
    }

    @Test
    fun `init handles null data`() = runTest {
        coEvery { getLearningStatusDataUseCase(any()) } returns null

        viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertEquals(0, state.cardState.moduleCountCompleted)
    }

    @Test
    fun `refresh calls use case with forceNetwork true`() = runTest {
        val myProgressData = MyProgressWidgetData(
            lastModifiedDate = Date(),
            data = listOf(
                MyProgressWidgetDataEntry(
                    courseId = 1L,
                    courseName = "Course 1",
                    userId = 1L,
                    userUUID = "uuid-1",
                    userName = "User 1",
                    userAvatarUrl = null,
                    userEmail = "user1@example.com",
                    moduleCountCompleted = 10,
                    moduleCountStarted = 2,
                    moduleCountLocked = 1,
                    moduleCountTotal = 13
                )
            )
        )

        coEvery { getLearningStatusDataUseCase(any()) } returns myProgressData

        viewModel = createViewModel()

        var refreshCompleted = false
        viewModel.uiState.value.onRefresh {
            refreshCompleted = true
        }

        coVerify { getLearningStatusDataUseCase(match { it.forceNetwork }) }
        assertEquals(true, refreshCompleted)
    }

    @Test
    fun `refresh handles error and completes`() = runTest {
        val myProgressData = MyProgressWidgetData(
            lastModifiedDate = Date(),
            data = listOf(
                MyProgressWidgetDataEntry(
                    courseId = 1L,
                    courseName = "Course 1",
                    userId = 1L,
                    userUUID = "uuid-1",
                    userName = "User 1",
                    userAvatarUrl = null,
                    userEmail = "user1@example.com",
                    moduleCountCompleted = 10,
                    moduleCountStarted = 2,
                    moduleCountLocked = 1,
                    moduleCountTotal = 13
                )
            )
        )

        coEvery { getLearningStatusDataUseCase(match { !it.forceNetwork }) } returns myProgressData
        coEvery { getLearningStatusDataUseCase(match { it.forceNetwork }) } throws Exception("Network error")

        viewModel = createViewModel()

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
        val myProgressData = MyProgressWidgetData(
            lastModifiedDate = Date(),
            data = listOf(
                MyProgressWidgetDataEntry(
                    courseId = 1L,
                    courseName = "Course 1",
                    userId = 1L,
                    userUUID = "uuid-1",
                    userName = "User 1",
                    userAvatarUrl = null,
                    userEmail = "user1@example.com",
                    moduleCountCompleted = 10,
                    moduleCountStarted = 2,
                    moduleCountLocked = 1,
                    moduleCountTotal = 13
                )
            )
        )

        coEvery { getLearningStatusDataUseCase(any()) } returns myProgressData

        viewModel = createViewModel()

        viewModel.uiState.value.onRefresh {}

        val state = viewModel.uiState.value
        assertEquals(DashboardItemState.SUCCESS, state.state)
    }

    @Test
    fun `filter widget data for courses that the user is enrolled in`() = runTest {
        val myProgressData = MyProgressWidgetData(
            lastModifiedDate = Date(),
            data = listOf(
                MyProgressWidgetDataEntry(
                    courseId = 1L,
                    courseName = "Course 1",
                    userId = 1L,
                    userUUID = "uuid-1",
                    userName = "User 1",
                    userAvatarUrl = null,
                    userEmail = "user1@example.com",
                    moduleCountCompleted = 5,
                    moduleCountStarted = 3,
                    moduleCountLocked = 2,
                    moduleCountTotal = 10
                ),
                MyProgressWidgetDataEntry(
                    courseId = 3L,
                    courseName = "Course 3",
                    userId = 1L,
                    userUUID = "uuid-1",
                    userName = "User 1",
                    userAvatarUrl = null,
                    userEmail = "user1@example.com",
                    moduleCountCompleted = 3,
                    moduleCountStarted = 4,
                    moduleCountLocked = 5,
                    moduleCountTotal = 7
                )
            )
        )
        coEvery { getLearningStatusDataUseCase(any()) } returns myProgressData
        viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertEquals(DashboardItemState.SUCCESS, state.state)
        assertEquals(5, state.cardState.moduleCountCompleted)
    }
}

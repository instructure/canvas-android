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
package com.instructure.horizon.features.learn

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import com.instructure.canvasapi2.managers.graphql.horizon.CourseWithModuleItemDurations
import com.instructure.canvasapi2.managers.graphql.horizon.CourseWithProgress
import com.instructure.canvasapi2.managers.graphql.horizon.journey.Program
import com.instructure.canvasapi2.managers.graphql.horizon.journey.ProgramRequirement
import com.instructure.journey.type.ProgramProgressCourseEnrollmentStatus
import com.instructure.journey.type.ProgramVariantType
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.unmockkAll
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LearnViewModelTest {
    private val context: Context = mockk(relaxed = true)
    private val repository: LearnRepository = mockk(relaxed = true)
    private val savedStateHandle: SavedStateHandle = SavedStateHandle()
    private val testDispatcher = UnconfinedTestDispatcher()

    private val testCourses = listOf(
        CourseWithProgress(
            courseId = 1L,
            courseName = "Course 1",
            courseSyllabus = "",
            progress = 50.0
        ),
        CourseWithProgress(
            courseId = 2L,
            courseName = "Course 2",
            courseSyllabus = "",
            progress = 75.0
        ),
        CourseWithProgress(
            courseId = 3L,
            courseName = "Course 3",
            courseSyllabus = "",
            progress = 25.0
        )
    )

    private val testProgram = Program(
        id = "prog1",
        name = "Program 1",
        description = "Program 1 description",
        sortedRequirements = listOf(
            ProgramRequirement(
                id = "req1",
                progressId = "prog1",
                courseId = 1L,
                required = true,
                progress = 2.0,
                enrollmentStatus = ProgramProgressCourseEnrollmentStatus.ENROLLED
            )
        ),
        startDate = null,
        endDate = null,
        variant = ProgramVariantType.LINEAR,
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        coEvery { repository.getPrograms(any()) } returns listOf(testProgram)
        coEvery { repository.getCoursesWithProgress(any()) } returns testCourses
        coEvery { repository.getCoursesById(any(), any()) } returns listOf(
            CourseWithModuleItemDurations(courseId = 1L, courseName = "Course 1")
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `Test data loads successfully`() = runTest {
        val viewModel = getViewModel()

        assertFalse(viewModel.state.value.screenState.isLoading)
        assertTrue(viewModel.state.value.learningItems.isNotEmpty())
    }

    @Test
    fun `Test standalone courses are separated from program courses`() = runTest {
        val viewModel = getViewModel()

        val learningItems = viewModel.state.value.learningItems

        assertTrue(learningItems.any { item ->
            item is LearningItem.CourseItem && item.courseWithProgress.courseId == 2L
        })
        assertTrue(learningItems.any { item ->
            item is LearningItem.CourseItem && item.courseWithProgress.courseId == 3L
        })
    }

    @Test
    fun `Test program courses are grouped`() = runTest {
        val viewModel = getViewModel()

        val learningItems = viewModel.state.value.learningItems

        assertTrue(learningItems.any { item ->
            item is LearningItem.ProgramGroupItem && item.programName == "Program 1"
        })
    }

    @Test
    fun `Test failed data load sets error state`() = runTest {
        coEvery { repository.getPrograms(any()) } throws Exception("Network error")

        val viewModel = getViewModel()

        assertFalse(viewModel.state.value.screenState.isLoading)
        assertTrue(viewModel.state.value.screenState.isError)
    }

    @Test
    fun `Test empty courses list`() = runTest {
        coEvery { repository.getCoursesWithProgress(any()) } returns emptyList()
        coEvery { repository.getPrograms(any()) } returns emptyList()

        val viewModel = getViewModel()

        assertFalse(viewModel.state.value.screenState.isLoading)
        assertTrue(viewModel.state.value.learningItems.isEmpty())
    }

    @Test
    fun `Test learningItemId from saved state`() = runTest {
        val savedStateWithId = SavedStateHandle(mapOf("learningItemId" to "testId"))

        val viewModel = LearnViewModel(context, repository, savedStateWithId)

        assertFalse(viewModel.state.value.screenState.isLoading)
    }

    private fun getViewModel(): LearnViewModel {
        return LearnViewModel(context, repository, savedStateHandle)
    }
}

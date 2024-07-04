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
 *
 */

package com.instructure.parentapp.features.courses.list

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.User
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.ThemedColor
import com.instructure.parentapp.features.dashboard.TestSelectStudentHolder
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test


@ExperimentalCoroutinesApi
class CoursesViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val lifecycleOwner: LifecycleOwner = mockk(relaxed = true)
    private val lifecycleRegistry = LifecycleRegistry(lifecycleOwner)
    private val testDispatcher = UnconfinedTestDispatcher()

    private val repository: CoursesRepository = mockk(relaxed = true)
    private val colorKeeper: ColorKeeper = mockk(relaxed = true)
    private val selectedStudentFlow = MutableSharedFlow<User>()
    private val selectedStudentHolder = TestSelectStudentHolder(selectedStudentFlow)
    private val courseGradeFormatter: CourseGradeFormatter = mockk(relaxed = true)

    private lateinit var viewModel: CoursesViewModel

    @Before
    fun setup() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        Dispatchers.setMain(testDispatcher)
        coEvery { colorKeeper.getOrGenerateUserColor(any()) } returns ThemedColor(1, 1)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `Load courses when student changes`() = runTest {
        val student = User(1L)
        coEvery { repository.getCourses(student.id, any()) } returns listOf(Course(id = 1L, name = "Course 1", courseCode = "code-1"))
        every { courseGradeFormatter.getGradeText(any(), any()) } returns "A+"

        createViewModel()
        selectedStudentFlow.emit(student)

        val expectedState = CoursesUiState(
            isLoading = false,
            isError = false,
            courseListItems = listOf(
                CourseListItemUiState(
                    courseId = 1L,
                    courseName = "Course 1",
                    courseCode = "code-1",
                    grade = "A+"
                )
            ),
            studentColor = 1
        )

        Assert.assertEquals(expectedState, viewModel.uiState.value)
    }

    @Test
    fun `Courses map correctly`() = runTest {
        val student = User(1L)
        val courses = listOf(
            Course(id = 1L, name = "Course 1", courseCode = "code-1"),
            Course(id = 2L, name = "Course 2", courseCode = "code-2"),
            Course(id = 3L, name = "Course 3", courseCode = "code-3")
        )
        coEvery { repository.getCourses(any(), any()) } returns courses
        every { courseGradeFormatter.getGradeText(any(), any()) } returns "A+"

        createViewModel()
        selectedStudentFlow.emit(student)

        val expectedState = CoursesUiState(
            isLoading = false,
            isError = false,
            courseListItems = courses.map {
                CourseListItemUiState(
                    courseId = it.id,
                    courseName = it.name,
                    courseCode = it.courseCode,
                    grade = "A+"
                )
            },
            studentColor = 1
        )

        Assert.assertEquals(expectedState, viewModel.uiState.value)
    }

    @Test
    fun `Error load courses`() = runTest {
        val student = User(1L)
        coEvery { repository.getCourses(student.id, any()) } throws Exception()

        createViewModel()
        selectedStudentFlow.emit(student)

        val expectedState = CoursesUiState(
            isLoading = false,
            isError = true,
            studentColor = 1
        )

        Assert.assertEquals(expectedState, viewModel.uiState.value)
    }

    @Test
    fun `Refresh reloads courses`() = runTest {
        createViewModel()
        selectedStudentHolder.updateSelectedStudent(User(1L))

        viewModel.handleAction(CoursesAction.Refresh)

        coVerify { repository.getCourses(any(), true) }
    }

    @Test
    fun `Navigate to course details`() = runTest {
        createViewModel()

        val events = mutableListOf<CoursesViewModelAction>()
        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }

        viewModel.handleAction(CoursesAction.CourseTapped(1L))

        val expected = CoursesViewModelAction.NavigateToCourseDetails(1L)
        Assert.assertEquals(expected, events.last())
    }

    private fun createViewModel() {
        viewModel = CoursesViewModel(repository, colorKeeper, selectedStudentHolder, courseGradeFormatter)
    }
}

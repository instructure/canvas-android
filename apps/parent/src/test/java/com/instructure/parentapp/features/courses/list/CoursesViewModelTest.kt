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

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.CourseGrade
import com.instructure.canvasapi2.models.CourseSettings
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.ThemedColor
import com.instructure.parentapp.R
import com.instructure.parentapp.features.main.TestSelectStudentHolder
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
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

    private val context: Context = mockk(relaxed = true)
    private val repository: CoursesRepository = mockk(relaxed = true)
    private val colorKeeper: ColorKeeper = mockk(relaxed = true)
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)
    private val selectedStudentFlow = MutableSharedFlow<User>()
    private val selectedStudentHolder = TestSelectStudentHolder(selectedStudentFlow)

    private lateinit var viewModel: CoursesViewModel

    @Before
    fun setup() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `Load courses when student changes`() = runTest {
        val student = User(1L)
        coEvery { colorKeeper.getOrGenerateUserColor(student) } returns ThemedColor(1, 1)
        coEvery { repository.getCourses(student.id, any()) } returns listOf(Course(id = 1L, name = "Course 1", courseCode = "code-1"))

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
                    grade = null
                )
            ),
            studentColor = 1
        )

        Assert.assertEquals(expectedState, viewModel.uiState.value)
    }

    @Test
    fun `Error load courses`() = runTest {
        val student = User(1L)
        coEvery { colorKeeper.getOrGenerateUserColor(student) } returns ThemedColor(1, 1)
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
    fun `Course grade maps correctly when locked`() = runTest {
        val student = User(1L)
        val course = spyk(Course(id = 1L, name = "Course 1", courseCode = "code-1", enrollments = mutableListOf(Enrollment(userId = student.id))))
        coEvery { repository.getCourses(student.id, any()) } returns listOf(course)
        every { course.getCourseGradeForGradingPeriodSpecificEnrollment(any()) } returns CourseGrade(isLocked = true)

        createViewModel()
        selectedStudentFlow.emit(student)

        Assert.assertEquals(null, viewModel.uiState.value.courseListItems.first().grade)
    }

    @Test
    fun `Course grade maps correctly when restricted without grade string`() = runTest {
        val student = User(1L)
        val course = spyk(
            Course(
                id = 1L, name = "Course 1", courseCode = "code-1",
                enrollments = mutableListOf(Enrollment(userId = student.id)),
                settings = CourseSettings(restrictQuantitativeData = true)
            )
        )
        coEvery { repository.getCourses(student.id, any()) } returns listOf(course)
        every { course.getCourseGradeForGradingPeriodSpecificEnrollment(any()) } returns CourseGrade()

        createViewModel()
        selectedStudentFlow.emit(student)

        Assert.assertEquals(null, viewModel.uiState.value.courseListItems.first().grade)
    }

    @Test
    fun `Course grade maps correctly when restricted with grade string`() = runTest {
        val student = User(1L)
        val course = spyk(
            Course(
                id = 1L, name = "Course 1", courseCode = "code-1",
                enrollments = mutableListOf(Enrollment(userId = student.id)),
                settings = CourseSettings(restrictQuantitativeData = true)
            )
        )
        coEvery { repository.getCourses(student.id, any()) } returns listOf(course)
        every { course.getCourseGradeForGradingPeriodSpecificEnrollment(any()) } returns CourseGrade(
            currentScore = 100.0,
            currentGrade = "A"
        )

        createViewModel()
        selectedStudentFlow.emit(student)

        Assert.assertEquals("A ", viewModel.uiState.value.courseListItems.first().grade)
    }

    @Test
    fun `Course grade maps correctly without grade string`() = runTest {
        val student = User(1L)
        val course = spyk(Course(id = 1L, name = "Course 1", courseCode = "code-1", enrollments = mutableListOf(Enrollment(userId = student.id))))
        coEvery { repository.getCourses(student.id, any()) } returns listOf(course)
        every { context.getString(R.string.noGrade) } returns "No Grade"
        every { course.getCourseGradeForGradingPeriodSpecificEnrollment(any()) } returns CourseGrade(
            currentScore = 100.0
        )

        createViewModel()
        selectedStudentFlow.emit(student)

        Assert.assertEquals("No Grade", viewModel.uiState.value.courseListItems.first().grade)
    }

    @Test
    fun `Course grade maps correctly with score and grade string`() = runTest {
        val student = User(1L)
        val course = spyk(Course(id = 1L, name = "Course 1", courseCode = "code-1", enrollments = mutableListOf(Enrollment(userId = student.id))))
        coEvery { repository.getCourses(student.id, any()) } returns listOf(course)
        every { course.getCourseGradeForGradingPeriodSpecificEnrollment(any()) } returns CourseGrade(
            currentScore = 100.0,
            currentGrade = "A"
        )

        createViewModel()
        selectedStudentFlow.emit(student)

        Assert.assertEquals("A 100%", viewModel.uiState.value.courseListItems.first().grade)
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
        coEvery { apiPrefs.fullDomain } returns "https://canvas.instructure.com"

        createViewModel()

        val events = mutableListOf<CoursesViewModelAction>()
        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }

        viewModel.handleAction(CoursesAction.CourseTapped(1L))

        val expected = CoursesViewModelAction.NavigateToCourseDetails("https://canvas.instructure.com/courses/1")
        Assert.assertEquals(expected, events.last())
    }

    private fun createViewModel() {
        viewModel = CoursesViewModel(context, repository, colorKeeper, apiPrefs, selectedStudentHolder)
    }
}

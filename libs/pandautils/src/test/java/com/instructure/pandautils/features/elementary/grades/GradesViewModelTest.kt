/*
 * Copyright (C) 2021 - present Instructure, Inc.
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
package com.instructure.pandautils.features.elementary.grades

import android.content.res.Resources
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.instructure.canvasapi2.managers.CourseManager
import com.instructure.canvasapi2.managers.EnrollmentManager
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.CourseSettings
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.models.GradingPeriod
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.R
import com.instructure.pandautils.features.elementary.grades.itemviewmodels.GradeRowItemViewModel
import com.instructure.pandautils.features.elementary.grades.itemviewmodels.GradingPeriodSelectorItemViewModel
import com.instructure.pandautils.mvvm.ViewState
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.ThemedColor
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import com.instructure.pandautils.features.elementary.grades.GradingPeriod as GradingPeriodView

@ExperimentalCoroutinesApi
class GradesViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private val lifecycleOwner: LifecycleOwner = mockk(relaxed = true)
    private val lifecycleRegistry = LifecycleRegistry(lifecycleOwner)

    private val testDispatcher = UnconfinedTestDispatcher()

    private val courseManager: CourseManager = mockk(relaxed = true)
    private val resources: Resources = mockk(relaxed = true)
    private val enrollmentManager: EnrollmentManager = mockk(relaxed = true)
    private val colorKeeper: ColorKeeper = mockk(relaxed = true, relaxUnitFun = true)

    private lateinit var viewModel: GradesViewModel

    @Before
    fun setUp() {
        every { resources.getString(R.string.currentGradingPeriod) } returns "Current"
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        Dispatchers.setMain(testDispatcher)

        every { colorKeeper.getOrGenerateColor(any()) } returns ThemedColor(0)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        
        unmockkAll()
    }

    @Test
    fun `Show error state if fetching courses fails`() {
        // Given
        every { resources.getString(R.string.failedToLoadGrades) } returns "Failed to load grades"
        every { courseManager.getCoursesWithGradesAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Fail()
        }

        // When
        viewModel = createViewModel()
        viewModel.state.observe(lifecycleOwner, {})

        // Then
        assertTrue(viewModel.state.value is ViewState.Error)
        assertEquals("Failed to load grades", (viewModel.state.value as ViewState.Error).errorMessage)
    }

    @Test
    fun `Show empty state if there are no courses`() {
        // Given
        every { courseManager.getCoursesWithGradesAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(emptyList())
        }

        // When
        viewModel = createViewModel()
        viewModel.state.observe(lifecycleOwner, {})

        // Then
        assertTrue(viewModel.state.value is ViewState.Empty)
        assertEquals(R.string.noGradesToDisplay, (viewModel.state.value as ViewState.Empty).emptyTitle)
    }

    @Test
    fun `Show success state and grades with correct data without grading periods if there are no grading periods`() {
        // Given
        val course1 = createCourseWithGrades(1, "Course with Grade", "", "www.1.com", 90.0, "A")
        val course2 = createCourseWithGrades(2, "Course with Score", "#123456", "www.1.com", 75.6, "")
        val course3 = createCourseWithGrades(3, "Course without scores", "#456789", "www.1.com", null, null)
        val course4 = createCourseWithGrades(4, "Hide Final Grades", "#456789", "www.1.com", 50.0, "C", hideFinalGrades = true)

        every { courseManager.getCoursesWithGradesAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(listOf(course1, course2, course3, course4))
        }

        // When
        viewModel = createViewModel()
        viewModel.state.observe(lifecycleOwner, {})

        // Then
        assertTrue(viewModel.state.value is ViewState.Success)
        assertEquals(4, viewModel.data.value!!.items.size) // We only expect 4 items here, because we don't have the grading period selector

        val gradeRows = viewModel.data.value!!.items.map { it as GradeRowItemViewModel }

        val expectedGradeRow1 = GradeRowViewData(1, "Course with Grade", ThemedColor(0), "www.1.com", 90.0, "A")
        val expectedGradeRow2 = GradeRowViewData(2, "Course with Score", ThemedColor(0), "www.1.com", 75.6, "76%")
        val expectedGradeRow3 = GradeRowViewData(3, "Course without scores", ThemedColor(0), "www.1.com", null, "--")
        val expectedGradeRow4 = GradeRowViewData(4, "Hide Final Grades", ThemedColor(0), "www.1.com", 0.0, "--", hideProgress = true)

        assertEquals(expectedGradeRow1, gradeRows[0].data)
        assertEquals(expectedGradeRow2, gradeRows[1].data)
        assertEquals(expectedGradeRow3, gradeRows[2].data)
        assertEquals(expectedGradeRow4, gradeRows[3].data)

        assertEquals(0.9f, gradeRows[0].percentage)
        assertEquals(0.756f, gradeRows[1].percentage)
        assertEquals(0.0f, gradeRows[2].percentage)
        assertEquals(0.0f, gradeRows[3].percentage)
    }

    @Test
    fun `Show success state and grades with correct data with grading periods`() {
        // Given
        val gradingPeriods = listOf(GradingPeriod(11, "Period 11"), GradingPeriod(12, "Period 12"))
        val course1 = createCourseWithGrades(1, "Course with Grade", "", "www.1.com", 90.0, "A", gradingPeriods)

        every { courseManager.getCoursesWithGradesAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(listOf(course1))
        }

        // When
        viewModel = createViewModel()
        viewModel.state.observe(lifecycleOwner, {})

        // Then
        assertTrue(viewModel.state.value is ViewState.Success)
        assertEquals(2, viewModel.data.value!!.items.size) // We have 1 item for grading period selector and 1 course item

        val gradeRows = viewModel.data.value!!.items
        assertTrue(gradeRows[0] is GradingPeriodSelectorItemViewModel)
        assertTrue(gradeRows[1] is GradeRowItemViewModel)

        val gradingPeriodsViewModel = gradeRows[0] as GradingPeriodSelectorItemViewModel
        assertTrue(gradingPeriodsViewModel.isNotEmpty())
        assertEquals(GradingPeriodView(-1, "Current"), gradingPeriodsViewModel.selectedGradingPeriod)
    }

    @Test
    fun `Refresh error for current grading period sends refresh error event`() {
        // Given
        val course1 = createCourseWithGrades(1, "Course with Grade", "", "www.1.com", 90.0, "A")

        every { courseManager.getCoursesWithGradesAsync(any()) } returns mockk {
            coEvery { await() } returnsMany listOf(DataResult.Success(listOf(course1)), DataResult.Fail())
        }

        // When
        viewModel = createViewModel()
        viewModel.state.observe(lifecycleOwner, {})
        viewModel.refresh()

        // Then
        assertEquals(GradesAction.ShowRefreshError, viewModel.events.value!!.getContentIfNotHandled()!!)
        assertEquals(ViewState.Error(), viewModel.state.value!!)
    }

    @Test
    fun `Do nothing when the same grading period is selected`() {
        // Given
        val gradingPeriods = listOf(GradingPeriod(11, "Period 11"), GradingPeriod(12, "Period 12"))
        val course1 = createCourseWithGrades(1, "Course with Grade", "", "www.1.com", 90.0, "A", gradingPeriods)

        every { courseManager.getCoursesWithGradesAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(listOf(course1))
        }

        // When
        viewModel = createViewModel()
        viewModel.state.observe(lifecycleOwner, {})

        clearMocks(courseManager, enrollmentManager)

        viewModel.gradingPeriodSelected(GradingPeriodView(-1, "Current"))

        // Then
        verify(exactly = 0) { courseManager.getCoursesWithGradesAsync(any()) }
        verify(exactly = 0) { enrollmentManager.getEnrollmentsForGradingPeriodAsync(any(), any()) }
    }

    @Test
    fun `Load grades for grading period if different grading period is selected`() {
        // Given
        val gradingPeriods = listOf(GradingPeriod(11, "Period 11"), GradingPeriod(12, "Period 12"))
        val course1 = createCourseWithGrades(1, "Course with Grade", "", "www.1.com", 90.0, "A", gradingPeriods)

        every { courseManager.getCoursesWithGradesAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(listOf(course1))
        }

        // When
        viewModel = createViewModel()
        viewModel.state.observe(lifecycleOwner, {})

        clearMocks(courseManager, enrollmentManager)

        viewModel.gradingPeriodSelected(GradingPeriodView(11, "Period 11"))

        // Then
        verify(exactly = 0) { courseManager.getCoursesWithGradesAsync(any()) }
        verify(exactly = 1) { enrollmentManager.getEnrollmentsForGradingPeriodAsync(eq(11), any()) }
    }

    @Test
    fun `Reselect previous grading period if there is an error loading grades for grading period`() {
        // Given
        val gradingPeriods = listOf(GradingPeriod(11, "Period 11"), GradingPeriod(12, "Period 12"))
        val course1 = createCourseWithGrades(1, "Course with Grade", "", "www.1.com", 90.0, "A", gradingPeriods)

        every { courseManager.getCoursesWithGradesAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(listOf(course1))
        }

        every { enrollmentManager.getEnrollmentsForGradingPeriodAsync(any(), any()) } returns mockk() {
            coEvery { await() } returns DataResult.Fail()
        }

        // When
        viewModel = createViewModel()
        viewModel.state.observe(lifecycleOwner, {})

        clearMocks(courseManager, enrollmentManager)

        viewModel.gradingPeriodSelected(GradingPeriodView(11, "Period 11"))

        // Then
        assertEquals(GradingPeriodView(-1, "Current"), (viewModel.data.value!!.items[0] as GradingPeriodSelectorItemViewModel).selectedGradingPeriod)
    }

    @Test
    fun `Load grades for course is current grading period is reselected`() {
        // Given
        val gradingPeriods = listOf(GradingPeriod(11, "Period 11"), GradingPeriod(12, "Period 12"))
        val course1 = createCourseWithGrades(1, "Course with Grade", "", "www.1.com", 90.0, "A", gradingPeriods)

        every { courseManager.getCoursesWithGradesAsync(any()) } returns mockk {
            coEvery { await() } returnsMany listOf(DataResult.Success(listOf(course1)), DataResult.Fail())
        }

        every { enrollmentManager.getEnrollmentsForGradingPeriodAsync(any(), any()) } returns mockk() {
            coEvery { await() } returns DataResult.Success(emptyList())
        }

        // When
        viewModel = createViewModel()
        viewModel.state.observe(lifecycleOwner, {})
        viewModel.gradingPeriodSelected(GradingPeriodView(11, "Period 11"))

        clearMocks(courseManager, enrollmentManager)

        viewModel.gradingPeriodSelected(GradingPeriodView(-1, "Current"))

        // Then
        verify(exactly = 1) { courseManager.getCoursesWithGradesAsync(any()) }
        verify(exactly = 0) { enrollmentManager.getEnrollmentsForGradingPeriodAsync(eq(11), any()) }
    }

    @Test
    fun `Open grades page whe grades row clicked`() {
        // Given
        val course1 = createCourseWithGrades(1, "Course with Grade", "", "www.1.com", 90.0, "A")

        every { courseManager.getCoursesWithGradesAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(listOf(course1))
        }

        // When
        viewModel = createViewModel()
        viewModel.state.observe(lifecycleOwner, {})

        val gradeRowViewModel = viewModel.data.value!!.items[0] as GradeRowItemViewModel
        gradeRowViewModel.onRowClicked()

        // Then
        assertEquals(GradesAction.OpenCourseGrades(course1), viewModel.events.value!!.getContentIfNotHandled())
    }

    @Test
    fun `Show grading period selector dialog if grading period is clicked`() {
        // Given
        val gradingPeriods = listOf(GradingPeriod(11, "Period 11"), GradingPeriod(12, "Period 12"))
        val course1 = createCourseWithGrades(1, "Course with Grade", "", "www.1.com", 90.0, "A", gradingPeriods)

        every { courseManager.getCoursesWithGradesAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(listOf(course1))
        }

        // When
        viewModel = createViewModel()
        viewModel.state.observe(lifecycleOwner, {})

        val gradingPeriodsViewModel = viewModel.data.value!!.items[0] as GradingPeriodSelectorItemViewModel
        gradingPeriodsViewModel.onClick()

        // Then
        val expectedGradingPeriods = listOf(
            GradingPeriodView(-1, "Current"),
            GradingPeriodView(11, "Period 11"),
            GradingPeriodView(12, "Period 12")
        )
        assertEquals(GradesAction.OpenGradingPeriodsDialog(expectedGradingPeriods, 0), viewModel.events.value!!.getContentIfNotHandled())
    }

    @Test
    fun `Hide progress when quantitative data is restricted`() {
        // Given
        val course = createCourseWithGrades(1, "Course with Grade", "", "www.1.com", 90.0, "A")
            .copy(settings = CourseSettings(restrictQuantitativeData = true))

        every { courseManager.getCoursesWithGradesAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(listOf(course))
        }

        // When
        viewModel = createViewModel()
        viewModel.state.observe(lifecycleOwner, {})

        // Then
        assertTrue(viewModel.state.value is ViewState.Success)
        assertEquals(1, viewModel.data.value!!.items.size)

        val gradeRows = viewModel.data.value!!.items.map { it as GradeRowItemViewModel }

        val expectedGradeRow1 = GradeRowViewData(1, "Course with Grade", ThemedColor(0), "www.1.com", 90.0, "A", hideProgress = true)

        assertEquals(expectedGradeRow1, gradeRows[0].data)
    }

    @Test
    fun `Do not show score when quantitative data is restricted and there is no grade`() {
        // Given
        val course = createCourseWithGrades(1, "Course with Grade", "", "www.1.com", 90.0, null)
            .copy(settings = CourseSettings(restrictQuantitativeData = true))

        every { courseManager.getCoursesWithGradesAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(listOf(course))
        }

        // When
        viewModel = createViewModel()
        viewModel.state.observe(lifecycleOwner, {})

        // Then
        assertTrue(viewModel.state.value is ViewState.Success)
        assertEquals(1, viewModel.data.value!!.items.size)

        val gradeRows = viewModel.data.value!!.items.map { it as GradeRowItemViewModel }

        val expectedGradeRow1 = GradeRowViewData(1, "Course with Grade", ThemedColor(0), "www.1.com", 90.0, "--", hideProgress = true)

        assertEquals(expectedGradeRow1, gradeRows[0].data)
    }


    private fun createViewModel() = GradesViewModel(courseManager, resources, enrollmentManager, colorKeeper)

    private fun createCourseWithGrades(
        id: Long,
        name: String,
        color: String,
        imageUrl: String,
        score: Double?,
        grade: String?,
        gradingPeriods: List<GradingPeriod>? = null,
        hideFinalGrades: Boolean = false
    ): Course {
        val enrollment = Enrollment(id = 123, computedCurrentScore = score, computedCurrentGrade = grade, type = Enrollment.EnrollmentType.Student)
        return Course(
            id = id,
            name = name,
            courseColor = color,
            imageUrl = imageUrl,
            enrollments = mutableListOf(enrollment),
            gradingPeriods = gradingPeriods,
            hideFinalGrades = hideFinalGrades)
    }
}
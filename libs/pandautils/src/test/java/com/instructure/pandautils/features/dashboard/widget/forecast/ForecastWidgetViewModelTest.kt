/*
 * Copyright (C) 2026 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.pandautils.features.dashboard.widget.forecast

import android.content.res.Resources
import androidx.fragment.app.FragmentActivity
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.PlannerItem
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.pandautils.R
import com.instructure.pandautils.domain.usecase.assignment.LoadAssignmentGroupsUseCase
import com.instructure.pandautils.domain.usecase.assignment.LoadMissingAssignmentsParams
import com.instructure.pandautils.domain.usecase.assignment.LoadMissingAssignmentsUseCase
import com.instructure.pandautils.domain.usecase.assignment.LoadUpcomingAssignmentsParams
import com.instructure.pandautils.domain.usecase.assignment.LoadUpcomingAssignmentsUseCase
import com.instructure.pandautils.domain.usecase.audit.LoadRecentGradeChangesParams
import com.instructure.pandautils.domain.usecase.audit.LoadRecentGradeChangesUseCase
import com.instructure.pandautils.domain.usecase.courses.LoadCourseUseCase
import com.instructure.pandautils.features.dashboard.widget.GlobalConfig
import com.instructure.pandautils.features.dashboard.widget.usecase.ObserveGlobalConfigUseCase
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.ThemedColor
import com.instructure.pandautils.utils.getSystemLocaleCalendar
import com.instructure.pandautils.utils.getUrl
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.unmockkStatic
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.util.Calendar

@OptIn(ExperimentalCoroutinesApi::class)
class ForecastWidgetViewModelTest {

    private val loadMissingAssignmentsUseCase: LoadMissingAssignmentsUseCase = mockk(relaxed = true)
    private val loadUpcomingAssignmentsUseCase: LoadUpcomingAssignmentsUseCase = mockk(relaxed = true)
    private val loadRecentGradeChangesUseCase: LoadRecentGradeChangesUseCase = mockk(relaxed = true)
    private val observeGlobalConfigUseCase: ObserveGlobalConfigUseCase = mockk(relaxed = true)
    private val loadCourseUseCase: LoadCourseUseCase = mockk(relaxed = true)
    private val loadAssignmentGroupsUseCase: LoadAssignmentGroupsUseCase = mockk(relaxed = true)
    private val assignmentWeightCalculator: AssignmentWeightCalculator = mockk(relaxed = true)
    private val forecastWidgetRouter: ForecastWidgetRouter = mockk(relaxed = true)
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)
    private val crashlytics: FirebaseCrashlytics = mockk(relaxed = true)
    private val resources: Resources = mockk(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var viewModel: ForecastWidgetViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        every { crashlytics.recordException(any()) } just Runs
        coEvery { observeGlobalConfigUseCase(Unit) } returns flowOf(GlobalConfig())
        coEvery { loadMissingAssignmentsUseCase(any()) } returns emptyList()
        coEvery { loadUpcomingAssignmentsUseCase(any()) } returns emptyList()
        coEvery { loadRecentGradeChangesUseCase(any()) } returns emptyList()
        every { apiPrefs.user } returns mockk(relaxed = true) {
            every { id } returns 12345L
        }
        every { resources.getString(R.string.gradingStatus_excused) } returns "Excused"

        mockkObject(ColorKeeper)
        every { ColorKeeper.createThemedColor(any()) } returns ThemedColor(0, 0)

        // Mock getSystemLocaleCalendar to return a simple Calendar instance for testing
        mockkStatic(::getSystemLocaleCalendar)
        every { getSystemLocaleCalendar() } returns Calendar.getInstance()
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
        unmockkAll()
        unmockkStatic(::getSystemLocaleCalendar)
    }

    private fun createViewModel(): ForecastWidgetViewModel {
        return ForecastWidgetViewModel(
            loadMissingAssignmentsUseCase = loadMissingAssignmentsUseCase,
            loadUpcomingAssignmentsUseCase = loadUpcomingAssignmentsUseCase,
            loadRecentGradeChangesUseCase = loadRecentGradeChangesUseCase,
            observeGlobalConfigUseCase = observeGlobalConfigUseCase,
            loadCourseUseCase = loadCourseUseCase,
            loadAssignmentGroupsUseCase = loadAssignmentGroupsUseCase,
            assignmentWeightCalculator = assignmentWeightCalculator,
            forecastWidgetRouter = forecastWidgetRouter,
            apiPrefs = apiPrefs,
            crashlytics = crashlytics,
            resources = resources
        )
    }

    @Test
    fun `initial state has weekPeriod set and isCurrentWeek is true`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertTrue(uiState.weekPeriod != null)
        assertTrue(uiState.isCurrentWeek)
    }

    @Test
    fun `initial state calls use cases to load data`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        coVerify { loadMissingAssignmentsUseCase(LoadMissingAssignmentsParams(false)) }
        coVerify { loadUpcomingAssignmentsUseCase(any()) }
        coVerify { loadRecentGradeChangesUseCase(any()) }
    }

    @Test
    fun `navigatePrevious updates weekPeriod and sets isCurrentWeek to false`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.uiState.value.onNavigatePrevious()
        advanceTimeBy(300)
        advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertFalse(uiState.isCurrentWeek)
        assertTrue(uiState.weekPeriod != null)
    }

    @Test
    fun `navigateNext updates weekPeriod and sets isCurrentWeek to false`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.uiState.value.onNavigateNext()
        advanceTimeBy(300)
        advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertFalse(uiState.isCurrentWeek)
        assertTrue(uiState.weekPeriod != null)
    }

    @Test
    fun `jumpToCurrentWeek resets to current week and sets isCurrentWeek to true`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        // Navigate away from current week
        viewModel.uiState.value.onNavigateNext()
        advanceTimeBy(300)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isCurrentWeek)

        // Jump back to current week
        viewModel.uiState.value.onJumpToCurrentWeek()
        advanceTimeBy(300)
        advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertTrue(uiState.isCurrentWeek)
        assertTrue(uiState.weekPeriod != null)
    }

    @Test
    fun `jumpToCurrentWeek from previous week sets isCurrentWeek to true`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        // Navigate to previous week
        viewModel.uiState.value.onNavigatePrevious()
        advanceTimeBy(300)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isCurrentWeek)

        // Jump back to current week
        viewModel.uiState.value.onJumpToCurrentWeek()
        advanceTimeBy(300)
        advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertTrue(uiState.isCurrentWeek)
    }

    @Test
    fun `jumpToCurrentWeek reloads data with forceRefresh`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        // Navigate away and back
        viewModel.uiState.value.onNavigateNext()
        advanceTimeBy(300)
        advanceUntilIdle()

        viewModel.uiState.value.onJumpToCurrentWeek()
        advanceTimeBy(300)
        advanceUntilIdle()

        // Verify data is reloaded with forceRefresh=true
        coVerify(atLeast = 1) { loadUpcomingAssignmentsUseCase(match { it.forceRefresh }) }
        coVerify(atLeast = 1) { loadRecentGradeChangesUseCase(match { it.forceRefresh }) }
    }

    @Test
    fun `navigating between weeks updates week period display text`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        val initialWeekPeriod = viewModel.uiState.value.weekPeriod

        viewModel.uiState.value.onNavigateNext()
        advanceTimeBy(300)
        advanceUntilIdle()

        val nextWeekPeriod = viewModel.uiState.value.weekPeriod

        assertTrue(initialWeekPeriod != null)
        assertTrue(nextWeekPeriod != null)
        assertTrue(initialWeekPeriod!!.displayText != nextWeekPeriod!!.displayText)
        assertTrue(initialWeekPeriod.startDate != nextWeekPeriod.startDate)
    }

    @Test
    fun `multiple week navigations and jump to current week works correctly`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        // Navigate forward twice
        viewModel.uiState.value.onNavigateNext()
        advanceTimeBy(300)
        advanceUntilIdle()

        viewModel.uiState.value.onNavigateNext()
        advanceTimeBy(300)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isCurrentWeek)

        // Jump back to current week
        viewModel.uiState.value.onJumpToCurrentWeek()
        advanceTimeBy(300)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isCurrentWeek)
    }

    @Test
    fun `week navigation sets isLoadingItems when section is selected`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        // Select a section
        viewModel.uiState.value.onSectionSelected(ForecastSection.MISSING)

        viewModel.uiState.value.onNavigateNext()

        // Should show loading state immediately
        assertTrue(viewModel.uiState.value.isLoadingItemsForSection[ForecastSection.RECENT_GRADES]!!)
        assertTrue(viewModel.uiState.value.isLoadingItemsForSection[ForecastSection.DUE]!!)
        assertFalse(viewModel.uiState.value.isLoadingItemsForSection[ForecastSection.MISSING]!!)

        advanceTimeBy(300)
        advanceUntilIdle()

        // After loading completes
        assertFalse(viewModel.uiState.value.isLoadingItemsForSection[ForecastSection.RECENT_GRADES]!!)
        assertFalse(viewModel.uiState.value.isLoadingItemsForSection[ForecastSection.DUE]!!)
        assertFalse(viewModel.uiState.value.isLoadingItemsForSection[ForecastSection.MISSING]!!)
    }

    @Test
    fun `toggleSection sets selectedSection when none is selected`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.uiState.value.onSectionSelected(ForecastSection.MISSING)

        assertEquals(ForecastSection.MISSING, viewModel.uiState.value.selectedSection)
    }

    @Test
    fun `toggleSection clears selectedSection when same section is selected`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        // Select a section
        viewModel.uiState.value.onSectionSelected(ForecastSection.MISSING)
        assertEquals(ForecastSection.MISSING, viewModel.uiState.value.selectedSection)

        // Toggle it off
        viewModel.uiState.value.onSectionSelected(ForecastSection.MISSING)
        assertEquals(null, viewModel.uiState.value.selectedSection)
    }

    @Test
    fun `toggleSection switches to different section`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.uiState.value.onSectionSelected(ForecastSection.MISSING)
        assertEquals(ForecastSection.MISSING, viewModel.uiState.value.selectedSection)

        viewModel.uiState.value.onSectionSelected(ForecastSection.DUE)
        assertEquals(ForecastSection.DUE, viewModel.uiState.value.selectedSection)
    }

    @Test
    fun `onAssignmentClick calls router`() = runTest {
        val activity: FragmentActivity = mockk(relaxed = true)
        val assignment = mockk<Assignment>(relaxed = true) {
            every { id } returns 123L
            every { courseId } returns 456L
            every { name } returns "Test Assignment"
            every { dueAt } returns "2025-03-01T10:00:00Z"
            every { pointsPossible } returns 10.0
            every { htmlUrl } returns "url"
            every { discussionTopicHeader } returns null
        }
        coEvery { loadMissingAssignmentsUseCase(any()) } returns listOf(assignment)

        viewModel = createViewModel()
        advanceUntilIdle()

        val assignmentItem = viewModel.uiState.value.missingAssignments.first()
        assignmentItem.onClick?.invoke(activity)

        verify { forecastWidgetRouter.routeToAssignmentDetails(activity, 123L, 456L) }
    }

    @Test
    fun `onPlannerItemClick calls router`() = runTest {
        val activity: FragmentActivity = mockk(relaxed = true)
        val expectedUrl = "https://example.com/planner/item"
        val plannerItem = mockk<PlannerItem>(relaxed = true) {
            every { courseId } returns 789L
            every { contextName } returns "Test Course"
            every { plannable } returns mockk(relaxed = true) {
                every { title } returns "Test Planner Item"
                every { pointsPossible } returns 15.0
            }
            every { plannableDate } returns mockk(relaxed = true)
            every { htmlUrl } returns expectedUrl
        }

        mockkStatic("com.instructure.pandautils.utils.PlannerItemExtensionsKt")
        every { plannerItem.getUrl(apiPrefs) } returns expectedUrl

        coEvery { loadUpcomingAssignmentsUseCase(any()) } returns listOf(plannerItem)

        viewModel = createViewModel()
        advanceUntilIdle()

        val assignmentItem = viewModel.uiState.value.dueAssignments.first()
        assignmentItem.onClick?.invoke(activity)

        verify { forecastWidgetRouter.routeToPlannerItem(activity, expectedUrl) }
    }

    @Test
    fun `refresh calls loadData with forceRefresh true`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.refresh()
        advanceUntilIdle()

        coVerify(atLeast = 2) { loadMissingAssignmentsUseCase(any()) }
    }

    @Test
    fun `error during load sets isError to true`() = runTest {
        coEvery { loadMissingAssignmentsUseCase(any()) } throws Exception("Test error")

        viewModel = createViewModel()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isError)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `retry after error sets isError to false and reloads data`() = runTest {
        coEvery { loadMissingAssignmentsUseCase(any()) } throws Exception("Test error")

        viewModel = createViewModel()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isError)

        // Fix the error and retry
        coEvery { loadMissingAssignmentsUseCase(any()) } returns emptyList()
        viewModel.uiState.value.onRetry()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isError)
    }

    @Test
    fun `missing assignments are sorted by dueAt`() = runTest {
        val assignment1 = mockk<Assignment>(relaxed = true) {
            every { id } returns 1L
            every { courseId } returns 100L
            every { name } returns "Assignment 1"
            every { dueAt } returns "2025-03-01T10:00:00Z"
            every { pointsPossible } returns 10.0
            every { htmlUrl } returns "url1"
        }
        val assignment2 = mockk<Assignment>(relaxed = true) {
            every { id } returns 2L
            every { courseId } returns 100L
            every { name } returns "Assignment 2"
            every { dueAt } returns "2025-02-15T10:00:00Z"
            every { pointsPossible } returns 20.0
            every { htmlUrl } returns "url2"
        }

        coEvery { loadMissingAssignmentsUseCase(any()) } returns listOf(assignment1, assignment2)

        viewModel = createViewModel()
        advanceUntilIdle()

        val assignments = viewModel.uiState.value.missingAssignments
        assertEquals(2, assignments.size)
        // Should be sorted by dueAt, so assignment2 should come first
        assertEquals("Assignment 2", assignments[0].assignmentName)
        assertEquals("Assignment 1", assignments[1].assignmentName)
    }

    @Test
    fun `global config updates background color`() = runTest {
        val configFlow = flowOf(GlobalConfig(backgroundColor = 0xFF0000))
        coEvery { observeGlobalConfigUseCase(Unit) } returns configFlow
        every { ColorKeeper.createThemedColor(0xFF0000) } returns ThemedColor(0xFF0000, 0xFF0000)

        viewModel = createViewModel()
        advanceUntilIdle()

        val backgroundColor = viewModel.uiState.value.backgroundColor
        assertEquals(0xFF0000, backgroundColor.light)
    }

    @Test
    fun `week period is calculated correctly for current week`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        val weekPeriod = viewModel.uiState.value.weekPeriod
        assertTrue(weekPeriod != null)
        assertTrue(weekPeriod!!.startDate.isBefore(LocalDate.now()) || weekPeriod.startDate.isEqual(LocalDate.now()))
        assertTrue(weekPeriod.endDate.isAfter(LocalDate.now()) || weekPeriod.endDate.isEqual(LocalDate.now()))
    }

    @Test
    fun `upcoming assignments are loaded with correct date range`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        val weekPeriod = viewModel.uiState.value.weekPeriod!!
        val expectedStartDate = weekPeriod.startDate.atStartOfDay().toApiString()
        val expectedEndDate = weekPeriod.endDate.atTime(23, 59, 59).toApiString()

        coVerify {
            loadUpcomingAssignmentsUseCase(
                LoadUpcomingAssignmentsParams(
                    startDate = expectedStartDate.orEmpty(),
                    endDate = expectedEndDate.orEmpty(),
                    forceRefresh = false
                )
            )
        }
    }

    @Test
    fun `recent grades are loaded with correct date range and user id`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        val weekPeriod = viewModel.uiState.value.weekPeriod!!
        val expectedStartDate = weekPeriod.startDate.atStartOfDay().toApiString()
        val expectedEndDate = weekPeriod.endDate.atTime(23, 59, 59).toApiString()

        coVerify {
            loadRecentGradeChangesUseCase(
                LoadRecentGradeChangesParams(
                    studentId = 12345L,
                    startTime = expectedStartDate.orEmpty(),
                    endTime = expectedEndDate.orEmpty(),
                    forceRefresh = false
                )
            )
        }
    }
}
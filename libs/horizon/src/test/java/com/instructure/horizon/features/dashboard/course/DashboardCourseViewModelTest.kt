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
package com.instructure.horizon.features.dashboard.course

import android.content.Context
import com.instructure.canvasapi2.managers.graphql.horizon.DashboardEnrollment
import com.instructure.canvasapi2.managers.graphql.horizon.journey.Program
import com.instructure.canvasapi2.managers.graphql.horizon.journey.ProgramRequirement
import com.instructure.horizon.domain.usecase.DashboardCoursesData
import com.instructure.horizon.domain.usecase.GetDashboardCoursesUseCase
import com.instructure.horizon.features.dashboard.DashboardEventHandler
import com.instructure.horizon.features.dashboard.widget.course.DashboardCourseViewModel
import com.instructure.journey.type.ProgramProgressCourseEnrollmentStatus
import com.instructure.journey.type.ProgramVariantType
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import com.instructure.pandautils.utils.ThemePrefs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardCourseViewModelTest {
    private val context: Context = mockk(relaxed = true)
    private val getDashboardCoursesUseCase: GetDashboardCoursesUseCase = mockk(relaxed = true)
    private val networkStateProvider: NetworkStateProvider = mockk(relaxed = true)
    private val featureFlagProvider: FeatureFlagProvider = mockk(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()
    private val dashboardEventHandler = DashboardEventHandler()

    private val activeEnrollments = listOf(
        DashboardEnrollment(
            enrollmentId = 1L,
            enrollmentState = DashboardEnrollment.STATE_ACTIVE,
            courseId = 1L,
            courseName = "Course 1",
            courseImageUrl = "url_1",
            courseSyllabus = "syllabus 1",
            institutionName = "Account 1",
            completionPercentage = 0.0
        ),
        DashboardEnrollment(
            enrollmentId = 2L,
            enrollmentState = DashboardEnrollment.STATE_ACTIVE,
            courseId = 2L,
            courseName = "Course 2",
            courseImageUrl = null,
            courseSyllabus = null,
            institutionName = null,
            completionPercentage = 0.0
        ),
    )
    private val completedEnrollments = listOf(
        DashboardEnrollment(
            enrollmentId = 4L,
            enrollmentState = DashboardEnrollment.STATE_COMPLETED,
            courseId = 4L,
            courseName = "Course 4",
            courseImageUrl = null,
            courseSyllabus = null,
            institutionName = null,
            completionPercentage = 100.0
        )
    )
    private val programs = listOf(
        Program(
            id = "1",
            name = "Program 1",
            description = "Program 1 description",
            startDate = null,
            endDate = null,
            variant = ProgramVariantType.LINEAR,
            sortedRequirements = emptyList()
        ),
        Program(
            id = "2",
            name = "Program 2",
            description = "Program 2 description",
            startDate = null,
            endDate = null,
            variant = ProgramVariantType.LINEAR,
            sortedRequirements = listOf(
                ProgramRequirement(
                    id = "1",
                    progressId = "1",
                    courseId = 2,
                    required = true,
                    progress = 5.0,
                    enrollmentStatus = ProgramProgressCourseEnrollmentStatus.ENROLLED
                )
            )
        )
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        mockkObject(ThemePrefs)
        every { ThemePrefs.brandColor } returns 1

        coEvery { getDashboardCoursesUseCase.invoke() } returns DashboardCoursesData(
            enrollments = activeEnrollments + completedEnrollments,
            programs = programs,
            unenrolledPrograms = listOf(programs[0]),
            nextModuleItemByCourseId = emptyMap()
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `Test course and empty programs are in the state list`() {
        val viewModel = getViewModel()
        val state = viewModel.uiState.value
        assertEquals(3, state.courses.size)
        assertTrue(state.courses.any { it.title == "Course 1" })
        assertTrue(state.courses.any { it.title == "Course 2" })
        assertTrue(state.courses.any { it.title == "Course 4" })
        assertTrue(state.courses.none { it.title == "Course 3" })
        assertEquals(1, state.programs.items.size)
    }

    @Test
    fun `Test use case is invoked on initialization`() {
        getViewModel()
        coVerify { getDashboardCoursesUseCase.invoke() }
    }

    private fun getViewModel(): DashboardCourseViewModel {
        return DashboardCourseViewModel(context, getDashboardCoursesUseCase, dashboardEventHandler, networkStateProvider, featureFlagProvider)
    }
}

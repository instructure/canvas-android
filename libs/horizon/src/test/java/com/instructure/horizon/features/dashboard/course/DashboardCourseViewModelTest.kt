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
import com.instructure.canvasapi2.GetCoursesQuery
import com.instructure.canvasapi2.managers.graphql.horizon.journey.Program
import com.instructure.canvasapi2.managers.graphql.horizon.journey.ProgramRequirement
import com.instructure.canvasapi2.models.ModuleItem
import com.instructure.canvasapi2.models.ModuleObject
import com.instructure.canvasapi2.type.EnrollmentWorkflowState
import com.instructure.horizon.features.dashboard.DashboardEventHandler
import com.instructure.horizon.features.dashboard.widget.course.DashboardCourseRepository
import com.instructure.horizon.features.dashboard.widget.course.DashboardCourseViewModel
import com.instructure.journey.type.ProgramProgressCourseEnrollmentStatus
import com.instructure.journey.type.ProgramVariantType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
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
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardCourseViewModelTest {
    private val context: Context = mockk(relaxed = true)
    private var repository: DashboardCourseRepository = mockk(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()
    private val dashboardEventHandler = DashboardEventHandler()

    private val courses = listOf<GetCoursesQuery.Course>(
        GetCoursesQuery.Course(
            id = "1",
            name = "Course 1",
            image_download_url = "url_1",
            syllabus_body = "syllabus 1",
            account = GetCoursesQuery.Account("Account 1"),
            usersConnection = null
        ),
        GetCoursesQuery.Course(
            id = "2",
            name = "Course 2",
            image_download_url = null,
            syllabus_body = null,
            account = null,
            usersConnection = null
        ),
        GetCoursesQuery.Course(
            id = "3",
            name = "Course 3",
            image_download_url = null,
            syllabus_body = null,
            account = null,
            usersConnection = null
        ),
        GetCoursesQuery.Course(
            id = "4",
            name = "Course 4",
            image_download_url = null,
            syllabus_body = null,
            account = null,
            usersConnection = null
        ),
    )
    private val activeEnrollments = listOf<GetCoursesQuery.Enrollment>(
        GetCoursesQuery.Enrollment(
            id = "1",
            state = EnrollmentWorkflowState.active,
            lastActivityAt = Date(),
            course = courses[0]
        ),
        GetCoursesQuery.Enrollment(
            id = "2",
            state = EnrollmentWorkflowState.active,
            lastActivityAt = Date(),
            course = courses[1]
        ),
    )
    private val invitedEnrollments = listOf<GetCoursesQuery.Enrollment>(
        GetCoursesQuery.Enrollment(
            id = "3",
            state = EnrollmentWorkflowState.invited,
            lastActivityAt = Date(),
            course = courses[2]
        )
    )
    private val completedEnrollments = listOf<GetCoursesQuery.Enrollment>(
        GetCoursesQuery.Enrollment(
            id = "4",
            state = EnrollmentWorkflowState.completed,
            lastActivityAt = Date(),
            course = courses[3]
        )
    )
    private val programs = listOf<Program>(
        Program( // Not started Program
            id = "1",
            name = "Program 1",
            description = "Program 1 description",
            startDate = null,
            endDate = null,
            variant = ProgramVariantType.LINEAR,
            sortedRequirements = emptyList()
        ),
        Program( // Program with Course 2
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
    private val modules = listOf<ModuleObject>(
        ModuleObject(
            id = 1,
            name = "Module 1",
            items = listOf(
                ModuleItem(
                    id = 1,
                    title = " Module Item 1",
                    moduleId = 1,
                    contentId = 1,
                    type = "Page",
                    estimatedDuration = "PT11M"
                )
            )
        )
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        coEvery { repository.getEnrollments(any()) } returns activeEnrollments + invitedEnrollments + completedEnrollments
        coEvery { repository.getPrograms(any()) } returns programs
        coEvery { repository.acceptInvite(any(), any()) } just runs
        coEvery { repository.getFirstPageModulesWithItems(any(), any()) } returns modules
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `Test course and empty programs are in the state list`() {
        coEvery { repository.getEnrollments(any()) } returns activeEnrollments + completedEnrollments
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
    fun `Test course invitations are automatically accepted`() {
        val viewModel = getViewModel()
        coVerify { repository.acceptInvite(3, 3) }
    }

    private fun getViewModel(): DashboardCourseViewModel {
        return DashboardCourseViewModel(context, repository, dashboardEventHandler)
    }
}
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

import com.instructure.canvasapi2.GetCoursesQuery
import com.instructure.canvasapi2.apis.EnrollmentAPI
import com.instructure.canvasapi2.apis.ModuleAPI
import com.instructure.canvasapi2.managers.graphql.horizon.HorizonGetCoursesManager
import com.instructure.canvasapi2.managers.graphql.horizon.journey.GetProgramsManager
import com.instructure.canvasapi2.managers.graphql.horizon.journey.Program
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.ModuleItem
import com.instructure.canvasapi2.models.ModuleObject
import com.instructure.canvasapi2.type.EnrollmentWorkflowState
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.horizon.features.dashboard.widget.course.DashboardCourseRepository
import com.instructure.journey.type.ProgramVariantType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class DashboardCourseRepositoryTest {
    private val horizonGetCoursesManager: HorizonGetCoursesManager = mockk(relaxed = true)
    private val moduleApi: ModuleAPI. ModuleInterface = mockk(relaxed = true)
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)
    private val enrollmentApi: EnrollmentAPI. EnrollmentInterface = mockk(relaxed = true)
    private val getProgramsManager: GetProgramsManager = mockk(relaxed = true)

    private val userId = 1L
    @Before
    fun setup() {
        every { apiPrefs.user?.id } returns userId
    }

    @Test
    fun `Test successful getEnrollments call`() = runTest {
        val enrollments = listOf(
            GetCoursesQuery.Enrollment(
                "1",
                EnrollmentWorkflowState.active,
                null,
                null
            )
        )
        coEvery { horizonGetCoursesManager.getEnrollments(any(), any()) } returns DataResult.Success(enrollments)
        val repository = getRepository()


        val result = repository.getEnrollments(forceNetwork = true)
        coVerify { horizonGetCoursesManager.getEnrollments(userId, true) }
        assertEquals(enrollments, result)
    }

    @Test(expected = IllegalStateException::class)
    fun `Test failed getEnrollments call`() = runTest {
        coEvery { horizonGetCoursesManager.getEnrollments(any(), any()) } returns DataResult.Fail()
        val repository = getRepository()

        repository.getEnrollments(forceNetwork = true)
        coVerify { horizonGetCoursesManager.getEnrollments(userId, true) }
    }

    @Test
    fun `Test successful acceptInvite call`() = runTest {
        val repository = getRepository()
        coEvery { enrollmentApi.acceptInvite(any(), any(), any()) } returns DataResult.Success(Unit)
        repository.acceptInvite(1, 1)
        coVerify { enrollmentApi.acceptInvite(1, 1, any()) }
    }

    @Test(expected = IllegalStateException::class)
    fun `Test failed acceptInvite call`() = runTest {
        val repository = getRepository()
        coEvery { enrollmentApi.acceptInvite(any(), any(), any()) } returns DataResult.Fail()
        repository.acceptInvite(1, 1)
        coVerify { enrollmentApi.acceptInvite(1, 1, any()) }
    }

    @Test
    fun `Test successful getPrograms call`() = runTest {
        val programs = listOf(
            Program(
                "1",
                "Program 1",
                null,
                null,
                null,
                ProgramVariantType.LINEAR,
                null,
                emptyList()
            ),
            Program(
                "2",
                "Program 2",
                null,
                null,
                null,
                ProgramVariantType.NON_LINEAR,
                null,
                emptyList()
            ),
        )
        coEvery { getProgramsManager.getPrograms(any()) } returns programs
        val repository = getRepository()

        val result = repository.getPrograms()
        coVerify { getProgramsManager.getPrograms(any()) }
        assertEquals(programs, result)
    }

    @Test
    fun `Test successful getFirstPageModulesWithItems call`() = runTest {
        val courseId = 1L
        val modules = listOf(
            ModuleObject(
                id = 1,
                name = "Module 1",
                items = listOf(
                    ModuleItem(
                        id = 1,
                        title = "Module Item 1",
                        moduleId = 1,
                        contentId = 1,
                        type = "Page",
                        estimatedDuration = "PT10M"
                    )
                )
            ),
            ModuleObject(
                id = 2,
                name = "Module 2",
                items = listOf(
                    ModuleItem(
                        id = 2,
                        title = "Module Item 2",
                        moduleId = 2,
                        contentId = 2,
                        type = "Assignment",
                        estimatedDuration = "PT10M"
                    )
                )
            ),
        )
        coEvery { moduleApi.getFirstPageModulesWithItems(any(), any(), any(), any()) } returns DataResult.Success(modules)
        val repository = getRepository()

        val result = repository.getFirstPageModulesWithItems(courseId, forceNetwork = true)
        coVerify { moduleApi.getFirstPageModulesWithItems(CanvasContext.Type.COURSE.apiString, courseId, any(), listOf("estimated_durations")) }
        assertEquals(modules, result)
    }

    @Test(expected = IllegalStateException::class)
    fun `Test failed getFirstPageModulesWithItems call`() = runTest {
        val courseId = 1L
        coEvery { moduleApi.getFirstPageModulesWithItems(any(), any(), any(), any()) } returns DataResult.Fail()
        val repository = getRepository()

        repository.getFirstPageModulesWithItems(courseId, forceNetwork = true)
        coVerify { moduleApi.getFirstPageModulesWithItems(CanvasContext.Type.COURSE.apiString, courseId, any(), listOf("estimated_durations")) }
    }

    private fun getRepository(): DashboardCourseRepository {
        return DashboardCourseRepository(
            horizonGetCoursesManager,
            moduleApi,
            apiPrefs,
            enrollmentApi,
            getProgramsManager
        )
    }
}
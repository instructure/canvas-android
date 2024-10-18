/*
 * Copyright (C) 2024 - present Instructure, Inc.
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

package com.instructure.pandautils.features.calendartodo.createupdate

import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.PlannerAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Plannable
import com.instructure.canvasapi2.models.postmodels.PlannerNoteBody
import com.instructure.canvasapi2.utils.DataResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class CreateUpdateToDoRepositoryTest {

    private val coursesApi: CourseAPI.CoursesInterface = mockk(relaxed = true)
    private val plannerApi: PlannerAPI.PlannerInterface = mockk(relaxed = true)

    private val createUpdateToDoRepository: CreateUpdateToDoRepository = object : CreateUpdateToDoRepository(plannerApi) {
        override suspend fun getCourses(): List<Course> {
            return coursesApi.getFirstPageCoursesCalendar(RestParams()).dataOrThrow
        }
    }

    private val plannable = Plannable(1, "", null, null, null, null, null, null, null, null, null, null, null)

    @Test(expected = IllegalStateException::class)
    fun `Throw exception when create todo fails`() = runTest {
        coEvery { plannerApi.createPlannerNote(any(), any()) } returns DataResult.Fail()

        createUpdateToDoRepository.createToDo(
            title = "title",
            details = "details",
            toDoDate = "toDoDate",
            courseId = 1
        )
    }

    @Test
    fun `Create todo successful`() = runTest {
        coEvery { plannerApi.createPlannerNote(any(), any()) } returns DataResult.Success(plannable)

        createUpdateToDoRepository.createToDo(
            title = "title",
            details = "details",
            toDoDate = "toDoDate",
            courseId = 1
        )

        coVerify {
            plannerApi.createPlannerNote(
                PlannerNoteBody(
                    title = "title",
                    details = "details",
                    toDoDate = "toDoDate",
                    courseId = 1
                ), any()
            )
        }
    }

    @Test(expected = IllegalStateException::class)
    fun `Throw exception when update todo fails`() = runTest {
        coEvery { plannerApi.updatePlannerNote(any(), any(), any()) } returns DataResult.Fail()

        createUpdateToDoRepository.updateToDo(
            id = 1,
            title = "title",
            details = "details",
            toDoDate = "toDoDate",
            courseId = 1
        )
    }

    @Test
    fun `Update todo successful`() = runTest {
        coEvery { plannerApi.updatePlannerNote(any(), any(), any()) } returns DataResult.Success(plannable)

        createUpdateToDoRepository.updateToDo(
            id = 1,
            title = "title",
            details = "details",
            toDoDate = "toDoDate",
            courseId = 1
        )

        coVerify {
            plannerApi.updatePlannerNote(
                1,
                PlannerNoteBody(
                    title = "title",
                    details = "details",
                    toDoDate = "toDoDate",
                    courseId = 1
                ), any()
            )
        }
    }
}

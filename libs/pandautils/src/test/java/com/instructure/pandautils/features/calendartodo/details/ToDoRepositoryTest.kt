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

package com.instructure.pandautils.features.calendartodo.details

import com.instructure.canvasapi2.apis.PlannerAPI
import com.instructure.canvasapi2.models.Plannable
import com.instructure.canvasapi2.utils.DataResult
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test

class ToDoRepositoryTest {

    private val plannerApi: PlannerAPI.PlannerInterface = mockk(relaxed = true)

    private val toDoRepository = ToDoRepository(plannerApi)

    @Test(expected = IllegalStateException::class)
    fun `Throw exception when get fails`() = runTest {
        coEvery { plannerApi.getPlannerNote(any(), any()) } returns DataResult.Fail()

        toDoRepository.getPlannerNote(1)
    }

    @Test
    fun `Get planner note successful`() = runTest {
        val note: Plannable = mockk(relaxed = true)
        coEvery { plannerApi.getPlannerNote(any(), any()) } returns DataResult.Success(note)

        val result = toDoRepository.getPlannerNote(1)

        Assert.assertEquals(note, result)
    }

    @Test(expected = IllegalStateException::class)
    fun `Throw exception when deleting fails`() = runTest {
        coEvery { plannerApi.deletePlannerNote(any(), any()) } returns DataResult.Fail()

        toDoRepository.deletePlannerNote(1)
    }

    @Test
    fun `Delete planner note successful`() = runTest {
        coEvery { plannerApi.deletePlannerNote(any(), any()) } returns DataResult.Success(Unit)

        val result = toDoRepository.deletePlannerNote(1)

        Assert.assertEquals(Unit, result)
    }
}

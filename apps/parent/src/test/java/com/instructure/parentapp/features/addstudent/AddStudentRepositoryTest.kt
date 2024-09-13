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
package com.instructure.parentapp.features.addstudent

import com.instructure.canvasapi2.apis.ObserverApi
import com.instructure.canvasapi2.utils.DataResult
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class AddStudentRepositoryTest {

    private lateinit var repository: AddStudentRepository

    private val observerApi: ObserverApi = mockk(relaxed = true)

    @Before
    fun setup() {
        repository = AddStudentRepository(observerApi)
    }

    @Test
    fun `pairStudent should return success`() = runTest {
        coEvery { observerApi.pairStudent(any(), any()) } returns DataResult.Success(Unit)

        val result = repository.pairStudent("pairingCode")

        assert(result is DataResult.Success)
    }

    @Test
    fun `pairStudent should return error`() = runTest {
        coEvery { observerApi.pairStudent(any(), any()) } returns DataResult.Fail()

        val result = repository.pairStudent("pairingCode")

        assert(result is DataResult.Fail)
    }
}
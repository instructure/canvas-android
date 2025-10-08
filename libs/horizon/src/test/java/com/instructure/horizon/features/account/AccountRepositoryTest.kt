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
package com.instructure.horizon.features.account

import com.instructure.canvasapi2.apis.ExperienceAPI
import com.instructure.canvasapi2.apis.UserAPI
import com.instructure.canvasapi2.models.ExperienceSummary
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.DataResult
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Test

class AccountRepositoryTest {
    private val userApi: UserAPI.UsersInterface = mockk(relaxed = true)
    private val experienceAPI: ExperienceAPI = mockk(relaxed = true)

    @Test
    fun `Test successful user details retrieval`() = runTest {
        val user = User(id = 1L, name = "Test User", email = "test@example.com")
        coEvery { userApi.getSelf(any()) } returns DataResult.Success(user)

        val result = getRepository().getUserDetails(false)

        assertEquals(user, result)
    }

    @Test(expected = IllegalStateException::class)
    fun `Test failed user details retrieval throws exception`() = runTest {
        coEvery { userApi.getSelf(any()) } returns DataResult.Fail()

        getRepository().getUserDetails(false)
    }

    @Test
    fun `Test successful experiences retrieval`() = runTest {
        val experiences = ExperienceSummary(availableApps = listOf("app1", "app2"))
        coEvery { experienceAPI.getExperienceSummary(any()) } returns DataResult.Success(experiences)

        val result = getRepository().getExperiences(false)

        assertEquals(2, result.size)
        assertTrue(result.contains("app1"))
        assertTrue(result.contains("app2"))
    }

    @Test
    fun `Test failed experiences retrieval returns empty list`() = runTest {
        coEvery { experienceAPI.getExperienceSummary(any()) } returns DataResult.Fail()

        val result = getRepository().getExperiences(false)

        assertTrue(result.isEmpty())
    }

    private fun getRepository(): AccountRepository {
        return AccountRepository(userApi, experienceAPI)
    }
}

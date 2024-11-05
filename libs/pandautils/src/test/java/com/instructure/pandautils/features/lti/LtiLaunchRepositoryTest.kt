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
package com.instructure.pandautils.features.lti

import com.instructure.canvasapi2.apis.LaunchDefinitionsAPI
import com.instructure.canvasapi2.models.LTITool
import com.instructure.canvasapi2.utils.DataResult
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class LtiLaunchRepositoryTest {

    private val launchDefinitionsApi: LaunchDefinitionsAPI.LaunchDefinitionsInterface = mockk(relaxed = true)

    private val repository = LtiLaunchRepository(launchDefinitionsApi)

    @Test
    fun `Get lti from authentication url throws exception when fails`() = runTest {
        val url = "https://www.instructure.com"
        val result = runCatching { repository.getLtiFromAuthenticationUrl(url) }
        assert(result.isFailure)
    }

    @Test
    fun `Get lti from authentication url returns data when successful`() = runTest {
        val url = "https://www.instructure.com"
        val expected = LTITool()
        coEvery { launchDefinitionsApi.getLtiFromAuthenticationUrl(url, any()) } returns DataResult.Success(expected)

        val result = repository.getLtiFromAuthenticationUrl(url)

        assertEquals(expected, result)
    }
}
/*
 * Copyright (C) 2024 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.instructure.parentapp.features.splash

import com.instructure.canvasapi2.apis.EnrollmentAPI
import com.instructure.canvasapi2.apis.ThemeAPI
import com.instructure.canvasapi2.apis.UserAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.BecomeUserPermission
import com.instructure.canvasapi2.models.CanvasColor
import com.instructure.canvasapi2.models.CanvasTheme
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.LinkHeaders
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test


class SplashRepositoryTest {

    private val themeApi: ThemeAPI.ThemeInterface = mockk(relaxed = true)
    private val userApi: UserAPI.UsersInterface = mockk(relaxed = true)
    private val enrollmentApi: EnrollmentAPI.EnrollmentInterface = mockk(relaxed = true)

    private val repository = SplashRepository(userApi, themeApi, enrollmentApi)

    @Test
    fun `Get students successfully returns data`() = runTest {
        val expected = listOf(User(id = 1L))
        val enrollments = expected.map { Enrollment(observedUser = it) }

        coEvery { enrollmentApi.firstPageObserveeEnrollmentsParent(any()) } returns DataResult.Success(enrollments)

        val result = repository.getStudents()
        assertEquals(expected, result)
    }

    @Test
    fun `Get students with pagination successfully returns data`() = runTest {
        val page1 = listOf(User(id = 1L))
        val enrollments1 = page1.map { Enrollment(observedUser = it) }
        val page2 = listOf(User(id = 2L))
        val enrollments2 = page2.map { Enrollment(observedUser = it) }

        coEvery { enrollmentApi.firstPageObserveeEnrollmentsParent(any()) } returns DataResult.Success(
            enrollments1,
            linkHeaders = LinkHeaders(nextUrl = "page_2_url")
        )
        coEvery { enrollmentApi.getNextPage("page_2_url", any()) } returns DataResult.Success(enrollments2)

        val result = repository.getStudents()
        assertEquals(page1 + page2, result)
    }

    @Test
    fun `Get students returns empty list when enrollments call fails`() = runTest {
        coEvery { enrollmentApi.firstPageObserveeEnrollmentsParent(any()) } returns DataResult.Fail()

        val result = repository.getStudents()
        assertTrue(result.isEmpty())
    }

    @Test
    fun `Get self successfully returns data`() = runTest {
        val expected = User(id = 1L)

        coEvery { userApi.getSelf(any()) } returns DataResult.Success(expected)

        val result = repository.getSelf()
        assertEquals(expected, result)
    }

    @Test
    fun `Get self returns null when call fails`() = runTest {
        coEvery { userApi.getSelf(any()) } returns DataResult.Fail()

        val result = repository.getSelf()
        assertNull(result)
    }

    @Test
    fun `Get colors successfully returns data`() = runTest {
        val expected = CanvasColor()

        coEvery { userApi.getColors(any()) } returns DataResult.Success(expected)

        val result = repository.getColors()
        assertEquals(expected, result)
    }

    @Test
    fun `Get colors returns null when call fails`() = runTest {
        coEvery { userApi.getColors(any()) } returns DataResult.Fail()

        val result = repository.getColors()
        assertNull(result)
    }

    @Test
    fun `Get theme successfully returns data`() = runTest {
        val expected = CanvasTheme("", "", "", "", "", "", "", "")

        coEvery { themeApi.getTheme(any()) } returns DataResult.Success(expected)

        val result = repository.getTheme()
        assertEquals(expected, result)
    }

    @Test
    fun `Get theme returns null when call fails`() = runTest {
        coEvery { themeApi.getTheme(any()) } returns DataResult.Fail()

        val result = repository.getTheme()
        assertNull(result)
    }

    @Test
    fun `GetBecomeUserPermission returns true when call succeeds and returns true`() = runTest {
        coEvery { userApi.getBecomeUserPermission(any<RestParams>()) } returns DataResult.Success(BecomeUserPermission(true))

        val result = repository.getBecomeUserPermission()
        assertTrue(result)
    }

    @Test
    fun `GetBecomeUserPermission returns false when call fails`() = runTest {
        coEvery { userApi.getBecomeUserPermission(any<RestParams>()) } returns DataResult.Fail()

        val result = repository.getBecomeUserPermission()
        assertFalse(result)
    }
}

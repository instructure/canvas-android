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

package com.instructure.parentapp.features.dashboard

import com.instructure.canvasapi2.apis.EnrollmentAPI
import com.instructure.canvasapi2.apis.LaunchDefinitionsAPI
import com.instructure.canvasapi2.apis.UnreadCountAPI
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.models.LaunchDefinition
import com.instructure.canvasapi2.models.UnreadConversationCount
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.LinkHeaders
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test


class DashboardRepositoryTest {

    private val enrollmentApi: EnrollmentAPI.EnrollmentInterface = mockk(relaxed = true)
    private val unreadCountApi: UnreadCountAPI.UnreadCountsInterface = mockk(relaxed = true)
    private val launchDefinitionsApi: LaunchDefinitionsAPI.LaunchDefinitionsInterface = mockk(relaxed = true)

    private val repository = DashboardRepository(enrollmentApi, unreadCountApi, launchDefinitionsApi)

    @Test
    fun `Get students successfully returns data`() = runTest {
        val expected = listOf(User(id = 1L))
        val enrollments = expected.map { Enrollment(observedUser = it) }

        coEvery { enrollmentApi.firstPageObserveeEnrollmentsParent(any()) } returns DataResult.Success(enrollments)

        val result = repository.getStudents(true)
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

        val result = repository.getStudents(true)
        assertEquals(page1 + page2, result)
    }

    @Test
    fun `Get students returns data distinctly and sorted`() = runTest {
        val expected = listOf(User(id = 1L, sortableName = "First"), User(id = 2L, sortableName = "Second"))
        val enrollments = expected.asReversed().map { Enrollment(observedUser = it) }
        val otherEnrollments = listOf(
            Enrollment(user = User(id = 3L)),
            Enrollment(observedUser = User(id = 1L))
        )

        coEvery { enrollmentApi.firstPageObserveeEnrollmentsParent(any()) } returns DataResult.Success(enrollments + otherEnrollments)

        val result = repository.getStudents(true)
        assertEquals(expected, result)
    }

    @Test
    fun `Get unread count return 0 when the request fails`() = runTest {
        coEvery { unreadCountApi.getUnreadConversationCount(any()) } returns DataResult.Fail()

        val result = repository.getUnreadCounts()
        assertEquals(0, result)
    }

    @Test
    fun `Get unread count return 0 when the response cannot be parsed`() = runTest {
        coEvery { unreadCountApi.getUnreadConversationCount(any()) } returns DataResult.Success(UnreadConversationCount(unreadCount = "not a number"))

        val result = repository.getUnreadCounts()
        assertEquals(0, result)
    }

    @Test
    fun `Get unread count return the unread count when the response is successful`() = runTest {
        coEvery { unreadCountApi.getUnreadConversationCount(any()) } returns DataResult.Success(UnreadConversationCount(unreadCount = "42"))

        val result = repository.getUnreadCounts()
        assertEquals(42, result)
    }

    @Test
    fun `Get launch definitions returns empty list when failed`() = runTest {
        coEvery { launchDefinitionsApi.getLaunchDefinitions(any()) } returns DataResult.Fail()

        val result = repository.getLaunchDefinitions()
        assertEquals(emptyList<LaunchDefinition>(), result)
    }

    @Test
    fun `Get launch definitions returns data when successful`() = runTest {
        val expected = listOf(LaunchDefinition("type", 1, "name", null, null, null, null))
        coEvery { launchDefinitionsApi.getLaunchDefinitions(any()) } returns DataResult.Success(expected)

        val result = repository.getLaunchDefinitions()
        assertEquals(expected, result)
    }
}

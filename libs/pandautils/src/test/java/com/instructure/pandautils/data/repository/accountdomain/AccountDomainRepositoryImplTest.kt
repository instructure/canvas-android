/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
 */
package com.instructure.pandautils.data.repository.accountdomain

import com.instructure.canvasapi2.apis.AccountDomainInterface
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.AccountDomain
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.LinkHeaders
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AccountDomainRepositoryImplTest {

    private val api: AccountDomainInterface = mockk(relaxed = true)
    private lateinit var repository: AccountDomainRepositoryImpl

    @Before
    fun setUp() {
        repository = AccountDomainRepositoryImpl(api)
    }

    @Test
    fun `search returns account domains from API`() = runTest {
        val domains = listOf(
            AccountDomain(domain = "school1.instructure.com", name = "School 1"),
            AccountDomain(domain = "school2.instructure.com", name = "School 2")
        )

        coEvery { api.campusSearch("school", any()) } returns DataResult.Success(domains)

        val result = repository.search("school")

        assertEquals(2, result.size)
        assertEquals("school1.instructure.com", result[0].domain)
        assertEquals("school2.instructure.com", result[1].domain)
    }

    @Test
    fun `search returns empty list when API fails`() = runTest {
        coEvery { api.campusSearch("school", any()) } returns DataResult.Fail()

        val result = repository.search("school")

        assertTrue(result.isEmpty())
    }

    @Test
    fun `search returns empty list when API returns no results`() = runTest {
        coEvery { api.campusSearch("xyz", any()) } returns DataResult.Success(emptyList())

        val result = repository.search("xyz")

        assertTrue(result.isEmpty())
    }

    @Test
    fun `search passes correct RestParams`() = runTest {
        val paramsSlot = slot<RestParams>()

        coEvery { api.campusSearch(any(), capture(paramsSlot)) } returns DataResult.Success(emptyList())

        repository.search("school")

        assertTrue(paramsSlot.captured.usePerPageQueryParam)
        assertTrue(paramsSlot.captured.isForceReadFromNetwork)
    }

    @Test
    fun `search passes query to API`() = runTest {
        coEvery { api.campusSearch(any(), any()) } returns DataResult.Success(emptyList())

        repository.search("test query")

        coVerify { api.campusSearch("test query", any()) }
    }

    @Test
    fun `search depaginates results across multiple pages`() = runTest {
        val page1 = listOf(AccountDomain(domain = "school1.instructure.com", name = "School 1"))
        val page2 = listOf(AccountDomain(domain = "school2.instructure.com", name = "School 2"))
        val page3 = listOf(AccountDomain(domain = "school3.instructure.com", name = "School 3"))

        coEvery { api.campusSearch("school", any()) } returns DataResult.Success(
            page1,
            linkHeaders = LinkHeaders(nextUrl = "page2")
        )
        coEvery { api.next("page2", any()) } returns DataResult.Success(
            page2,
            linkHeaders = LinkHeaders(nextUrl = "page3")
        )
        coEvery { api.next("page3", any()) } returns DataResult.Success(page3)

        val result = repository.search("school")

        assertEquals(3, result.size)
        assertEquals("school1.instructure.com", result[0].domain)
        assertEquals("school2.instructure.com", result[1].domain)
        assertEquals("school3.instructure.com", result[2].domain)
    }

    @Test
    fun `search stops depagination when next page fails`() = runTest {
        val page1 = listOf(AccountDomain(domain = "school1.instructure.com", name = "School 1"))

        coEvery { api.campusSearch("school", any()) } returns DataResult.Success(
            page1,
            linkHeaders = LinkHeaders(nextUrl = "page2")
        )
        coEvery { api.next("page2", any()) } returns DataResult.Fail()

        val result = repository.search("school")

        assertEquals(1, result.size)
        assertEquals("school1.instructure.com", result[0].domain)
        coVerify(exactly = 0) { api.next("page3", any()) }
    }
}
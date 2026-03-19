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
package com.instructure.pandautils.domain.usecase.accountdomain

import com.instructure.canvasapi2.models.AccountDomain
import com.instructure.pandautils.data.repository.accountdomain.AccountDomainRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchAccountDomainUseCaseTest {

    private val repository: AccountDomainRepository = mockk(relaxed = true)
    private val useCase = SearchAccountDomainUseCase(repository)

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `execute returns domains from repository`() = runTest {
        val domains = listOf(
            AccountDomain(domain = "school.instructure.com", name = "School"),
            AccountDomain(domain = "university.instructure.com", name = "University")
        )

        coEvery { repository.search("school") } returns domains

        val result = useCase(SearchAccountDomainUseCase.Params("school"))

        assertEquals(2, result.size)
        assertEquals("school.instructure.com", result[0].domain)
        assertEquals("university.instructure.com", result[1].domain)
    }

    @Test
    fun `execute returns empty list when query is shorter than 3 characters`() = runTest {
        val result = useCase(SearchAccountDomainUseCase.Params("ab"))

        assertTrue(result.isEmpty())
        coVerify(exactly = 0) { repository.search(any()) }
    }

    @Test
    fun `execute returns empty list for single character query`() = runTest {
        val result = useCase(SearchAccountDomainUseCase.Params("a"))

        assertTrue(result.isEmpty())
        coVerify(exactly = 0) { repository.search(any()) }
    }

    @Test
    fun `execute returns empty list for empty query`() = runTest {
        val result = useCase(SearchAccountDomainUseCase.Params(""))

        assertTrue(result.isEmpty())
        coVerify(exactly = 0) { repository.search(any()) }
    }

    @Test
    fun `execute calls repository when query is exactly 3 characters`() = runTest {
        coEvery { repository.search("abc") } returns emptyList()

        useCase(SearchAccountDomainUseCase.Params("abc"))

        coVerify(exactly = 1) { repository.search("abc") }
    }

    @Test
    fun `execute passes query to repository`() = runTest {
        coEvery { repository.search(any()) } returns emptyList()

        useCase(SearchAccountDomainUseCase.Params("test university"))

        coVerify(exactly = 1) { repository.search("test university") }
    }
}
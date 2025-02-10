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
 */
package com.instructure.pandautils.features.smartsearch

import com.instructure.canvasapi2.apis.SmartSearchApi
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.SmartSearchContentType
import com.instructure.canvasapi2.models.SmartSearchFilter
import com.instructure.canvasapi2.models.SmartSearchResult
import com.instructure.canvasapi2.models.SmartSearchResultWrapper
import com.instructure.canvasapi2.utils.DataResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SmartSearchRepositoryTest {

    private val smartSearchApi: SmartSearchApi = mockk(relaxed = true)

    @Test
    fun `Smart Search success response`() = runTest {
        val result = SmartSearchResultWrapper(
            listOf(
                SmartSearchResult(
                    contentId = 1L,
                    contentType = SmartSearchContentType.ASSIGNMENT,
                    title = "Assignment 1",
                    htmlUrl = "https://www.instructure.com",
                    relevance = 85,
                    distance = 0.256,
                    body = "This is the body of the assignment"
                ),
                SmartSearchResult(
                    contentId = 2L,
                    contentType = SmartSearchContentType.DISCUSSION_TOPIC,
                    title = "Discussion 1",
                    htmlUrl = "https://www.instructure.com",
                    relevance = 75,
                    distance = 0.256,
                    body = "This is the body of the discussion"
                )
            )
        )
        coEvery { smartSearchApi.smartSearch(any(), any(), any(), any()) } returns DataResult.Success(
            result
        )

        val repository = createRepository()
        val response = repository.smartSearch(1L, "query", filter = listOf(SmartSearchFilter.PAGES, SmartSearchFilter.ANNOUNCEMENTS))

        coVerify {
            smartSearchApi.smartSearch(1L, "query", listOf("pages", "announcements"), RestParams(isForceReadFromNetwork = true))
        }

        assertEquals(result.results, response)
    }

    @Test(expected = IllegalStateException::class)
    fun `Smart Search error`() = runTest {
        coEvery { smartSearchApi.smartSearch(any(), any(), any(), any()) } returns DataResult.Fail()

        val repository = createRepository()

        repository.smartSearch(1L, "query")
    }

    private fun createRepository(): SmartSearchRepository {
        return SmartSearchRepository(smartSearchApi)
    }

    @Test
    fun `Filter result with less than 50 relevance`() = runTest {
        val result = SmartSearchResultWrapper(
            listOf(
                SmartSearchResult(
                    contentId = 1L,
                    contentType = SmartSearchContentType.ASSIGNMENT,
                    title = "Assignment 1",
                    htmlUrl = "https://www.instructure.com",
                    relevance = 85,
                    distance = 0.256,
                    body = "This is the body of the assignment"
                ),
                SmartSearchResult(
                    contentId = 2L,
                    contentType = SmartSearchContentType.DISCUSSION_TOPIC,
                    title = "Discussion 1",
                    htmlUrl = "https://www.instructure.com",
                    relevance = 75,
                    distance = 0.256,
                    body = "This is the body of the discussion"
                ),
                SmartSearchResult(
                    contentId = 3L,
                    contentType = SmartSearchContentType.DISCUSSION_TOPIC,
                    title = "Discussion 2",
                    htmlUrl = "https://www.instructure.com",
                    relevance = 49,
                    distance = 0.256,
                    body = "This is the body of the discussion"
                ),
                SmartSearchResult(
                    contentId = 4L,
                    contentType = SmartSearchContentType.DISCUSSION_TOPIC,
                    title = "Discussion 3",
                    htmlUrl = "https://www.instructure.com",
                    relevance = 50,
                    distance = 0.256,
                    body = "This is the body of the discussion"
                )
            )
        )
        coEvery { smartSearchApi.smartSearch(any(), any(), any(), any()) } returns DataResult.Success(
            result
        )

        val repository = createRepository()
        val response = repository.smartSearch(1L, "query")

        val expected = listOf(result.results[0], result.results[1], result.results[3])

        coVerify {
            smartSearchApi.smartSearch(1L, "query", any(), RestParams(isForceReadFromNetwork = true))
        }

        assertEquals(expected, response)
    }
}
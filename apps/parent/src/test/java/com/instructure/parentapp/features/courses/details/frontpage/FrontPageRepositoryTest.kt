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

package com.instructure.parentapp.features.courses.details.frontpage

import com.instructure.canvasapi2.apis.PageAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Page
import com.instructure.canvasapi2.utils.DataResult
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test


class FrontPageRepositoryTest {

    private val pageApi = mockk<PageAPI.PagesInterface>(relaxed = true)

    private val repository = FrontPageRepository(pageApi)

    @Test
    fun `Get front page successfully returns data`() = runTest {
        val expected = Page(id = 1L)

        coEvery {
            pageApi.getFrontPage(
                CanvasContext.Type.COURSE.apiString,
                1L,
                RestParams(isForceReadFromNetwork = false)
            )
        } returns DataResult.Success(expected)

        val result = repository.loadFrontPage(1L, false)
        Assert.assertEquals(expected, result)
    }

    @Test(expected = IllegalStateException::class)
    fun `Get front page throws exception when fails`() = runTest {
        coEvery {
            pageApi.getFrontPage(
                CanvasContext.Type.COURSE.apiString,
                1L,
                RestParams(isForceReadFromNetwork = true)
            )
        } returns DataResult.Fail()

        repository.loadFrontPage(1L, true)
    }
}

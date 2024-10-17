/*
 * Copyright (C) 2023 - present Instructure, Inc.
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
 *
 */

package com.instructure.pandautils.features.discussion.router

import com.instructure.canvasapi2.apis.DiscussionAPI
import com.instructure.canvasapi2.apis.GroupAPI
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.models.GroupTopicChild
import com.instructure.canvasapi2.utils.DataResult
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DiscussionRouteHelperNetworkDataSourceTest {
    private val discussionApi: DiscussionAPI.DiscussionInterface = mockk(relaxed = true)
    private val groupApi: GroupAPI.GroupInterface = mockk(relaxed = true)

    private val dataSource =
        DiscussionRouteHelperNetworkDataSource(discussionApi, groupApi)

    @Test
    fun `getDiscussionTopicHeader returns correct data`() = runTest {
        val canvasContext = CanvasContext.defaultCanvasContext()
        val expected = DiscussionTopicHeader(1L)

        coEvery { discussionApi.getDiscussionTopicHeader(any(), any(), any(), any()) } returns DataResult.Success(
            expected
        )

        val result = dataSource.getDiscussionTopicHeader(canvasContext, 1L, true)

        assertEquals(expected, result)
    }

    @Test
    fun `getAllGroups returns correct data if group exists`() = runTest {
        val discussionTopicHeader = DiscussionTopicHeader(
            1L, groupTopicChildren = listOf(
                GroupTopicChild(1L, 1L)
            )
        )
        val groups = listOf(Group(1L))

        coEvery { groupApi.getFirstPageGroups(any()) } returns DataResult.Success(groups)
        coEvery { groupApi.getNextPageGroups(any(), any()) } returns DataResult.Success(emptyList())

        val result = dataSource.getAllGroups(discussionTopicHeader, 1L, true)

        assertEquals(groups, result)
    }
}
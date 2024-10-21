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

import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.models.GroupTopicChild
import com.instructure.pandautils.room.offline.facade.DiscussionTopicHeaderFacade
import com.instructure.pandautils.room.offline.facade.GroupFacade
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DiscussionRouteHelperLocalDataSourceTest {
    private val discussionTopicHeaderFacade: DiscussionTopicHeaderFacade = mockk(relaxed = true)
    private val groupFacade: GroupFacade = mockk(relaxed = true)

    private val dataSource = DiscussionRouteHelperLocalDataSource(discussionTopicHeaderFacade, groupFacade)

    @Test
    fun `getDiscussionTopicHeader returns correct data`() = runTest {
        val expected = DiscussionTopicHeader(1L)

        coEvery { discussionTopicHeaderFacade.getDiscussionTopicHeaderById(any()) } returns expected

        val result = dataSource.getDiscussionTopicHeader(mockk(), 1L, true)

        assertEquals(expected, result)
    }

    @Test
    fun `getAllGroups returns correct data if group exists`() = runTest {
        val discussionTopicHeader = DiscussionTopicHeader(1L, groupTopicChildren = listOf(
            GroupTopicChild(1L, 1L)
        ))
        val groups = listOf(Group(1L))

        coEvery { groupFacade.getGroupsByUserId(any()) } returns groups

        val result = dataSource.getAllGroups(discussionTopicHeader, 1L, true)

        assertEquals(groups, result)
    }
}
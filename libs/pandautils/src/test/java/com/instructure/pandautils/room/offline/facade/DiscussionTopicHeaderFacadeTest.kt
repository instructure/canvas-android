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
 */

package com.instructure.pandautils.room.offline.facade

import com.instructure.canvasapi2.models.DiscussionParticipant
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.pandautils.room.offline.daos.DiscussionParticipantDao
import com.instructure.pandautils.room.offline.daos.DiscussionTopicHeaderDao
import com.instructure.pandautils.room.offline.entities.DiscussionParticipantEntity
import com.instructure.pandautils.room.offline.entities.DiscussionTopicHeaderEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test

@ExperimentalCoroutinesApi
class DiscussionTopicHeaderFacadeTest {

    private val discussionTopicHeaderDao: DiscussionTopicHeaderDao = mockk(relaxed = true)
    private val discussionParticipantDao: DiscussionParticipantDao = mockk(relaxed = true)

    private val facade = DiscussionTopicHeaderFacade(discussionTopicHeaderDao, discussionParticipantDao)

    @Test
    fun `Calling insertDiscussion should insert discussion topic header and related entities`() = runTest {
        val discussionParticipant = DiscussionParticipant(id = 1L)
        val discussionTopicHeader = DiscussionTopicHeader(author = discussionParticipant)

        coEvery { discussionParticipantDao.insert(any()) } returns 1L
        coEvery { discussionTopicHeaderDao.insert(any()) } returns 1L

        facade.insertDiscussion(discussionTopicHeader)

        coVerify { discussionParticipantDao.insert(DiscussionParticipantEntity(discussionParticipant)) }
        coVerify { discussionTopicHeaderDao.insert(DiscussionTopicHeaderEntity(discussionTopicHeader)) }
    }

    @Test
    fun `Calling getDiscussionTopicHeaderById should return the discussion topic header with the specified ID`() = runTest {
        val discussionTopicHeaderId = 1L
        val discussionParticipant = DiscussionParticipant(id = 1L, displayName = "displayName")
        val discussionTopicHeader = DiscussionTopicHeader(id = discussionTopicHeaderId, author = discussionParticipant, title = "Title")

        coEvery { discussionParticipantDao.findById(any()) } returns DiscussionParticipantEntity(discussionParticipant)
        coEvery { discussionTopicHeaderDao.findById(any()) } returns DiscussionTopicHeaderEntity(discussionTopicHeader)

        val result = facade.getDiscussionTopicHeaderById(discussionTopicHeaderId)!!

        Assert.assertEquals(discussionParticipant, result.author)
        Assert.assertEquals(discussionTopicHeader, result)
    }
}
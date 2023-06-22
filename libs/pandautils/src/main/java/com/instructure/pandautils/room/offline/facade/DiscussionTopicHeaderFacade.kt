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

import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.pandautils.room.offline.daos.DiscussionParticipantDao
import com.instructure.pandautils.room.offline.daos.DiscussionTopicHeaderDao
import com.instructure.pandautils.room.offline.entities.DiscussionParticipantEntity
import com.instructure.pandautils.room.offline.entities.DiscussionTopicHeaderEntity

class DiscussionTopicHeaderFacade(
    private val discussionTopicHeaderDao: DiscussionTopicHeaderDao,
    private val discussionParticipantDao: DiscussionParticipantDao
) {

    suspend fun insertDiscussion(discussionTopicHeader: DiscussionTopicHeader, courseId: Long): Long {
        discussionTopicHeader.author?.let { discussionParticipantDao.insert(DiscussionParticipantEntity(it)) }
        return discussionTopicHeaderDao.insert(DiscussionTopicHeaderEntity(discussionTopicHeader, courseId))
    }

    suspend fun insertDiscussions(discussionTopicHeaders: List<DiscussionTopicHeader>, courseId: Long) {
        val authors = discussionTopicHeaders
            .map { it.author }
            .filterNotNull()
            .map { DiscussionParticipantEntity(it) }
        discussionParticipantDao.insertAll(authors)

        val discussionEntities = discussionTopicHeaders.map { DiscussionTopicHeaderEntity(it, courseId) }
        discussionTopicHeaderDao.insertAll(discussionEntities)
    }

    suspend fun getDiscussionsForCourse(courseId: Long): List<DiscussionTopicHeader> {
        return discussionTopicHeaderDao.findAllDiscussionsForCourse(courseId)
            .map { discussionTopic ->
                val authorEntity =
                    discussionTopic.authorId?.let { discussionParticipantDao.findById(it) }
                discussionTopic.toApiModel(authorEntity?.toApiModel())
            }
    }

    suspend fun getAnnouncementsForCourse(courseId: Long): List<DiscussionTopicHeader> {
        return discussionTopicHeaderDao.findAllAnnouncementsForCourse(courseId)
            .map { discussionTopic -> createDiscussionApiModel(discussionTopic) }
    }

    suspend fun getDiscussionTopicHeaderById(id: Long): DiscussionTopicHeader? {
        val discussionTopicHeaderEntity = discussionTopicHeaderDao.findById(id)
        return if (discussionTopicHeaderEntity != null) createDiscussionApiModel(discussionTopicHeaderEntity) else null
    }

    private suspend fun createDiscussionApiModel(discussionTopicHeaderEntity: DiscussionTopicHeaderEntity): DiscussionTopicHeader {
        val authorEntity = discussionTopicHeaderEntity.authorId?.let { discussionParticipantDao.findById(it) }
        return discussionTopicHeaderEntity.toApiModel(authorEntity?.toApiModel())
    }
}
package com.instructure.pandautils.room.offline.facade

import com.instructure.canvasapi2.models.DiscussionEntry
import com.instructure.canvasapi2.models.DiscussionParticipant
import com.instructure.canvasapi2.models.DiscussionTopic
import com.instructure.pandautils.room.offline.daos.DiscussionEntryDao
import com.instructure.pandautils.room.offline.daos.DiscussionParticipantDao
import com.instructure.pandautils.room.offline.daos.DiscussionTopicDao
import com.instructure.pandautils.room.offline.entities.DiscussionEntryEntity
import com.instructure.pandautils.room.offline.entities.DiscussionParticipantEntity
import com.instructure.pandautils.room.offline.entities.DiscussionTopicEntity

class DiscussionTopicFacade(
    private val discussionTopicDao: DiscussionTopicDao,
    private val discussionParticipantDao: DiscussionParticipantDao,
    private val discussionEntryDao: DiscussionEntryDao,

) {
    suspend fun insertDiscussionTopic(topicId: Long, discussionTopic: DiscussionTopic) {
        val participantIds = discussionParticipantDao.insertAll(discussionTopic.participants?.map { DiscussionParticipantEntity(it) }.orEmpty())
        val discussionEntryIds = discussionEntryDao.insertAll(discussionTopic.views.map { DiscussionEntryEntity(it) })
        discussionTopicDao.insert(DiscussionTopicEntity(discussionTopic, participantIds, discussionEntryIds, topicId))
    }

    suspend fun getDiscussionTopic(topicId: Long): DiscussionTopic? {
        val topicEntity = discussionTopicDao.findById(topicId)

        val participants = mutableListOf<DiscussionParticipant>()
        topicEntity?.participantIds?.forEach {
            val participant = discussionParticipantDao.findById(it)
            participant?.let { participants.add(it.toApiModel()) }
        }

        val views = mutableListOf<DiscussionEntry>()
        topicEntity?.viewIds?.forEach {
            val view = discussionEntryDao.findById(it)
            view?.let { views.add(it.toApiModel()) }
        }

        return topicEntity?.toApiModel(participants, views)
    }
}
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
    private val discussionEntryDao: DiscussionEntryDao
) {
    suspend fun insertDiscussionTopic(topicId: Long, discussionTopic: DiscussionTopic) {
        discussionParticipantDao.upsertAll(discussionTopic.participants?.map { DiscussionParticipantEntity(it) }.orEmpty())
        val discussionEntryIds = insertDiscussionEntries(discussionTopic.views)
        discussionTopicDao.insert(
            DiscussionTopicEntity(
                discussionTopic,
                discussionTopic.participants?.map { it.id }.orEmpty(),
                discussionEntryIds,
                topicId
            )
        )
    }

    private suspend fun insertDiscussionEntries(entries: List<DiscussionEntry>): List<Long> {
        discussionParticipantDao.upsertAll(entries.mapNotNull { discussionEntry ->
            discussionEntry.author?.let {
                DiscussionParticipantEntity(it)
            }
        })
        val replyIds = mutableListOf<List<Long>>()
        entries.forEach { entry ->
            entry.replies?.let { replyIds.add(insertDiscussionEntries(it)) }
        }
        return discussionEntryDao.insertAll(entries.mapIndexed { index, discussionEntry ->
            DiscussionEntryEntity(discussionEntry, replyIds[index])
        })
    }

    suspend fun getDiscussionTopic(topicId: Long): DiscussionTopic? {
        val topicEntity = discussionTopicDao.findById(topicId)

        val participants = mutableListOf<DiscussionParticipant>()
        topicEntity?.participantIds?.forEach { participantId ->
            val participant = discussionParticipantDao.findById(participantId)
            participant?.let { participants.add(it.toApiModel()) }
        }

        val views = mutableListOf<DiscussionEntry>()
        topicEntity?.viewIds?.forEach { viewId ->
            val entry = getDiscussionEntries(viewId)
            entry?.let { views.add(it) }
        }

        return topicEntity?.toApiModel(participants, views)
    }

    private suspend fun getDiscussionEntries(discussionEntryId: Long): DiscussionEntry? {
        val view = discussionEntryDao.findById(discussionEntryId)
        val author = view?.authorId?.let { discussionParticipantDao.findById(it) }
        val replies = view?.replyIds?.mapNotNull { replyId ->
            getDiscussionEntries(replyId)
        }
        return view?.toApiModel(author = author?.toApiModel(), replyDiscussionEntries = replies?.toMutableList().orEmpty().toMutableList())
    }
}
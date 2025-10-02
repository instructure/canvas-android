package com.instructure.pandautils.room.offline.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.instructure.canvasapi2.models.DiscussionEntry
import com.instructure.canvasapi2.models.DiscussionParticipant
import com.instructure.canvasapi2.models.DiscussionTopic

@Entity
data class DiscussionTopicEntity(
    @PrimaryKey
    val id: Long,
    val unreadEntries: List<Long>,
    val participantIds: List<Long>,
    val viewIds: List<Long>,
) {
    constructor(discussionTopic: DiscussionTopic, participantIds: List<Long>, viewIds: List<Long>, topicId: Long) : this(
        id = topicId,
        unreadEntries = discussionTopic.unreadEntries,
        participantIds = participantIds,
        viewIds = viewIds
    )

    fun toApiModel(participants: List<DiscussionParticipant>, views: List<DiscussionEntry>): DiscussionTopic {
        return DiscussionTopic(
            unreadEntries = unreadEntries.toMutableList(),
            participants = participants,
            unreadEntriesMap = hashMapOf(),
            entryRatings = hashMapOf(),
            views = views.toMutableList().toMutableList()
        )
    }
}
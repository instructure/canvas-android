package com.instructure.student.features.discussion.routing.datasource

import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.Group
import com.instructure.pandautils.room.offline.facade.DiscussionTopicHeaderFacade
import com.instructure.pandautils.room.offline.facade.GroupFacade

class DiscussionRouteHelperStudentLocalDataSource(
    private val discussionTopicHeaderFacade: DiscussionTopicHeaderFacade,
    private val groupFacade: GroupFacade,
): DiscussionRouteHelperStudentDataSource {

    override suspend fun getDiscussionTopicHeader(
        canvasContext: CanvasContext,
        discussionTopicHeaderId: Long,
        forceNetwork: Boolean
    ): DiscussionTopicHeader? {
        return discussionTopicHeaderFacade.getDiscussionTopicHeaderById(discussionTopicHeaderId)
    }

    override suspend fun getAllGroups(
        discussionTopicHeader: DiscussionTopicHeader,
        userId: Long,
        forceNetwork: Boolean
    ): Pair<Group, Long>? {
        val groups = groupFacade.getGroupsByUserId(userId)
        for (group in groups) {
            val groupsMap = discussionTopicHeader.groupTopicChildren.associateBy({ it.groupId }, { it.id })
            if (groupsMap.contains(group.id) && groupsMap[group.id] != null) {
                groupsMap[group.id]?.let { topicHeaderId ->
                    return Pair(group, topicHeaderId)
                }

                return null // There is a group, but not a matching topic header id
            }
        }
        // If we made it to here, there are no groups that match this
        return null
    }

}
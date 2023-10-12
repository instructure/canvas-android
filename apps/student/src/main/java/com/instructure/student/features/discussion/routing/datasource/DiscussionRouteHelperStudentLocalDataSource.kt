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
    ): List<Group> {
        return groupFacade.getGroupsByUserId(userId)
    }

}
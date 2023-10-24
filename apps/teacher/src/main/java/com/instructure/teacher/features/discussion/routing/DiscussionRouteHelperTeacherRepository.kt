package com.instructure.teacher.features.discussion.routing

import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.Group
import com.instructure.pandautils.features.discussion.router.DiscussionRouteHelperNetworkDataSource
import com.instructure.pandautils.features.discussion.router.DiscussionRouteHelperRepository

class DiscussionRouteHelperTeacherRepository(
    private val networkDataSource: DiscussionRouteHelperNetworkDataSource
): DiscussionRouteHelperRepository {
    override suspend fun getEnabledFeaturesForCourse(
        canvasContext: CanvasContext,
        forceNetwork: Boolean
    ): Boolean {
        return networkDataSource.getEnabledFeaturesForCourse(canvasContext, forceNetwork)
    }

    override suspend fun getDiscussionTopicHeader(
        canvasContext: CanvasContext,
        discussionTopicHeaderId: Long,
        forceNetwork: Boolean
    ): DiscussionTopicHeader? {
        return networkDataSource.getDiscussionTopicHeader(canvasContext, discussionTopicHeaderId, forceNetwork)
    }

    override suspend fun getAllGroups(
        discussionTopicHeader: DiscussionTopicHeader,
        userId: Long,
        forceNetwork: Boolean
    ): List<Group> {
        return networkDataSource.getAllGroups(discussionTopicHeader, userId, forceNetwork)
    }
}
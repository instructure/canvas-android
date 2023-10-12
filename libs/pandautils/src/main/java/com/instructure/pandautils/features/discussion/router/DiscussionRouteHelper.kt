package com.instructure.pandautils.features.discussion.router

import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.Group

class DiscussionRouteHelper(
    private val discussionRouteHelperRepository: DiscussionRouteHelperRepository,
) {

    suspend fun isDiscussionRedesignEnabled(canvasContext: CanvasContext): Boolean {
        return discussionRouteHelperRepository.getEnabledFeaturesForCourse(canvasContext, false)
    }

    suspend fun getDiscussionHeader(
        canvasContext: CanvasContext,
        discussionTopicHeaderId: Long
    ): DiscussionTopicHeader? {
       return discussionRouteHelperRepository.getDiscussionTopicHeader(canvasContext, discussionTopicHeaderId, false)
       }

    suspend fun getDiscussionGroup(discussionTopicHeader: DiscussionTopicHeader): Pair<Group, Long>? {
        return discussionRouteHelperRepository.getAllGroups(discussionTopicHeader, false)
    }
}
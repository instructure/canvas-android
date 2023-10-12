package com.instructure.pandautils.features.discussion.router

import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.Group

interface DiscussionRouteHelperRepository {
    suspend fun getEnabledFeaturesForCourse(canvasContext: CanvasContext, forceNetwork: Boolean): Boolean
    suspend fun getDiscussionTopicHeader(canvasContext: CanvasContext, discussionTopicHeaderId: Long, forceNetwork: Boolean): DiscussionTopicHeader?
    suspend fun getAllGroups(discussionTopicHeader: DiscussionTopicHeader, userId: Long, forceNetwork: Boolean): List<Group>
}
package com.instructure.teacher.features.discussion.routing.datasource

import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.Group

interface DiscussionRouteHelperTeacherDataSource {
    suspend fun getDiscussionTopicHeader(canvasContext: CanvasContext, discussionTopicHeaderId: Long, forceNetwork: Boolean): DiscussionTopicHeader?
    suspend fun getAllGroups(discussionTopicHeader: DiscussionTopicHeader, userId: Long, forceNetwork: Boolean): Pair<Group, Long>?
}
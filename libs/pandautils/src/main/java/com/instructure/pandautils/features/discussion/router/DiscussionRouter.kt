package com.instructure.pandautils.features.discussion.router

import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.DiscussionTopicHeader

interface DiscussionRouter {

    fun routeToDiscussion(canvasContext: CanvasContext, isRedesign: Boolean, discussionTopicHeader: DiscussionTopicHeader)
}
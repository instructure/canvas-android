package com.instructure.pandautils.features.discussion.router

import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.DiscussionTopicHeader

sealed class DiscussionRouterAction {
    data class RouteToDiscussion(val canvasContext: CanvasContext, val isRedesignEnabled: Boolean, val discussionTopicHeader: DiscussionTopicHeader): DiscussionRouterAction()
}
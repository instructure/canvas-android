package com.instructure.pandautils.features.discussion.router

import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.Group

sealed class DiscussionRouterAction {
    data class RouteToDiscussion(
        val canvasContext: CanvasContext,
        val isRedesignEnabled: Boolean,
        val discussionTopicHeader: DiscussionTopicHeader,
        val isAnnouncement: Boolean
    ) : DiscussionRouterAction()

    data class RouteToGroupDiscussion(
        val group: Group,
        val id: Long,
        val header: DiscussionTopicHeader,
        val isRedesignEnabled: Boolean
    ) : DiscussionRouterAction()

    data class RouteToDiscussionWebView(
        val canvasContext: CanvasContext,
        val discussionTopicHeaderId: Long
    ) : DiscussionRouterAction()

    data class ShowToast(val toast: String) : DiscussionRouterAction()
}
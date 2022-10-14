package com.instructure.student.features.discussion

import androidx.fragment.app.FragmentActivity
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.Group
import com.instructure.pandautils.features.discussion.details.DiscussionDetailsWebViewFragment
import com.instructure.pandautils.features.discussion.router.DiscussionRouter
import com.instructure.student.fragment.DiscussionDetailsFragment
import com.instructure.student.router.RouteMatcher

class StudentDiscussionRouter(private val fragmentActivity: FragmentActivity) : DiscussionRouter {
    override fun routeToDiscussion(
        canvasContext: CanvasContext,
        isRedesign: Boolean,
        discussionTopicHeader: DiscussionTopicHeader
    ) {
        val route = when {
            isRedesign -> DiscussionDetailsWebViewFragment.makeRoute(canvasContext, discussionTopicHeader)
            else -> DiscussionDetailsFragment.makeRoute(canvasContext, discussionTopicHeader)
        }
        route.apply {
            removePreviousScreen = true
        }

        RouteMatcher.route(fragmentActivity, route)
    }

    override fun routeToGroupDiscussion(group: Group, id: Long) {
        val route = DiscussionDetailsFragment.makeRoute(group, id)
        route.apply {
            removePreviousScreen = true
        }

        RouteMatcher.route(fragmentActivity, route)
    }
}
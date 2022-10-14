package com.instructure.teacher.features.discussion

import androidx.fragment.app.FragmentActivity
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.Group
import com.instructure.interactions.router.Route
import com.instructure.pandautils.features.discussion.details.DiscussionDetailsWebViewFragment
import com.instructure.pandautils.features.discussion.router.DiscussionRouter
import com.instructure.teacher.fragments.DiscussionsDetailsFragment
import com.instructure.teacher.router.RouteMatcher

class TeacherDiscussionRouter(private val activity: FragmentActivity) : DiscussionRouter {
    override fun routeToDiscussion(
        canvasContext: CanvasContext,
        isRedesign: Boolean,
        discussionTopicHeader: DiscussionTopicHeader
    ) {
        val route = when {
            isRedesign -> DiscussionDetailsWebViewFragment.makeRoute(canvasContext, discussionTopicHeader)
            discussionTopicHeader.announcement -> {
                val bundle = DiscussionsDetailsFragment.makeBundle(discussionTopicHeader, true)
                Route(null, DiscussionsDetailsFragment::class.java, canvasContext, bundle)
            }
            else -> {
                val bundle = DiscussionsDetailsFragment.makeBundle(discussionTopicHeader)
                Route(null, DiscussionsDetailsFragment::class.java, canvasContext, bundle)
            }
        }

        route.apply {
            removePreviousScreen = true
        }

        RouteMatcher.route(activity, route)

        activity.finish()
    }

    override fun routeToGroupDiscussion(group: Group, id: Long, header: DiscussionTopicHeader, isRedesign: Boolean) = Unit
}
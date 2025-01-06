package com.instructure.student.features.discussion.routing

import androidx.fragment.app.FragmentActivity
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.Group
import com.instructure.pandautils.features.discussion.details.DiscussionDetailsWebViewFragment
import com.instructure.pandautils.features.discussion.router.DiscussionRouter
import com.instructure.pandautils.utils.NetworkStateProvider
import com.instructure.student.features.discussion.details.DiscussionDetailsFragment
import com.instructure.student.router.RouteMatcher

class StudentDiscussionRouter(
    private val fragmentActivity: FragmentActivity,
    private val networkStateProvider: NetworkStateProvider
) : DiscussionRouter {

    override fun routeToDiscussion(
        canvasContext: CanvasContext,
        isRedesign: Boolean,
        discussionTopicHeader: DiscussionTopicHeader,
        isAnnouncement: Boolean
    ) {
        val route = when {
            isRedesign && networkStateProvider.isOnline() -> DiscussionDetailsWebViewFragment.makeRoute(canvasContext, discussionTopicHeader)
            else -> DiscussionDetailsFragment.makeRoute(canvasContext, discussionTopicHeader)
        }
        route.apply {
            removePreviousScreen = true
        }

        RouteMatcher.route(fragmentActivity, route)
    }

    override fun routeToGroupDiscussion(group: Group, id: Long, header: DiscussionTopicHeader, isRedesign: Boolean) {
        val route = if (isRedesign) {
            DiscussionDetailsWebViewFragment.makeRoute(group, header)
        } else {
            DiscussionDetailsFragment.makeRoute(group, id)
        }
        route.apply {
            removePreviousScreen = true
        }

        RouteMatcher.route(fragmentActivity, route)
    }

    override fun routeToNativeSpeedGrader(courseId: Long, assignmentId: Long, submissionIds: List<Long>, selectedIdx: Int, anonymousGrading: Boolean?) = Unit
}
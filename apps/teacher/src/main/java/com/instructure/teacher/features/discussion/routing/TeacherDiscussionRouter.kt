package com.instructure.teacher.features.discussion.routing

import androidx.fragment.app.FragmentActivity
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.Group
import com.instructure.interactions.router.Route
import com.instructure.interactions.router.RouteContext
import com.instructure.pandautils.features.discussion.details.DiscussionDetailsWebViewFragment
import com.instructure.pandautils.features.discussion.router.DiscussionRouter
import com.instructure.teacher.activities.FullscreenActivity
import com.instructure.teacher.activities.SpeedGraderActivity
import com.instructure.teacher.activities.SpeedGraderActivity.Companion.DISCUSSION_ENTRY_ID
import com.instructure.teacher.router.RouteMatcher

class TeacherDiscussionRouter(private val activity: FragmentActivity) : DiscussionRouter {
    override fun routeToDiscussion(
        canvasContext: CanvasContext,
        isRedesign: Boolean,
        discussionTopicHeader: DiscussionTopicHeader,
        isAnnouncement: Boolean
    ) {
        val route = DiscussionDetailsWebViewFragment.makeRoute(canvasContext, discussionTopicHeader)

        route.apply {
            removePreviousScreen = true
        }

        RouteMatcher.route(activity, route)

        if (activity is FullscreenActivity) activity.finish()
    }

    override fun routeToGroupDiscussion(group: Group, id: Long, header: DiscussionTopicHeader, isRedesign: Boolean) = Unit

    override fun routeToNativeSpeedGrader(
        courseId: Long,
        assignmentId: Long,
        submissionIds: List<Long>,
        selectedIdx: Int,
        anonymousGrading: Boolean?,
        discussionTopicEntryId: Long?,
    ) {
        val bundle = SpeedGraderActivity.makeBundle(
            courseId = courseId,
            assignmentId = assignmentId,
            selectedIdx = selectedIdx,
            anonymousGrading = anonymousGrading,
            filteredSubmissionIds = submissionIds.toLongArray(),
        ).apply {
            discussionTopicEntryId?.let { putLong(DISCUSSION_ENTRY_ID, it) }
        }
        RouteMatcher.route(activity, Route(bundle, RouteContext.SPEED_GRADER))
    }
}
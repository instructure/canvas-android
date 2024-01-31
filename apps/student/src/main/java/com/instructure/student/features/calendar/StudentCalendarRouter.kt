package com.instructure.student.features.calendar

import androidx.fragment.app.FragmentActivity
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.features.calendar.CalendarRouter
import com.instructure.pandautils.features.discussion.router.DiscussionRouterFragment
import com.instructure.student.activity.NavigationActivity
import com.instructure.student.features.assignments.details.AssignmentDetailsFragment
import com.instructure.student.fragment.BasicQuizViewFragment
import com.instructure.student.fragment.CalendarEventFragment
import com.instructure.student.router.RouteMatcher

class StudentCalendarRouter(private val activity: FragmentActivity) : CalendarRouter {
    override fun openNavigationDrawer() {
        (activity as? NavigationActivity)?.openNavigationDrawer()
    }

    override fun openAssignment(canvasContext: CanvasContext, assignmentId: Long) {
        val route = AssignmentDetailsFragment.makeRoute(canvasContext, assignmentId)
        RouteMatcher.route(activity, route)
    }

    override fun openDiscussion(canvasContext: CanvasContext, discussionId: Long) {
        val route = DiscussionRouterFragment.makeRoute(canvasContext, discussionId)
        RouteMatcher.route(activity, route)
    }

    override fun openQuiz(canvasContext: CanvasContext, htmlUrl: String) {
        if (!RouteMatcher.canRouteInternally(activity, htmlUrl, ApiPrefs.domain, true)) {
            val route = BasicQuizViewFragment.makeRoute(canvasContext, htmlUrl)
            RouteMatcher.route(activity, route)
        }
    }

    override fun openCalendarEvent(canvasContext: CanvasContext, eventId: Long) {
        val route = CalendarEventFragment.makeRoute(canvasContext, eventId)
        RouteMatcher.route(activity, route)
    }
}
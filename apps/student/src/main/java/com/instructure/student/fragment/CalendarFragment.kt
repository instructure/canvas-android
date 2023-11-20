/*
 * Copyright (C) 2020 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.instructure.student.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import com.instructure.canvasapi2.models.PlannableType
import com.instructure.canvasapi2.models.PlannerItem
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.interactions.router.Route
import com.instructure.pandautils.analytics.SCREEN_VIEW_CALENDAR
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.features.discussion.router.DiscussionRouterFragment
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.student.R
import com.instructure.student.activity.NavigationActivity
import com.instructure.student.features.assignments.details.AssignmentDetailsFragment
import com.instructure.student.flutterChannels.FlutterComm
import com.instructure.student.router.RouteMatcher
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel

@ScreenView(SCREEN_VIEW_CALENDAR)
@PageView(url = "calendar")
class CalendarFragment : ParentFragment() {
    override fun title() = getString(R.string.calendar)

    override fun applyTheme() {
        val color = FlutterComm.statusBarColor.takeUnless { it == 0 } ?: ThemePrefs.primaryColor
        if (color == Color.WHITE) {
            ViewStyler.setStatusBarLight(requireActivity())
        } else {
            ViewStyler.setStatusBarDark(requireActivity(), color)
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        flutterFragment?.onHiddenChanged(hidden)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_calendar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        flutterFragment?.calendarScreenChannel?.let { setupChannelCallbacks(it) }
        super.onViewCreated(view, savedInstanceState)
    }

    private fun setupChannelCallbacks(channel: CalendarScreenChannel) {
        channel.onRouteToItem = ::routeToItem
        channel.onOpenDrawer = ::openDrawer
        channel.onShowDialog = ::showDialog
    }

    private fun openDrawer() {
        (navigation as? NavigationActivity?)?.openNavigationDrawer()
    }

    private fun routeToItem(item: PlannerItem) {
        val route: Route? = when (item.plannableType) {
            PlannableType.ASSIGNMENT -> {
                AssignmentDetailsFragment.makeRoute(item.canvasContext, item.plannable.id)
            }
            PlannableType.DISCUSSION_TOPIC -> {
                DiscussionRouterFragment.makeRoute(item.canvasContext, item.plannable.id)
            }
            PlannableType.QUIZ -> {
                if (item.plannable.assignmentId != null) {
                    // This is a quiz assignment, go to the assignment page
                    AssignmentDetailsFragment.makeRoute(item.canvasContext, item.plannable.assignmentId!!)
                } else {
                    var htmlUrl = item.htmlUrl.orEmpty()
                    if (htmlUrl.startsWith('/')) htmlUrl = ApiPrefs.fullDomain + htmlUrl
                    if (!RouteMatcher.canRouteInternally(requireActivity(), htmlUrl, ApiPrefs.domain, true)) {
                        BasicQuizViewFragment.makeRoute(item.canvasContext, htmlUrl)
                    } else null
                }
            }
            PlannableType.CALENDAR_EVENT -> {
                CalendarEventFragment.makeRoute(item.canvasContext, item.plannable.id)
            }
            else -> {
                // This is a type that we don't handle - do nothing
                null
            }
        }

        route?.let { RouteMatcher.route(requireActivity(), it) }
    }

    private fun showDialog(call: MethodCall, result: MethodChannel.Result) {
        AlertDialog.Builder(requireActivity(), R.style.AccentDialogTheme)
            .setTitle(call.argument<String>("title"))
            .setMessage(call.argument<String>("message"))
            .setPositiveButton(call.argument<String>("positiveButtonText")) { _, _ -> result.success(true) }
            .setNegativeButton(call.argument<String>("negativeButtonText")) { _, _ -> result.success(false) }
            .create()
            .show()
    }

    override fun handleBackPressed(): Boolean = flutterFragment?.handleBackPressed() ?: false

    private val flutterFragment: FlutterCalendarFragment?
        get() = (childFragmentManager.findFragmentById(R.id.flutter_calendar_fragment) as? FlutterCalendarFragment?)

    companion object {
        fun newInstance(route: Route) = if (validRoute(route)) CalendarFragment() else null

        private fun validRoute(route: Route) = route.primaryClass == CalendarFragment::class.java

        fun makeRoute() = Route(CalendarFragment::class.java, null)
    }
}

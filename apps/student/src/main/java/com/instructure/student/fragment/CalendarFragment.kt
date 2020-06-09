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
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.instructure.canvasapi2.models.PlannerItem
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.interactions.router.Route
import com.instructure.pandautils.utils.TelemetryUtils
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.student.R
import com.instructure.student.activity.NavigationActivity
import com.instructure.student.flutterChannels.FlutterComm
import com.instructure.student.mobius.assignmentDetails.ui.AssignmentDetailsFragment
import com.instructure.student.router.RouteMatcher
import kotlinx.android.extensions.CacheImplementation
import kotlinx.android.extensions.ContainerOptions

@PageView(url = "calendar")
@ContainerOptions(cache = CacheImplementation.NO_CACHE)
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

    override fun onCreate(savedInstanceState: Bundle?) {
        TelemetryUtils.setInteractionName(this::class.java.simpleName)
        super.onCreate(savedInstanceState)
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
    }

    private fun openDrawer() {
        (navigation as? NavigationActivity?)?.openNavigationDrawer()
    }

    private fun routeToItem(item: PlannerItem) {
        val route: Route? = when (item.plannableType) {
            "assignment" -> {
                AssignmentDetailsFragment.makeRoute(item.canvasContext, item.plannable.id)
            }
            "announcement", "discussion_topic" -> {
                DiscussionDetailsFragment.makeRoute(item.canvasContext, item.plannable.id, title = item.plannable.title)
            }
            "quiz" -> {
                if (item.plannable.assignmentId != null) {
                    // This is a quiz assignment, go to the assignment page
                    AssignmentDetailsFragment.makeRoute(item.canvasContext, item.plannable.id)
                } else {
                    var htmlUrl = item.htmlUrl.orEmpty()
                    if (htmlUrl.startsWith('/')) htmlUrl = ApiPrefs.fullDomain + htmlUrl
                    if (!RouteMatcher.canRouteInternally(requireActivity(), htmlUrl, ApiPrefs.domain, true)) {
                        BasicQuizViewFragment.makeRoute(item.canvasContext, htmlUrl)
                    } else null
                }
            }
            "calendar_event" -> {
                CalendarEventFragment.makeRoute(item.canvasContext, item.plannable.id)
            }
            else -> {
                // This is a type that we don't handle - do nothing
                null
            }
        }

        route?.let { RouteMatcher.route(requireContext(), it) }
    }

    override fun handleBackPressed(): Boolean = flutterFragment?.handleBackPressed() ?: false

    private val flutterFragment: FlutterCalendarFragment?
        get() = (childFragmentManager.findFragmentById(R.id.flutter_calendar_fragment) as? FlutterCalendarFragment?)

    companion object {
        @JvmStatic
        fun newInstance(route: Route) = if (validRoute(route)) CalendarFragment() else null

        private fun validRoute(route: Route) = route.primaryClass == CalendarFragment::class.java

        fun makeRoute() = Route(CalendarFragment::class.java, null)
    }
}

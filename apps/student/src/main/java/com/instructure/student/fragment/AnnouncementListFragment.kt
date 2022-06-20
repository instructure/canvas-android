/*
 * Copyright (C) 2018 - present Instructure, Inc.
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

import com.instructure.student.R
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.interactions.router.Route
import com.instructure.pandautils.analytics.SCREEN_VIEW_ANNOUNCEMENT_LIST
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.utils.makeBundle

@ScreenView(SCREEN_VIEW_ANNOUNCEMENT_LIST)
@PageView(url = "{canvasContext}/announcements")
class AnnouncementListFragment : DiscussionListFragment() {

    override val isAnnouncement: Boolean
        get() = true

    override fun title(): String = getString(R.string.announcements)

    companion object {
        fun newInstance(route: Route) =
                if (validateRoute(route)) {
                    AnnouncementListFragment().apply {
                        arguments = route.canvasContext!!.makeBundle(route.arguments)
                    }
                } else null

        fun makeRoute(canvasContext: CanvasContext?) = Route(AnnouncementListFragment::class.java, canvasContext)

        private fun validateRoute(route: Route) = route.canvasContext != null
    }
}

/*
 * Copyright (C) 2017 - present Instructure, Inc.
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
package com.instructure.teacher.fragments

import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.utils.pageview.PageViewUrlParam
import com.instructure.pandautils.analytics.SCREEN_VIEW_ANNOUNCEMENT_LIST
import com.instructure.pandautils.analytics.ScreenView

@ScreenView(SCREEN_VIEW_ANNOUNCEMENT_LIST)
class AnnouncementListFragment : DiscussionsListFragment() {

    @PageViewUrlParam("type")
    override fun makePageViewUrl(): String = "announcements"

    companion object {
        fun newInstance(canvasContext: CanvasContext) = AnnouncementListFragment().apply {
            this.canvasContext = canvasContext
            isAnnouncements = true
        }
    }
}

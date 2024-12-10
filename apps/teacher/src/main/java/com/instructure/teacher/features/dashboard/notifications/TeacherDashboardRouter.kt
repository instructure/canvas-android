/*
 * Copyright (C) 2021 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.teacher.features.dashboard.notifications

import androidx.fragment.app.FragmentActivity
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.interactions.router.Route
import com.instructure.pandautils.features.dashboard.notifications.DashboardRouter
import com.instructure.pandautils.fragments.HtmlContentFragment
import com.instructure.teacher.fragments.FileListFragment
import com.instructure.teacher.router.RouteMatcher

class TeacherDashboardRouter(private val activity: FragmentActivity) : DashboardRouter {
    override fun routeToGlobalAnnouncement(subject: String, message: String) {
        val args = HtmlContentFragment.makeBundle(title = subject, html = message, darkToolbar = true)
        val route = Route(HtmlContentFragment::class.java, null, args)
        RouteMatcher.route(activity, route)
    }

    override fun routeToSubmissionDetails(canvasContext: CanvasContext, assignmentId: Long, attemptId: Long) {}

    override fun routeToMyFiles(canvasContext: CanvasContext, folderId: Long) {
        val args = FileListFragment.makeBundle(canvasContext)
        RouteMatcher.route(
            activity,
            Route(FileListFragment::class.java, canvasContext, args)
        )
    }

    override fun routeToSyncProgress() = Unit
}
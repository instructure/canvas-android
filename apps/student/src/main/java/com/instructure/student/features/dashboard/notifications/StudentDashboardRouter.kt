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

package com.instructure.student.features.dashboard.notifications

import android.content.Intent
import androidx.fragment.app.FragmentActivity
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.features.dashboard.notifications.DashboardRouter
import com.instructure.pandautils.features.offline.sync.progress.SyncProgressFragment
import com.instructure.pandautils.features.dashboard.customize.CustomizeDashboardFragment
import com.instructure.student.activity.LoginActivity
import com.instructure.student.features.files.list.FileListFragment
import com.instructure.student.fragment.InternalWebviewFragment
import com.instructure.student.mobius.assignmentDetails.submissionDetails.ui.SubmissionDetailsRepositoryFragment
import com.instructure.student.router.RouteMatcher
import com.jakewharton.processphoenix.ProcessPhoenix

class StudentDashboardRouter(private val activity: FragmentActivity) : DashboardRouter {

    override fun routeToGlobalAnnouncement(subject: String, message: String) {
        RouteMatcher.route(
            activity,
            InternalWebviewFragment.makeRoute(
                "",
                subject,
                false,
                message,
                allowUnsupportedRouting = false
            )
        )
    }

    override fun routeToSubmissionDetails(canvasContext: CanvasContext, assignmentId: Long, attemptId: Long) {
        RouteMatcher.route(
            activity,
            SubmissionDetailsRepositoryFragment.makeRoute(canvasContext, assignmentId, initialSelectedSubmissionAttempt = attemptId)
        )
    }

    override fun routeToMyFiles(canvasContext: CanvasContext, folderId: Long) {
        RouteMatcher.route(
            activity,
            FileListFragment.makeRoute(canvasContext, folderId)
        )
    }

    override fun routeToSyncProgress() {
        RouteMatcher.route(
            activity,
            SyncProgressFragment.makeRoute()
        )
    }

    override fun routeToCustomizeDashboard() {
        RouteMatcher.route(
            activity,
            CustomizeDashboardFragment.makeRoute()
        )
    }

    override fun restartApp() {
        val startupIntent = Intent(ContextKeeper.appContext, LoginActivity::class.java)
        startupIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        ProcessPhoenix.triggerRebirth(activity, startupIntent)
    }
}
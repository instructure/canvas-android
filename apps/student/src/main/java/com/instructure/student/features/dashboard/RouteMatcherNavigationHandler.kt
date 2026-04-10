/*
 * Copyright (C) 2026 - present Instructure, Inc.
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

package com.instructure.student.features.dashboard

import android.content.Intent
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import androidx.fragment.app.FragmentActivity
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Recipient
import com.instructure.pandares.R
import com.instructure.pandautils.features.assignments.details.AssignmentDetailsFragment
import com.instructure.pandautils.features.calendartodo.createupdate.CreateUpdateToDoFragment
import com.instructure.pandautils.features.dashboard.DashboardNavigationEvent
import com.instructure.pandautils.features.dashboard.DashboardNavigationHandler
import com.instructure.pandautils.features.dashboard.customize.CustomizeDashboardFragment
import com.instructure.pandautils.features.dashboard.edit.EditDashboardFragment
import com.instructure.pandautils.features.dashboard.widget.courses.customize.CustomizeCourseFragment
import com.instructure.pandautils.features.discussion.router.DiscussionRouterFragment
import com.instructure.pandautils.features.inbox.compose.InboxComposeFragment
import com.instructure.pandautils.features.inbox.utils.InboxComposeOptions
import com.instructure.pandautils.features.inbox.utils.InboxComposeOptionsDefaultValues
import com.instructure.pandautils.features.inbox.utils.InboxComposeOptionsMode
import com.instructure.pandautils.features.offline.offlinecontent.OfflineContentFragment
import com.instructure.pandautils.features.offline.sync.progress.SyncProgressFragment
import com.instructure.pandautils.features.shareextension.WORKER_ID
import com.instructure.pandautils.utils.color
import com.instructure.student.features.coursebrowser.CourseBrowserFragment
import com.instructure.student.features.files.list.FileListFragment
import com.instructure.student.features.shareextension.StudentShareExtensionActivity
import com.instructure.student.fragment.AnnouncementListFragment
import com.instructure.student.fragment.InternalWebviewFragment
import com.instructure.student.mobius.assignmentDetails.submissionDetails.ui.SubmissionDetailsRepositoryFragment
import com.instructure.student.router.RouteMatcher

class RouteMatcherNavigationHandler(
    private val activity: FragmentActivity
) : DashboardNavigationHandler {

    override fun handleCoursesNavigation(event: DashboardNavigationEvent.Courses) {
        when (event) {
            is DashboardNavigationEvent.Courses.NavigateToCourse -> {
                RouteMatcher.route(activity, CourseBrowserFragment.makeRoute(event.course))
            }
            is DashboardNavigationEvent.Courses.NavigateToGroup -> {
                RouteMatcher.route(activity, CourseBrowserFragment.makeRoute(event.group))
            }
            is DashboardNavigationEvent.Courses.ManageOfflineContent -> {
                RouteMatcher.route(activity, OfflineContentFragment.makeRoute(event.course))
            }
            is DashboardNavigationEvent.Courses.CustomizeCourse -> {
                RouteMatcher.route(activity, CustomizeCourseFragment.makeRoute(event.course))
            }
            DashboardNavigationEvent.Courses.NavigateToAllCourses -> {
                RouteMatcher.route(activity, EditDashboardFragment.makeRoute())
            }
            is DashboardNavigationEvent.Courses.NavigateToAnnouncement -> {
                RouteMatcher.route(
                    activity,
                    DiscussionRouterFragment.makeRoute(event.course, event.announcement, isAnnouncement = true)
                )
            }
            is DashboardNavigationEvent.Courses.NavigateToAnnouncementList -> {
                RouteMatcher.route(activity, AnnouncementListFragment.makeRoute(event.course))
            }
            is DashboardNavigationEvent.Courses.NavigateToGroupMessage -> {
                val group = event.group
                val allInGroupRecipient = Recipient(
                    stringId = group.contextId,
                    name = activity.getString(R.string.all_recipients_in_selected_context, group.name),
                    userCount = group.users.size
                )

                val options = InboxComposeOptions(
                    mode = InboxComposeOptionsMode.NEW_MESSAGE,
                    defaultValues = InboxComposeOptionsDefaultValues(
                        contextCode = group.contextId,
                        contextName = group.name,
                        recipients = listOf(allInGroupRecipient)
                    )
                )
                RouteMatcher.route(activity, InboxComposeFragment.makeRoute(options))
            }
        }
    }

    override fun handleTodoNavigation(event: DashboardNavigationEvent.Todo) {
        when (event) {
            is DashboardNavigationEvent.Todo.NavigateToTodo -> {
                RouteMatcher.routeUrl(activity, event.htmlUrl)
            }
            is DashboardNavigationEvent.Todo.CreateTodo -> {
                val route = CreateUpdateToDoFragment.makeRoute(event.initialDateString)
                RouteMatcher.route(activity, route)
            }
        }
    }

    override fun handleForecastNavigation(event: DashboardNavigationEvent.Forecast) {
        when (event) {
            is DashboardNavigationEvent.Forecast.NavigateToAssignment -> {
                val canvasContext = Course(id = event.courseId)
                val route = AssignmentDetailsFragment.makeRoute(canvasContext, event.assignmentId)
                RouteMatcher.route(activity, route)
            }
            is DashboardNavigationEvent.Forecast.NavigateToPlannerItem -> {
                RouteMatcher.routeUrl(activity, event.htmlUrl)
            }
        }
    }

    override fun handleProgressNavigation(event: DashboardNavigationEvent.Progress) {
        when (event) {
            is DashboardNavigationEvent.Progress.OpenProgressDialog -> {
                val intent = Intent(activity, StudentShareExtensionActivity::class.java)
                intent.putExtra(WORKER_ID, event.workerId)
                activity.startActivity(intent)
            }
            is DashboardNavigationEvent.Progress.NavigateToSubmissionDetails -> {
                RouteMatcher.route(
                    activity,
                    SubmissionDetailsRepositoryFragment.makeRoute(
                        event.course,
                        event.assignmentId,
                        initialSelectedSubmissionAttempt = event.attemptId
                    )
                )
            }
            is DashboardNavigationEvent.Progress.NavigateToMyFiles -> {
                RouteMatcher.route(
                    activity,
                    FileListFragment.makeRoute(event.user, event.folderId)
                )
            }
            DashboardNavigationEvent.Progress.OpenSyncProgress -> {
                RouteMatcher.route(activity, SyncProgressFragment.makeRoute())
            }
        }
    }

    override fun handleConferencesNavigation(event: DashboardNavigationEvent.Conferences) {
        when (event) {
            is DashboardNavigationEvent.Conferences.LaunchConference -> {
                val colorScheme = CustomTabColorSchemeParams.Builder()
                    .setToolbarColor(event.canvasContext.color)
                    .build()
                CustomTabsIntent.Builder()
                    .setDefaultColorSchemeParams(colorScheme)
                    .setShowTitle(true)
                    .build()
                    .launchUrl(activity, event.url.toUri())
            }
        }
    }

    override fun handleDashboardNavigation(event: DashboardNavigationEvent.Dashboard) {
        when (event) {
            is DashboardNavigationEvent.Dashboard.NavigateToGlobalAnnouncement -> {
                RouteMatcher.route(
                    activity,
                    InternalWebviewFragment.makeRoute(
                        "",
                        event.subject,
                        false,
                        event.message,
                        allowUnsupportedRouting = false
                    )
                )
            }
            DashboardNavigationEvent.Dashboard.NavigateToManageOfflineContent -> {
                RouteMatcher.route(activity, OfflineContentFragment.makeRoute())
            }
            DashboardNavigationEvent.Dashboard.NavigateToCustomizeDashboard -> {
                RouteMatcher.route(activity, CustomizeDashboardFragment.makeRoute())
            }
        }
    }
}

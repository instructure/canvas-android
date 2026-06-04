/*
 * Copyright (C) 2026 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

package com.instructure.ngc.navigation

import android.util.Log
import androidx.navigation.NavController
import com.instructure.pandautils.features.dashboard.DashboardNavigationEvent
import com.instructure.pandautils.features.dashboard.DashboardNavigationHandler

/**
 * Navigation handler for the NGC (New Generation Canvas) Compose-based dashboard.
 *
 * This handler uses Jetpack Compose Navigation for navigating between NGC screens.
 *
 * As NGC screens are implemented, this handler should be updated to use NavController
 * navigation to navigate to the actual screens.
 */
class NGCComposeNavigationHandler(
    private val navController: NavController
) : DashboardNavigationHandler {

    override fun handleCoursesNavigation(event: DashboardNavigationEvent.Courses) {
        when (event) {
            is DashboardNavigationEvent.Courses.NavigateToCourse -> {
                navController.navigate(NGCNavigationRoute.CourseHome.createRoute(event.course.id))
            }
            is DashboardNavigationEvent.Courses.NavigateToGroup -> {
                Log.d(TAG, "NavigateToGroup: groupId=${event.group.id}, groupName=${event.group.name}")
            }
            is DashboardNavigationEvent.Courses.ManageOfflineContent -> {
                Log.d(TAG, "ManageOfflineContent: courseId=${event.course.id}")
            }
            is DashboardNavigationEvent.Courses.CustomizeCourse -> {
                Log.d(TAG, "CustomizeCourse: courseId=${event.course.id}")
            }
            DashboardNavigationEvent.Courses.NavigateToAllCourses -> {
                Log.d(TAG, "NavigateToAllCourses")
            }
            is DashboardNavigationEvent.Courses.NavigateToAnnouncement -> {
                Log.d(TAG, "NavigateToAnnouncement: courseId=${event.course.id}, announcementId=${event.announcement.id}")
            }
            is DashboardNavigationEvent.Courses.NavigateToAnnouncementList -> {
                Log.d(TAG, "NavigateToAnnouncementList: courseId=${event.course.id}")
            }
            is DashboardNavigationEvent.Courses.NavigateToGroupMessage -> {
                Log.d(TAG, "NavigateToGroupMessage: groupId=${event.group.id}, groupName=${event.group.name}")
            }
        }
    }

    override fun handleTodoNavigation(event: DashboardNavigationEvent.Todo) {
        when (event) {
            is DashboardNavigationEvent.Todo.NavigateToTodo -> {
                Log.d(TAG, "NavigateToTodo: htmlUrl=${event.htmlUrl}")
            }
            is DashboardNavigationEvent.Todo.CreateTodo -> {
                Log.d(TAG, "CreateTodo: initialDateString=${event.initialDateString}")
            }
        }
    }

    override fun handleForecastNavigation(event: DashboardNavigationEvent.Forecast) {
        when (event) {
            is DashboardNavigationEvent.Forecast.NavigateToAssignment -> {
                Log.d(TAG, "NavigateToAssignment: courseId=${event.courseId}, assignmentId=${event.assignmentId}")
            }
            is DashboardNavigationEvent.Forecast.NavigateToPlannerItem -> {
                Log.d(TAG, "NavigateToPlannerItem: htmlUrl=${event.htmlUrl}")
            }
        }
    }

    override fun handleProgressNavigation(event: DashboardNavigationEvent.Progress) {
        when (event) {
            is DashboardNavigationEvent.Progress.OpenProgressDialog -> {
                Log.d(TAG, "OpenProgressDialog: workerId=${event.workerId}")
            }
            is DashboardNavigationEvent.Progress.NavigateToSubmissionDetails -> {
                Log.d(TAG, "NavigateToSubmissionDetails: courseId=${event.course.id}, assignmentId=${event.assignmentId}, attemptId=${event.attemptId}")
            }
            is DashboardNavigationEvent.Progress.NavigateToMyFiles -> {
                Log.d(TAG, "NavigateToMyFiles: userId=${event.user.id}, folderId=${event.folderId}")
            }
            DashboardNavigationEvent.Progress.OpenSyncProgress -> {
                Log.d(TAG, "OpenSyncProgress")
            }
        }
    }

    override fun handleConferencesNavigation(event: DashboardNavigationEvent.Conferences) {
        when (event) {
            is DashboardNavigationEvent.Conferences.LaunchConference -> {
                Log.d(TAG, "LaunchConference: url=${event.url}, contextId=${event.canvasContext.id}")
            }
        }
    }

    override fun handleDashboardNavigation(event: DashboardNavigationEvent.Dashboard) {
        when (event) {
            is DashboardNavigationEvent.Dashboard.NavigateToGlobalAnnouncement -> {
                Log.d(TAG, "NavigateToGlobalAnnouncement: subject=${event.subject}")
            }
            DashboardNavigationEvent.Dashboard.NavigateToManageOfflineContent -> {
                Log.d(TAG, "NavigateToManageOfflineContent")
            }
            DashboardNavigationEvent.Dashboard.NavigateToCustomizeDashboard -> {
                Log.d(TAG, "NavigateToCustomizeDashboard")
            }
        }
    }

    companion object {
        private const val TAG = "NgcComposeNavHandler"
    }
}

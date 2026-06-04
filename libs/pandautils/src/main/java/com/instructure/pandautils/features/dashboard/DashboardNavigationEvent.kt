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

package com.instructure.pandautils.features.dashboard

import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.models.User
import java.util.UUID

sealed interface DashboardNavigationEvent {

    // Courses Widget Events
    sealed interface Courses : DashboardNavigationEvent {
        data class NavigateToCourse(val course: Course) : Courses
        data class NavigateToGroup(val group: Group) : Courses
        data class ManageOfflineContent(val course: Course) : Courses
        data class CustomizeCourse(val course: Course) : Courses
        data object NavigateToAllCourses : Courses
        data class NavigateToAnnouncement(val course: Course, val announcement: DiscussionTopicHeader) : Courses
        data class NavigateToAnnouncementList(val course: Course) : Courses
        data class NavigateToGroupMessage(val group: Group) : Courses
    }

    // Todo Widget Events
    sealed interface Todo : DashboardNavigationEvent {
        data class NavigateToTodo(val htmlUrl: String) : Todo
        data class CreateTodo(val initialDateString: String?) : Todo
    }

    // Forecast Widget Events
    sealed interface Forecast : DashboardNavigationEvent {
        data class NavigateToAssignment(val assignmentId: Long, val courseId: Long) : Forecast
        data class NavigateToPlannerItem(val htmlUrl: String) : Forecast
    }

    // Progress Widget Events
    sealed interface Progress : DashboardNavigationEvent {
        data class OpenProgressDialog(val workerId: UUID) : Progress
        data class NavigateToSubmissionDetails(val course: Course, val assignmentId: Long, val attemptId: Long) : Progress
        data class NavigateToMyFiles(val user: User, val folderId: Long) : Progress
        data object OpenSyncProgress : Progress
    }

    // Conferences Widget Events
    sealed interface Conferences : DashboardNavigationEvent {
        data class LaunchConference(val canvasContext: CanvasContext, val url: String) : Conferences
    }

    // Dashboard-level Events (not tied to specific widgets)
    sealed interface Dashboard : DashboardNavigationEvent {
        data class NavigateToGlobalAnnouncement(val subject: String, val message: String) : Dashboard
        data object NavigateToManageOfflineContent : Dashboard
        data object NavigateToCustomizeDashboard : Dashboard
    }
}

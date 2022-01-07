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

package com.instructure.pandautils.features.dashboard.notifications

import androidx.annotation.DrawableRes
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Conference
import com.instructure.pandautils.mvvm.ItemViewModel

data class DashboardNotificationsViewData(
    val items: List<ItemViewModel>
)

data class InvitationViewData(
    val title: String,
    val description: String,
    val enrollmentId: Long,
    val courseId: Long
)

data class ConferenceViewData(
    val subtitle: String?,
    val conference: Conference
)

data class AnnouncementViewData(
    val id: Long,
    val subject: String,
    val message: String,
    val color: String,
    @DrawableRes val icon: Int
)

sealed class DashboardNotificationsActions {
    data class ShowToast(val toast: String): DashboardNotificationsActions()
    data class LaunchConference(val canvasContext: CanvasContext, val url: String): DashboardNotificationsActions()
    data class OpenAnnouncement(val subject: String, val message: String): DashboardNotificationsActions()
}

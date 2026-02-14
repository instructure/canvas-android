/*
 * Copyright (C) 2025 - present Instructure, Inc.
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

package com.instructure.pandautils.features.dashboard.widget

data class WidgetMetadata(
    val id: String,
    val position: Int,
    val isVisible: Boolean,
    val isEditable: Boolean = true
) {
    companion object {
        const val WIDGET_ID_PROGRESS = "progress"
        const val WIDGET_ID_GLOBAL = "global"
        const val WIDGET_ID_CONFERENCES = "conferences"
        const val WIDGET_ID_COURSE_INVITATIONS = "course_invitations"
        const val WIDGET_ID_INSTITUTIONAL_ANNOUNCEMENTS = "institutional_announcements"
        const val WIDGET_ID_WELCOME = "welcome"
        const val WIDGET_ID_COURSES = "courses"
        const val WIDGET_ID_FORECAST = "forecast"
        const val WIDGET_ID_TODO = "todo"
    }
}
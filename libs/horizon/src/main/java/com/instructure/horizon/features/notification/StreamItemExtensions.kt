/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.horizon.features.notification

import com.instructure.canvasapi2.models.StreamItem

internal fun StreamItem.isNotificationItemScored(): Boolean {
    return this.grade != null || this.score != -1.0
}

internal fun StreamItem.isDueDateNotification(): Boolean {
    return this.notificationCategory == "Due Date"
}

internal fun StreamItem.isCourseNotification(): Boolean {
    return this.type == "Announcement"
}
/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
package com.instructure.pandautils.utils

import android.net.Uri
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.canvasapi2.utils.toSimpleDate
import java.util.Date

val ScheduleItem.dueAt: Date?
    get() {
        return if (this.isAllDay) {
            this.allDayDate ?: this.startAt.toSimpleDate()
        } else {
            this.startAt.toSimpleDate() ?: this.allDayDate
        }
    }

val ScheduleItem.eventHtmlUrl: String?
    get() {
        if (this.htmlUrl == null) return null

        val htmlUri = Uri.parse(this.htmlUrl)
        val eventId = htmlUri.getQueryParameter("event_id")

        return "${htmlUri.scheme}://${htmlUri.host}/${this.contextType?.apiString}/${this.contextId}/calendar_events/${eventId}"
    }
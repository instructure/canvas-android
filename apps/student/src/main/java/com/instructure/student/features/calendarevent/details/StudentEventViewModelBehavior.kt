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

package com.instructure.student.features.calendarevent.details

import android.appwidget.AppWidgetManager
import android.content.Context
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.pandautils.features.calendarevent.details.EventViewModelBehavior
import com.instructure.pandautils.features.inbox.utils.InboxComposeOptions
import com.instructure.student.widget.WidgetUpdater
import dagger.hilt.android.qualifiers.ApplicationContext


class StudentEventViewModelBehavior(
    @ApplicationContext private val context: Context,
    private val widgetUpdater: WidgetUpdater,
    private val appWidgetManager: AppWidgetManager
) : EventViewModelBehavior {

    override val shouldShowMessageFab = false

    override fun getInboxComposeOptions(canvasContext: CanvasContext?, event: ScheduleItem): InboxComposeOptions {
        throw NotImplementedError("This method should not be called")
    }

    override fun updateWidget() {
        context.sendBroadcast(widgetUpdater.getTodoWidgetUpdateIntent(appWidgetManager))
    }
}

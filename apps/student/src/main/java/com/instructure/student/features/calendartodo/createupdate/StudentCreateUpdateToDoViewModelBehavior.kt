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

package com.instructure.student.features.calendartodo.createupdate

import android.appwidget.AppWidgetManager
import android.content.Context
import com.instructure.pandautils.features.calendartodo.createupdate.CreateUpdateToDoViewModelBehavior
import com.instructure.student.widget.WidgetUpdater
import dagger.hilt.android.qualifiers.ApplicationContext


class StudentCreateUpdateToDoViewModelBehavior(
    @ApplicationContext private val context: Context,
    private val widgetUpdater: WidgetUpdater,
    private val appWidgetManager: AppWidgetManager
) : CreateUpdateToDoViewModelBehavior {

    override fun updateWidget() {
        context.sendBroadcast(widgetUpdater.getTodoWidgetUpdateIntent(appWidgetManager))
    }
}

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

package com.instructure.pandautils.features.calendarevent.details

import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.ScheduleItem

interface EventRouter {

    fun openLtiScreen(canvasContext: CanvasContext?, url: String)

    fun launchInternalWebViewFragment(url: String, canvasContext: CanvasContext?)

    fun openMediaFromWebView(mime: String, url: String, filename: String, canvasContext: CanvasContext?)

    fun canRouteInternallyDelegate(url: String): Boolean

    fun routeInternallyCallback(url: String)

    fun openEditEvent(scheduleItem: ScheduleItem)
}

/*
 * Copyright (C) 2021 - present Instructure, Inc.
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
package com.instructure.pandautils.navigation

import android.os.Bundle
import com.instructure.canvasapi2.models.CanvasContext

interface WebViewRouter {

    fun canRouteInternally(url: String, routeIfPossible: Boolean = false): Boolean

    fun routeInternally(url: String, extras: Bundle? = null)

    fun openMedia(url: String, mime: String = "", filename: String = "", canvasContext: CanvasContext? = null)

    fun routeExternally(url: String)

    fun openLtiScreen(canvasContext: CanvasContext?, url: String)

    fun launchInternalWebViewFragment(url: String, canvasContext: CanvasContext?)
}
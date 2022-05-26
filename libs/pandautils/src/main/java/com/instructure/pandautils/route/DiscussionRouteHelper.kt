/*
 * Copyright (C) 2022 - present Instructure, Inc.
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

package com.instructure.pandautils.route

import android.content.Context
import androidx.fragment.app.FragmentActivity
import com.instructure.canvasapi2.models.CanvasContext

interface DiscussionRouteHelper {

    fun canRouteInternally(context: Context,
                           url: String,
                           domain: String,
                           routeIfPossible: Boolean,
                           allowUnsupported: Boolean = true): Boolean

    fun routeInternalWebView(context: Context, url: String, title: String, authenticate: Boolean, html: String, canvasContext: CanvasContext)

    fun openMedia(activity: FragmentActivity, url: String)
}